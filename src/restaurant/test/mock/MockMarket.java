package restaurant.test.mock;


import java.util.Hashtable;
import java.util.List;


import restaurant.interfaces.Cashier;
//import restaurant.interfaces.Cashier;
import restaurant.interfaces.Market;

/**
 * A sample MockCustomer built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public class MockMarket extends Mock implements Market {

	/**
	 * Reference to the Cashier under test that can be set by the unit test.
	 */
	

	public MockMarket(String name) {
		super(name);

	}

	
	public EventLog log = new EventLog();{
		log.clear();
	}
	private Hashtable<Cashier, Double> pastRestDebt = new Hashtable<Cashier, Double>();

	public Cashier cashier;

	@Override
	//from cook , dont need when testing cashier cases
	public void msgOutOf(String food, Integer amt) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgOutOf from cook. Food: " + food + " amt: " + amt));
		
	}
	
	

	@Override
	//from cashier
	public void msgHereIsPayment(Double amt, Cashier cashier) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgHereIsPayment from cashier. Bill = " + amt));
		cashier.msgThankYou(this);
		
	}

	@Override
	public void msgCantPay(Double paidAmt, String food, Cashier cashier) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received msgCantPay from cashier. paid amount = " + paidAmt));
		
		cashier.msgYouHaveToPay(158.99 + pastRestDebt.get(cashier), this);
		pastRestDebt.remove(cashier);
		
	}



	@Override
	public Hashtable<Cashier, Double> getPastDebt() {
		// TODO Auto-generated method stub
		return pastRestDebt;
	}



	@Override
	public void setPastDebt(Cashier c, double debt) {
		// TODO Auto-generated method stub
		pastRestDebt.put(c, debt);
		
	}





}
