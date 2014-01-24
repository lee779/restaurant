package restaurant.test;

import restaurant.CashierAgent;
import restaurant.CashierAgent.billState;
import restaurant.CashierAgent.checkState;
import restaurant.test.mock.LoggedEvent;
import restaurant.test.mock.MockBank;
import restaurant.test.mock.MockCustomer;
import restaurant.test.mock.MockMarket;
import restaurant.test.mock.MockWaiter;

import junit.framework.*;

/**
 * 
 * This class is a JUnit test class to unit test the CashierAgent's basic interaction
 * with waiters, customers, and the host.
 * It is provided as an example to students in CS201 for their unit testing lab.
 *
 * @author Monroe Ekilah
 */
public class CashierTest extends TestCase
{
	//these are instantiated for each test separately via the setUp() method.
	CashierAgent cashier;
	MockWaiter waiter;
	MockCustomer customer;
	MockMarket market1;
	MockMarket market2;
	MockBank bank;

	/**
	 * This method is run before each test. You can use it to instantiate the class variables
	 * for your agent and mocks, etc.
	 */
	public void setUp() throws Exception{
		super.setUp();		
		cashier = new CashierAgent("cashier");		
		customer = new MockCustomer("mockcustomer");		
		waiter = new MockWaiter("mockwaiter");
		market1 = new MockMarket("mockmarket1");
		market2 = new MockMarket("mockmarket2");
		bank = new MockBank("mockbank");

	}	
	/**
	 * cashier test#1: Normative-condition: waiter asks for check and customer pays the full amount
	 */
	public void testOneNormalCustomerScenario()
	{
		//setUp() runs first before this test!

		customer.cashier = cashier;//You can do almost anything in a unit test.			

		///check preconditions
		assertEquals("Cashier should have 0 checks made. It doesn't." , cashier.BookKeeping.size(), 0);
		assertEquals("Cashier should have an empty event log before the Cashier's msgCreateCheck is called. Instead, the Cashier's event log reads: "
				+ cashier.log.toString(), 0, cashier.log.size());

		assertTrue("Cashier has hashTable for chicken to cost. It doesn't.", cashier.cost.containsKey("Chicken"));
		assertTrue("Cashier has hashTable for steak to cost. It doesn't.", cashier.cost.containsKey("Steak"));
		assertTrue("Cashier has hashTable for salad to cost. It doesn't.", cashier.cost.containsKey("Salad"));
		assertTrue("Cashier has hashTable for pizza to cost. It doesn't.", cashier.cost.containsKey("Pizza"));


		//step 1 of the test
		//waiter sends message to cashier to create a check
		cashier.msgCreateCheck("Steak", 1, waiter, customer);


		//check postconditions for step 1 and preconditions for step 2
		assertEquals("Cashier should have one event log. Instead it's empty.",  1, cashier.log.size());
		assertEquals("Cashier should have 1 check created in it. It doesn't.",  1, cashier.BookKeeping.size());
		assertEquals("check information: food is not correctly imputed.", cashier.BookKeeping.get(0).food, "Steak");
		assertEquals("check information: table is not correctly imputed.", cashier.BookKeeping.get(0).tableNum, 1);
		assertEquals("check information: waiter is not correctly imputed.", cashier.BookKeeping.get(0).myWaiter, waiter);
		assertEquals("check information: customer is not correctly imputed.", cashier.BookKeeping.get(0).myCust, customer);
		assertEquals("check information: check state should be new. Instead: " + cashier.BookKeeping.get(0).state, cashier.BookKeeping.get(0).state, checkState.New);
		assertEquals(
				"MockWaiter should have an empty event log since the Cashier's scheduler is not called yet. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		//calculateCheck is called in cashier
		assertFalse("Cashier shouldn't find customer in the pastCustDebt. But it did: " + cashier.pastCustDebt.get(customer),
				cashier.pastCustDebt.containsKey(customer));


		//step 2 cashier sends message to waiter

		//cashier scheduler returns true
		assertTrue("Cashier's scheduler should have returned true, but didn't.",
				cashier.pickAndExecuteAnAction());
		//cashier sends message to waiter, msgHereIsCheck
		assertEquals(
				"MockWaiter should have one event log. Instead, the MockWaiter is empty: ",
				1, waiter.log.size());
		assertTrue("Cashier should contain a check with the right customer in it. It doesn't.", 
				cashier.BookKeeping.get(0).myCust == customer);
		assertEquals(
				"MockCustomer should have one event log. Instead, the MockCustomer is empty: "
						+ customer.log.toString(), 1, customer.log.size());


		//step 3 customer sends cashier message
		customer.setThief(false); // customer is not thief and will pay
		assertTrue("Cashier should have logged \"Received hereIsMoney\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received hereIsMoney"));
		assertTrue("check state should equal to paid, but instead: " + cashier.BookKeeping.get(0).state, 
				cashier.BookKeeping.get(0).state == checkState.Paying);

		assertEquals("Cashier should have received 15.99 from customer. Instead, got $" + cashier.getStoreBank(), 15.99, cashier.getStoreBank());


		//step 4 scheduler called and cashier sends message to customer
		assertTrue("Cashier's scheduler should have returned true, but didn't.",
				cashier.pickAndExecuteAnAction());
		assertTrue("Cashier should have logged \"payment received\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("payment received"));
		assertTrue("Customer should have logged \"Received msgThankYou\" but didn't. His log reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received msgThankYou"));
		//check is removed
		assertTrue("Cashier's bookKeeping should be empty, but it's not.", cashier.BookKeeping.isEmpty());


		cashier.log.clear();

	}//end one normal customer scenario



