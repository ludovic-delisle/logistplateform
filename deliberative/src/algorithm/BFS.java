package algorithm;

import logist.plan.Plan;
import logist.topology.Topology.City;
import model.State;

import java.util.LinkedList;
import java.util.Queue;

public class BFS {
	
	public static Plan find_best_plan(State startingState) {
		
		//down below is the BFS Algorithm
		Queue<State> start_queue = new LinkedList<State>();
		start_queue.add(startingState);
		myPriorityQueue queue = new myPriorityQueue(start_queue);
		City startCity = startingState.getCurrentCity();
		State bestState=null;		
		 
	    if (startingState.isFinalState()) {
	    	bestState = startingState;
	    }
	    //by using a queue we visit the states layer by layer
	    //as it is not a priorityQueue, it is first entered first out
	    while (!queue.isEmpty()) {
	    	State best_state_candidat = queue.poll();
	    	if (!queue.isVisited(best_state_candidat)) {
	    		queue.visit_state(best_state_candidat);
                queue.addAll(best_state_candidat.getPossibleStates());
            }
	    	//whenever we come upon a final state, we check if it is better than the current best final state 
            if (best_state_candidat.isFinalState() && (bestState == null || best_state_candidat.getCurrentCost() < bestState.getCurrentCost())) {
                bestState = best_state_candidat;
            }
	    	
	    }
	
	return bestState.toPlan(startCity);
	}
}
