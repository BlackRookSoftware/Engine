/*******************************************************************************
 * Copyright (c) 2016-2021 Black Rook Software
 * This program and the accompanying materials are made available under the 
 * terms of the GNU Lesser Public License v2.1 which accompanies this 
 * distribution, and is available at 
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.state;

import com.blackrook.engine.handler.EngineInputHandler;
import com.blackrook.engine.handler.EngineUpdateHandler;

/**
 * The state manager class for maintaining exclusive, updatable states.
 * @author Matthew Tropiano
 */
public class StateManager implements EngineUpdateHandler, EngineInputHandler
{
	/** Current game state. */
	private State[] states;
	/** Size. */
	private int size;

	public StateManager()
	{
		states = new State[4];
		size = 0;
	}
	
	/**
	 * Changes the current state by emptying the state 
	 * stack and pushing new ones onto the stack by name.
	 * Calls {@link State#exit()} on each state popped and {@link State#enter(StateConfig)} on each state pushed. 
	 * @param changeStates the states to push in the specified order.
	 */
	public synchronized void change(final StateEntry ... changeStates)
	{
		while (!isEmpty()) 
			pop();
		push(changeStates);
	}

	/**
	 * Pushes new states onto the stack.
	 * Calls {@link State#enter(StateConfig)} on each state pushed. 
	 * @param pushStates the states to push in the specified order.
	 * onto the stack, false if at least one was not.
	 */
	public synchronized void push(final StateEntry ... pushStates)
	{
		for (StateEntry entry : pushStates)
		{
			if (size() == states.length)
			{
				State[] newarray = new State[states.length * 2];
				System.arraycopy(states, 0, newarray, 0, pushStates.length);
				states = newarray;
			}
			entry.state.enter(entry.stateConfig);
			states[size++] = entry.state;
		}
	}

	/**
	 * Pops a bunch of game states off of the state stack as returns it.
	 * Calls {@link State#exit()} on the state popped.
	 * @return the state popped.
	 */
	public synchronized State pop()
	{
		if (isEmpty())
			return null;
		
		State out = states[--size];
		states[size] = null;
		out.exit();
		return out;
	}

	/**
	 * Pops a bunch of game states off of the state stack.
	 * Calls {@link State#exit()} on each state popped.
	 * @param stateCount the amount of states to pop.
	 */
	public synchronized void pop(int stateCount)
	{
		while (!isEmpty() && stateCount-- > 0)
		{
			pop();
		}
	}

	@Override
	public synchronized void update(long tick, long currentNanos)
	{
		for (int i = size - 1; i >= 0; i--)
			states[i].update(tick, currentNanos);
	}

	@Override
	public synchronized boolean onInputFlag(String code, boolean set)
	{
		for (int i = size - 1; i >= 0; i--)
			if (states[i].onInputFlag(code, set))
				return true;
		return false;
	}

	@Override
	public synchronized boolean onInputValue(String code, double value)
	{
		for (int i = size - 1; i >= 0; i--)
			if (states[i].onInputValue(code, value))
				return true;
		return false;
	}

	/**
	 * @return the amount of states in the stack.
	 */
	public int size()
	{
		return size;
	}

	/**
	 * @return true if there are no states on the stack.
	 */
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
	/**
	 * State entry.
	 */
	public static class StateEntry
	{
		private State state;
		private StateConfig stateConfig;
		
		/**
		 * Creates a new State Entry for pushing onto the state stack.
		 * @param state the state to push.
		 * @param stateConfig the state's configuration parameters.
		 */
		public StateEntry(State state, StateConfig stateConfig)
		{
			this.state = state;
			this.stateConfig = stateConfig;
		}
	}
	
}
