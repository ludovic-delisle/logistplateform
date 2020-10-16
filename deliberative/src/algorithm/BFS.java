package algorithm;

import logist.plan.Plan;
import model.State;

import java.util.LinkedList;
import java.util.Queue;
import algorithm.myPriorityQueue;

public class BFS {
	
	public static Plan find_best_plan(State startingState) {
		Queue<State> start_queue = new LinkedList<>();
		start_queue.add(startingState);
		myPriorityQueue queue =new myPriorityQueue(start_queue);
		queue.add_all_possible_states_to_queue(startingState.getNextStates());
		
		State bestState=null;
		
		 
	    if (startingState.isFinalState()) {
	    	bestState = startingState;
	    }
	    
	    while (!queue.isEmpty()) {
	    	
	    	State best_state_candidat = queue.poll();
	    	
	    	if (!queue.isVisited(best_state_candidat)) {
	    		queue.visit_state(best_state_candidat);
                queue.addAll(best_state_candidat.getNextStates());
            }
            if (best_state_candidat.isFinalState() && (bestState == null || best_state_candidat.getCurrentCost() < bestState.getCurrentCost())) {
                bestState = best_state_candidat;
            }
	    	
	    }
	
	return bestState.toPlan();
	}
}
