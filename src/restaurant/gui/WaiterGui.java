package restaurant.gui;

import static java.lang.System.*;

import restaurant.CustomerAgent;
import restaurant.WaiterAgent;
import sun.security.provider.SystemSigner;


import java.util.*;
import java.util.List;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class WaiterGui implements Gui {

    private WaiterAgent agent = null;
    
    private int xPos = -20, yPos = -20, xPast, yPast;//default waiter position
    private int xDestination = -20, yDestination = -20;//default start position
    
    private int rCount, lCount, dCount, uCount;
    private enum face
    {left, right, down, up};
    face dir = face.right;
    face pastDir = face.right;
    
    
    
    private int xHome, yHome;
    private int wSize = 20; //waitergui size
    private String foodItem="";
    public boolean dontUpdate = true;
    public enum placeType
    {pickUpCust, atOutside, atCashier, atCook, none, atTable};
    placeType whereAmI = placeType.none;
    public int xTable = AnimationPanel.hashTable.get(1).x;
    private RestaurantGui restGui;
    private CustomerGui currentGui;
    public int yTable = AnimationPanel.hashTable.get(1).y;
    
    public String msg = "None";
    Image img;
    
    String thinkingBubble;
	 public Boolean isThinking =false;
	 public Boolean pickUpFood = false;
    Image bubbleL, bubbleR;
    public List<Image> waiterL = new ArrayList<Image>(3);
    public List<Image> waiterR = new ArrayList<Image>(3);
    public List<Image> waiterU = new ArrayList<Image>(3);
    public List<Image> waiterD = new ArrayList<Image>(3);

	 ImageIcon bubbleIconL, bubbleIconR, waiterIl, waiterIr, waiterIu, waiterId;
	  public Hashtable<String,Image> foodObjects = new Hashtable<String, Image>();
    	 ImageIcon steakI, chickenI,pizzaI, saladI, checkI;
        
    private boolean wantBreak = false; 

    public WaiterGui(WaiterAgent agent) {
        this.agent = agent;
        
    }
    
    
    
    public WaiterGui(WaiterAgent w, RestaurantGui gui) {
    	
    		agent = w;
    		xPos = 40;
    		yPos = -20;
    		xDestination = 40;
    		yDestination = -20;
    		xHome = 0;
    		yHome = 0;
    		foodItem = "";
    		wSize= 20; 
    		restGui = gui;

    		bubbleIconL = new ImageIcon("Images/waiter_bubble_left.png");
            bubbleL = bubbleIconL.getImage();
            bubbleIconR = new ImageIcon("Images/waiter_bubble_right.png");
            bubbleR = bubbleIconR.getImage();
            
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
            	waiterIl = new ImageIcon("Images/waiter_left"+i+".png");
            	waiterIr = new ImageIcon("Images/waiter_right"+i+".png");
            	waiterId = new ImageIcon("Images/waiter_down"+i+".png");
            	waiterIu = new ImageIcon("Images/waiter_up"+i+".png");
            	
            	
            	waiterL.add(waiterIl.getImage());
            	waiterR.add(waiterIr.getImage());
            	waiterU.add(waiterIu.getImage());
            	waiterD.add(waiterId.getImage());
            	
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
         		foodObjects.put("Check", checkI.getImage());
              
	}
    
    public void setHome(int x, int y){
    	xHome = x;
    	yHome = y;
    	xDestination = x;
    	yDestination = y;
    }
    
    public int getXHome(){
    	return xHome;
    }

    public int getYHome(){
    	return yHome;
    }

	public void updatePosition() {
		xPast =xPos;
		yPast = yPos;    	
		
		if (xPos < xDestination)
    	{
			xPos++;
    	}
        else if (xPos > xDestination){
        	xPos--;
        }
	
        if (yPos < yDestination)
        {
        	yPos++;
		}
        	
        
        else if (yPos > yDestination)
        {	
        	yPos--;
        }

        
        if (xPos == xDestination && yPos == yDestination
        		&& whereAmI == placeType.atTable) {
        	whereAmI = placeType.none;
        	agent.msgAtTable();
        	System.out.println("at table");
        	pickUpFood= false;
        	dir = face.down;
        }
        if(xPos == xDestination && yPos == yDestination && whereAmI == placeType.atCook){
        		
        	whereAmI = placeType.none;
        	System.out.println("facing down");
        	dir = face.down;
        	agent.msgAtCook();
        }
        if(xPos == xDestination && yPos == yDestination
        		&& whereAmI == placeType.atOutside){
        	whereAmI = placeType.none;
        	dir = face.right;
        	agent.msgAtOutside();}
        
        if(xPos == xDestination && yPos == yDestination 
        		&& whereAmI == placeType.pickUpCust){
        	whereAmI = placeType.none;
        	agent.msgAtHome();}
        
        if(xPos == xDestination && yPos == yDestination
        		 & whereAmI == placeType.atCashier){
        	whereAmI = placeType.none;
        	dir = face.right;
        	pickUpFood =true;
        	foodItem ="Check";
        	agent.msgAtCashier();}
        
        if(xPos == xHome && yPos == yHome){
        	dir = face.down;
        }
      
        
    }
    
    public void doGoToBreak(){
    	breakStatus("Granted");
    	whereAmI = placeType.atOutside;
    	xDestination = 20;
    	yDestination = 540;
    }


    public void draw(Graphics2D g) {
    	
    	pastDir = dir;
    	
    	if(xPast<xPos){
    		//moved right
    		rCount++;
    		dir = face.right;
    		if(dir.equals(pastDir)){
    			//if continuous right movement
        		g.drawImage(waiterR.get(rCount%40 +1 ),xPos,yPos,restGui);
    		}
    		else{
    			rCount = 0;
        		g.drawImage(waiterR.get(rCount),xPos,yPos,restGui);
    		}
    	}
    	else if(xPast>xPos){
    		//moved left
    		lCount ++;
    		dir = face.left;
    		if(dir.equals(pastDir)){
    			//if continuous right movement
        		g.drawImage(waiterL.get(lCount%40 + 1),xPos,yPos,restGui);
    		}
    		else{
    			lCount = 0;
        		g.drawImage(waiterL.get(lCount),xPos,yPos,restGui);
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
            		g.drawImage(waiterD.get(dCount%40 + 1),xPos,yPos,restGui);
        		}
        		else{
        			dCount = 0;
            		g.drawImage(waiterD.get(dCount),xPos,yPos,restGui);
        		}
    		}
    		else if(yPast > yPos){
    			//moved up
        		uCount ++;
        		dir = face.up;
        		if(dir.equals(pastDir)){
        			//if continuous right movement
            		g.drawImage(waiterU.get(uCount%40 +1 ),xPos,yPos,restGui);
        		}
        		else{
        			uCount = 0;
            		g.drawImage(waiterU.get(uCount),xPos,yPos,restGui);
        		}
    		}
    		else{
    			if(dir.equals(face.down))
    				g.drawImage(waiterD.get(0),xPos, yPos,restGui);
    			else if (dir.equals(face.up))
    				g.drawImage(waiterU.get(0),xPos, yPos,restGui);
    			else if (dir.equals(face.left))
    				g.drawImage(waiterL.get(0),xPos, yPos,restGui);
    			else 
    				g.drawImage(waiterR.get(0),xPos, yPos,restGui);
    		}
    		
    		

    	}
    	
    	/*g.setColor(Color.MAGENTA);
        g.fillRect(xPos, yPos, wSize, wSize);*/
        /*
    	g.setColor(Color.BLACK);
		g.setFont(new Font(null, Font.PLAIN, 20));
		g.drawString(foodItem, xPos,yPos+6);
		*/
		
		

		if(isThinking){
			///thinking something or saying something must display the message
			//(x,y) top left
			g.drawImage(bubbleR,xPos+20,yPos-bubbleL.getHeight(restGui), restGui);
			g.setColor(Color.BLACK);
			int fontsize = 14;
			if(thinkingBubble.length()>15){
				fontsize = 13;
			}
			g.setFont(new Font("TimesRoman", Font.PLAIN, fontsize));
			g.drawString(thinkingBubble, xPos+30,yPos-bubbleL.getHeight(restGui)+35);
		}
		
		if(pickUpFood){
			g.drawImage(foodObjects.get(foodItem), xPos-20, yPos,restGui);
		}

 
    }
    
    public void breakStatus(String statusB){
    	msg = statusB;
    	restGui.updateBreak(statusB, agent);}
    
    public void returnWork(){
    	msg = "None";
    	agent.msgWantToReturn();
    }
    
    public void setBreak(){
    	wantBreak = true;
    	agent.msgWantToGoToBreak();
    }
    
    public boolean wantBreak() {
		return wantBreak;
	}

    public boolean isPresent() {
        return true;
    }

    public void DoBringToTable(CustomerAgent customer, int tableNumber) {
    	
    	
    	xTable = AnimationPanel.hashTable.get(tableNumber).x;
    	yTable = AnimationPanel.hashTable.get(tableNumber).y;
   
    	customer.getGui().goTo(xTable,yTable); //hack from host gui to customer gui to go to certain table
 
        xDestination = xTable + 20;
        yDestination = yTable - 45;
    	whereAmI = placeType.atTable;

    	
    }
    
    
    
    public void goToCashier()
    {
    	whereAmI = placeType.atCashier;
		System.out.println("going to cashier");

    	xDestination = 500;
    	yDestination = 130;
    }

    public void goToTable(int tableNumber)
    {
    	xTable = AnimationPanel.hashTable.get(tableNumber).x;
    	yTable = AnimationPanel.hashTable.get(tableNumber).y;
    	System.out.println("destination: table" + tableNumber + " x: " + xTable + " y: "+ yTable);
    	xDestination = xTable + 20;
    	yDestination = yTable - 45;
    	
    	whereAmI = placeType.atTable;
    }

    public void DoPickCustomer(CustomerAgent cust) {
		whereAmI = placeType.pickUpCust;
		System.out.println("picking customer");
        xDestination = cust.getGui().getX() + 20;
        yDestination = cust.getGui().getY()- 20;
        
    }
    
	public void msgThinking(String msg){
		isThinking =true;
		thinkingBubble = msg;
	}
    
	public void DoLeaveCustomer() {
        xDestination = xHome;
        yDestination = yHome;
    }
	
	public void doReturnWork(){
		xDestination = xHome;
        yDestination = yHome;	
        //set break button again
        restGui.updateInfoPanel("Waiter", (Object)agent, false);
	}
	
	public void DoGoToCook(){
		whereAmI = placeType.atCook;
		xDestination = 500;
		yDestination = 525;
	}
	
	public void DoPickUpFood(String food){
		whereAmI = placeType.atCook;
		
		xDestination = 500;
		yDestination = 525;
		
	}


	public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }
    public void createCustomerFood(String food){
    	if(food == "Steak")
    		foodItem = "St";
    	else if(food == "Chicken")
    		foodItem = "Ch";
    	else if(food == "Salad")
    		foodItem = "Sa";
    	else if(food == "Pizza")
    		foodItem = "Pi";
    	else if(food == "Check")
    		foodItem = "$";
    	else
    		foodItem ="";
    	
    	foodItem = food;
    }
    


}
