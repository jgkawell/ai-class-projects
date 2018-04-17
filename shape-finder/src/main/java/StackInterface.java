public interface StackInterface
{
	/** Task: Adds a new entry to the top of the stack.
	 *  @param newEntry  an object to be added to the stack */
	public void push(Object newEntry);

	/** Task: Removes and returns the top of the stack.
	 *  @return either the object at the top of the stack or null if
	 *          the stack was empty */
	public Object pop();

	/** Task: Retrieves the top of the stack.
	 *  @return either the object at the top of the stack or null if
	 *          the stack is empty */
	public Object peek();

	/** Task: Determines whether the stack is empty.
	 *  @return true if the stack is empty */
	public boolean isEmpty();

	/** Task: Removes all entries from the stack */
	public void clear();
} // end StackInterface