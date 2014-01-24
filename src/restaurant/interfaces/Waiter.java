package restaurant.interfaces;





public interface Waiter {




	

	public abstract void msgHereIsCheck(Customer cust, String food, Double money, int table );
	public abstract String getName();
	public abstract void msgOutOfFood(String choice, int tableNum);
	public abstract void msgRestocked(String item);
	public abstract void msgFoodIsReady(String choice, int tableNum);
	public abstract void msgReadyToOrder(Customer customerAgent);
	public abstract void msgGiveOrder(Customer customerAgent, String myFood);
	public abstract void msgAskForCheck(Customer customerAgent);
	public abstract void msgLeavingTable(Customer customerAgent);
	

}