	/**
	 * cashier test#2: NonNormative scenario: customer can't pay the bill. He's a thief. Goes away with a warning
	 */
	public void testTwoNormalMarketScenario(){
		customer.cashier = cashier;//You can do almost anything in a unit test.			
		customer.myCash = 10.0; //set customer cash to $10.0
		cashier.setStoreBank(0.0); //set store bank to $0.00
		customer.setThief(true); //set customer to be thief
		///check preconditions
		assertEquals("Cashier should have 0 checks made. It doesn't." , cashier.BookKeeping.size(), 0);
		assertEquals("Cashier should have an empty event log before the Cashier's msgCreateCheck is called. Instead, the Cashier's event log reads: "
				+ cashier.log.toString(), 0, cashier.log.size());

		assertTrue("Cashier has hashTable for chicken to cost. It doesn't.", cashier.cost.containsKey("Chicken"));
		assertTrue("Cashier has hashTable for steak to cost. It doesn't.", cashier.cost.containsKey("Steak"));
		assertTrue("Cashier has hashTable for salad to cost. It doesn't.", cashier.cost.containsKey("Salad"));
		assertTrue("Cashier has hashTable for pizza to cost. It doesn't.", cashier.cost.containsKey("Pizza"));
		assertEquals("Cashier has no money. It doesn't. Instead " + cashier.getStoreBank(), 0.0, cashier.getStoreBank());
		assertTrue("customer should be thief, but it's not", customer.isThief());
		assertTrue("customer should have $10.00, but instead " + customer.myCash, 10.0 == customer.myCash);

		//step 1 of the test
		//waiter sends message to cashier to create a check
		cashier.msgCreateCheck("Steak", 1, waiter, customer);


		//check postconditions for step 1 and preconditions for step 2
		assertEquals("Cashier should have one event log. Instead it's empty.",  1, cashier.log.size());
		assertEquals("Cashier should have 1 check created in it. It doesn't.",  1, cashier.BookKeeping.size());
		assertEquals("check information: food is not correctly imputed.", cashier.BookKeeping.get(0).food, "Steak");
		assertEquals("check information: cost is not correctly imputed.", 15.99, cashier.BookKeeping.get(0).money);
		assertEquals("check information: table is not correctly imputed.", cashier.BookKeeping.get(0).tableNum, 1);
		assertEquals("check information: waiter is not correctly imputed.", cashier.BookKeeping.get(0).myWaiter, waiter);
		assertEquals("check information: customer is not correctly imputed.", cashier.BookKeeping.get(0).myCust, customer);
		assertTrue("check information: check state should be new. Instead: " + cashier.BookKeeping.get(0).state, cashier.BookKeeping.get(0).state == checkState.New);
		assertEquals(
				"MockWaiter should have an empty event log since the Cashier's scheduler is not called yet. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		//calculateCheck is called in cashier
		assertFalse("Cashier shouldn't find customer in the pastCustDebt. But it did: " + cashier.pastCustDebt.get(customer),
				cashier.pastCustDebt.containsKey(customer));
		assertTrue("calculate check should be 15.99, but instead, " + cashier.BookKeeping.get(0).money,
				cashier.BookKeeping.get(0).money == 15.99);

		//step 2 cashier sends message to waiter

		//cashier scheduler returns true
		assertTrue("Cashier's scheduler should have returned true, but didn't.",
				cashier.pickAndExecuteAnAction());
		//cashier sends message to waiter, msgHereIsCheck
		assertEquals(
				"MockWaiter should have one event log. Instead, the MockWaiter is empty: ",
				1, waiter.log.size());
		assertTrue("Cashier should contain a check with the right customer in it. It doesn't.", 
				cashier.BookKeeping.get(0).myCust == customer);
		assertEquals(
				"MockCustomer should have one event log. Instead, the MockCustomer is empty: "
						+ customer.log.toString(), 1, customer.log.size());
		assertTrue("Customer should have logged \"Received msgHereIsCheck\" but didn't. His log reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received msgHereIsCheck"));


		//step 3 customer sends cashier message
		//cashier.msgCantPay(customer, customer.myCash);
		assertTrue("Cashier should have logged \"Received msgCantPay\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgCantPay"));
		assertEquals("Cashier should have received $10.00 from customer. Instead, got $" + (15.99-cashier.BookKeeping.get(0).money), 10.00, (cashier.getStoreBank()));
		assertTrue("check state should equal to cantPay, but instead: " + cashier.BookKeeping.get(0).state, 
				cashier.BookKeeping.get(0).state == checkState.cantPay);


