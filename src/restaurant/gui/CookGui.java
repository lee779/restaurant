package restaurant.gui;

import restaurant.CookAgent;
import restaurant.CustomerAgent;
import restaurant.WaiterAgent;
import restaurant.gui.WaiterGui.placeType;


import java.util.*;
import java.util.List;
import java.awt.*;

import javax.swing.ImageIcon;

public class CookGui implements Gui {

    private CookAgent agent = null;
    public boolean pickup = false;
    private int xPos = 400, yPos = 420, xHome, yHome;//default waiter position
    private int xDestination = 370, yDestination = 400;//default start position
    
    private String currFood;
    private int wSize = 20; //waitergui size
    private String foodItem="";
    
    public boolean dontUpdate = true;
    public enum placeType
    {cooking, nothing, pickup, plating, sink};
    placeType whereAmI = placeType.nothing;
    String thinkingBubble;
	 public Boolean isThinking =false;
	 public Boolean foodDone = false;
    private RestaurantGui restGui;
   
    public String msg = "None";

 
    private int xPast, yPast;
    private int rCount, lCount, dCount, uCount;
    private enum face
    {left, right, down, up};
    private face dir = face.right;
    face pastDir = face.right;
    

    public List<Image> cookL = new ArrayList<Image>(3);
    public List<Image> cookR = new ArrayList<Image>(3);
    public List<Image> cookU = new ArrayList<Image>(3);
    public List<Image> cookD = new ArrayList<Image>(3);

    
    
	
	 Image bubbleL, bubbleR;
	 ImageIcon bubbleIconL, bubbleIconR;
	 ImageIcon steakI, chickenI,pizzaI, saladI;
	 ImageIcon  cookIl, cookIr, cookIu, cookId;

	
	public Hashtable<String,Image> foodObjects = new Hashtable<String, Image>();
	public List<food> foods = new ArrayList<food>();

    public CookGui(CookAgent agent) {
        this.agent = agent;
      
    	
    }

    
    
    public void setUp(){
    	dir = face.up;
    	System.out.println("face up");
    }
 
    
    public CookGui(CookAgent c, RestaurantGui gui) {
    	
    		agent = c;
    		xPos = 450;
    		yPos = 800;
    		xDestination = 500;
    		yDestination = 610;
    		xHome = 500;
    		yHome = 610;
    		foodItem = "";
    		wSize= 20; 
    		restGui = gui;
    		whereAmI = placeType.nothing;
    		
    		 bubbleIconR = new ImageIcon("Images/cook_bubble_right.png");
    	        bubbleR = bubbleIconR.getImage();
    	     bubbleIconL = new ImageIcon("Images/cook_bubble_left.png");
    	        bubbleL = bubbleIconL.getImage();
    		 /////////////cook icons///////////////////
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
            	cookIl = new ImageIcon("Images/cook_left"+i+".png");
            	cookIr = new ImageIcon("Images/cook_right"+i+".png");
            	cookId = new ImageIcon("Images/cook_down"+i+".png");
            	cookIu = new ImageIcon("Images/cook_up"+i+".png");
            	
            	cookL.add(cookIl.getImage());
            	cookR.add(cookIr.getImage());
            	cookU.add(cookIu.getImage());
            	cookD.add(cookId.getImage());
            	
            }       
            
            
            steakI = new ImageIcon("Images/steak.png");
        	chickenI = new ImageIcon("Images/chicken.png");
        	saladI = new ImageIcon("Images/salad.png");
        	pizzaI = new ImageIcon("Images/pizza.png");

        	
        	
