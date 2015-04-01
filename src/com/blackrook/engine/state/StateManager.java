package com.blackrook.engine.state;

import com.blackrook.commons.Sizable;
import com.blackrook.engine.roles.EngineInputListener;
import com.blackrook.engine.roles.EngineUpdateListener;

/**
 * The state manager class for maintaining exclusive, updatable states.
 * @author Matthew Tropiano
 */
public class StateManager implements EngineUpdateListener, EngineInputListener, Sizable
{
	/** Current game state. */
	private State[] states;
	/** Size. */
	private int size;

	StateManager()
	{
		states = new State[4];
		size = 0;
	}
	
	/**
	 * Changes the current state by emptying the state 
	 * stack and pushing new ones onto the stack by name.
	 * Calls {@link State#exit()} on each state popped and {@link State#enter()} on each state pushed. 
	 * @param states the states to push in the specified order.
	 */
	public synchronized void change(State ... states)
	{
		while (!isEmpty()) 
			pop();
		push(states);
	}

	/**
	 * Pushes new states onto the stack.
	 * Calls {@link State#enter()} on each state pushed. 
	 * @param states the states to push in the specified order.
	 * onto the stack, false if at least one was not.
	 */
	public synchronized void push(State ... states)
	{
		for (State s : states)
		{
			if (size() == states.length)
			{
				State[] newarray = new State[states.length * 2];
				System.arraycopy(states, 0, newarray, 0, states.length);
				states = newarray;
			}
			s.enter();
			states[size++] = s;
		}
	}

	/**
	 * Pops a bunch of game states off of the state stack as returns it.
	 * Calls {@link State#exit()} on the state popped.
	 */
	public synchronized State pop()
	{
		if (isEmpty())
			return null;
		
		State out = states[--size];
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
	public synchronized boolean isUpdatable()
	{
		return true;
	}

	@Override
	public synchronized void update(long tick, long currentNanos)
	{
		for (int i = size - 1; i >= 0; i--)
			states[i].update(tick, currentNanos);
	}

	@Override
	public synchronized boolean onInputSet(int code, boolean set)
	{
		for (int i = size - 1; i >= 0; i--)
			if (states[i].onInputSet(code, set))
				return true;
		return false;
	}

	@Override
	public synchronized boolean onInputValue(int code, double value)
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
