package restaurant.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;


import restaurant.CustomerAgent;
import restaurant.gui.WaiterGui.placeType;
import javax.swing.ImageIcon;

public class CustomerGui implements Gui{

	private CustomerAgent agent = null;
	private boolean isPresent = false;
	private boolean isHungry = false;
	
	private int xPast, yPast;
    private int rCount, lCount, dCount, uCount;
    private enum face
    {left, right, down, up};
    face dir = face.right;
    face pastDir = face.right;
	
	

	//private HostAgent host;
	RestaurantGui gui;

	private int xPos, yPos, cSize, yCord = 100;
	private int offCord = -40;
	private int xDestination, yDestination, xHome, yHome;
	private enum Command {noCommand, GoToWaiting, GoToSeat, GoToCashier, GoToCook,LeaveRestaurant};
	private Command command=Command.noCommand;
	private enum FoodAnimation{none,waiting,thinking,steak, chicken,salad, pizza, paying};
	private FoodAnimation foodAnimate = FoodAnimation.none;
	 String thinkingBubble;
	 public Boolean isThinking =false;
	 public Boolean foodReceived =false, menuReceived =false, checkReceived = false;
	private String food;
	 Image bubbleL, bubbleR, menu;
	 ImageIcon bubbleIconL, bubbleIconR, menuIcon;
	  public Hashtable<String,Image> foodObjects = new Hashtable<String, Image>();
 	 ImageIcon steakI, chickenI,pizzaI, saladI, checkI;
 	 
	    public List<Image> custL = new ArrayList<Image>(3);
	    public List<Image> custR = new ArrayList<Image>(3);
	    public List<Image> custU = new ArrayList<Image>(3);
	    public List<Image> custD = new ArrayList<Image>(3);

		ImageIcon  custIl, custIr, custIu, custId;



	public CustomerGui(CustomerAgent c, RestaurantGui gui){ //HostAgent m) {
		agent = c;
		xPos = -40;
		yPos = -40;
		xDestination = -40;
		yDestination = -40;
		cSize= 20; 
		this.gui = gui;
		
		bubbleIconL = new ImageIcon("Images/cust_bubble_left.png");
        bubbleL = bubbleIconL.getImage();
        bubbleIconR = new ImageIcon("Images/cust_bubble_right.png");
        bubbleR = bubbleIconR.getImage();
        menuIcon = new ImageIcon("Images/menu.png");
        menu = menuIcon.getImage();
        
        /////////////waiter icons///////////////////
        int i = 1;
        for (int j=1;j<=41;j++){
        	
        	/*i = j;
        	if(j == 3){
        		i = 1;
        	}
        	if(j==4){
        		i = 3;
        	}*/
        	i = j;
        	if(j>=2 && j<=21){
        		i = 2;
        	}
        	if (j>=22 && j<=41){
        		i=3;
        	}
        	custIl = new ImageIcon("Images/cust_left"+i+".png");
        	custIr = new ImageIcon("Images/cust_right"+i+".png");
        	custId = new ImageIcon("Images/cust_down"+i+".png");
        	custIu = new ImageIcon("Images/cust_up"+i+".png");
        	
        	
        	custL.add(custIl.getImage());
        	custR.add(custIr.getImage());
        	custU.add(custIu.getImage());
        	custD.add(custId.getImage());
        	
        }       
        steakI = new ImageIcon("Images/steak.png");
     	chickenI = new ImageIcon("Images/chicken.png");
     	saladI = new ImageIcon("Images/salad.png");
     	pizzaI = new ImageIcon("Images/pizza.png");
     	checkI = new ImageIcon("Images/check.png");
     	
     		foodObjects.put("Steak", steakI.getImage());
     		foodObjects.put("Chicken", chickenI.getImage());
     		foodObjects.put("Salad", saladI.getImage());
     		foodObjects.put("Pizza", pizzaI.getImage());
     		
        
        
	}
	
	public void setDestination(int x, int y){
		xDestination = x;
		yDestination = y;
		xHome = x;
		yHome = y;
		command = Command.GoToWaiting;
	}
	
	public void msgThinking(String msg){
		isThinking =true;
		thinkingBubble = msg;
	}
	
	public void goToWaiting(){
		xDestination = xHome;
		yDestination = yHome;
		command = Command.GoToWaiting;	
	}
	
	public int getX(){
		return xDestination;
		
	}
	public int getY(){
		return yDestination;
		
	}


	public void updatePosition() {

		xPast = xPos;
		yPast = yPos;
		if (xPos < xDestination)
			xPos++;
		else if (xPos > xDestination)
			xPos--;

		if (yPos < yDestination)
			yPos++;
		else if (yPos > yDestination)
			yPos--;

		if (xPos == xDestination && yPos == yDestination) {
			if (command==Command.GoToSeat){
				command = Command.noCommand;

				agent.msgAnimationFinishedGoToSeat();
			}
			else if (command==Command.LeaveRestaurant) {
				command = Command.noCommand;

				agent.msgAnimationFinishedLeaveRestaurant();
				System.out.println("about to call gui.setCustomerEnabled(agent);");
				isHungry = false;
				gui.setCustomerEnabled(agent);
			}
			else if (command == Command.GoToCashier){
				command = Command.noCommand;
				checkReceived = false;
				agent.msgAtCashier();
				System.out.println(agent.getName() + " at cashier");
			}
			else if (command == Command.GoToCook){
				command = Command.noCommand;

				agent.msgAtCook();
				System.out.println(agent.getName() + " goes cleaning.");
			}
			else if (command == Command.GoToWaiting){
				command = Command.noCommand;

				agent.imInRestaurant();
				System.out.println(agent.getName() + " goes to restaurant.");
			}
			
			command=Command.noCommand;
		}
		
	}

