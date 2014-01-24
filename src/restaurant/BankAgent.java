package restaurant;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import restaurant.interfaces.Bank;
import restaurant.interfaces.Cashier;
import agent.Agent;


/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class BankAgent extends Agent implements Bank {


	
	private Hashtable<Cashier, Double> bankAccount = new Hashtable<Cashier, Double>();
	public List<accountMoney> acct = new ArrayList<accountMoney>();

	private String name;


	public class accountMoney{
		Cashier cashier;
		Double money;
	}
	

	public Hashtable<Cashier, Double> getBankAccount() {
		// TODO Auto-generated method stub
		return bankAccount;
	}

	public void setAccount(Cashier c, double money){
		bankAccount.put(c, money);
	}

	public BankAgent(String type) {
		super();
		this.name = type;
	}

	public String getName() {
		return name;
	}

	
	
	
	public void msgNeedMoney(Cashier cashier, Double bill){
		
		accountMoney ac = new accountMoney();
		ac.cashier = cashier;
		double bal = 	bankAccount.get(cashier) - bill;
		bankAccount.put(cashier, bal);
		ac.money = bill;

		acct.add(ac);
		stateChanged();
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


		if(!acct.isEmpty()){
			giveMoney(acct.get(0));
			acct.remove(0);
			return true;
		}

		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}
	
	private void giveMoney(accountMoney ac){
		ac.cashier.msgHereIsBankMoney(ac.money);
		Do("Here is the requested amount, " + ac.cashier.getName());
	}




}