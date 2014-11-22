package com.blackrook.engine;

import com.blackrook.commons.ResettableIterator;
import com.blackrook.commons.linkedlist.Stack;
import com.blackrook.engine.roles.EngineState;
import com.blackrook.engine.roles.EngineUpdatable;

/**
 * The state manager class for maintaining engine state.
 * @author Matthew Tropiano
 */
public class EngineStateManager implements EngineUpdatable
{
	/** Current game state. */
	private Stack<EngineState> currentState;
	/** Stack iterator. */
	private ResettableIterator<EngineState> currentStateIterator;

	EngineStateManager()
	{
		currentState = new Stack<EngineState>();
		currentStateIterator = currentState.iterator();
	}
	
	/**
	 * Returns the current game state.
	 * WARNING: Can be null.
	 */
	public EngineState getCurrentState()
	{
		return currentState.peek();
	}

	/**
	 * Changes the current state by emptying the state 
	 * stack and pushing new ones onto the stack by name.
	 * Calls {@link EngineState#exit()} on each state popped and {@link EngineState#enter()} on each state pushed. 
	 * @param states the states to push in the specified order.
	 */
	public void stateChange(EngineState ... states)
	{
		while (!currentState.isEmpty()) 
			statePop();
		statePush(states);
	}

	/**
	 * Pushes new states onto the stack.
	 * Calls {@link EngineState#enter()} on each state pushed. 
	 * @param states the states to push in the specified order.
	 * onto the stack, false if at least one was not.
	 */
	public void statePush(EngineState ... states)
	{
		for (EngineState s : states)
		{
			s.enter();
			currentState.push(s);
		}
	}

	/**
	 * Convenience method for <code>popState(1)</code>.
	 */
	public void statePop()
	{
		statePop(1);
	}

	/**
	 * Pops a bunch of game states off of the state stack.
	 * Calls {@link EngineState#exit()} on each state popped.
	 * @param stateCount the amount of states to pop.
	 */
	public void statePop(int stateCount)
	{
		while (stateCount > 0)
		{
			if (currentState.peek() != null)
			{
				currentState.peek().exit();
				currentState.pop();
			}
			stateCount--;
		}
	}

	@Override
	public boolean isUpdatable()
	{
		return true;
	}

	@Override
	public void update(long tick, long currentNanos)
	{
		currentStateIterator.reset();
		while (currentStateIterator.hasNext())
			currentStateIterator.next().update(tick, currentNanos);
	}
	
}
