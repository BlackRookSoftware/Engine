/*******************************************************************************
 * Copyright (c) 2016 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package com.blackrook.engine.state;

import com.blackrook.commons.Sizable;
import com.blackrook.engine.roles.EngineInputHandler;
import com.blackrook.engine.roles.EngineUpdateHandler;

/**
 * The state manager class for maintaining exclusive, updatable states.
 * @author Matthew Tropiano
 */
public class StateManager<S extends State> implements EngineUpdateHandler, EngineInputHandler, Sizable
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
	 * Calls {@link State#exit()} on each state popped and {@link State#enter()} on each state pushed. 
	 * @param changeStates the states to push in the specified order.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void change(final S ... changeStates)
	{
		while (!isEmpty()) 
			pop();
		push(changeStates);
	}

	/**
	 * Pushes new states onto the stack.
	 * Calls {@link State#enter()} on each state pushed. 
	 * @param pushStates the states to push in the specified order.
	 * onto the stack, false if at least one was not.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void push(final S ... pushStates)
	{
		for (State s : pushStates)
		{
			if (size() == states.length)
			{
				State[] newarray = new State[states.length * 2];
				System.arraycopy(states, 0, newarray, 0, pushStates.length);
				states = newarray;
			}
			s.enter();
			states[size++] = s;
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
	public synchronized boolean onInputSet(String code, boolean set)
	{
		for (int i = size - 1; i >= 0; i--)
			if (states[i].onInputSet(code, set))
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

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
}
