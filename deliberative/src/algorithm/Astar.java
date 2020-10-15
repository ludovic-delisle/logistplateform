package algorithm;

import logist.plan.Plan;
import logist.topology.Topology;
import model.State;

import java.util.*;

public class Astar {
	
	public static Plan run(final State startState) {
		
		Plan plan1 = new Plan(startState.getCurrentCity());
		
	
		int nSteps=0;
		State state = startState;
		PriorityQueue<State> statesQueue = new PriorityQueue<State>();
		
		while (!state.isFinalState() && !statesQueue.isEmpty()) {
	        nSteps += 1;
	        state = statesQueue.poll();
	        //How to get a specific element from a PriorityQueue?? Or change Collection type?? HashMap
	        if (!statesQueue.contains(state) || (state.getCurrentCost() < statesQueue.get(state).getCurrentCost())) {
	            statesQueue.add(state);
	            statesQueue.addAll(state.getNextStates());
	        }
		}
		//Implement conversion state to Plan.
		return state.toPlan();
	}

}