	public void draw(Graphics2D g) {

		//g.setColor(Color.GREEN);
		//g.fillRect(xPos, yPos, cSize, cSize);
		pastDir = dir;
    	
    	if(xPast<xPos){
    		//moved right
    		rCount++;
    		dir = face.right;
    		if(dir.equals(pastDir)){
    			//if continuous right movement
        		g.drawImage(custR.get(rCount%40 +1 ),xPos,yPos,gui);
    		}
    		else{
    			rCount = 0;
        		g.drawImage(custR.get(rCount),xPos,yPos,gui);
    		}
    	}
    	else if(xPast>xPos){
    		//moved left
    		lCount ++;
    		dir = face.left;
    		if(dir.equals(pastDir)){
    			//if continuous right movement
        		g.drawImage(custL.get(lCount%40 + 1),xPos,yPos,gui);
    		}
    		else{
    			lCount = 0;
        		g.drawImage(custL.get(lCount),xPos,yPos,gui);
    		}
    	}
    	else{
    		//didn't move left or right
    		if(yPast<yPos){
    			//moved down
        		dCount ++;
        		dir = face.down;
        		if(dir.equals(pastDir)){
        			//if continuous right movement
            		g.drawImage(custD.get(dCount%40 + 1),xPos,yPos,gui);
        		}
        		else{
        			dCount = 0;
            		g.drawImage(custD.get(dCount),xPos,yPos,gui);
        		}
    		}
    		else if(yPast > yPos){
    			//moved up
        		uCount ++;
        		dir = face.up;
        		if(dir.equals(pastDir)){
        			//if continuous right movement
            		g.drawImage(custU.get(uCount%40 +1 ),xPos,yPos,gui);
        		}
        		else{
        			uCount = 0;
            		g.drawImage(custU.get(uCount),xPos,yPos,gui);
        		}
    		}
    		else{
    			if(dir.equals(face.down))
    				g.drawImage(custD.get(0),xPos, yPos,gui);
    			else if (dir.equals(face.up))
    				g.drawImage(custU.get(0),xPos, yPos,gui);
    			else if (dir.equals(face.left))
    				g.drawImage(custL.get(0),xPos, yPos,gui);
    			else 
    				g.drawImage(custR.get(0),xPos, yPos,gui);
    		}
    		
    		

    	}

		
		

		
		if(isThinking){
			///thinking something or saying something must display the message
			//(x,y) top left
			g.drawImage(bubbleR,xPos+20,yPos-bubbleL.getHeight(gui), gui);
			g.setColor(Color.BLACK);
			int fontsize = 14;
			if(thinkingBubble.length()>15){
				fontsize = 13;
			}
			g.setFont(new Font("TimesRoman", Font.PLAIN, fontsize));
			g.drawString(thinkingBubble, xPos+30,yPos-bubbleL.getHeight(gui)+35);
		}
		
		if(foodReceived){
			g.drawImage(foodObjects.get(food),xPos+20, yPos,gui);
		}
		if(menuReceived){
			g.drawImage(menu,xPos+20,yPos, gui);
		}
		if (checkReceived){
			g.drawImage(checkI.getImage(), xPos + 20, yPos, gui);
		}

	}

	public void createThinkingFood(){

		foodAnimate = FoodAnimation.thinking;

	}
	public void waitingMyFood(String order){
		this.msgThinking("I want "+order);
				
	//	foodAnimate = FoodAnimation.waiting;

	}
	
	public void createPaying(){
		foodAnimate = FoodAnimation.paying;

	}
	
	

	public void createMyFood(String myFood)
	{
		food = myFood;
		foodReceived = true;
		if (myFood == "Steak")
			foodAnimate = FoodAnimation.steak;
		else if(myFood == "Chicken")
			foodAnimate = FoodAnimation.chicken;
		else if(myFood == "Salad")
			foodAnimate = FoodAnimation.salad;
		else if(myFood == "Pizza")
			foodAnimate = FoodAnimation.pizza;
		else
			foodAnimate = FoodAnimation.none;

	}

	public boolean isPresent() {
		return isPresent;
	}
	public void setHungry() {
		isHungry = true;
		agent.gotHungry();
		setPresent(true);
	}
	public boolean isHungry() {
		return isHungry;
	}

	public void setPresent(boolean p) {
		isPresent = p;
	}

	public void goTo(int x, int y) {//later you will map seatnumber to table coordinates.

		xDestination = x;
		yDestination = y;

		command = Command.GoToSeat;
		isThinking =false;
	}

	public void DoGoToCook(){

		xDestination = -20;
		yDestination = 200;
		command = Command.GoToCook;
		System.out.println("going to cook");

	}


	public void goToCashier()
	{
		checkReceived= true;
		createPaying();
		command  = Command.GoToCashier;
		System.out.println("going to cashier");
		xDestination = 500;
		yDestination = 140;
	}




	public void DoExitRestaurant() {
		this.msgThinking("Goodbye");
		xDestination = offCord;
		yDestination = offCord;
		command = Command.LeaveRestaurant;
	}
}
