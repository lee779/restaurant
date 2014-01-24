package restaurant;


import restaurant.CookAgent.OrderState;
import restaurant.gui.CustomerGui;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Waiter;
import agent.Agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

/**
 * Restaurant customer agent.
 */
public class CustomerAgent extends Agent implements Customer{
	private String name;
	private int hungerLevel = 5;        // determines length of meal
	private int cleaning = 2;
	private int thinking = 2;

	Timer timer = new Timer();
	Timer thinkingTime = new Timer();
	Timer cleaningTime = new Timer();

	private CustomerGui customerGui;
	private HostAgent host;
	private Waiter waiter;
	private Cashier cashier;

	private boolean thief;

	private double myCash;
	private double myBill;

	public enum AgentState
	{DoingNothing, WaitingInRestaurant, GoingToRestaurant, WaitingToBeSeated, goSomewhereElse, BeingSeated, Seated, GotMenu,ReadyToOrder,Ordered,
		waitingForFood,Eating, DoneEating, AskForCheck, GoToCashier, GoToCook, poorLeaving,
		Leaving};
		private AgentState state = AgentState.DoingNothing;//The start state

		public enum AgentEvent 
		{none, Ready, gotHungry, wait, impatient,followHost, seated,thinking,tooPoor, ordering,giveOrder,gotFood,doneEating, 
			waitingForCheck, paying, paid, cleanDishes, gotWarning, finishedJob, doneLeaving};
			AgentEvent event = AgentEvent.none;

			private List<restaurant.WaiterAgent.foodPrice> FoodItems;// = new ArrayList<String>();
			private String myFood = null;
			private Random generator = new Random();
			private Semaphore atCashier = new Semaphore(0, true);
			private Semaphore atCook = new Semaphore(0, true);



			public AgentState getState(){
				return state;
			}

			public CustomerAgent(String name){
				super();
				this.name = name;
			}

			/**
			 * hack to establish connection to Host agent.
			 */
			public void setHost(HostAgent host) {
				this.host = host;
			}

			public void setWaiter (Waiter waiter){
				this.waiter  = waiter;
			}

			public void setCashier (CashierAgent cashier){
				this.cashier  = cashier;
			}


			public String getCustomerName() {
				return name;
			}
			// Messages

			public void gotHungry() {//from animation
				print("I'm hungry");	
				event = AgentEvent.gotHungry;
				stateChanged();
			}
			public void imInRestaurant(){ // from gui
				event = AgentEvent.Ready;
				stateChanged();

			}
			
			public void msgFullRest(){
				int j = generator.nextInt(2); 
				if(j == 0)
					event = AgentEvent.wait;
				else
					event = AgentEvent.impatient;
				stateChanged();

			}

			public void msgFollowMe(Waiter myWaiter) {
				setWaiter(myWaiter);
				event = AgentEvent.followHost;
				stateChanged();
			}

			public void msgHereIsMenu(List<restaurant.WaiterAgent.foodPrice> menu){
				FoodItems = menu;
				stateChanged();
			}

			public void msgHereIsUpdatedMenu(List<restaurant.WaiterAgent.foodPrice> menu){
				FoodItems = menu;
				state  = AgentState.GotMenu;
				event = AgentEvent.thinking;
				stateChanged();
			}

			public void msgWhatIsOrder()
			{
				customerGui.menuReceived = false;
				event = AgentEvent.ordering;
				stateChanged();		
			}


			public void msgHereIsFood()
			{
				event = AgentEvent.gotFood;
				stateChanged();
			}

			public void msgHereIsCheck(double money){
				myBill = money;
				event = AgentEvent.paying;
				stateChanged();
			}

			public void msgCleanDishes(){
				event = AgentEvent.cleanDishes;
				myCash = 0;
				myBill = 0;
				stateChanged();
			}

			public void msgWarning(){
				event = AgentEvent.gotWarning;
				myCash = 0;
				myBill = 0;
				stateChanged();
			}

			public void msgThankYou(){
				event = AgentEvent.paid;
				myBill = 0;
				stateChanged();
			}

			public void msgAtCashier(){
				atCashier.release();
				stateChanged();
			}

			public void msgAtCook(){
				atCook.release();
				stateChanged();
			}

			public void msgAnimationFinishedGoToSeat() {
				//from animation
				event = AgentEvent.seated;
				stateChanged();
			}
			public void msgAnimationFinishedLeaveRestaurant() {
				//from animation
				event = AgentEvent.doneLeaving;
				stateChanged();
			}

