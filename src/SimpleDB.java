import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * A Simple database with the following commands
 * SET <VAR> <VALUE>
 * UNSET <VAR>
 * GET <VAR>
 * NUMEQUALTO <VALUE>
 * 
 * Transactions commands:
 * BEGIN
 * COMMIT
 * ROLLBACK
 * 
 * @author Vibinash
 */
public class SimpleDB {
	
	private Map<String, List<String>> map;
	private List<String> transactions;
	private Map<String, Set<String>> valueCounts;
	private int beginTransaction;
	private int transactionCount;
	
	/**
	 * Constructs an empty database
	 * Uses a Hashmap of LinkedLists - similar to the concept of versions 
	 * in databases. Only uses memory when a new value has been set in the 
	 * same transaction block and releases the used memory when it's no longer 
	 * needed
	 */
	public SimpleDB() {
		map = new HashMap<String, List<String>>();
		transactions = new ArrayList<String>();
		valueCounts = new HashMap<String, Set<String>>();
		beginTransaction = 0;
		transactionCount = 0;
	}
	
	/**
	 * Stores the variable to the given value
	 * Runs in O(1) time
	 * 
	 * Overrides the value with the new value, if the variable has been 
	 * set in the same transaction block
	 * 
	 * @param name: Sets the variable name
	 * @param value: Sets the value mapped to the name
	 */
	public void set(String name, String value){
		if (beginTransaction > 0){
			transactionCount++;
			transactions.add(name);
		}
		LinkedList<String> l = (LinkedList<String>)map.get(name);
		if (l == null){
			l = new LinkedList<String>();
		} 
		String oldValue = null;
		if (!l.isEmpty()){
			oldValue = l.getFirst();
		}
		if (beginTransaction > 0 || l.size() ==0){
			l.addFirst(value);
		} else if (beginTransaction <= 0){
			l.set(0, value);
		}
		map.put(name, l);
		
		// update the values count for "NUMEQUALTO"
		updateValuesCount(oldValue, value, name);
	}
	
	/**
	 * Removes the value mapped to the name
	 * Runs in O(1) time
	 * 
	 * @param name: The variable to be deleted
	 */
	public void unSet(String name){
		if (beginTransaction > 0){
			transactionCount++;
			transactions.add(name);
		}
		LinkedList<String> l = (LinkedList<String>)map.get(name);
		String value = null;
		if (l != null){
			if (!l.isEmpty()){
				value = l.getFirst();
			}
			if (beginTransaction > 0 || l.size() ==0){
				l.addFirst("NULL");
			} else if (beginTransaction <= 0) {
				l.set(0, "NULL");
			}
			map.put(name, l);
		} else {
			System.out.println(name+" IS NOT ASSIGNED");
		}
		if (value != null) {
			updateValuesCount(value, "NULL", name);
		}
	}
	
	/**
	 * Returns the value associated to the variable
	 * Runs in O(1) time
	 * 
	 * If no value has been set or the variable has been previously unset,
	 * then returns "NULL"
	 * @param name: the variable name of the value to be returned.
	 */
	public void get(String name){
		List<String> l = map.get(name);
		if (l == null){
			System.out.println("NULL");
		} else {
			String result = (l.size() > 0) ? l.get(0) : "NULL";
			System.out.println(result);
		}
	}
	
	/**
	 * Return the number of variables with the same value in the database
	 * Runs in O(1) time
	 * 
	 * @param value: the value to be searched for
	 */
	public void numEqualTo(String value){
		Set<String> s = valueCounts.get(value);
		int result = (s == null) ? 0 : s.size();
		System.out.println(result);
	}
	
	/**
	 * Creates a new transaction block
	 */
	public void begin(){
		beginTransaction++;
		transactions.add("BEGIN");
	}
	
	/**
	 * Closes all the open transaction blocks and commits all the new 
	 * changes. If no transaction block has been created, it returns 
	 * "NO TRANSACTION", or if there has been no operations in a block
	 * has been made, it returns "NOTHING TO COMMIT" and closes the block
	 */
	public void commit(){
		if (transactionCount ==0){
			System.out.println("NO TRANSACTION");
		} else if (beginTransaction > 0 && transactionCount == 0){
			System.out.println("NOTHING TO COMMIT, CLOSING TRANSACTION");
			beginTransaction = 0;
			transactionCount =0;
		} else {
			// Iterate through all the transactions and cleanup by releasing
			// unused blocks
			for (int i=0; i< transactions.size(); i++){
				LinkedList<String> l = (LinkedList<String>)map.get(transactions.get(i));
				if (l != null){
					while(l.size() >1){
						l.removeLast();
					}
				}
			}
			beginTransaction = 0;
			transactionCount =0;
			transactions.clear();
		}
	}
	
