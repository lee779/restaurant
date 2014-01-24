package restaurant.gui;

import restaurant.CustomerAgent;
import restaurant.HostAgent;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Subpanel of restaurantPanel.
 * This holds the scroll panes for the customers and, later, for waiters
 */
public class ListPanel extends JPanel implements ActionListener {

	public JScrollPane pane =
			new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private JPanel view = new JPanel();

	public JScrollPane waiterPane =
			new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private JPanel waiterView = new JPanel();



	private List<JButton> list = new ArrayList<JButton>();
	private List<JButton> wList = new ArrayList<JButton>();
	private JButton addWaiterB = new JButton("Add");
	private JButton addPersonB = new JButton("Add");
	

	JPanel addFeaturePanel = new JPanel();
	JTextField waiterName = new JTextField();

	JTextField name = new JTextField();



	//pop up panel variables
	private static final int pRow = 2, pCol =1, pSpace = 5 ; //panel row, column, spacing
	private int textSize = 20;
	//button sizing
	private int bWidth = 20;
	private int bHeight = 3;



	private RestaurantPanel restPanel;
	private String type;
	private JCheckBox hungryBox = new JCheckBox();
	private JCheckBox breakBox = new JCheckBox();
	private JCheckBox thiefBox = new JCheckBox();
	
	private JTextField cashF = new JTextField();
	private JButton cashB = new JButton("Add");
	


	/**
	 * Constructor for ListPanel.  Sets up all the gui
	 *
	 * @param rp   reference to the restaurant panel
	 * @param type indicates if this is for customers or waiters
	 */
	public ListPanel(RestaurantPanel rp, String type) {
		restPanel = rp;

		this.type = type;

		setLayout(new GridLayout(6,1));
		//add(new JLabel("<html><pre> <u>" + type + "</u><br></pre></html>"));

		addPersonB.addActionListener(this);
		addWaiterB.addActionListener(this);

		add(new JLabel("Waiter"));
		waiterView.setLayout(new BoxLayout((Container) waiterView, BoxLayout.PAGE_AXIS));
		waiterPane.setViewportView(waiterView);
		add(waiterPane);

		add(new JLabel("Customer"));
		view.setLayout(new BoxLayout((Container) view, BoxLayout.Y_AXIS));
		pane.setViewportView(view);
		add(pane);
		
		popUpCreate();
		add(addFeaturePanel);
	}

	/**
	 * Method from the ActionListener interface.
	 * Handles the event of the add button being pressed
	 */
	public void actionPerformed(ActionEvent e) {
		
		for(int j = 0; j< list.size(); j++){
			if(list.get(j) == e.getSource()){
				restPanel.showInfo("Customers", list.get(j).getName(), hungryBox.isSelected());//puts hungry button on pane
			}
		}
		
		for(int t = 0; t< wList.size(); t++){
			if(wList.get(t) == e.getSource()){
				
				restPanel.showInfo("Waiters", wList.get(t).getName(), breakBox.isSelected());//puts hungry button on pane
			}
		}
		
		
		
		if (e.getSource() == addPersonB) {
			if(name.getText().length() > 0 && cashF.getText().length() > 0)
			{
				addCustomer(name.getText(), cashF.getText(), thiefBox.isSelected());
				hungryBox.setSelected(false);
				thiefBox.setSelected(false);
			}
		}

		else if(e.getSource() == addWaiterB)
		{
			//name and cash amount needs to be entered
			if(waiterName.getText().length()>0)
			{
				addWaiter(waiterName.getText());
			}

		}
		

		else {
		
			for (JButton temp:list){
				if (e.getSource() == temp)
					restPanel.showInfo(type, temp.getText(),false);
			}
		} }
	public void popUpCreate(){
		hungryBox.setSelected(false);
		cashB.addActionListener(this);
		
		thiefBox.setSelected(false);
		thiefBox.setVisible(true);
		thiefBox.addActionListener(this);
		addFeaturePanel.setLayout(new GridLayout(4,4,pSpace,pSpace));

		hungryBox.setVisible(true);
		hungryBox.addActionListener(this);
		

		
		addFeaturePanel.add(new JLabel ("Waiter name: "));
		addFeaturePanel.add(waiterName);
		addFeaturePanel.add(addWaiterB);
		addFeaturePanel.add(new JLabel(""));
		
		addFeaturePanel.add(new JLabel ("Customer name: "));
		addFeaturePanel.add(name);
		addFeaturePanel.add(new JLabel(""));
		addFeaturePanel.add(new JLabel(""));

		addFeaturePanel.add(new JLabel("Hungry?"));
		addFeaturePanel.add(hungryBox);
		addFeaturePanel.add(new JLabel("Thief?"));
		addFeaturePanel.add(thiefBox);

		
		addFeaturePanel.add(new JLabel("Cash Amount? "));
		addFeaturePanel.add(cashF);
		addFeaturePanel.add(addPersonB);

		
		addFeaturePanel.setVisible(true);





	}

	/**
	 * If the add button is pressed, this function creates
	 * a spot for it in the scroll pane, and tells the restaurant panel
	 * to add a new person.
	 *
	 * @param name name of new person
	 */
	public void addWaiter(String name){
		if(name!=null){
			
			JButton waiterButton = new JButton(name);
			waiterButton.setName(name);
			waiterButton.setBackground(Color.magenta);
			Dimension paneSize = pane.getSize();
			Dimension buttonSize = new Dimension(paneSize.width - bWidth,
					(int) (paneSize.height / (bHeight)));
			waiterButton.setPreferredSize(buttonSize);
			waiterButton.setMinimumSize(buttonSize);
			waiterButton.setMaximumSize(buttonSize);
			waiterButton.addActionListener(this);

		
			breakBox.setSelected(false);
			breakBox.setVisible(true);
			breakBox.addActionListener(this);
			
			waiterView.add(waiterButton);
			wList.add(waiterButton);
			
			
			restPanel.addPerson( name);//puts customer on list
			restPanel.showInfo("Waiters",name, breakBox.isSelected());

			validate();
		}
	}

	public void addCustomer(String name, String cash, Boolean thief) {
		if (name != null) {
			JButton Customerbutton = new JButton(name);
			Customerbutton.setName(name);
			Customerbutton.setBackground(Color.GREEN);
			
			Dimension paneSize = pane.getSize();
			Dimension buttonSize = new Dimension(paneSize.width - bWidth,
					(int) (paneSize.height / bHeight));
			Customerbutton.setPreferredSize(buttonSize);
			Customerbutton.setMinimumSize(buttonSize);
			Customerbutton.setMaximumSize(buttonSize);
			Customerbutton.addActionListener(this);

			view.add(Customerbutton);
			list.add(Customerbutton);
			restPanel.addCust(name, cash, thief);//puts customer on list
			restPanel.showInfo(type, name, hungryBox.isSelected());//puts hungry button on panel
			validate();
		}
	}
}
