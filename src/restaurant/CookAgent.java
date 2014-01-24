package restaurant;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import restaurant.gui.CookGui;
import restaurant.interfaces.Cook;
import restaurant.interfaces.Market;
import restaurant.interfaces.Waiter;
import agent.Agent;


/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class CookAgent extends Agent implements Cook {

	Timer timer = new Timer();
	public enum OrderState
	{raw, cooking,pickup,remove,done};
	public OrderState oState;
	public class CookOrder{
		String choice;
		OrderState state = OrderState.raw;
		Waiter myWaiter;
		int tableNum;
	}

	public CookGui cookGui;
	public List<CookOrder> CookOrders = new ArrayList<CookOrder>();
	private String name;
	private Semaphore doneCooking = new Semaphore(0,true);
	public List<order> fromMarket = new ArrayList<order>();

	private Hashtable<String, Integer> cookTimerTable = new Hashtable<String, Integer>();
	{cookTimerTable.put("Chicken", 5 );
	cookTimerTable.put("Steak", 5);
	cookTimerTable.put("Salad", 5);
	cookTimerTable.put("Pizza",5);}


	public enum inventoryState
	{under, over, reStocked, waitingDelivery};
	public class FoodCheck{
		String foodItem;
		Integer amount;

		Double threshold;
		Integer maxAmount;
		Integer stillNeed;
		Integer waitingAmt;
		inventoryState state = inventoryState.over;


		public FoodCheck(String f, Integer a, Double t) {
			this.foodItem = f;
			this.amount = a;
			this.threshold = t;
			this.maxAmount = a;
			this.stillNeed = 0;
			this.waitingAmt = 0;

			this.state = inventoryState.over;

		}


	}
	public enum marketState
	{outOfChicken, outOfPizza, outOfSalad, outOfSteak, stocked};
	public class Store{
		Market myMarket;
		marketState mState = marketState.stocked;

		public Store(Market market){
			myMarket = market;
			mState = marketState.stocked;

		}

	}
	public List<Store> Markets = new ArrayList<Store>();
	public List<Waiter> cookWaiters = new ArrayList<Waiter>();

	public List<FoodCheck> Inventory = new ArrayList<FoodCheck>();



	private Waiter currentWaiter;

	public CookAgent(String name) {
		super();
		this.name = name;
	}

	public String getMaitreDName() {
		return name;
	}

	public String getName() {
		return name;
	}
	public void setWaiter(Waiter waiter) { //this will be a waiterlist
		this.currentWaiter = waiter;
	}

	public void addWaiter(Waiter waiter){
		cookWaiters.add(waiter);
	}

	public void setMarket(List<MarketAgent> cookMarkets) { //this will be a waiterlist
		for (int j = 0; j<cookMarkets.size(); j++){
			Markets.add(new Store(cookMarkets.get(j)));

		}
	}


	public void msgGotFood(){
		cookGui.foods.remove(0);
		cookGui.foodDone = false;
	}

	public void startingInventory(){ /// host to cook
		Inventory.add(new FoodCheck("Steak", 4,0.4));
		Inventory.add(new FoodCheck("Salad", 2, 0.5));
		Inventory.add(new FoodCheck("Chicken", 5, 0.3));
		Inventory.add(new FoodCheck("Pizza", 5, 0.3));	
	}


	public void msgHereIsOrder(String foodToCook, int forLocation, Waiter myWaiter)
	{

		addOrderList(foodToCook, forLocation, myWaiter);
		//cook doesn't need to know customer name but the waiter name
		stateChanged();

	}

	public void msgWeHave(String food, int amt, boolean complete){

		if(!complete){
			for(FoodCheck item: Inventory){
				if (item.foodItem.equals(food)){
					item.stillNeed = item.maxAmount- amt;
					item.waitingAmt = amt;
					Do("not complete: Need to order from different market");
					item.state = inventoryState.under;
				}
			}
		}
		else{
			for(FoodCheck item: Inventory){
				if (item.foodItem.equals(food)){
					Do("complete: I got " + amt + " " + food);
					item.waitingAmt = amt;
					item.state = inventoryState.waitingDelivery;
				}
			}
		}

		stateChanged();

	}

	public void msgHereIsDelivery(String food, Integer amt){
		for(FoodCheck item: Inventory){
			if (item.foodItem.equals(food)){
				item.amount = item.amount + amt;
				item.waitingAmt  = item.waitingAmt -  amt;
				Do("received: " + amt + " "+ food);
				item.state = inventoryState.reStocked;
				if(item.amount == item.maxAmount){
					item.stillNeed = 0;
				}

			}
		}

		stateChanged();
	}

	public void msgWeAreOut(Market market, String food){
		for(Store out: Markets){
			if(out.myMarket.equals(market)){
				if(food.equals("Steak")){
					out.mState = marketState.outOfSteak;
				}
				else if(food.equals("Chicken")){
					out.mState = marketState.outOfChicken;
				}
				else if(food.equals("Salad")){
					out.mState = marketState.outOfSalad;
				}
				else{
					out.mState = marketState.outOfPizza;
				}
			}
		}

		stateChanged();
	}

	//utility
	private void addOrderList(String foodToCook, int forLocation, Waiter waiter)
	{
		CookOrder foodOrder = new CookOrder(); //copy the order class
		foodOrder.state = OrderState.raw;
		foodOrder.choice = foodToCook;
		foodOrder.tableNum = forLocation;
		foodOrder.myWaiter = waiter;
		CookOrders.add(foodOrder);
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


		for (CookOrder food : CookOrders) {

			if (food.state == OrderState.raw) {
				cooking(food);
				return true;
			}

		}
		for (CookOrder food : CookOrders) {
			if (food.state == OrderState.pickup) {
				foodReadyForPickup(food);
				return true;
			}

		}

		for(FoodCheck order: Inventory){
			if(order.state == inventoryState.under){
				order.state = inventoryState.waitingDelivery;
				order(order.stillNeed, order);
			}
		}

		for(FoodCheck order: Inventory){
			if(order.state == inventoryState.reStocked){
				order.state = inventoryState.over;
				updateMenu(order.foodItem);
			}
		}



		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions


	public void cooking(CookOrder food)
	{
		food.state = OrderState.cooking; //change state
		//check food
		for ( FoodCheck cFood : Inventory){
			if (cFood.foodItem == food.choice)
			{
				if (cFood.amount == 0)
				{
					Do("I'm out of food");
					if(cFood.waitingAmt == 0){
						checkInventory(cFood.amount, cFood);}
					food.myWaiter.msgOutOfFood(food.choice, food.tableNum);
					for (CookOrder order :CookOrders)
					{
						if (order.choice == food.choice){
							order.state = OrderState.remove;
						}
					}


				}
				else
				{
					cFood.amount --;

					print("cooking " + food.choice+ " for table " + food.tableNum );
					print("I have " + cFood.amount + " left");
					if(cFood.waitingAmt == 0){ // only if not waiting  for delivery
						checkInventory(cFood.amount, cFood);}
					
					cookGui.DoCooking(cFood.foodItem);
					cookTime(food);
					try {
						doneCooking.acquire();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}




			}
		}




	}


	private void updateMenu(String item){
		for(Waiter w: cookWaiters){
			w.msgRestocked(item);
		}
	}



	private void order(int amtOrder,FoodCheck cFood ){
		boolean outOfStock = true;
		if(cFood.foodItem.equals("Chicken")){
			// meat market
			for (Store market: Markets){
				if( (market.myMarket.getName().equals("Meat1") || market.myMarket.getName().equals("Meat2"))  && market.mState != marketState.outOfChicken){
					market.myMarket.msgOutOf(cFood.foodItem, amtOrder);
					outOfStock = false;
				}
			}
		}
		else if (cFood.foodItem.equals("Steak")){
			for (Store market: Markets){
				if((market.myMarket.getName().equals("Meat1") || market.myMarket.getName().equals("Meat2"))&& market.mState != marketState.outOfSteak){
					market.myMarket.msgOutOf(cFood.foodItem, amtOrder);
					outOfStock = false;
				}
			}

		}
		else if (cFood.foodItem.equals("Salad")){
			for (Store market: Markets){
				if( (market.myMarket.getName().equals("Italian1") || market.myMarket.getName().equals("Italian2"))  && market.mState != marketState.outOfSalad){
					Do("I need " + (amtOrder));
					market.myMarket.msgOutOf(cFood.foodItem, amtOrder);
					outOfStock = false;
					return;
				}
			}

		}
		else if (cFood.foodItem.equals("Pizza")){
			for (Store market: Markets){
				if((market.myMarket.getName().equals("Italian1") || market.myMarket.getName().equals("Italian2")) && market.mState != marketState.outOfPizza){
					market.myMarket.msgOutOf(cFood.foodItem, amtOrder);
					outOfStock = false;
				}
			}

		}

		if(outOfStock){
			Do("Markets are out of stock");
		}

	}

	private void checkInventory(int amtOrder, FoodCheck cFood){

		if(amtOrder <= (int)(cFood.threshold * cFood.maxAmount)){ //if food falls under the minimum treshold
			///find the right market
			boolean outOfStock = true;
			if(cFood.foodItem.equals("Chicken")){
				// meat market
				for (Store market: Markets){
					if( (market.myMarket.getName().equals("Meat1") || market.myMarket.getName().equals("Meat2"))  && market.mState != marketState.outOfChicken){
						market.myMarket.msgOutOf(cFood.foodItem, cFood.maxAmount - amtOrder);
						outOfStock = false;
					}
				}
			}
			else if (cFood.foodItem.equals("Steak")){
				for (Store market: Markets){
					if((market.myMarket.getName().equals("Meat1") || market.myMarket.getName().equals("Meat2"))&& market.mState != marketState.outOfSteak){
						market.myMarket.msgOutOf(cFood.foodItem, cFood.maxAmount-amtOrder);
						outOfStock = false;
					}
				}

			}
			else if (cFood.foodItem.equals("Salad")){
				for (Store market: Markets){
					if( (market.myMarket.getName().equals("Italian1") || market.myMarket.getName().equals("Italian2"))  && market.mState != marketState.outOfSalad){
						Do("I need " + (cFood.maxAmount-amtOrder));
						market.myMarket.msgOutOf(cFood.foodItem, cFood.maxAmount-amtOrder);
						outOfStock = false;
						return;
					}
				}

			}
			else if (cFood.foodItem.equals("Pizza")){
				for (Store market: Markets){
					if((market.myMarket.getName().equals("Italian1") || market.myMarket.getName().equals("Italian2")) && market.mState != marketState.outOfPizza){
						market.myMarket.msgOutOf(cFood.foodItem, cFood.maxAmount-amtOrder);
						outOfStock = false;
					}
				}

			}

			if(outOfStock){
				Do("Markets are out of stock");
			}

		}


	}


	public void cookTime(final CookOrder food)
	{

		//This next complicated line creates and starts a timer thread.
		//We schedule a deadline of getCookTime() * 1000 milliseconds.
		timer.schedule(new TimerTask() 
		{
			public void run() 
			{
				print ("Finish cooking: " + food.choice + "\n");
				food.state = OrderState.pickup;
				cookGui.isThinking =false;
				cookGui.goPlating();

				stateChanged();
			}
		},cookTimerTable.get(food.choice) * 2000 );//how long to wait before running task
	
		doneCooking.release();
		stateChanged();




	}

	public void foodReadyForPickup(CookOrder food)
	{
		setWaiter(food.myWaiter);
		currentWaiter.msgFoodIsReady(food.choice, food.tableNum);// call the specific waiter who gave this order
		for (int i = 0; i<CookOrders.size();i++)
		{
			if(CookOrders.get(i) == food)
			{
				CookOrders.remove(i); //remove from the food to cook
			}
			else if(CookOrders.get(i).state == OrderState.remove){
				CookOrders.remove(i);
			}

		}


		stateChanged();
	}


	public class order{
		String food;
		int amt;
	}
	public void setGui(CookGui gui) {
		cookGui = gui;
	}

	public CookGui getGui(){
		return cookGui;
	}
	




}

