package restaurant.test.mock;


import java.util.List;

import restaurant.WaiterAgent.foodPrice;
import restaurant.test.mock.*;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Waiter;

/**
 * A sample MockCustomer built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public class MockCustomer extends Mock implements Customer {

	/**
	 * Reference to the Customer under test that can be set by the unit test.
	 */

	
	
	public MockCustomer(String name) {
		super(name);

	}
	public String getName() {
		return name;
	}
	
	
	public EventLog log = new EventLog();{
		log.clear();
	}
	
	public Cashier cashier;
	public double myCash;
	public boolean thief;


	@Override
	//from waiter
	public void msgHereIsCheck(double money) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgHereIsCheck from waiter. Bill = " + money));
		
		//testing different scenarios
		
		//if customer is a thief
		if(this.isThief()){ 
			cashier.msgCantPay(this, myCash);
		}
		else{ //if customer is honest
			cashier.hereIsMoney(this, money);
		}
		
	}

	@Override
	public void msgCleanDishes() {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgCleanDishes from cashier."));
	}

	@Override
	public void msgWarning() {
		log.add(new LoggedEvent("Received msgWarning from cashier."));
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgThankYou() {
		log.add(new LoggedEvent("Received msgThankYou from cashier."));
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isThief() {
		// TODO Auto-generated method stub
		return thief;
	}

	@Override
	public void setThief(boolean thief) {
		// TODO Auto-generated method stub
		this.thief = thief;
		
	}
	@Override
	public void msgHereIsMenu(List<foodPrice> menu) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void msgFollowMe(Waiter waiterAgent) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void msgWhatIsOrder() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void msgHereIsUpdatedMenu(List<foodPrice> menu) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void msgHereIsFood() {
		// TODO Auto-generated method stub
		
	}


}
