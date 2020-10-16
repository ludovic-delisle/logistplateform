package algorithm;

import java.util.Comparator;
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
	
	public myPriorityQueue(Comparator<State> comparator) {
		new PriorityQueue<State>(10, comparator);
    }        
	
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
	
	public void add_all_possible_states_to_queue(List<State> possible_states) {
		for(State state : possible_states) {
			if (!visited_states.containsKey(state) || state.getCurrentCost() < visited_states.get(state).getCurrentCost()) {
	            states_queue.add(state);
	        }
		}
	}

	public boolean compare(State state) {
		while(!this.isEmpty()) {
			model.State s = this.poll();
			if(s.getCurrentCity() == state.getCurrentCity() && s.getCurrentTasks() == state.getCurrentTasks()
					&& s.getCurrentCost() < state.getCurrentCost() ){
				return true;
			}
	    }
		return false;
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