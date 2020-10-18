package algorithm;

import logist.plan.Plan;
import model.State;
import template.DeliberativeTemplate.Heuristic;

import java.util.*;

public class Astar {

	public static Plan run(final State startState, Heuristic h) {

		//our priority queue needs to be sorted according to the total cost to reach each state
		class StateComparator implements Comparator<State>{ 
            
			public int compare(State s1, State s2) {
				switch(h) {
					case DISTANCE:
						return (int)Double.compare(s1.getCurrentCost(), s2.getCurrentCost());
					default:
						return (int)Double.compare(s1.getCurrentCost(), s2.getCurrentCost());
				}
            }
        } 
		
		StateComparator comparator = new StateComparator();		
		myPriorityQueue queue = new myPriorityQueue(new PriorityQueue<>(comparator));
		queue.visit_state(startState);
		queue.add(startState);
		queue.add_all_possible_states_to_queue(startState.getNextStates());
		System.out.println("A pl in" + queue.size());
			
        int nSteps = 0;
        State state = startState;
        while (!state.isFinalState() && !queue.isEmpty()) {
            nSteps += 1;
            state = queue.poll();

            if (!queue.isVisited(state) && queue.compare(state)) {
            	queue.visit_state(state);
            	queue.add(state);
            	queue.add_all_possible_states_to_queue(state.getNextStates());
            }
        }

        if (!state.isFinalState()) {
            throw new IllegalStateException("ASTAR did not find any final state");
        }
		
		System.out.println("A plan was found in" + nSteps);
		
		Plan p = state.toPlan();

		return p;
	}
		
}

