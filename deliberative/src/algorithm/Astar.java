package algorithm;

import logist.plan.Plan;
import logist.task.Task;
import logist.topology.Topology.City;
import model.State;
import template.DeliberativeTemplate.Heuristic;

import java.util.*;

public class Astar {
		
	public static double calculateHeuristic(final State state) {
		//The heuristic function takes the first reachable final state and returns the distance to reach it
	       
	        double dist_estimation = 0.0;

	        for (Task task: state.getTasks()){
	        	dist_estimation += task.pickupCity.distanceTo(task.deliveryCity);
	        }
	        for (Task task: state.getCurrentTasks()){
	        	dist_estimation += state.getCurrentCity().distanceTo(task.deliveryCity);
	        }
	        return dist_estimation;
	   }
	
	public static double calculateHeuristicSimple(final State state) {
		//The heuristic function takes the first reachable final state and returns the distance to reach it
	        double quantile_dist = 70;
	        
	        return state.getCostKm()*quantile_dist*(state.getTasks().size()*2 + state.getCurrentTasks().size());
	   }

	
	public static Plan run(final State startState, Heuristic h) {
		
		//our priority queue needs to be sorted according to the total cost to reach each state
		class StateComparator implements Comparator<State>{ 
			@Override
			public int compare(State s1, State s2) {
				if(h == Heuristic.DISTANCE) {
				//we order our priority queue according to the total cost (currentCost + heuristic cost)
					return (int)Integer.compare((int)s1.getCurrentCost() + (int)calculateHeuristicSimple(s1), 
							(int)s2.getCurrentCost() + (int)calculateHeuristicSimple(s2));
				}else {
					return (int)Integer.compare((int)s1.getCurrentCost(), (int)s2.getCurrentCost());
				}
            }
			
			public boolean isSmaller(State s1, State s2) {
				//we order our priority queue according to the total cost (currentCost + heuristic cost)
				if(s1.getCurrentCost() < s2.getCurrentCost()) return true;
				else return false;
            }
        } 
	
		int nSteps = 0;
        State state = startState;
		StateComparator comparator = new StateComparator();		
		myPriorityQueue queue = new myPriorityQueue(comparator);
		City startCity = startState.getCurrentCity();
		
		if (startState.isFinalState()) {
	    	return startState.toPlan(startCity);
	    }
		
		queue.add(state);
		
        while (!state.isFinalState() && !queue.isEmpty()) {
            nSteps += 1;
            state = queue.poll();
            
            if (!queue.isVisited(state) || comparator.isSmaller(state, queue.get(state))) {
            	queue.visit_state(state);
            	queue.addAll(state.getPossibleStates());
            }
        }

        if (!state.isFinalState()) {
            throw new IllegalStateException("Error: No final state found");
        }
		
		System.out.println("Plan found after steps: " + nSteps );
		System.out.println("The plan is: " + state.toPlan(startCity).toString());
		return state.toPlan(startCity);
	}
		
}

