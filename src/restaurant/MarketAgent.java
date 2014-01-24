package restaurant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import restaurant.interfaces.Cashier;
import restaurant.interfaces.Cook;
import restaurant.interfaces.Market;
import agent.Agent;


/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class MarketAgent extends Agent implements Market {

	Timer timer = new Timer();
	private String name;
	private Cook myCook;
	private Cashier myCashier;
	private double interest = 1.1; // 10% interest for debt


	public enum billState{
		paid, billed, cantPay,warning, haveToPay, delivered, none, newBill;
	}
	public class order{
		String food;
		Integer amt;
		Double cost = 0.0;
		billState bState = billState.newBill;
		Cashier cashier;
		order(String f, int a, Cashier c){
			food = f;
			amt = a;
			cost = 0.0;
			bState = billState.newBill;
			cashier = c;


		}
	}




	private Hashtable<String, Integer> deliveryTimer = new Hashtable<String, Integer>();
	{deliveryTimer.put("Chicken", 5 );
	deliveryTimer.put("Steak", 5);
	deliveryTimer.put("Salad", 2);
	deliveryTimer.put("Pizza",3);}

	private Hashtable<String, Double> deliveryPrices = new Hashtable<String, Double>();
	{deliveryPrices.put("Chicken", 4.99 );
	deliveryPrices.put("Steak", 6.99);
	deliveryPrices.put("Salad", 2.99);
	deliveryPrices.put("Pizza",3.99);}

	

	public List<order> ordersToFill = Collections.synchronizedList(new ArrayList<order>());
	private Hashtable<String, Integer> Inventory = new Hashtable<String, Integer>();
	private Hashtable<Cashier, Double> pastRestDebt = new Hashtable<Cashier, Double>();




	public MarketAgent(String type) {
		super();
		this.name = type;
	}


	public Hashtable<Cashier, Double> getPastDebt() {
		// TODO Auto-generated method stub
		return pastRestDebt;
	}

	public void setPastDebt(Cashier c, double debt){
		pastRestDebt.put(c, debt);
	}


	public String getName() {
		return name;
	}

	public void setInventory(String food, int amt){
		Inventory.put(food, amt);
	}

	public void setCook(Cook cook) { //this will be a waiterlist
		myCook = cook;
	}

	public void setCashier(Cashier cashier) { //this will be a waiterlist
		myCashier = cashier;
	}

	public void msgOutOf(String food, Integer amt ){ //from cook
		Do("received new order: food " + food + " amt: " + amt);
		order o1 = new order(food, amt, myCashier);
		o1.cost = deliveryPrices.get(food) * o1.amt;
		ordersToFill.add(o1);
		stateChanged();

	}

	public void msgHereIsPayment(Double amt, Cashier cashier){
		//receive payment
		for(order payment: ordersToFill){

			if(payment.cashier.equals(cashier)){
				payment.bState = billState.paid;
				payment.cost = payment.cost - amt;
				stateChanged();
			}
		}

	}


	public void msgCantPay(Double paidAmt, String food, Cashier cashier){ // give what it can pay
		
		if(pastRestDebt.containsKey(cashier)){
			//past debt, second warning
			pastRestDebt.put(cashier, pastRestDebt.get(cashier) - paidAmt); //update
			for(order strike: ordersToFill){
				if(strike.cashier.equals(cashier) && strike.food.equals(food)){
					strike.bState = billState.haveToPay;
					strike.cost = strike.cost - paidAmt;
					stateChanged();

				}
			}
		}
		else{
			//first warning
			for(order strike: ordersToFill){
				if(strike.cashier.equals(cashier) && strike.food.equals(food)){
					strike.bState = billState.warning;
					strike.cost = strike.cost - paidAmt;
					pastRestDebt.put(cashier, strike.cost); //update
					stateChanged();
				}
			}

		}

	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		/* Think of this next rule as:
            Does there exist a table and customer,
            so that table is unoccupied and customer is waiting.
            If so seat him at the table.
		 */


		if(!ordersToFill.isEmpty() ){
			for (order newOrder: ordersToFill)
				//for(foodItems newOrder: ordersToFill){
				if(newOrder.bState == billState.newBill){
					newOrder.bState = billState.none;
					giveQuote(newOrder);
					return true;
				}
		}	


		for(order payment: ordersToFill){
			if(payment.bState == billState.paid){
				thankYou(payment.cashier);
				ordersToFill.remove(payment);
				return true;
			}
		}

		for(order delivery: ordersToFill){
			if(delivery.bState == billState.delivered){
				delivery.bState = billState.none;
				sendBill(delivery);
				return true;
			}
		}


		for(order payment: ordersToFill){
			if(payment.bState == billState.haveToPay){
				getMoney(payment);
				payment.bState = billState.none;
				return true;
			}
			
			
		}
		synchronized(ordersToFill){
		for(order payment: ordersToFill){
			
			if(payment.bState == billState.warning){
				giveWarning(payment);
				payment.bState = billState.none;
				return true;
			}
		}
		}

		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	private void getMoney(order money){
		if(pastRestDebt.containsKey(money.cashier)){
			pastRestDebt.remove(money.cashier);
		}
		money.cashier.msgYouHaveToPay(money.cost, this);
		Do("You have to pay, " + money.cashier.getName());

	}
	
	private void giveWarning(order warning){
		
		warning.cashier.msgWarning(this);
		Do("You have one warning, " + warning.cashier.getName() + " pay next time");
		ordersToFill.remove(warning);
	}
	
	
	private void thankYou(Cashier cashier){
		if(pastRestDebt.containsKey(cashier)){
			pastRestDebt.remove(cashier);
		}
		cashier.msgThankYou(this);
		Do("thank you for the payment, " + cashier.getName());

	}

	private void giveQuote(order quote){
		boolean fulfilled;
		int result;
		if(Inventory.get(quote.food) >= quote.amt){
			//can be fulfilled
			fulfilled = true;
			Do("I can fullfill this order");
			result = Inventory.get(quote.food) - quote.amt;
			Inventory.put(quote.food, result);
		}
		else{
			fulfilled = false;
			Do("I can't fullfill this order");
			quote.amt = Inventory.get(quote.food);
			quote.cost = quote.amt * deliveryPrices.get(quote.food);
			result = 0;
			Inventory.put(quote.food, 0);

		}
		myCook.msgWeHave(quote.food, quote.amt, fulfilled);

		if(result == 0){
			myCook.msgWeAreOut(this, quote.food);
			Do("our market is out of " + quote.food);

		}
	delivery(quote);
}


private void sendBill(order bill){
	
	if(pastRestDebt.containsKey(bill.cashier)){
		bill.cost = bill.cost + (pastRestDebt.get(bill.cashier)*interest);
	}
	
	Do("here's the bill: $"+ bill.cost + " for orders: " + bill.food);
	bill.cashier.msgHereIsBill( bill.cost,bill.food, this);
	bill.bState = billState.billed;
}


private void delivery( final order delivery){

	timer.schedule(new TimerTask() 
	{
		public void run() 
		{
			
				Do ("Here is the delivery of "+ delivery.food);
				myCook.msgHereIsDelivery(delivery.food, delivery.amt);
				delivery.bState = billState.delivered;
				stateChanged();

		}
	},


	(deliveryTimer.get(delivery.food) * (1000* delivery.amt)));//how long to wait before running task

}




}