	/**
	 * Ignores all the commands in the most recent transaction block.
	 * Similar to a "revert" to an older version in databases
	 * 
	 * If no block has been created, it returns "NO TRANSACTION"
	 * or if no operations/commands has been made in the block,
	 * it returns "NOTHING TO ROLLBACK TO" and closes the transaction
	 */
	public void rollback(){
		if (transactionCount ==0){
			System.out.println("NO TRANSACTION");
		} else if (beginTransaction > 0 && transactionCount == 0){
			System.out.println("NOTHING TO ROLLBACK TO, CLOSING TRANSACTION");
			beginTransaction--;
		} else {	
			String name = transactions.remove(transactions.size()-1);
			String value = "";
			String newValue = null;
			int deltaTransactions =0;
			while(!name.equals("BEGIN")) {
				LinkedList<String> l = (LinkedList<String>)map.get(name);
				value = l.removeFirst();
				deltaTransactions++;
				if(!l.isEmpty()){
					newValue = l.get(0);
				}
				map.put(name, l);
				
				Set<String> s = valueCounts.get(value);
				s.remove(name);
				valueCounts.put(value, s);
				
				if (newValue != null){
					s = valueCounts.get(newValue);
					s.add(name);
					valueCounts.put(newValue, s);
				}
				
				name = transactions.remove(transactions.size()-1);
			}
			beginTransaction--;
			transactionCount -= deltaTransactions;
		}
	}
	
	/**
	 * Private method to update the variables that have the same value
	 * Helper method for "NUMEQUALTO"
	 */
	private void updateValuesCount(String oldValue, String newValue, String name){
		// Remove oldValue from set
		updateValuesCountHelper(oldValue, name, false);
		// Add newValue to the set
		updateValuesCountHelper(newValue, name, true);
	}
	
	/**
	 * Private method to update the variables that have the same value
	 * Helper method for "NUMEQUALTO"
	 */
	private void updateValuesCountHelper(String value, String name, boolean toAdd){
		Set<String> s = valueCounts.get(value);
		if (s == null){
			s = new HashSet<String>();
		}
		if (toAdd) {
			s.add(name);
		} else{
			s.remove(name);
		}
		valueCounts.put(value, s);
	}
	
	/**
	 * 
	 * Interact with the database by running it in a shell and typing the 
	 * valid commands
	 */
	public static void main(String[] args) {
		
		SimpleDB db = new SimpleDB();
		Scanner scan = new Scanner(System.in);
		String line;
		
		while(scan.hasNextLine() && 
				!(line = scan.nextLine()).equals(Commands.END.name())) {
			String[] tokens = line.split(" ");
			try {
				Commands c = Commands.valueOf(tokens[0]);
				int len = tokens.length;
				switch (c) {
					case SET:
						if (validateCommand(len, 3))
							db.set(tokens[1], tokens[2]);
						break;
					case UNSET:
						if (validateCommand(len, 2))
							db.unSet(tokens[1]);
						break;
					case GET:
						if (validateCommand(len, 2))
							db.get(tokens[1]);
						break;
					case NUMEQUALTO:
						if (validateCommand(len, 2))
							db.numEqualTo(tokens[1]);	
						break;
					case BEGIN:
						if (validateCommand(len, 1))
							db.begin();
						break;
					case COMMIT:
						if (validateCommand(len, 1))
							db.commit();
						break;
					case ROLLBACK:
						if (validateCommand(len, 1))
							db.rollback();
						break;
					default:
						break;	
				}
			} catch(IllegalArgumentException e1){
				System.err.println("Invalid input: Command not processed");
			} 
		}
		scan.close();
	}
	
	/**
	 * Private method to validate the length of the inputs/commands
	 * @param len
	 * @param i
	 * @return
	 */
	private static boolean validateCommand(int len, int i) {
		if(len != i){
			System.err.println("Input input: Wrong number of arguments");
			return false;
		}
		return true;
	}

	/**
	 * The Valid Commands for this database
	 *
	 */
	private enum Commands {
		SET,
		GET,
		UNSET,
		NUMEQUALTO,
		BEGIN,
		COMMIT,
		ROLLBACK,
		END;
	}
}
