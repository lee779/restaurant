package restaurant;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import restaurant.gui.WaiterGui;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Cook;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Waiter;
import agent.Agent;

/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class WaiterAgent extends Agent implements Waiter {



	//note that tables is typed with Collection semantics.
	//Later we will see how it is implemented
	private Integer totalCustomer = 0;
	private String name;
	Timer timer = new Timer();
	//semaphores help with no msgs being overwritten
	private Semaphore atTable = new Semaphore(0,true); 
	private Semaphore atCook = new Semaphore(0,true);
	private Semaphore atHome = new Semaphore(0,true);
	private Semaphore atOutside = new Semaphore(0, true);
	private Semaphore atCashier = new Semaphore(0, true);
	
	Timer breakTime = new Timer();
	private boolean busy = false; //for the host to know when this waiter can be called
	int tableLocation;


	private HostAgent host;
	private Cashier cashier;
	private Customer currentCustomer; //the current customer that waiter is attending
	private Cook cook;
	public WaiterGui waiterGui;

	public class foodPrice{
		String food;
		Double price;
		
		foodPrice(String f, Double p){
			food = f;
			price = p;
		}
	}
	
	private List<foodPrice> Menu = new ArrayList<foodPrice>();

	public enum CustomerState
	{WaitingToSeat, RequestMenu,CallingToOrder, needToReOrder, GetCheck,Leaving, BeingAttended};
	public class myCustomer{
		Customer cust;
		int tableNum;
		CustomerState cState;
	}
	
	public enum checkState
	{New, Given};
	public class custCheck{
		Customer cust;
		String food;
		double money;
		int table;
		checkState cState = checkState.New;
		
		
		custCheck(Customer c, String f, int t, Double m){
			cust = c;
			food = f;
			table = t;
			money = m;
			cState = checkState.New;
		}
	}
	public List<custCheck> checksToGive = new ArrayList<custCheck>();
	
	private enum BreakState
	{None,Pending,Waiting,Granted,Rejected,Outside,ComeBack};

	private BreakState waiterState = BreakState.None;



	public List<myCustomer> MyCustomers = new ArrayList<myCustomer>();//this is unique to each waiter and should pop it after the order is sent to the cook
	public List<Order> Orders = new ArrayList<Order>();


	private Hashtable<String, Double> menuPrice = new Hashtable<String, Double>();
	{menuPrice.put("Chicken", 10.99 );
	menuPrice.put("Steak", 15.99);
	menuPrice.put("Salad", 5.99);
	menuPrice.put("Pizza",8.99);}
	
	public enum OrderState
	{None, CreatedOrder, AtCook,Done}
	public class Order{
		String choice = "nothing";
		OrderState oState= OrderState.None;
		Customer cust;
		int tableNum;
	}
	
	

	public WaiterAgent(String name) {
		super();
		//set menu
		Menu.add(new foodPrice("Chicken", 10.99 ));
		Menu.add(new foodPrice("Steak", 15.99));
		Menu.add(new foodPrice("Salad", 5.99));
		Menu.add(new foodPrice("Pizza",8.99));
		this.name = name;

	}

	//messages

	public void msgSitCustomer(Customer cust, int tableNumber){
		
		setCustomer(cust);
		addNewCustomer(tableNumber);
		stateChanged();
	}

	public void msgWantToGoToBreak(){
		//from gui
		waiterState = BreakState.Pending;
		stateChanged();
	}
	
	public void msgWantToReturn(){
		waiterState = BreakState.ComeBack;
		stateChanged();
	}

	public void msgBreakRequestAnswer(boolean answer){
		if (answer){ //request a
			waiterState = BreakState.Granted;
			stateChanged();
		}
		else //request denied
		{
			waiterState = BreakState.Rejected;
			stateChanged();

		}
	}

    public void msgRestocked(String food){ //from cook
    	boolean update = true;
    	for(foodPrice menuItem: Menu){
    		if(menuItem.food.equals(food)){
    			update = false;
    		}
    	}
    	
    	if(update){
    		Menu.add(new foodPrice(food, menuPrice.get(food)));
    	}
    }

	public void msgReadyToOrder(Customer customer){
		for(myCustomer currC:MyCustomers)
		{
			if(currC.cust == customer) // should be the same customer that you just seated
			{
				currC.cState = CustomerState.CallingToOrder;
				stateChanged();
				break;
			}
		}


	}

	public void msgGiveOrder(Customer customer, String choice){
		//add new order to queue
		//set the information for the class order
		addNewOrder(customer,choice);
		//once its added into order list, customer can have new waiters serving food
		// so pop the customer from the MyCustomers list
		//perhaps

		stateChanged();

	}

	public void msgFoodIsReady(String foodReady, int tableNum)
	{
		for(Order request:Orders)
		{
			if(request.choice == foodReady && request.tableNum == tableNum)
			{
				request.oState = OrderState.Done;
				break;
			}
		}
		stateChanged();

	}
	
	public void msgAskForCheck(Customer cust){
		//from customer
		
		for (myCustomer checkCust: MyCustomers){
			if(checkCust.cust == cust){
				checkCust.cState = CustomerState.GetCheck;
				stateChanged();
			}
		}
	}

	public void msgHereIsCheck(Customer cust, String food, Double money, int table ){
		//go to cashier
		checksToGive.add(new custCheck(cust, food, table, money));
		stateChanged();
		
	}
	
	public void msgLeavingTable(Customer cust) {
		for (myCustomer leavingCust: MyCustomers)
		{
			if(cust.equals(leavingCust.cust)){
			leavingCust.cState = CustomerState.Leaving;}
		}
		stateChanged();

	}

	public void msgAtTable() {//from animation
		atTable.release();
		stateChanged();
	}

	public void msgAtCook(){
		atCook.release();
		stateChanged();
	}
	
	public void msgAtCashier(){
		atCashier.release();
		stateChanged();
	}
	
	public void msgAtOutside(){
		atOutside.release();
		stateChanged();
	}
	
	public void msgAtHome(){
		atHome.release();
		stateChanged();
	}


	public void msgOutOfFood(String choice, int tableNum) {
		// from cook
		//updateMenu
		for(foodPrice removeItem: Menu){
			if(removeItem.food.equals(choice)){
				Menu.remove(removeItem);
			}
		}
		
		
		for (myCustomer reOrder: MyCustomers){
			if (reOrder.tableNum == tableNum)
			{
				reOrder.cState = CustomerState.needToReOrder;
				stateChanged();
			}
		}

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
		if (waiterState == BreakState.Pending){
			waiterState = BreakState.Waiting;
			requestBreak();
			return true;
		}

		if (waiterState == BreakState.Rejected){
			waiterState = BreakState.None;
			rejectBreak();
			return true;
		}
		
		if (waiterState == BreakState.ComeBack){
			waiterState = BreakState.None;
			returnWork();
			return true;
		}

		if (waiterState == BreakState.Granted){
			if(totalCustomer == MyCustomers.size()){
				Do("go to break");
				goToBreak();
				return true;
			}
			if (MyCustomers.isEmpty()){
				Do("no customers");
				goToBreak();
				return true;
			}
		}
	


		for(myCustomer customer: MyCustomers)
		{
			if(customer.cState == CustomerState.WaitingToSeat)
			{
				customer.cState = CustomerState.BeingAttended;
				seatCustomer(customer.cust,customer.tableNum);
				return true;
			}
		}


		for(myCustomer customer: MyCustomers)
		{
			if(customer.cState == CustomerState.CallingToOrder)
			{
				customer.cState = CustomerState.BeingAttended;
				getOrder(customer.cust);
				return true;
			}
		}
		for(myCustomer customer: MyCustomers)
		{
			if(customer.cState == CustomerState.needToReOrder)
			{
				customer.cState = CustomerState.BeingAttended;
				reOrder(customer.cust,customer.tableNum);
				return true;
			}
		}
		for(Order request:Orders)
		{
			if(request.oState == OrderState.CreatedOrder)
			{
				HereIsOrder(request.choice, request.tableNum);
				request.oState = OrderState.AtCook;
				return true;
			}
		}

		for(Order request:Orders)
		{
			if(request.oState == OrderState.Done)
			{
				request.oState = OrderState.None;
				PickUpFood(request);
				return true;
			}
		}
		
		for(myCustomer checkCust: MyCustomers)
		{
			if(checkCust.cState == CustomerState.GetCheck)
			{
				checkCust.cState = CustomerState.BeingAttended;
				for(Order request:Orders){
					if(request.cust == checkCust.cust){
						tellCashier(request.cust, request.choice, request.tableNum);
						return true;
					}
					
				}
				
			}
		}
		
		for(custCheck cCheck: checksToGive){
			if (cCheck.cState == checkState.New){
				cCheck.cState = checkState.Given;
				giveCheck(cCheck);
				return true;
			}
		}

		for(myCustomer leaving:MyCustomers)
		{
			if(leaving.cState == CustomerState.Leaving)
			{
				leaving.cState = CustomerState.BeingAttended;
				totalCustomer++;
				Do("tellin host " + leaving.cust.getName() + " is leaving");
				tellHost(leaving.cust);
				return true;
			}
		}



		return false;

	}

	// Actions

	public void seatCustomer(Customer customer, int tableNumber) {

		customer.msgFollowMe(this);
		print("Seating " + customer + " at " + tableNumber);

		waiterGui.DoPickCustomer((CustomerAgent) customer);
		try {
			atHome.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		waiterGui.msgThinking("Follow me");
		timer.schedule(new TimerTask() 
		{
			public void run() 
			{
				waiterGui.isThinking = false;
			}
		}, 500 );
		
		
		waiterGui.DoBringToTable((CustomerAgent)customer, tableNumber);
		
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		customer.msgHereIsMenu(Menu);
		waiterGui.DoLeaveCustomer();

	}



	private void goToBreak(){
		//tell gui to go to Break
		
       Do("going to break");
       
		waiterGui.msgThinking("Break time");

		waiterState = BreakState.Outside;
		
		waiterGui.doGoToBreak();
		try {
			atOutside.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}
	
	private void returnWork(){
		Do("come back");
		waiterGui.msgThinking("back from break");
		timer.schedule(new TimerTask() 
		{
			public void run() 
			{
				waiterGui.isThinking = false;
			}
		}, 500 );
		
		waiterGui.doReturnWork();
		host.msgBackFromBreak(this);
		
	}

	private void getOrder(final Customer customer)
	{
		//get order 
		//send message to customer to give order
		for(myCustomer currC:MyCustomers)
		{
			if (currC.cust == customer)
			{
				print("waiter " + this.getName() + " gonna take order for customer " + customer.getName());
				waiterGui.goToTable(currC.tableNum); //go to table
				break;
			}
		}
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		waiterGui.msgThinking("What would you like?");
		timer.schedule(new TimerTask() 
		{
			public void run() 
			{
				customer.msgWhatIsOrder();
				waiterGui.isThinking =false;

			}
		}, 600 );

	}

	public void reOrder(Customer customer,Integer tableNumber){

		waiterGui.goToTable(tableNumber);
		try {

			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Do("Sorry,please reorder");
		customer.msgHereIsUpdatedMenu(Menu);
	}


	public void HereIsOrder(String orderChoice, int tableNumber)
	{
		Do("going to cook with order");
		waiterGui.DoGoToCook();
		try {

			atCook.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cook.msgHereIsOrder(orderChoice, tableNumber, this); // added pointer to waiter so cook knows who to call? is this a bad idea?
		waiterGui.msgThinking("order.");
		timer.schedule(new TimerTask() 
		{
			public void run() 
			{
				waiterGui.isThinking = false;
				waiterGui.DoLeaveCustomer();
			}
		}, 300 );
	
		
		if(waiterState == BreakState.None){
			host.msgNotBusy(this);// tells host not busy any more
			}

	}

	private void PickUpFood(Order foodToDeliver)
	{

		waiterGui.DoPickUpFood(foodToDeliver.choice);
		try {
			atCook.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Do(this.name + " waiter is picking up food for table " + foodToDeliver.choice);
		this.setCustomer(foodToDeliver.cust);
		cook.msgGotFood();
		giveFood(foodToDeliver);


	}

	private void giveFood(Order food)
	{
		waiterGui.createCustomerFood(food.choice);
		waiterGui.pickUpFood = true;
		waiterGui.goToTable(food.tableNum);
		try {

			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		currentCustomer.msgHereIsFood();
		Do("Here is your " +food.choice + ", enjoy.");
		waiterGui.msgThinking("Here is "+food.choice);
		timer.schedule(new TimerTask() 
		{
			public void run() 
			{
				waiterGui.msgThinking("Enjoy");
				waiterGui.createCustomerFood("");

			}
		}, 400 );
		timer.schedule(new TimerTask() 
		{
			public void run() 
			{
				waiterGui.isThinking = false;
				waiterGui.DoLeaveCustomer();
			}
		}, 400 );
		
		
		
	}
	
	private void tellCashier(Customer c, String choice, int t ){
		//tell cashier to create check
		//go to cashier
		for (int done = 0 ; done <Orders.size(); done ++)
		{
			if(Orders.get(done).cust == c)
			{
				Orders.remove(done);
			}
		}//remove order
		
		waiterGui.goToCashier();
		Do("go to cashier");
		try {

			atCashier.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cashier.msgCreateCheck(choice, t, this, c);
		waiterGui.msgThinking("check for table" + t);
		timer.schedule(new TimerTask() 
		{
			public void run() 
			{
				waiterGui.isThinking =false;
			//	waiterGui.DoLeaveCustomer();
				waiterGui.createCustomerFood("Check");
				//return back to station
			}
		}, 500 );
		
	}
	
	private void giveCheck(custCheck check){
		//go to customer
		waiterGui.goToTable(check.table);
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//tell customer 
		
		check.cust.msgHereIsCheck(check.money);
		Do("here is your check " + check.cust.getName());
		waiterGui.msgThinking("Here is check.");
		timer.schedule(new TimerTask() 
		{
			public void run() 
			{
				waiterGui.msgThinking("Pay upfront");
			}
		}, 500 );
		timer.schedule(new TimerTask() 
		{
			public void run() 
			{
				waiterGui.isThinking = false;
				waiterGui.createCustomerFood("None");
				waiterGui.DoLeaveCustomer();
			}
		}, 400 );
		
		
	}
	
	private void tellHost(Customer cust){
		waiterGui.DoLeaveCustomer();
		Do("I'm in tell host");
		host.msgCustomerLeft((CustomerAgent) cust, this);
		
	}
	
	private void requestBreak(){
		host.msgAskHostBreak(this); //ask host
		this.getGui().breakStatus("Pending");
		
	}
	private void rejectBreak(){
		this.getGui().breakStatus("Rejected");
		
	}
	



	//utilities

	public boolean availability(){
		return busy;
	}

	public void setBusy(){
		busy = true;
	}
	
	public void setCashier(CashierAgent cashier) {
		this.cashier = cashier;
	}

	public void setHost(HostAgent host) {
		this.host = host;
	}

	public void setCook (CookAgent cook){
		this.cook  = cook;
	}

	public void setCustomer(Customer customer){
		this.currentCustomer = customer;
	}

	public String getName() {
		return name;
	}



	public void addNewOrder(Customer cust, String choice)
	{
		Order newOrder = new Order();
		newOrder.cust = cust;
		newOrder.choice = choice;
		newOrder.oState = OrderState.CreatedOrder;
		for(myCustomer currC:MyCustomers)
		{
			if (currC.cust == cust)
			{
				newOrder.tableNum = currC.tableNum;
				break;
			}
		}

		Orders.add(newOrder);
	}

	public void addNewCustomer(int tableNumber)
	{

		myCustomer newCustomer = new myCustomer();
		newCustomer.cust = currentCustomer;
		newCustomer.tableNum = tableNumber;
		newCustomer.cState = CustomerState.WaitingToSeat;
		MyCustomers.add(newCustomer);
	}

	public void setGui(WaiterGui gui) {
		waiterGui = gui;
	}

	public WaiterGui getGui() {
		return waiterGui;
	}

	public Cook getCook(){
		return cook;
	}



	

}


