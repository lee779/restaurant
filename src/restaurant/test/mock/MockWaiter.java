package restaurant.test.mock;


import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Waiter;

/**
 * A sample MockCustomer built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public class MockWaiter extends Mock implements Waiter {

	/**
	 * Reference to the Cashier under test that can be set by the unit test.
	 */
	//public Cashier cashier;

	public MockWaiter(String name) {
		super(name);

	}
	public String getName() {
		return name;
	}

	public EventLog log = new EventLog();{
		log.clear();
	}
	public Cashier cashier;
	public Customer customer;

	@Override
	public void msgHereIsCheck(Customer cust, String food, Double money, int table) {
		
		log.add(new LoggedEvent("Received msgHereIsCheck from cashier. Cust = " + cust.getClass().getName() + 
				", food: " + food + ", bill: $" + money.toString() + ", for table #: "  + table)) ;
		// TODO Auto-generated method stub
		
		cust.msgHereIsCheck(money);
	}
	@Override
	public void msgOutOfFood(String choice, int tableNum) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void msgRestocked(String item) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void msgFoodIsReady(String choice, int tableNum) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void msgReadyToOrder(Customer customerAgent) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void msgGiveOrder(Customer customerAgent, String myFood) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void msgAskForCheck(Customer customerAgent) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void msgLeavingTable(Customer customerAgent) {
		// TODO Auto-generated method stub
		
	}

}
