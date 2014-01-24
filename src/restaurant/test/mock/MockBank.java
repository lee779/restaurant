package restaurant.test.mock;



import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import restaurant.BankAgent.accountMoney;
import restaurant.interfaces.Bank;
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
public class MockBank extends Mock implements Bank {

	public MockBank(String name) {
		super(name);

	}
	private Hashtable<Cashier, Double> bankAccount = new Hashtable<Cashier, Double>();
	public List<accountMoney> acct = new ArrayList<accountMoney>();
	public EventLog log = new EventLog();{
		log.clear();
	}

	@Override
	public Hashtable<Cashier, Double> getBankAccount() {
		// TODO Auto-generated method stub
		return bankAccount;
	}

	@Override
	public void setAccount(Cashier c, double money) {
		// TODO Auto-generated method stub
		bankAccount.put(c, money);
	}

	@Override
	public void msgNeedMoney(Cashier cashier, Double bill) {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("received msgNeedMoney from cashier for: $" + bill));
		bankAccount.put(cashier, bankAccount.get(cashier)-bill);
		cashier.msgHereIsBankMoney(bill);
	}
	
}
