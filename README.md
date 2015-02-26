# SimpleDB

About SimpleLRUCache
========
A simple version of Reddis-like in-memory database in Java

API Reference
========
- `SET <VAR> <VALUE>`
   * Stores the variable to the given value
	 * Runs in O(1) time
	 * 
	 * Overrides the value with the new value, if the variable has been 
	 * set in the same transaction block
      
  
- `UNSET <VAR>`
  * Removes the value mapped to the name
	* Runs in O(1) time
	
- `GET <VAR>`
  * Returns the value associated to the variable
	* Runs in O(1) time
	* 
	* If no value has been set or the variable has been previously unset,
	* then returns "NULL"
	
- `NUMEQUALTO <VALUE>`
  * Return the number of variables with the same value in the database
	* Runs in O(1) time

 Transactions commands:
- `BEGIN`
  * Creates a new transaction block
  
- `COMMIT`
  * Closes all the open transaction blocks and commits all the new 
	* changes. If no transaction block has been created, it returns 
	* "NO TRANSACTION", or if there has been no operations in a block
	* has been made, it returns "NOTHING TO COMMIT" and closes the block

- `ROLLBACK`
  * Ignores all the commands in the most recent transaction block.
	* Similar to a "revert" to an older version in databases
	* 
	* If no block has been created, it returns "NO TRANSACTION"
	* or if no operations/commands has been made in the block,
	* it returns "NOTHING TO ROLLBACK TO" and closes the transaction
