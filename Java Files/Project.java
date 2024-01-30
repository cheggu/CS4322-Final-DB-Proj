// Ben Marksberry
// CS4322
// "Duluth Fine Bakery" Database Final Project


package project;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Project {
	
	String jdbcUrl = "jdbc:postgresql://localhost:63333/marks208";
	Connection conn;
	
	public Connection getDBConnection() throws SQLException{ 

		if(conn == null) {
			// Display a message to get the password from the user 
			JLabel label = new JLabel("Postgres Username: "); 
			JTextField jtf = new JTextField();
			JLabel label2 = new JLabel("Postgres Password:"); 
			JPasswordField jpf = new JPasswordField(); 
			JOptionPane.showConfirmDialog(null,
					new Object[]{label, jtf, label2, jpf}, 
					"Password:", JOptionPane.OK_CANCEL_OPTION);

			String password = String.valueOf(jpf.getPassword());
			conn = DriverManager.getConnection(jdbcUrl, jtf.getText(), password ); 
		}
		conn.setAutoCommit(true);
		return conn;
	}
	
	public String getCustomerIDFromEmail(String email) throws SQLException {
		// Connect to the database. 
		getDBConnection();
				
		// Create a query string to grab the id associated with an email
		String sqlSelectCustomerID =
				"SELECT id "
				+ "FROM customer "
				+ "WHERE email = ?";
				
		// Execute inside a try/catch to allow for graceful exceptions
		try(PreparedStatement SelectStmt =
			conn.prepareStatement(sqlSelectCustomerID);){
				
			SelectStmt.setString(1, email);
			ResultSet rs  = SelectStmt.executeQuery();
			String id = "";
			
			while(rs.next()){
		        //Retrieve id
				id = rs.getString("id");
			}
			return id;
				
		} catch(SQLException e) {
			//e.printStackTrace(); 
			System.out.println("Error! Please try again");
			return "No associated ID";
		} 
	}
	
	public String getCustomerNameFromID(String id) throws SQLException {
		// Connect to the database. 
		getDBConnection();
				
		// Create a query string to grab the name associated with the id
		String sqlSelectCustomerName =
				"SELECT first_name, last_name "
				+ "FROM customer "
				+ "WHERE id = ?";
				
		// Execute inside a try/catch to allow for graceful exceptions
		try(PreparedStatement SelectStmt =
			conn.prepareStatement(sqlSelectCustomerName);){
				
			SelectStmt.setString(1, id);
			ResultSet rs  = SelectStmt.executeQuery();
			String first_name = "";
			String last_name = "";
			
			while(rs.next()){
		        //Retrieve name
				first_name = rs.getString("first_name");
				last_name = rs.getString("last_name");
			}
			return (first_name + " " + last_name);
				
		} catch(SQLException e) {
			//e.printStackTrace(); 
			System.out.println("Error! Please try again");
			return "No associated user";
		} 
	}
	
	// Function to get an item's associated cost
	public double getItemCost(String item) {
		String sqlSelectCheckAvailableQuantities = "select cost from item where name = ?";
		
		try(PreparedStatement SelectStmt =
				conn.prepareStatement(sqlSelectCheckAvailableQuantities);){
		
			SelectStmt.setString(1, item);
			
			ResultSet rs  = SelectStmt.executeQuery();
			
			double cost = 0.0;
			while(rs.next()){
		        cost = rs.getDouble("cost");
			}
			
			return cost;
			
		} catch(SQLException e) {
			//e.printStackTrace(); 
			System.out.println("Error! Either the item doesn't exist or a typo occured.");
			return -1.0;
		} 
	}
	
	public static void main(String[] args) throws Exception{ 
		Project p = new Project();
		p.getDBConnection();
		
		// Scanner allows for user input, main user-focused section
		Scanner input = new Scanner(System.in);
		System.out.println("Welcome to the Duluth Fine Bakery order application!");
		System.out.println("FUNCTIONS\n1 --- Place order for existing client\n2 --- Check orders ready for pickup");
		int choice = 0;
		choice = input.nextInt();
		
		// Creating a cake order
		if (choice == 1) { 
			String email = "";
			String id = "";
			Date dueDate;
			String specialInstructions;
			boolean payAtPickup;
			Integer quantity;
			
			System.out.println("Email: ");
			email = input.next();
			
			System.out.println("Due date (YYYY-MM-DD): ");
			dueDate = Date.valueOf(input.next());
			
			input.nextLine();
			System.out.println("Special Instructions: ");
			specialInstructions = input.nextLine();
			
			System.out.println("Paying at pickup? (y/n): ");
			String tempbool = input.next();
			if (tempbool.contains("y")) {
				payAtPickup = true;
			}
			else {
				payAtPickup = false;
			}
			
			System.out.println("Quantity: ");
			quantity = input.nextInt();
			
			// Get cust_id from provided email address for easier lookup via index
			try {
				id = p.getCustomerIDFromEmail(email);
			} catch (SQLException e) {
				//e.printStackTrace();
				System.out.println("Error getting custid from mail! Please try again");
			}

			p.placeCakeOrder(id, dueDate, specialInstructions, payAtPickup, quantity);
		}
		// Querying all ready-for-pickup orders
		else if (choice == 2) { 
			p.selectAllReadyPickups();
		}
		else {
			System.out.println("Incorrect input! Please try again.");
		}
		
		input.close();
	}
	
	// This function gathers the names and phone numbers of all customers whose orders are ready for pickup
	public void selectAllReadyPickups() throws SQLException {
		getDBConnection();
		
		// Create a query string that will get all customer ids with ready orders,
		// then query name and phone number from that embedded query
		String sqlSelectReadyPickups = "select first_name, last_name, phone "
				+ "from customer "
				+ "where id in "
				+ "    (select customer "
				+ "    from \"order\" "
				+ "    where is_ready = true);";
				
		try(PreparedStatement SelectStmt =
				conn.prepareStatement(sqlSelectReadyPickups);){
			
			ResultSet rs  = SelectStmt.executeQuery();
			
			System.out.println("Orders ready for pickup...\n");
			
			while(rs.next()){
				// Get required and print info from result
		        String first_name = rs.getString("first_name");
		        String last_name = rs.getString("last_name");
		        String phone = rs.getString("phone");
		         
		        System.out.println(first_name + " " + last_name + " | " + phone);
			}
			
		} catch(SQLException e) {
			//e.printStackTrace(); 
			System.out.println("Error! Please check your connection and try again");
			return;
		} 
	}
	
	public void placeCakeOrder(String id, Date dueDate, String specialInstructions, boolean payingAtPickup, Integer quantity) throws SQLException {
		getDBConnection();
		
		int err = 0;
		
		// Query the available amounts all required ingredients
		String sqlSelectCheckAvailableQuantities =
				"SELECT amount_left "
				+ "FROM ingredient "
				+ "where amount_left > ? and (name='Oil' or name='Flour' or name='Eggs' or name='Sugar' or name='Milk' or name='Chocolate')";
		
		List<Integer> amounts = new ArrayList<Integer>();
		
		try(PreparedStatement SelectStmt =
				conn.prepareStatement(sqlSelectCheckAvailableQuantities);){
		
			SelectStmt.setInt(1, quantity);
			
			ResultSet rs  = SelectStmt.executeQuery();
			
			int count = 0;
			
			while(rs.next()){
				// Append each amount to a list for further analysis
		        String amount_left = rs.getString("amount_left");
		        amounts.add(Integer.parseInt(amount_left));
		        count++;	   
			}
			
			// If the count is not 6, one or more of the needed ingredients are empty and the order cannot be fulfilled
			if (count != 6) {
				System.out.println("Not enough ingredients to fulfil order!");
				throw new SQLException();
				
			}
		} catch(SQLException e) {
			//e.printStackTrace(); 
			System.out.println("Error! Please check your connection and try again");
			return;
		} 
		
		
		// Create a query string that will return the new order number, based off the previously largest order number.
		String newOrdernum = "";
		String sqlGetLargestOrderNumber = 
				"SELECT max(order_id) + 1"
				+ "from \"order\""; 
		
		try(PreparedStatement SelectStmt = 
				conn.prepareStatement(sqlGetLargestOrderNumber);) {
			
			ResultSet rs = SelectStmt.executeQuery();
			
			while(rs.next()) {
				newOrdernum = rs.getString(1);
			}
			
		} catch(SQLException e) {
			//e.printStackTrace(); 
			System.out.println("Error! Please check your connection and try again");
		} 
		
		
		@SuppressWarnings("unused")
		int updatedRows = 0;
		String curTime = java.time.Clock.systemUTC().instant().toString().substring(0,10);
		//String strDueDate = java.time.Clock.systemUTC().instant().plusSeconds(259200).toString().substring(0,10);
		Date datePlaced = Date.valueOf(curTime);
		//Date dueDate = Date.valueOf(strDueDate);
		
		// Create a new order using all the info we gathered and received from user input
		String sqlInsertOrder =
				"INSERT INTO \"order\" VALUES(?,?,?,?,?,?)";
		
		try(PreparedStatement orderInsertStmt =
				conn.prepareStatement(sqlInsertOrder);){

			conn.setAutoCommit(false);
			
			orderInsertStmt.setInt(1, Integer.parseInt(newOrdernum));
			orderInsertStmt.setDate(2, datePlaced);
			orderInsertStmt.setDate(3, dueDate);
			orderInsertStmt.setBoolean(4, false);
			orderInsertStmt.setString(5, specialInstructions);
			orderInsertStmt.setString(6, id);
			
			// Run the insert query
			updatedRows  = orderInsertStmt.executeUpdate();
			
			// Commit the transaction to make the changes permanent
			conn.commit();

		} catch(SQLException e) {
			conn.rollback();
			err = 3;
			//e.printStackTrace(); 
			System.out.println("Error! Please check your connection and try again");
			
		} finally {
			//System.out.println("Modified rows: " + updatedRows);
		}
		
		
		// Create a query string that will insert a new order_item entry with the order number, item purchased and quantity
		updatedRows = 0;
		String sqlInsertOrderItems =
				"INSERT INTO order_items VALUES(?,?,?)";
		
		try(PreparedStatement orderInsertStmt =
				conn.prepareStatement(sqlInsertOrderItems);){

			conn.setAutoCommit(false);
			
			orderInsertStmt.setInt(1, Integer.parseInt(newOrdernum));
			orderInsertStmt.setString(2, "Chocolate Cake");
			orderInsertStmt.setInt(3, quantity);
			
			// Run the insert query
			updatedRows  = orderInsertStmt.executeUpdate();
			
			// Commit the transaction to make the changes permanent
			conn.commit();

		} catch(SQLException e) {
			// Rollback the transaction
			conn.rollback();
			//e.printStackTrace(); 
			System.out.println("Error! Please check your connection and try again");
		} finally {
			//System.out.println("Modified rows: " + updatedRows);
		}
		
		// Create a query string that will insert a new order_payment with the ordernum, payment type and total cost. 
		// Basically an invoice list
		updatedRows = 0;
		String sqlInsertOrderPayment =
				"INSERT INTO order_payments VALUES(?,?,?)";
		
		try(PreparedStatement orderInsertStmt =
				conn.prepareStatement(sqlInsertOrderPayment);){

			conn.setAutoCommit(false);
			
			String payment;
			
			// If they are paying at pickup no need to assign a real payment type
		    if (payingAtPickup) {
		    	payment = "pickup";
		    }
		    // If we were going to fully implement this the user would specify a payment type,
		    // but for time's sake it's implemented as a 50-50 split between credit and paypal
		    else {
		    	Random randomNum = new Random();
			    int result = randomNum.nextInt(2);
		    	if (result==0) {
			    	payment = "credit";
			    }
			    else {
			    	payment = "paypal";
			    }
		    }
			
			orderInsertStmt.setInt(1, Integer.parseInt(newOrdernum));
			orderInsertStmt.setString(2, payment);
			orderInsertStmt.setDouble(3, (quantity.doubleValue() * 19.99));
			
			// Run the insert query
			updatedRows  = orderInsertStmt.executeUpdate();
			
			// Commit the transaction to make the changes permanent
			conn.commit();

		} catch(SQLException e) {
			// Rollback the transaction
			conn.rollback();
			//e.printStackTrace(); 
			System.out.println("Error! Please check your connection and try again");
		} finally {
			//System.out.println("Modified rows: " + updatedRows);
		}
		
		updatedRows = 0;	
		// Create a list of ingredients that cakes require to later iterate through to build queries
		List<String> ingredients = new ArrayList<String>();
		ingredients.add("Oil");
		ingredients.add("Flour");
		ingredients.add("Eggs");
		ingredients.add("Sugar");
		ingredients.add("Milk");
		ingredients.add("Chocolate");
		
		// Create a query string that updates ingredients amounts by a specified quantity.
		// In a more complex implementation quantities could actually be calculated,
		// but ingredient stock is an unknown in this situation so each recipe uses one quantity of each listed ingredient.
		String sqlUpdateIngredients = 
				"UPDATE ingredient"
				+ " SET amount_left = ?" 
				+ " WHERE name= ?";
		
		try(PreparedStatement orderInsertStmt =
				conn.prepareStatement(sqlUpdateIngredients);){

			conn.setAutoCommit(false);
			
			// For each ingredient, subtract a quantity and update the amount_left
			for (int i = 0; i < 6; i++) {
				Integer newStock = amounts.get(i) - quantity;
				orderInsertStmt.setInt(1, newStock);
				orderInsertStmt.setString(2, ingredients.get(i));

				updatedRows  = orderInsertStmt.executeUpdate();
			}
			
			// Commit the transaction to make the changes permanent
			conn.commit();

		} catch(SQLException e) {
			// Rollback the transaction
			conn.rollback();
			//e.printStackTrace(); 
			System.out.println("Error! Please check your connection and try again");
		} finally {
			//System.out.println("Modified rows: " + updatedRows);
		}
		
		// Create a list of chefIDs for later iteration
		List<String> chefIds = new ArrayList<String>();
		
		// Grab all chef ids
		String sqlSelectChefIds = 
				"SELECT id FROM chef";
		
		try(PreparedStatement SelectStmt =
				conn.prepareStatement(sqlSelectChefIds);){
			
			ResultSet rs  = SelectStmt.executeQuery();
			
			while(rs.next()){
		         String chefid  = rs.getString("id");
		         chefIds.add(chefid);  
			}

		} catch(SQLException e) {
			System.out.println("Error! Please check your connection and try again");
			//e.printStackTrace(); 
		} 
		
		// Again, in a more complicated simulation the user could
		// assign a chef to each order, but we will just leave it to chance.
		Random randomNum = new Random();
	    int result = randomNum.nextInt(chefIds.size());
	    String selectedChef = chefIds.get(result);
		
	    // Create a query string that inserts a new chef_baked_items entry with chefid, item and ordernum
	    // chef_baked_items is a list of all the items chefs baked for an order
		updatedRows = 0;
		String sqlInsertChefBakedItems =
				"INSERT INTO chef_baked_items VALUES(?,?,?)";
		
		try(PreparedStatement orderInsertStmt =
				conn.prepareStatement(sqlInsertChefBakedItems);){

			conn.setAutoCommit(false);
			
			orderInsertStmt.setString(1, selectedChef);
			orderInsertStmt.setString(2, "Chocolate Cake");
			orderInsertStmt.setInt(3, Integer.parseInt(newOrdernum));
			
			// Run the insert query
			updatedRows  = orderInsertStmt.executeUpdate();
			
			// Commit the transaction to make the changes permanent
			conn.commit();

		} catch(SQLException e) {
			// Rollback the transaction
			conn.rollback();
			System.out.println("Error! Please check your connection and try again");
			//e.printStackTrace(); 
		} finally {
			//System.out.println("Modified rows: " + updatedRows);
		}
		
		if (err == 3) {
			System.out.println("Invalid due date set! Make sure the due date is greater than today's date.");
			return;
		}
		
		// Print the final order invoice for further reference
		double cost = getItemCost("Chocolate Cake");
		String name = getCustomerNameFromID(id); 
		
		System.out.println("Order created! OrderID: " + newOrdernum);
		System.out.println("Chocolate Cake x" + quantity);
		System.out.println("Total: " + (quantity * cost));
		System.out.println("Billed to " + name);
	}	
	
}
