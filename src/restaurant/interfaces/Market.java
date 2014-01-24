package restaurant.interfaces;

import java.util.Hashtable;

;


/*Market interface*/

public interface Market{

	
	public abstract void msgOutOf(String food, Integer amt );
	
	public abstract void msgHereIsPayment(Double amt, Cashier cashier);
	
	public abstract void msgCantPay(Double paidAmt, String food,Cashier cashier);
	
	public abstract String getName();
	
	public abstract Hashtable<Cashier, Double> getPastDebt();
	
	public abstract void setPastDebt(Cashier c, double debt);
	

	
}