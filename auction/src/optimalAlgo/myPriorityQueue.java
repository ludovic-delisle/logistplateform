package optimalAlgo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import optimalAlgo.State;


public class myPriorityQueue extends PriorityQueue<State> {
	
	private Map<State, State> visited_states;
    private Queue<State> states_queue;
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * constructor for the A* algorithm where we need a comparator
	 * @param comp that's the comparator for the priorityQueue
	 */
	public myPriorityQueue(Comparator<State> comp) {
		this.states_queue=new PriorityQueue<State>(comp);
		this.visited_states = new HashMap<State,State>();
    }  
	/**
	 * Constructor for BFS where we don't need a comparator
	 * @param startingState is a queue that contains just the initial state
	 */
	public myPriorityQueue(Queue<State> startingState) {
		this.states_queue=startingState;
		this.visited_states = new HashMap<State, State>();
    }  
	/**
	 * if a state has already been "visited" it should be in visited_states
	 * If the state that we are visiting has already been visited and that it has a higher cost than the same one that has been already visited,
	 * we don't want to add it to the visited_states map
	 * @param state is the state that may or may not be added to the map of visited states
	 */
	public void visit_state(State state) {
		if(!visited_states.containsKey(state) || state.getCurrentCost()< visited_states.get(state).getCurrentCost()) {
			visited_states.put(state,  state);
		}
	}
	
	/**
	 * @param state 
	 * @return if the input state has already been visited
	 */
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