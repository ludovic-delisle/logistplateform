package algorithm;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import model.State;


public class myPriorityQueue extends PriorityQueue<State> {
	
	private Map<State, State> visited_states;
    private Queue<State> states_queue;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public myPriorityQueue(Comparator<State> comp) {
		this.states_queue=new PriorityQueue<State>(comp);
		this.visited_states = new HashMap<State,State>();
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
	
	@Override
	public boolean add(State state) {
	       return states_queue.add(state);
	}
	
	public Queue<State> get_all_states() {
		return states_queue;
	}

	
	public void add_no_check(State state) {
	        states_queue.add(state);
	}
	
	public boolean compare(State state) {
		return !visited_states.containsKey(state) || state.getCurrentCost() < visited_states.get(state).getCurrentCost(); 
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
		return states_queue.poll();
	}
	@Override
    public int size() {
        return states_queue.size();
    }
	public State get(State key) {
        return visited_states.get(key);
    }

}