        		foodObjects.put("Steak", steakI.getImage());
        		foodObjects.put("Chicken", chickenI.getImage());
        		foodObjects.put("Salad", saladI.getImage());
        		foodObjects.put("Pizza", pizzaI.getImage());
    	
	}
    
    public void msgThinking(String msg){
		isThinking =true;
		thinkingBubble = msg;
	}
    
    public void setHome(int x, int y){
    	xHome = x;
    	yHome = y;
    }
    
    public int getXHome(){
    	return xHome;
    }

    public int getYHome(){
    	return yHome;
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

        if(xPos == xDestination && yPos == yDestination){
    		dir = face.up;

        	if(whereAmI == placeType.cooking){
        		whereAmI = placeType.nothing;
        		goToSink();
        	}
        	if(whereAmI == placeType.sink){
        		whereAmI = placeType.nothing;
        	}
        	
        	if (whereAmI == placeType.plating){
        		whereAmI = placeType.nothing;
            	createCustomerFood("");
            	this.msgThinking("Pickup Food");
            	
            	goHome();
        	}
        	if( whereAmI == placeType.pickup){
        		whereAmI = placeType.nothing;
        		goHome();
        	}
        	if(xPos == xHome && yPos == yHome){
        		this.isThinking =false;
        	}
        	
        	
        	
        }
        		
        
      
        
    }
    
   
    public void draw(Graphics2D g) {
    	
    	pastDir = dir;
    	
    	if(xPast<xPos){
    		//moved right
    		rCount++;
    		dir = face.right;
    		if(dir.equals(pastDir)){
    			//if continuous right movement
        		g.drawImage(cookR.get(rCount%40 +1 ),xPos,yPos,restGui);
    		}
    		else{
    			rCount = 0;
        		g.drawImage(cookR.get(rCount),xPos,yPos,restGui);
    		}
    	}
    	else if(xPast>xPos){
    		//moved left
    		lCount ++;
    		dir = face.left;
    		if(dir.equals(pastDir)){
    			//if continuous right movement
        		g.drawImage(cookL.get(lCount%40 + 1),xPos,yPos,restGui);
    		}
    		else{
    			lCount = 0;
        		g.drawImage(cookL.get(lCount),xPos,yPos,restGui);
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
            		g.drawImage(cookD.get(dCount%40 + 1),xPos,yPos,restGui);
        		}
        		else{
        			dCount = 0;
            		g.drawImage(cookD.get(dCount),xPos,yPos,restGui);
        		}
    		}
    		else if(yPast > yPos){
    			//moved up
        		uCount ++;
        		dir = face.up;
        		if(dir.equals(pastDir)){
        			//if continuous right movement
            		g.drawImage(cookU.get(uCount%40 +1 ),xPos,yPos,restGui);
        		}
        		else{
        			uCount = 0;
            		g.drawImage(cookU.get(uCount),xPos,yPos,restGui);
        		}
    		}
    		else{
    			if(dir.equals(face.down))
    				g.drawImage(cookD.get(0),xPos, yPos,restGui);
    			else if (dir.equals(face.up))
    				g.drawImage(cookU.get(0),xPos, yPos,restGui);
    			else if (dir.equals(face.left))
    				g.drawImage(cookL.get(0),xPos, yPos,restGui);
    			else 
    				g.drawImage(cookR.get(0),xPos, yPos,restGui);
    		}
    		
    		

    	}
    	
    	
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
        
    	if(foodDone){
    		if(xPos == 500 & yPos >570){
    			g.drawImage(foods.get(0).img, 500, 560,restGui);
    		}
    		else{
    		g.drawImage(foods.get(0).img, xPos+10,yPos+5, restGui);}
    	}
    	/*g.setColor(Color.BLACK);
		g.setFont(new Font(null, Font.PLAIN, 20));
		g.drawString(foodItem, xPos + 10,yPos +5);*/
 
    }
    

    public boolean isPresent() {
        return true;
    }
    
    public void goHome(){
    	xDestination = xHome;
    	yDestination = yHome;
    	whereAmI = placeType.nothing;
    	
    	
    	
    }
    
    public void goPlating(){
    	xDestination = 500;
    	yDestination = 570;
    	whereAmI = placeType.plating;
    	createCustomerFood(currFood);
    	foodDone = true;
    	food f = new food();
    	f.img = foodObjects.get(currFood);
    	foods.add(f);
    	System.out.println("foods size: " + foods.size());
    }

    public void DoCooking(String food){
    	
    	xDestination = 440;
    	yDestination = 600;
    	whereAmI = placeType.cooking;
    	currFood = food;
    	
    	
    }
    
    public void goToSink(){
    	xDestination = 550;
    	yDestination = 590;
		this.msgThinking("Cooking...");

    	whereAmI = placeType.sink;
    	
    }
    
    public void goToWaiter(){
    	xDestination = 500;
    	yDestination = 570;
    	whereAmI = placeType.pickup;
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
    }
    
    public class food{
    	Image img;
    }
   

}
