package restaurant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import restaurant.gui.HostGui;
import agent.Agent;


/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class HostAgent extends Agent {


	static final int NTABLES = 3;//a global for the number of tables.
	//Notice that we implement waitingCustomers using ArrayList, but type it
	//with List semantics.
	private List<Chair> chairs = new ArrayList<Chair>();{
		for (int i = 1; i<=3; i++){
		chairs.add(new Chair(i));}
	}

	
	public enum CustState
	{New, Waiting, DontMatter, Pending, Patient, Impatient, Seated};
	public class HostCustomers{
		CustomerAgent myCust;
		CustState myCustState = CustState.DontMatter;
	}
	
	public List<CustomerAgent> newCust = new ArrayList<CustomerAgent>();
	public List<CustomerAgent> pendingCust = new ArrayList<CustomerAgent>();
	

	public enum WaiterState
	{RequestedBreak, InBreak, Busy,None};
	public class HostWaiters{
		//boolean busy = false; //initialize all hostWaiters to be not busy
		WaiterState myWaiterState = WaiterState.None;
		WaiterAgent waiter;
		int numberCust = 0; //number of customers currently working with
	}
	public List<Integer> index = new ArrayList<Integer>();
	public List<Integer> custN = new ArrayList<Integer>();
	public List<HostWaiters> waitingWaiters = new ArrayList<HostWaiters>();
	public Collection<Table> tables;
	//note that tables is typed with Collection semantics.
	//Later we will see how it is implemented

	private int fullRest = 0;

	private CookAgent cook;
	private String name;
	public HostGui hostGui;
	//private Semaphore atTable = new Semaphore(0,true);




	public  HostAgent(String name) {
		super();

		this.name = name;
		// make some tables

		tables = new ArrayList<Table>(NTABLES);
		for (int ix = 1; ix <= NTABLES; ix++) {
			tables.add(new Table(ix));//how you add to a collections
		}

		//add waiters that are working 
		waitingWaiters = new ArrayList<HostWaiters> ();
	}

	public void setCook(CookAgent cook) {
		this.cook = cook;
		cook.startingInventory();

	}

	public void addWaiters(WaiterAgent waiter){
		HostWaiters hw = new HostWaiters();
		hw.waiter = waiter;
		waitingWaiters.add(hw);
	}


	public String getName() {
		return name;
	}



	public void msgIWantFood(CustomerAgent cust) {
		Do("new customer");
		 
		for(Table full: tables){
			if(full.isOccupied()){
				fullRest++;
				
			}
		}
		
		if(fullRest == tables.size()){
			
		
			pendingCust.add(cust);
		
		}
		else{
			
			newCust.add(cust);
	
		}
		
	
		fullRest = 0;
		stateChanged();
	}
	public void msgImWaiting(CustomerAgent cust){
		
		newCust.add(cust);
		stateChanged();
	}

	public void msgImLeaving(CustomerAgent cust){
		
		stateChanged();
	}

	public void msgBackFromBreak(WaiterAgent waiter){
		for(HostWaiters hostWaiter: waitingWaiters){
			if(hostWaiter.waiter == waiter){
				hostWaiter.myWaiterState = WaiterState.None;
			}
		}
		stateChanged();
	}

	public void msgNotBusy(WaiterAgent waiter){ 
		for(HostWaiters hostWaiter: waitingWaiters){
			if(hostWaiter.waiter == waiter){
				Do("received not busy");
				hostWaiter.myWaiterState = WaiterState.None;
				break;
			}
		}
		stateChanged();
	}

	public void msgAskHostBreak(WaiterAgent waiter){
		for(HostWaiters hostWaiter: waitingWaiters){
			if(hostWaiter.waiter == waiter){
				hostWaiter.myWaiterState = WaiterState.RequestedBreak;
				Do(waiter.getName() + " requested for break");
				break;
			}
		}
		stateChanged();
	}


	public void msgCustomerLeft(CustomerAgent cust, WaiterAgent waiter) {
		print("i'm called " + cust.getName());
		for (Table table : tables) {
			if (table.getOccupant() == cust) {
				print(table.getOccupant().getName() + " leaving " + table);
				table.setUnoccupied();

			}
		}

		for (HostWaiters w: waitingWaiters){
			if(w.equals(waiter)){
				w.numberCust--;
			}
		}

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

		
		
		for(HostWaiters waiter:waitingWaiters){
			if(waiter.myWaiterState == WaiterState.RequestedBreak){
				//if requested break, check their myCustomer
				checkRestaurantStatus(waiter);
				return true;
			}
		}

		if(!pendingCust.isEmpty()){
			fullRestaurant();
		}
		
		
		

		for (Table table : tables) {
			if (!table.isOccupied()) { // get empty table
				if (!newCust.isEmpty()) { //there are customers waiting

					for(HostWaiters waiter:waitingWaiters){ //for all the element of HostWaiters in waitingWaiters

						if(waiter.myWaiterState == WaiterState.None && waiter.numberCust == 0) //look for waiters that are not doing anything
						{
							
							table.setOccupant(newCust.get(0));
							waiter.myWaiterState = WaiterState.Busy; //set the waiter busy to be true

							waiter.waiter.msgSitCustomer(newCust.get(0), table.tableNumber); //gives table number to waiter agent
							waiter.numberCust++;
							newCust.remove(newCust.get(0)); //remove customer from the list

							return true;//table number can be given since the waiterAgent won't alter it (NO DATA SHARING requirement)
						}
					}

					for(HostWaiters waiter:waitingWaiters){
						if (waiter.myWaiterState == WaiterState.None){
							table.setOccupant(newCust.get(0));
							waiter.myWaiterState = WaiterState.Busy; //set the waiter busy to be true
							waiter.numberCust++;
							waiter.waiter.msgSitCustomer(newCust.get(0), table.tableNumber); //gives table number to waiter agent
							newCust.remove(newCust.get(0)); //remove customer from the list

							return true;
						}
					}


				}
				//return true to the abstract agent to re-invoke the scheduler.

			}



		}//end of for loop





		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions
	private void checkRestaurantStatus(HostWaiters myWaiter){
		//first check if only one waiter is working
		boolean answer = true;
		if(waitingWaiters.size() == 1){
			Do("not possible, only one waiter working");
			myWaiter.myWaiterState = WaiterState.None;
			hostGui.msgDeniedBreak();
			answer = false;

		}
		else{
			int counter = 0;
			for (HostWaiters onBreak: waitingWaiters){
				if(onBreak.myWaiterState == WaiterState.InBreak){
					counter++;
				}
			}
			if(counter == waitingWaiters.size()-1){ //if all but this waiters are on break, you can't go
				Do("not possible, all other waiters are on break");
				myWaiter.myWaiterState = WaiterState.None;
				hostGui.msgDeniedBreak();
				answer = false;
			}
			else{
				Do("granted: " + myWaiter.waiter.getName() + ", only if you finished pending orders");
				myWaiter.myWaiterState = WaiterState.InBreak;
				hostGui.msgGrantedBreak();	
			}



		}

		myWaiter.waiter.msgBreakRequestAnswer(answer);
		
	}


	private void fullRestaurant(){
		pendingCust.get(0).msgFullRest();
		pendingCust.remove(0);
	}

	
	

	//utilities
	public void setGui(HostGui gui) {
		hostGui = gui;
	}

	public int getIndexOfMin(List<Integer> data) {
		int min = Integer.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < data.size(); i++) {
			Integer I = data.get(i);
			if (I.intValue() < min) {
				min = I.intValue();
				index = i;
			}
		}
		return index;
	}

	class Table {
		CustomerAgent occupiedBy;
		int tableNumber;


		Table(int tableNumber) {
			this.tableNumber = tableNumber;
		}

		void setOccupant(CustomerAgent cust) {
			occupiedBy = cust;
		}

		void setUnoccupied() {
			occupiedBy = null;
		}

		CustomerAgent getOccupant() {
			return occupiedBy;
		}

		boolean isOccupied() {
			return occupiedBy != null;
		}

		public String toString() {
			return "table " + tableNumber;
		}



	}

	public class Chair {

		Boolean occupied;
		int num;


		public Chair(int num) {
			this.num = num;
		}

		void setOccupied(){
			occupied = true;
		}

		void setUnoccupied() {
			occupied = false;
		}

		boolean isOccupied() {
			return occupied;
		}

	}

}


