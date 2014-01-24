package restaurant.interfaces;

import java.util.List;

import restaurant.WaiterAgent.foodPrice;

/**
 * A sample Customer interface built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public interface Customer {
	

	
	public abstract void msgHereIsCheck(double money);

	public abstract void msgCleanDishes();

	public abstract void msgWarning();

	public abstract void msgThankYou();

	public abstract boolean isThief();

	public abstract void setThief(boolean thief);
	
	public abstract String getName();

	public abstract void msgHereIsMenu(List<foodPrice> menu);


	public abstract void msgFollowMe(Waiter waiterAgent);

	public abstract void msgWhatIsOrder();

	public abstract void msgHereIsUpdatedMenu(List<foodPrice> menu);

	public abstract void msgHereIsFood();
	
}
	
	
/*	
	public abstract String getName();
	
	public abstract int getHungerLevel();

	public abstract int getThinkingTime();

	public abstract int getCleaningTime();

	public abstract void setHungerLevel(int hungerLevel);

	public abstract void setGui(CustomerGui g);

	public abstract void setCash(int cash);

	public abstract CustomerGui getGui();

	public abstract boolean isThief();

	public abstract void setThief(boolean thief);*/
	
	