			/**
			 * Scheduler.  Determine what action is called for, and do it.
			 */
			protected boolean pickAndExecuteAnAction() {
				//	CustomerAgent is a finite state machine

				if (state == AgentState.DoingNothing && event == AgentEvent.gotHungry ){
					state = AgentState.GoingToRestaurant;
					goToWaitingArea();
					return true;
				}
				if (state == AgentState.GoingToRestaurant && event == AgentEvent.Ready){
					state = AgentState.WaitingToBeSeated;

					goToRestaurant();
					return true;
				}
				
				if (state == AgentState.WaitingInRestaurant && event == AgentEvent.wait ){
					state = AgentState.WaitingToBeSeated;
					waiting();
					return true;
				}
				if (state == AgentState.WaitingInRestaurant && event == AgentEvent.impatient ){
					state = AgentState.goSomewhereElse;
					leaving();
					return true;
				}
				
				if (state == AgentState.WaitingToBeSeated && event == AgentEvent.followHost ){
					state = AgentState.BeingSeated;
					SitDown();
					return true;
				}
				
				if (state == AgentState.WaitingInRestaurant && event == AgentEvent.followHost ){
					state = AgentState.BeingSeated;
					SitDown();
					return true;
				}
				if (state == AgentState.BeingSeated && event == AgentEvent.seated){
					state = AgentState.GotMenu;
					WhatToOrder();
					return true;
				}


				if (state == AgentState.GotMenu && event == AgentEvent.thinking){
					state = AgentState.ReadyToOrder;
					CallWaiterToOrder();
					return true;
				}

				if (state == AgentState.ReadyToOrder && event == AgentEvent.ordering){
					state = AgentState.Ordered;
					orderFood();
					return true;
				}
				
			

				if (state == AgentState.Ordered && event == AgentEvent.giveOrder)
				{
					state = AgentState.waitingForFood;
					waitingFood();
					return true;
				}

				if (state == AgentState.waitingForFood && event == AgentEvent.gotFood)
				{
					state = AgentState.Eating;
					EatFood();
					return true;
				}

				if (state == AgentState.Eating && event == AgentEvent.doneEating){
					state = AgentState.AskForCheck;
					getCheck();
					return true;
				}

				if (state == AgentState.AskForCheck && event == AgentEvent.paying){
					state = AgentState.GoToCashier;
					giveCheck();
					return true;
				}

				if (state == AgentState.GoToCashier && (event == AgentEvent.gotWarning || 
						event == AgentEvent.paid)){
					state = AgentState.Leaving;
					leaveTable();
					return true;
				}

				if (state == AgentState.GoToCashier && event == AgentEvent.cleanDishes){
					state = AgentState.GoToCook;
					cleaningDishes();
					return true;
				}

				if (state == AgentState.GoToCook && event == AgentEvent.finishedJob){
					state = AgentState.Leaving;
					leavingRestaurant();
					return true;
				}

				if (state == AgentState.Leaving && event == AgentEvent.doneLeaving){
					state = AgentState.DoingNothing;

					//no action
					return true;
				}
				return false;
			}

			// Actions


			private void waitingFood() {
				// TODO Auto-generated method stub
				timer.schedule(new TimerTask() 
				{
					public void run() 
					{
						customerGui.isThinking =false;

						stateChanged();
					}
				},		700);//how long to wait before running task
			}

			private void goToWaitingArea() {
				customerGui.goToWaiting();
			}
			
			private void goToRestaurant() {
				Do("Going to restaurant");
				customerGui.msgThinking("waiting to be seated");
				host.msgIWantFood(this);//send our instance, so he can respond to us
				
			}
			
			private void waiting(){
				Do("I'm going to wait");
				host.msgImWaiting(this);
			}
			
			private void leaving(){
				Do("I don't want to wait");
				customerGui.DoExitRestaurant();
				host.msgImLeaving(this);

			}

			private void SitDown() { 

				Do("Being seated. Going to table");
			}




			private void WhatToOrder(){
				Do("Thinking what to order");
				customerGui.menuReceived = true;
				thinkingTime.schedule(new TimerTask(){
					public void run(){
						//int i = generator.nextInt(4); // random [0- 4)

						event = AgentEvent.thinking;
						stateChanged();

					}
				}, getThinkingTime()*1000); //how long customer thinks about their order


			}

			private void CallWaiterToOrder()
			{
				Do("Call waiter");
				
				customerGui.msgThinking("Ready.");
				timer.schedule(new TimerTask() 
				{
					public void run() 
					{
						customerGui.isThinking = false;
					}
				}, 500 );
				waiter.msgReadyToOrder(this);

				
			}

