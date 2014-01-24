package restaurant.interfaces;

import java.util.Hashtable;


public interface Bank {

	
	
	public abstract Hashtable<Cashier, Double> getBankAccount();

	public abstract void setAccount(Cashier c, double money);

	
	public abstract String getName();

	public abstract void msgNeedMoney(Cashier cashier, Double bill);


}