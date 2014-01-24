package restaurant.test.mock;



import restaurant.interfaces.Cashier;
import restaurant.interfaces.Cook;
import restaurant.interfaces.Waiter;
import restaurant.interfaces.Market;

/**
 * A sample MockCustomer built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public class MockCook extends Mock implements Cook {

	public MockCook(String name) {
		super(name);

	}

	public EventLog log;
	public Cashier cashier;
	@Override
	public void msgHereIsOrder(String foodToCook, int forLocation,
			Waiter myWaiter) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void msgWeHave(String food , int amt, boolean complete) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void msgWeAreOut(Market market, String food) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void msgHereIsDelivery(String food, Integer amt) {
		// TODO Auto-generated method stub
		
	}

}
