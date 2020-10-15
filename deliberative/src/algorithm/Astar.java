package algorithm;

import logist.plan.Plan;
import model.State;

import java.util.*;

public class Astar {
	
	public static Plan run(final State startState) {
				
	
		int nSteps=0;
		State state = startState;
		//our priority queue needs to be sorted according to the total cost to reach each state
		class StateComparator implements Comparator<State>{ 
            @Override
			public int compare(State s1, State s2) {
				return (int) s1.getCurrentCost() - (int) s2.getCurrentCost();
               
            }
        } 
		
		Comparator<State> comparator = new StateComparator();
		myPriorityQueue statesQueue = new myPriorityQueue(comparator);
		
		while (!state.isFinalState() && !statesQueue.isEmpty()) {
	        nSteps += 1;
	        state = statesQueue.poll();
	        //How to get a specific element from a PriorityQueue?? Or change Collection type?? HashMap
	        if (!statesQueue.contains(state) || statesQueue.compare(state)){
	        	//since we use a priority queue, the sort steps is already done implicitly
	            statesQueue.add(state);
	            statesQueue.addAll(state.getNextStates());
	        }
		}
		
		System.out.println("A plan was found in" + nSteps);
		//Implement conversion state to Plan.
		return state.toPlan();
		
	}

}