		//step 4 scheduler called  cashier sends message to customer after adding him to pastCustDebt
		//assertFalse("Customer should not be in the pastCustDebt, but he does with a debt of" + cashier.pastCustDebt.get(customer), 
		//	cashier.pastCustDebt.containsKey(customer));

		assertTrue("Cashier's scheduler should have returned true, but didn't.",
				cashier.pickAndExecuteAnAction());
		//correct pastcustdebt was added
		assertEquals("Customer is now added since it cant pay, but it didn't", customer.myCash- 15.99, cashier.pastCustDebt.get(customer));
		//customer got message
		assertTrue("Customer should have logged \"Received msgWarning\" but didn't. His log reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received msgWarning"));
		assertTrue("Cashier's bookKeeping should be empty, but it's not.", cashier.BookKeeping.isEmpty());
		assertFalse("Cashier's scheduler should have returned false, but didn't.",
				cashier.pickAndExecuteAnAction());

	}


	/**
	 * cashier test#3: NonNormative scenario: customer can't pay the bill. He's a thief. This is second warning and must go clean dishes
	 */
	public void testThreeNormalMarketScenario(){
		customer.cashier = cashier;//You can do almost anything in a unit test.			
		customer.myCash = 8.0; //set customer cash to $10.0
		cashier.setStoreBank(0.0); //set store bank to $0.00
		customer.setThief(true); //set customer to be thief
		cashier.pastCustDebt.put(customer, -13.99); //added pastcust debt
		///check preconditions
		assertEquals("Cashier should have 0 checks made. It doesn't." , cashier.BookKeeping.size(), 0);
		assertEquals("Cashier should have an empty event log before the Cashier's msgCreateCheck is called. Instead, the Cashier's event log reads: "
				+ cashier.log.toString(), 0, cashier.log.size());

		assertTrue("Cashier has hashTable for chicken to cost. It doesn't.", cashier.cost.containsKey("Chicken"));
		assertEquals("chicken should cost $10.99, but instead costs " + cashier.cost.get("Chicken"), 10.99, cashier.cost.get("Chicken")); 
		assertEquals("Cashier has no money. It doesn't. Instead " + cashier.getStoreBank(), 0.0, cashier.getStoreBank());
		assertTrue("customer should be thief, but it's not", customer.isThief());
		assertTrue("customer should have $8.00, but instead " + customer.myCash, 8.0 == customer.myCash);

		assertTrue("customer should be already in pastCustDebt list, but it doesn't", cashier.pastCustDebt.containsKey(customer));


		//step 1 of the test
		//waiter sends message to cashier to create a check

		cashier.msgCreateCheck("Chicken", 2, waiter, customer);


		//check postconditions for step 1 and preconditions for step 2, creating check
		assertEquals("Cashier should have one event log. Instead it's empty.",  1, cashier.log.size());
		assertEquals("Cashier should have 1 check created in it. It doesn't.",  1, cashier.BookKeeping.size());
		assertEquals("check information: food is not correctly imputed.", cashier.BookKeeping.get(0).food, "Chicken");
		assertEquals("check information: table is not correctly imputed.", cashier.BookKeeping.get(0).tableNum, 2);
		assertEquals("check information: waiter is not correctly imputed.", cashier.BookKeeping.get(0).myWaiter, waiter);
		assertEquals("check information: customer is not correctly imputed.", cashier.BookKeeping.get(0).myCust, customer);
		assertTrue("check information: check state should be new. Instead: " + cashier.BookKeeping.get(0).state, cashier.BookKeeping.get(0).state == checkState.New);
		assertEquals("MockWaiter should have an empty event log since the Cashier's scheduler is not called yet. " +
				"Instead, the MockWaiter's event log reads: " + waiter.log.toString(), 0, waiter.log.size());

		//calculateCheck is called in cashier
		assertTrue("Cashier should find customer in the pastCustDebt. But it didn't: " + cashier.pastCustDebt.get(customer),
				cashier.pastCustDebt.containsKey(customer));
		assertEquals("the debt should be -$13.99, instead it's "+ cashier.pastCustDebt.get(customer), -13.99,  cashier.pastCustDebt.get(customer));
		assertEquals("the final check amount should be $24.98, " ,
				((cashier.cost.get("Chicken") + (cashier.pastCustDebt.get(customer)*-1))), (cashier.BookKeeping.get(0).money + -1*cashier.pastCustDebt.get(customer)));




		//step 2 cashier sends message to waiter
		//cashier sends message to waiter, msgHereIsCheck

		assertTrue("Cashier's scheduler should have returned true, but didn't.",
				cashier.pickAndExecuteAnAction());
		assertTrue("Sending message to the right waiter, but they don't match", cashier.BookKeeping.get(0).myWaiter.equals(waiter) );
		assertTrue("Waiter should have logged \"Received msgHereIsCheck\" but didn't. His log reads instead: " 
				+ waiter.log.getLastLoggedEvent().toString(), waiter.log.containsString("Received msgHereIsCheck"));

		//step3: waiter sends customer check
		assertEquals(
				"MockCustomer should have one event log. Instead, the MockCustomer is empty: "
						+ customer.log.toString(), 1, customer.log.size());
		assertTrue("Customer should have logged \"Received msgHereIsCheck\" but didn't. His log reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received msgHereIsCheck"));

		//step 4 customer sends cashier message
		//cashier.msgCantPay(customer, customer.myCash);
		assertTrue("Cashier should have logged \"Received msgCantPay\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgCantPay"));
		assertEquals("Cashier should have received $8.00 from customer. Instead, got $" + (24.99-cashier.BookKeeping.get(0).money), 8.00, (cashier.getStoreBank()));
		assertTrue("check state should equal to cantPay, but instead: " + cashier.BookKeeping.get(0).state, 
				cashier.BookKeeping.get(0).state == checkState.cantPay);




		//step5 scheduler called  cashier sends message to customer after checking his previous warning
		assertTrue("Customer should be in the pastCustDebtm receivig second warning ", 
				cashier.pastCustDebt.containsKey(customer));
		assertTrue("Cashier's scheduler should have returned true, but didn't.",
				cashier.pickAndExecuteAnAction());

		//customer in pastcustdebt is deleted, second warning
		assertFalse("Customer is now deleted because it's second warning, but instead" + cashier.pastCustDebt.get(customer),
				cashier.pastCustDebt.containsKey(customer));
		//customer got message
		assertTrue("Customer should have logged \"Received msgCleanDishes\" but didn't. His log reads instead: " 
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received msgCleanDishes"));
		assertTrue("Cashier's bookKeeping should be empty, but it's not.", cashier.BookKeeping.isEmpty());
		assertFalse("Cashier's scheduler should have returned false, but didn't.",
				cashier.pickAndExecuteAnAction());

	}

	/**
	 * cashier test#4: Normative-condition: one market asks for bill and cashier pays full amount
	 */
	public void testFourNormalMarketScenario(){
		market1.cashier = cashier;//You can do almost anything in a unit test.		
		cashier.setStoreBank(50.0); //set store bank to $50.00

		///check preconditions
		assertEquals("Cashier should have 0 bils to pay. It doesn't." , cashier.moneyToPay.size(), 0);
		assertEquals("Cashier should have an empty event log before the Cashier's msgCreateCheck is called. Instead, the Cashier's event log reads: "
				+ cashier.log.toString(), 0, cashier.log.size());
		assertEquals("Market should have an empty event log before receiving msg from cashier. Instead, the Market's event log reads: "
				+ market1.log.toString(), 0, market1.log.size());
		assertEquals("Cashier should have $50.00 in store nank, but instead has " + cashier.getStoreBank(), 50.0,cashier.getStoreBank()); 
		assertTrue("Market1 is communicating with the correct cashier. Instead, the Market1's cashier's name: "  
				+ market1.cashier.getName(), cashier.equals(market1.cashier));

		//step 1 of the test

		//cashier receives message from market with one bill\
		cashier.msgHereIsBill(34.99, "Steak",market1);

		//check postconditions for step 1 and preconditions for step 2, creating check
		assertTrue("Cashier should have logged \"Received msgHereIsBill\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgHereIsBill"));		assertEquals("Cashier should have 1 bill created in it. It doesn't.",  1, cashier.moneyToPay.size());
				assertEquals("Bill information: market is not correctly imputed.",  market1, cashier.moneyToPay.get(0).getMarket());
				assertEquals("Bill information: bill is not correctly imputed.", 34.99,cashier.moneyToPay.get(0).getAmt());
				assertEquals("Bill information: food is not correctly imputed.", "Steak", cashier.moneyToPay.get(0).getFood());
				assertEquals("MockMarket should have an empty event log since the Cashier's scheduler is not called yet. " +
						"Instead, the MockMarket's event log reads: " + market1.log.toString(), 0, market1.log.size());

				//payMarket is called in cashier to figure if it can pay
				assertTrue("Cashier should have enough money to pay the bill, but it doesn't", 
						cashier.getStoreBank() > cashier.moneyToPay.get(0).getAmt());
				assertEquals("the final store amount should be 15.01 " ,
						15.01, cashier.getStoreBank() - cashier.moneyToPay.get(0).getAmt(), 0.001);




				//step 2 cashier scheduler is called and sends message to market that it can pay 

				//cashier sends message to waiter, msgHereIsCheck
				assertTrue("Sending message to the right market, but they don't match", 
						cashier.moneyToPay.get(0).getMarket().equals(market1) );		
				//scheduler//
				assertTrue("Cashier's scheduler should have returned true, but didn't.",
						cashier.pickAndExecuteAnAction());

				//checking payment
				assertEquals("Cashier's storebank should have decreased to $15.01, but instead " + cashier.getStoreBank(), 
						15.01, cashier.getStoreBank(), 0.001);
				assertTrue("Market should have logged \"Received msgHereIsPayment\" but didn't. His log reads instead: " 
						+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received msgHereIsPayment"));
				assertFalse("Cashier should not be in the debt list in market1, but it does, " + market1.getPastDebt().contains(cashier),
						market1.getPastDebt().contains(cashier));


				//step 3: payment acknowledged, cashier receives thank you from market

				assertTrue("Cashier should have logged \"Received msgThankYou\" from market but didn't. His log reads instead: " 
						+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgThankYou"));
				assertTrue("Cashier doesn't have anything else to pay, moneyPay state should be done, instead" + 
						cashier.moneyToPay.get(0).state, cashier.moneyToPay.get(0).state == billState.Done);
				assertFalse("Cashier's scheduler should have returned false, but didn't.",
						cashier.pickAndExecuteAnAction());
	}

	/**
	 * cashier test#5: Normative-condition: two market asks for two bills and cashier pays full amount
	 */
	public void testFiveNormalMarketScenario(){
		market1.cashier = cashier; //two markets	
		market2.cashier = cashier;
		cashier.setStoreBank(70.0); //set store bank to $70.00

		///check preconditions
		assertEquals("Cashier should have 0 bils to pay. It doesn't." , cashier.moneyToPay.size(), 0);
		assertEquals("Cashier should have an empty event log before the Cashier's msgCreateCheck is called. Instead, the Cashier's event log reads: "
				+ cashier.log.toString(), 0, cashier.log.size());
		assertEquals("Market1 should have an empty event log before receiving msg from cashier. Instead, the Market1's event log reads: "
				+ market1.log.toString(), 0, market1.log.size());
		assertEquals("Market2 should have an empty event log before receiving msg from cashier. Instead, the Market2's event log reads: "
				+ market2.log.toString(), 0, market2.log.size());
		assertEquals("Cashier should have $70.00 in store bank, but instead has " + cashier.getStoreBank(), 70.0,cashier.getStoreBank()); 
		assertTrue("Market1 is communicating with the correct cashier. Instead, the Market1's cashier's name: "  
				+ market1.cashier.getName(), cashier.equals(market1.cashier));
		assertTrue("Market2 is communicating with the correct cashier. Instead, the Market2's cashier's name: "  
				+ market2.cashier.getName(), cashier.equals(market2.cashier));		

		//step 1 of the test, cashier receies two bills

		//cashier receives message from market1 with one bill
		cashier.msgHereIsBill(34.99, "Pizza", market1);
		//post condition
		assertTrue("Cashier should have logged \"Received msgHereIsBill\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgHereIsBill"));

		//cashier receives message from market2 with one bill
		cashier.msgHereIsBill(17.99, "Pizza", market2);
		//post condition
		assertTrue("Cashier should have logged \"Received msgHereIsBill\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgHereIsBill"));	

		//preconditions for step 2, creating check
		assertEquals("Cashier should have two event logs. " +
				"Instead, the Cashier event log has: " + cashier.log.size(), 2, cashier.log.size());
		assertEquals("Cashier should have 2 bills created in it. It doesn't.",  2, cashier.moneyToPay.size());

		assertEquals("Bill information: first market is not correctly imputed.",  market1, cashier.moneyToPay.get(0).getMarket());
		assertEquals("bill information: first bill is not correctly imputed.", 34.99, cashier.moneyToPay.get(0).getAmt());
		assertEquals("bill information: first food is not correctly imputed.", "Pizza", cashier.moneyToPay.get(0).getFood());
		
		assertEquals("Bill information: second market is not correctly imputed.",  market2, cashier.moneyToPay.get(1).getMarket());
		assertEquals("bill information: second bill is not correctly imputed.", 17.99, cashier.moneyToPay.get(1).getAmt());
		assertEquals("bill information: second food is not correctly imputed.", "Pizza", cashier.moneyToPay.get(1).getFood());


		assertEquals("MockMarket1 should have an empty event log since the Cashier's scheduler is not called yet. " +
				"Instead, the MockMarket1's event log reads: " + market1.log.toString(), 0, market1.log.size());
		assertEquals("MockMarket2 should have an empty event log since the Cashier's scheduler is not called yet. " +
				"Instead, the MockMarket2's event log reads: " + market2.log.toString(), 0, market2.log.size());







		//step 2 cashier scheduler is called and sends message to market1 that it can pay 

			//payMarket is called in cashier to figure if it can pay the first market
			assertTrue("Cashier should have enough money to pay the bill, but it doesn't", 
				cashier.getStoreBank() > cashier.moneyToPay.get(0).getAmt());
			assertEquals("the final store amount should be 35.01 " ,
				35.01, cashier.getStoreBank() - cashier.moneyToPay.get(0).getAmt(), 0.001);

			//cashier sends message to waiter, msgHereIsCheck
			assertTrue("Sending message to the right market, but they don't match", 
					cashier.moneyToPay.get(0).getMarket().equals(market1) );		
			//scheduler//
			assertTrue("Cashier's scheduler should have returned true, but didn't.",
				cashier.pickAndExecuteAnAction());

			//checking payment
			assertTrue("Cashier should have logged \"paying the full amount\" but didn't. His log reads instead: " 
					+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("paying the full amount"));
			assertEquals("Cashier's storebank should have decreased to $15.01, but instead " + cashier.getStoreBank(), 
				35.01, cashier.getStoreBank(), 0.001);
			assertTrue("Market should have logged \"Received msgHereIsPayment\" but didn't. His log reads instead: " 
				+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received msgHereIsPayment"));
			assertFalse("Cashier should not be in the debt list in market1, but it does, " + market1.getPastDebt().contains(cashier),
				market1.getPastDebt().contains(cashier));
	
			//payment acknowledged from market1 by cashier
			assertTrue("Cashier should have logged \"Received msgThankYou\" from market but didn't. His log reads instead: " 
					+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgThankYou"));
	
			assertEquals("Cashier's first payment should be in state done now, instead " +
					cashier.moneyToPay.get(0).state, billState.Done, cashier.moneyToPay.get(0).state);
			assertFalse("Cashier's second payment should be not done, but it is", cashier.moneyToPay.get(1).state == billState.Done) ;
				
		//step 3: market2 also gets paid
			//payMarket is called in cashier to figure if it can pay the second market
			assertEquals("Cashier should have 35.01, but it doesn't", 
					cashier.getStoreBank(), 35.01, cashier.getStoreBank() );
			assertTrue("Cashier should have enough money to pay the bill, but it doesn't", 
					cashier.getStoreBank() > cashier.moneyToPay.get(0).getAmt());
			assertEquals("the final store amount should be 17.02 " ,
					17.02, cashier.getStoreBank() - cashier.moneyToPay.get(1).getAmt(), 0.001);
			
			//cashier sends message to waiter, msgHereIsCheck
			assertTrue("Cashier should have logged \"paying the full amount\" but didn't. His log reads instead: " 
					+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("paying the full amount"));
			assertTrue("Sending payment to the right market, but they don't match", 
					cashier.moneyToPay.get(1).getMarket().equals(market2) );		
			//scheduler//
			assertTrue("Cashier's scheduler should have returned true, but didn't.",
					cashier.pickAndExecuteAnAction());
			//checking payment
			assertEquals("Cashier's storebank should have decreased to $17.02, but instead " + cashier.getStoreBank(), 
				17.02, cashier.getStoreBank(), 0.001);
			assertTrue("Market should have logged \"Received msgHereIsPayment\" but didn't. His log reads instead: " 
				+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received msgHereIsPayment"));
			assertFalse("Cashier should not be in the debt list in market1, but it does, " + market2.getPastDebt().contains(cashier),
				market2.getPastDebt().contains(cashier));

		//step 4: payment acknowledged, cashier receives thank you from market
			assertTrue("Cashier should have logged \"Received msgThankYou\" from market but didn't. His log reads instead: " 
					+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgThankYou"));
			assertEquals("Cashier should have four event logs. " +
					"Instead, the Cashier event log has: " + cashier.log.size(), 6, cashier.log.size());
			assertTrue("Cashier doesn't have anything else to pay, instead moneyToPay state isn't done for second payment",
					cashier.moneyToPay.get(0).state == billState.Done);
			assertFalse("Cashier's scheduler should have returned false, but didn't.",
					cashier.pickAndExecuteAnAction());

	}
	
	/**
	 * cashier test#6: nonNormative-condition: one market asks for bill, cashier can't pay, this is his second time
	 * appropriate measures are taken. Cashier is already in the pastRestDebt list and must get money from bank to pay
	 */
	public void testSixNormalMarketScenario(){
		market1.cashier = cashier; 
		cashier.setStoreBank(30.0); //set store bank to $30.00
		cashier.setBank(bank); //set bank connction
		bank.setAccount(cashier, 400.0); //set bank account
		market1.getPastDebt().put(cashier, 100.0); //already in pastRestDebt
		

		///check preconditions
		assertEquals("Cashier should have 0 bils to pay. It doesn't." , cashier.moneyToPay.size(), 0);
		assertEquals("Cashier should have an empty event log before the Cashier's msgCreateCheck is called. Instead, the Cashier's event log reads: "
				+ cashier.log.toString(), 0, cashier.log.size());
		assertEquals("Market1 should have an empty event log before receiving msg from cashier. Instead, the Market1's event log reads: "
				+ market1.log.toString(), 0, market1.log.size());
		assertEquals("Bank should have an empty event log before receiving msg from cashier. Instead, the Market1's event log reads: "
				+ bank.log.toString(), 0, bank.log.size());
		
		
		assertEquals("Cashier should have $30.00 in store bank, but instead has " + cashier.getStoreBank(), 30.0,cashier.getStoreBank()); 
		assertTrue("Market1 is communicating with the correct cashier. Instead, the Market1's cashier's name: "  
				+ market1.cashier.getName(), cashier.equals(market1.cashier));
		assertTrue("Cashier is communicating with the correct bank. Instead, the cashier's bank name: "  
				+ cashier.getBank().getName(), bank.equals(cashier.getBank()));		
		
		assertTrue("Cashier should have an account under Bank. It doesn't", bank.getBankAccount().containsKey(cashier));
		assertEquals("Cashier has $300.0 in his bank account, but instead has, " + bank.getBankAccount().get(cashier), 
				400.0, bank.getBankAccount().get(cashier) );
		assertTrue("Cashier is already in debt, but he's not. " , market1.getPastDebt().containsKey(cashier));
	

		
		//step 1
		//cashier receives message from market1 with one bill
		cashier.msgHereIsBill(188.99,"Steak", market1);
		
		//post condition
		assertTrue("Cashier should have logged \"Received msgHereIsBill\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgHereIsBill"));


		//preconditions for step 2, creating check
		assertEquals("Cashier should have one event log. " +
				"Instead, the Cashier event log has: " + cashier.log.size(), 1, cashier.log.size());
		assertEquals("Cashier should have 1 bill created in it. It doesn't.",  1, cashier.moneyToPay.size());
		assertEquals("Bill information:  market is not correctly imputed.",  market1, cashier.moneyToPay.get(0).getMarket());
		assertEquals("bill information:  bill is not correctly imputed.", 188.99, cashier.moneyToPay.get(0).getAmt());
		assertEquals("bill information:  food is not correctly imputed.", "Steak", cashier.moneyToPay.get(0).getFood());
	

		assertEquals("MockMarket1 should have an empty event log since the Cashier's scheduler is not called yet. " +
				"Instead, the MockMarket1's event log reads: " + market1.log.toString(), 0, market1.log.size());
		

		//step 2 cashier scheduler is called and sends message to market1 that it can't pay 

			//payMarket is called in cashier to figure if it can pay the first market
			assertFalse("Cashier shouldn't have enough money to pay the bill, but it does", 
				cashier.getStoreBank() > cashier.moneyToPay.get(0).getAmt());
			
			assertEquals("the final store amount should be 0.0 " ,
				0.0, cashier.getStoreBank() - cashier.getStoreBank(), 0.001);
			assertEquals("the cashier will have a debt of" ,
					-158.99, cashier.getStoreBank() - cashier.moneyToPay.get(0).getAmt(), 0.001);

			//cashier sends message to narket, msgCantPay
			assertTrue("Sending message to the right market, but they don't match", 
					cashier.moneyToPay.get(0).getMarket().equals(market1) );		
			//scheduler//
			assertTrue("Cashier's scheduler should have returned true, but didn't.",
				cashier.pickAndExecuteAnAction());

			//checking events after scheduler
			assertTrue("Cashier should have logged \"can't pay full amount\" but didn't. His log reads instead: " 
					+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("can't pay full amount"));
			assertEquals("Cashier's storebank should have decreased to 0.0, but instead " + cashier.getStoreBank(), 
				0.0, cashier.getStoreBank(), 0.001);
			assertTrue("Market should have logged \"Received msgCantPay\" but didn't. His log reads instead: " 
					+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received msgCantPay"));
			
			
			///cashier is told that he needs to pay
			//cashier.msgYouHaveToPay(158.99, market1);
			assertTrue("Cashier should have logged \"Received msgYouHaveToPay\" but didn't. His log reads instead: " 
					+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("msgYouHaveToPay"));
			
			//cashier goes to bank since he needs to pay
			assertTrue("Cashier's bill state should be pending, instead " + cashier.moneyToPay.get(0).state,
					cashier.moneyToPay.get(0).state.equals(billState.goToBank));
			//adds the debt money as well
			assertEquals("Cashier needs to ask $258.99 from bank insteed" + cashier.moneyToPay.get(0).getAmt(), 258.99,
					cashier.moneyToPay.get(0).getAmt(), 0.001);
			
			assertTrue("Cashier's scheduler should have returned true, but didn't.",
					cashier.pickAndExecuteAnAction());
			
			
			//bank received and gives money
			assertTrue("Bank should have gottern \"received msgNeedMoney\" but didn't. His log reads instead: " 
					+ bank.log.getLastLoggedEvent().toString(), bank.log.containsString("received msgNeedMoney"));
			//storebank is updated
			assertEquals("cashier money should be updated to: 358.99, but instead " + cashier.getStoreBank(), 358.99, cashier.getStoreBank());
			
			// bank account is updated to 
			assertEquals("Bank account should be updated to 41.01, but instead " + bank.getBankAccount().get(cashier),
					41.01, bank.getBankAccount().get(cashier), 0.001);
			assertTrue("Cashier should have logged \"Received bank money\" but didn't. His log reads instead: " 
					+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received bank money"));
			assertTrue("Cashier's bill state should be paying, instead " + cashier.moneyToPay.get(0).state,
					cashier.moneyToPay.get(0).state.equals(billState.paying));
			
			//scheduler 
			assertTrue("Cashier's scheduler should have returned true, but didn't.",
					cashier.pickAndExecuteAnAction());
			//cashier pays the market
			assertEquals("Cashier's bill decreases to $100.0, but instead, " + cashier.getStoreBank(), 358.99-258.99, cashier.getStoreBank());
			assertTrue("Cashier should have logged \"paying full amount\" but didn't. His log reads instead: " 
					+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("full amount"));
			assertTrue("Market should have logged \"Received msgHereIsPayment\" but didn't. His log reads instead: " 
					+ market1.log.getLastLoggedEvent().toString(), market1.log.containsString("Received msgHereIsPayment"));
			
			
		
		   //payment acknowledged, cashier receives thank you from market
			assertTrue("Cashier should have logged \"Received msgThankYou\" from market but didn't. His log reads instead: " 
					+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgThankYou"));
			assertTrue("Cashier doesn't have anything else to pay, moneyToPay should be done", 
					cashier.moneyToPay.get(0).state == billState.Done);
			assertFalse("Cashier should not be in debt anymore, but he still is " + market1.getPastDebt().get(cashier),
					market1.getPastDebt().containsKey(cashier));
			assertFalse("Cashier's scheduler should have returned false, but didn't.",
					cashier.pickAndExecuteAnAction());

	}



}