			private void orderFood(){
				
				if(thief)//if customer is thief
				{
					boolean cheating = false;
					for(int i = 0; i<FoodItems.size(); i++){
						if(FoodItems.get(i).price > myCash){ //order something beyond what he can pay
							myFood = FoodItems.get(i).food;
							cheating = true;
						}
					}
					if(!cheating)
					{//thief has too much money!
						int j = generator.nextInt(FoodItems.size()); 
						myFood = FoodItems.get(j).food;
					}
				}

				else{//if not a thief
					boolean poor = true;
					 List<Integer> rand  = new ArrayList<Integer>();
					 rand.clear();
					for(int i = 0; i<FoodItems.size(); i++){
						if(FoodItems.get(i).price < myCash){ //order something beyond what he can pay
							rand.add(i);
							poor = false;
						}
					}
					
					
					if(poor || rand.size()== 0)//couldn't buy anything
						event = AgentEvent.tooPoor;
					else{
						int k = generator.nextInt(rand.size()); 
						myFood = FoodItems.get(rand.get(k)).food;
					}

				}

				if(thief || event !=AgentEvent.tooPoor  ){
				Do("order " + myFood);
				
				
				customerGui.waitingMyFood(myFood);
				
				waiter.msgGiveOrder(this,myFood);
				event = AgentEvent.giveOrder;
				stateChanged();

				}
				else{
					customerGui.msgThinking("Too Expensive.");
					timer.schedule(new TimerTask() 
					{
						public void run() 
						{
							customerGui.msgThinking("Can't afford.");
							
						}
					},		400);//how long to wait before running task
					
					timer.schedule(new TimerTask() 
					{
						public void run() 
						{
							leaveTable();
							stateChanged();
						}
					},		600);//how long to wait before running task
					
				}
			}


			private void EatFood() {
				Do("Eating Food");
				customerGui.createMyFood(myFood);
				//This next complicated line creates and starts a timer thread.
				//We schedule a deadline of getHungerLevel()*1000 milliseconds.
				//When that time elapses, it will call back to the run routine
				//located in the anonymous class created right there inline:
				//TimerTask is an interface that we implement right there inline.
				//Since Java does not all us to pass functions, only objects.
				//So, we use Java syntactic mechanism to create an
				//anonymous inner class that has the public method run() in it.
				timer.schedule(new TimerTask() {

					public void run() {
						print("Done eating, " + myFood);
						event = AgentEvent.doneEating;
						customerGui.createMyFood("Done");
						customerGui.foodReceived = false;
						stateChanged();
					}
				},
				getHungerLevel() * 1000);//how long to wait before running task

			}

			private void getCheck(){
				Do("ask for check");
				
				customerGui.msgThinking("Check Please.");
				timer.schedule(new TimerTask() 
				{
					public void run() 
					{
						customerGui.isThinking = false;
					}
				}, 400 );//how long to wait before running task
			

				waiter.msgAskForCheck(this);
				event = AgentEvent.waitingForCheck;
				stateChanged();
			}

			private void giveCheck(){
				//go to cashier
				customerGui.goToCashier();
				Do("go to cashier to pay");
				try {
					atCashier.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if(myCash < myBill){//means can't pay
					
					customerGui.msgThinking("I don't have enough.");
					timer.schedule(new TimerTask() 
					{
						public void run() 
						{
							customerGui.isThinking = false;
						}
					}, 400 );//how long to wait before running task
					cashier.msgCantPay(this, myCash);
				}
				else
				{
					myCash = myCash - myBill;
					
					customerGui.msgThinking("Here is money.");
					timer.schedule(new TimerTask() 
					{
						public void run() 
						{
							customerGui.isThinking = false;

						}
					}, 400 );//how long to wait before running task
					cashier.hereIsMoney(this, myBill);


				}

			}

			private void cleaningDishes() {
				// go to cook
				customerGui.createMyFood("done");
				waiter.msgLeavingTable(this);
				customerGui.DoGoToCook();
				try {
					atCook.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//at the cook, clean for 10 sec
				customerGui.msgThinking("cleaning dishes");
				timer.schedule(new TimerTask() {

					public void run() {
						Do("Done cleaning");
						customerGui.msgThinking("Done");
						event = AgentEvent.finishedJob;
						stateChanged();
					}
				},
				getCleaningTime() * 1000);//how long to wait before running task
			}


			private void leaveTable() {
				customerGui.createMyFood("done");
				Do("Leaving.");
				if(state == AgentState.poorLeaving){
					Do("Too poor to buy anything");
				}
				
				waiter.msgLeavingTable(this);
				customerGui.msgThinking("Goodbye");

				customerGui.DoExitRestaurant();
			}

			private void leavingRestaurant() {

				Do("Leaving.");
				customerGui.msgThinking("Goodbye");
				customerGui.DoExitRestaurant();
			}


			// Accessors, etc.

			public String getName() {
				return name;
			}

			public int getHungerLevel() {
				return hungerLevel;
			}

			public int getThinkingTime(){
				return thinking;
			}

			public int getCleaningTime(){
				return cleaning;
			}

			public void setHungerLevel(int hungerLevel) {
				this.hungerLevel = hungerLevel;
				//could be a state change. Maybe you don't
				//need to eat until hunger lever is > 5?
			}

			public String toString() {
				return "customer " + getName();
			}

			public void setGui(CustomerGui g) {
				customerGui = g;
			}

			public void setCash(int cash){
				myCash = (double)cash;
			}

			public CustomerGui getGui() {
				return customerGui;
			}

			public boolean isThief() {
				return thief;
			}

			public void setThief(boolean thief) {
				this.thief = thief;
			}


			public class foodPrice{
				String food;
				Double price;

				foodPrice(String f, Double p){
					food = f;
					price = p;
				}
			}


			
}

