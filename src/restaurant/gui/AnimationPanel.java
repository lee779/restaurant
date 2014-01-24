package restaurant.gui;

import javax.swing.*;

import java.util.Hashtable;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;
import java.util.ArrayList;

public class AnimationPanel extends JPanel implements ActionListener {

	
	
	public class pair{
		int x;
		int y;
		public pair(int xnum, int ynum){
			x = xnum;
			y = ynum;
		}
	}
	public static Hashtable<Integer, pair> hashTable = new Hashtable<Integer, pair>();
	{hashTable.put(1,new pair(140,250));
	hashTable.put(2, new pair(290,320));
	hashTable.put(3, new pair(440,390));}
	
	
	
     Image table, cashierArea, kitchen;
	 ImageIcon tableIcon, cashierIcon, kitchenIcon;
   
	
	private final int frameInterval = 7;
	
	private final int WINDOW_W = 450;
    private final int WINDOW_H = 400;
    private final int WINDOW_X = 0;
    private final int WINDOW_Y = 0;
    
    private final int TABLE_Y = 250;
    private final int TABLE_H = 75;
    private final int TABLE_W =50;
    
    private Image bufferImage;
    private Dimension bufferSize;

    private List<Gui> guis = new ArrayList<Gui>();
    public boolean paused = false;

    public AnimationPanel() {
    	
    	tableIcon = new ImageIcon("Images/table.png");
    	cashierIcon = new ImageIcon("Images/cashierarea.png");
    	kitchenIcon = new ImageIcon("Images/kitchen.png");
        table = tableIcon.getImage();
        cashierArea = cashierIcon.getImage();
        kitchen = kitchenIcon.getImage();
        
        
        
        
    	setSize(WINDOW_W, WINDOW_H);
        setVisible(true);
        
        bufferSize = this.getSize();
 
    	Timer timer = new Timer(frameInterval, this );
    	timer.start();
    }

	public void actionPerformed(ActionEvent e) {
		repaint();  //Will have paintComponent called
	}

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
       

        
        //Clear the screen by painting a rectangle the size of the frame
        g2.setColor(getBackground());
        g2.fillRect(WINDOW_X, WINDOW_Y, this.getWidth(), this.getHeight() );

        //draw boundary
        g2.setColor(Color.BLACK);
        g2.drawRect(WINDOW_X, WINDOW_Y, 635, this.getHeight());
        
        //draw patio for waiter
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(1,this.getHeight()-140, 75 , 140);
        g2.setColor(Color.BLACK);
        g2.drawRect(1,this.getHeight()-140, 75 , 140);
        
       
        //kitchen station
        g2.drawImage(kitchen,this.getWidth()-240, this.getHeight()-114, null);
      
	   //cashier station
     	 g2.drawImage(cashierArea, 535,100,null);
        
       
        //draw waiting station for customer
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(1, 1, 80, 220);
        
        //draw waiter home station
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(100,2, 300 , 70);
        g2.setColor(Color.BLACK);
        g2.drawRect(100,2, 300 , 70);
        
        for (int i = 0; i < 6; i ++){
        	 g2.setColor(Color.BLACK);
             g2.drawRect(100 + i*50,2, 35 , 35);
        }
        
       
    

        
        
        //draw cashier station
        
        //Here is the table
        

       // g2.setColor(Color.YELLOW);
        for (int j=1;j<=3;j++){
		g2.drawImage(table,hashTable.get(j).x-10,hashTable.get(j).y-20, null);
        }
    
        
       


        for(Gui gui : guis) {
            if (gui.isPresent()) {
            	if(!paused){
                gui.updatePosition();}
            }
        }

        for(Gui gui : guis) {
        	
            if (gui.isPresent()) {
                gui.draw(g2);
            }
        }
    }
    
    public void addGui(WaiterGui gui){
    	guis.add(gui);
    }

    public void addGui(CustomerGui gui) {
        guis.add(gui);
    }
    
    public void addGui(CookGui gui) {
        guis.add(gui);
    }

  
}
