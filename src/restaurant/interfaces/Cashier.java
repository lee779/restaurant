package restaurant.interfaces;


/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public interface Cashier{


	public abstract void msgCreateCheck(String food, int table, Waiter waiter, Customer cust);

	public abstract void msgCantPay(Customer cust, double debt);

	public abstract void hereIsMoney(Customer cust, double cash);
	
	public abstract void msgHereIsBill(Double bill, String food,Market market);
	
	public abstract void msgWarning(Market market);
	
	public abstract String getName();

	public abstract void msgThankYou(Market market);

	public abstract void msgYouHaveToPay(Double cost, Market market);

	public abstract void msgHereIsBankMoney(Double money);
	
}

	
