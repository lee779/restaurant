package restaurant.gui;

import restaurant.CustomerAgent;
import restaurant.WaiterAgent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main GUI class.
 * Contains the main frame and subsequent panels
 */
public class RestaurantGui extends JFrame implements ActionListener {
	/* The GUI has two frames, the control frame (in variable gui) 
	 * and the animation frame, (in variable animationFrame within gui)
	 */
	JFrame animationFrame = new JFrame("Restaurant Animation");
	AnimationPanel animationPanel = new AnimationPanel();

	/* restPanel holds 2 panels
	 * 1) the staff listing, menu, and lists of current customers all constructed
	 *    in RestaurantPanel()
	 * 2) the infoPanel about the clicked Customer (created just below)
	 */    
	private RestaurantPanel restPanel = new RestaurantPanel(this);

	/* infoPanel holds information about the clicked customer, if there is one*/
	private JPanel infoPanel;
	private JLabel infoLabel; //part of infoPanel

	private JPanel interactionPanel = new JPanel();

	private JCheckBox stateCB;//part of infoLabel
	private JLabel stateL;

	private Object currentPerson;/* Holds the agent that the info is about.
    								Seems like a hack */

	
	//animation frame bound setup 
	private double restPanelDim = .8, infoPanelDim = 0.2;
	/**
	 * Constructor for RestaurantGui class.
	 * Sets up all the gui components.
	 */
	public RestaurantGui() {
		int WINDOWX = 1300;
		int WINDOWY = 700;

		stateCB = new JCheckBox();
		stateCB.setVisible(false);
		stateCB.addActionListener(this);


		stateL = new JLabel();
		stateL.setVisible(false);

		//animationFrame.add(animationPanel); 

		setBounds( WINDOWX, WINDOWY, WINDOWX, WINDOWY);


		interactionPanel.setLayout(new FlowLayout());

		Dimension restDim = new Dimension((int)(WINDOWX*.5), (int)(WINDOWY * restPanelDim));
		restPanel.setPreferredSize(restDim);
		restPanel.setMinimumSize(restDim);
		restPanel.setMaximumSize(restDim);

		interactionPanel.add(restPanel);

		// Now, setup the info panel
		Dimension infoDim = new Dimension((int)(WINDOWX*.5), (int) (WINDOWY * infoPanelDim));
		infoPanel = new JPanel();
		infoPanel.setPreferredSize(infoDim);
		infoPanel.setMinimumSize(infoDim);
		infoPanel.setMaximumSize(infoDim);
		infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));


		//CHANGED INFOPANEL LAYOUT TO BORDERLAYOUT FROM GRID
		infoPanel.setLayout(new GridLayout(1,4));  
		infoLabel = new JLabel(); 
		infoLabel.setText("<html><pre><i>Add to make customers/waiters</i></pre></html>");
		infoPanel.add(infoLabel);
		infoPanel.add(stateCB);


		interactionPanel.add(infoPanel);

		setLayout(new GridLayout(1,2));
		Dimension rest = new Dimension((int)(WINDOWX*.5) ,WINDOWY);
		animationPanel.setMinimumSize(rest);
		animationPanel.setPreferredSize(rest);
		animationPanel.setMaximumSize(rest);




		add(animationPanel);
		add(interactionPanel);




	}
	/**
	 * updateInfoPanel() takes the given customer (or, for v3, Host) object and
	 * changes the information panel to hold that person's info.
	 *
	 * @param person customer (or waiter) object
	 */
	public void updateInfoPanel(String type, Object person, Boolean check) {

		currentPerson = person;
		if(type.equals("Customers")){


			if (person instanceof CustomerAgent) {
				stateCB.setVisible(true);

				CustomerAgent customer = (CustomerAgent) person;
				if(check)
				{

					stateCB.setText("I'm hungry");
					stateCB.setEnabled(false);
					customer.getGui().setHungry();
				}
				else{

				stateCB.setText("Hungry?");}
				stateCB.setSelected(customer.getGui().isHungry());
				stateCB.setEnabled(!customer.getGui().isHungry());
				infoLabel.setText(
						"    Name: " + customer.getName() );
			}
			infoPanel.validate();
		}
		else if (type.equals("Waiters")){

			if (person instanceof WaiterAgent) {
				stateCB.setVisible(true);
				WaiterAgent waiter = (WaiterAgent) person;
				//if(check)
				//{
				if(waiter.getGui().msg.equals("None")){
					stateCB.setSelected(false);
					stateCB.setEnabled(true);
					stateCB.setText("Break?");
					infoLabel.setText(
							"<html><pre>     Name: " + waiter.getName() + " </pre></html>");
				}
				else{
					
					updateBreak(waiter.getGui().msg, waiter);
				}
					
			
			}
			infoPanel.validate();
		}
	}

	public void updateBreak(String message, WaiterAgent person){
		stateCB.setVisible(true);
		//WaiterAgent waiter =  person;
		if(message.equals("Pending") ){
			stateCB.setVisible(false);
			infoLabel.setText("<html><pre>     Name: " + person.getName() + "       pending break request</pre></html>");
		}
		else if(message.equals("Rejected")){
			infoLabel.setText("<html><pre>     Name: " + person.getName() + "       rejected break request</pre></html>");
			stateCB.setVisible(true);
			stateCB.setEnabled(true);
			stateCB.setSelected(false);
			stateCB.setText("Request break again?");
		}
		else if(message.equals("Granted")){
			infoLabel.setText(
					"<html><pre>     Name: " + person.getName() + " granted </pre></html>");
			stateCB.setVisible(true);
			stateCB.setEnabled(true);
			stateCB.setSelected(false);
			stateCB.setText("Return");
			
		}
		
	}

	/**
	 * Action listener method that reacts to the checkbox being clicked;
	 * If it's the customer's checkbox, it will make him hungry
	 * For v3, it will propose a break for the waiter.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == stateCB) {
			if (currentPerson instanceof CustomerAgent) {
				CustomerAgent c = (CustomerAgent) currentPerson;
				c.getGui().setHungry();

				stateCB.setEnabled(false);}
			else if(currentPerson instanceof WaiterAgent) {
				WaiterAgent w = (WaiterAgent) currentPerson;
				if(stateCB.getText().equals("Return")){
					w.getGui().returnWork();
					stateCB.setEnabled(false);
					stateCB.setText("Returning");
				}
				else{
				w.getGui().setBreak();
				stateCB.setEnabled(false);
				stateCB.setText("Request break");}
			}
		}
	}
	/**
	 * Message sent from a customer gui to enable that customer's
	 * "I'm hungry" checkbox.
	 *
	 * @param c reference to the customer
	 */
	public void setCustomerEnabled(CustomerAgent c) {
		if (currentPerson instanceof CustomerAgent) {
			CustomerAgent cust = (CustomerAgent) currentPerson;
			if (c.equals(cust)) {
				stateCB.setEnabled(true);
				stateCB.setSelected(false);
			}
		}
	}
	/**
	 * Main routine to get gui started
	 */
	public static void main(String[] args) {
		RestaurantGui gui = new RestaurantGui();
		gui.setTitle("csci201 Restaurant");
		gui.setVisible(true);
		gui.setResizable(false);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
