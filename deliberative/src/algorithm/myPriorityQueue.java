package algorithm;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.List;
import model.State;


public class myPriorityQueue extends PriorityQueue<State> {
	
	private Map<State, State> visited_states;
    private Queue<State> states_queue;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public myPriorityQueue(Queue<State> startingState) {
		this.states_queue=startingState;
		this.visited_states = new HashMap<>();
    }  
	
	public void visit_state(State state) {
		if(!visited_states.containsKey(state) || state.getCurrentCost()< visited_states.get(state).getCurrentCost()) {
			visited_states.put(state,  state);
		}
	}
	public Boolean isVisited(State state) {
		return visited_states.containsKey(state);
	}
	
	@Override
	public boolean add(State state) {
		if (!visited_states.containsKey(state) || state.getCurrentCost() < visited_states.get(state).getCurrentCost()) {
	           states_queue.add(state);
	           return true;
	       }
		return false;
	}
	
	public void add_all_possible_states_to_queue(List<State> possible_states) {
		for(State state : possible_states) {
			if (!visited_states.containsKey(state) || state.getCurrentCost() < visited_states.get(state).getCurrentCost()) {
	            states_queue.add(state);
	        }
		}
	}
	public double calculateHeuristic(State state) {
		return 0.0;
	}
	
	public boolean compare(State state) {
		double state_cost = state.getCurrentCost() + calculateHeuristic(state);
		if(!visited_states.containsKey(state)) return true;
		else {
			double queue_state_cost = visited_states.get(state).getCurrentCost() + calculateHeuristic(visited_states.get(state));
			if(state_cost < queue_state_cost) return true;
			else return false;
		}
	}
	
	@Override
    public boolean contains(Object o) {
        return states_queue.contains(o);
    }
	@Override
    public boolean isEmpty() {
        return states_queue.isEmpty();
    }
	@Override
	public State poll() {
	    State state = states_queue.poll();
		return state;
	}
	@Override
    public int size() {
        return states_queue.size();
    }

}