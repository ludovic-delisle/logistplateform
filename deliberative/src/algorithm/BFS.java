package algorithm;

import logist.plan.Plan;
import logist.topology.Topology.City;
import model.State;

import java.util.LinkedList;
import java.util.Queue;

public class BFS {
	
	public static Plan find_best_plan(State startingState) {
		Queue<State> start_queue = new LinkedList<>();
		start_queue.add(startingState);
		myPriorityQueue queue =new myPriorityQueue(start_queue);
		queue.visit_state(startingState);
		queue.add_all_possible_states_to_queue(startingState.getPossibleStates());
		City startCity = startingState.getCurrentCity();
		State bestState=null;		
		 
	    if (startingState.isFinalState()) {
	    	bestState = startingState;
	    }
	    
	    while (!queue.isEmpty()) {
	    	State best_state_candidat = queue.poll();
	    	if (!queue.isVisited(best_state_candidat)) {
	    		queue.visit_state(best_state_candidat);
                queue.add_all_possible_states_to_queue(best_state_candidat.getPossibleStates());
            }
            if (best_state_candidat.isFinalState() && (bestState == null || best_state_candidat.getCurrentCost() < bestState.getCurrentCost())) {
                bestState = best_state_candidat;
            }
	    	
	    }
	
	return bestState.toPlan(startCity);
	}
}
