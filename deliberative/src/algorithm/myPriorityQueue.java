package algorithm;

import java.util.Comparator;
import java.util.PriorityQueue;
import model.State;


public class myPriorityQueue extends PriorityQueue<State> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public myPriorityQueue(Comparator<State> comparator) {
		new PriorityQueue<State>(10, comparator);
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

}