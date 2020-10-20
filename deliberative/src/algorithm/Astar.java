package algorithm;

import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import model.State;
import template.DeliberativeTemplate.Heuristic;

import java.util.*;

public class Astar {
	
	public static double calculateHeuristic(final State state) {
		//The heuristic function takes the first reachable final state and returns the distance to reach it
		List<State> state_list = new ArrayList<State>();
		state_list.add(state);
		for(State st: state_list) {
			List<State> ss = new ArrayList<State>(st.getPossibleStates());
			
			for(State s: ss) {
				if(s.isFinalState()) {
					return state.toPlan(state.getCurrentCity()).totalDistance() * state.getCostKm();
				}
			}
		}
		return 1000.0;
	}
	
	public static double calculateHeuristic2(final State state) {
		//The heuristic function takes the first reachable final state and returns the distance to reach it
		TaskSet ts = TaskSet.union(state.getTasks(), state.getCurrentTasks());
		City current_city = state.getCurrentCity();
		double total_dist = 0.0;
		
		for(Task t: ts) {
			if(t.pickupCity == current_city) {
				total_dist = total_dist + current_city.distanceTo(t.deliveryCity);
				current_city = t.deliveryCity;
				ts.remove(t);
			}

		}
		return total_dist * state.getCostKm();
	}
	
	public static double calculateHeuristic3(final State state) {
		//The heuristic function takes the first reachable final state and returns the distance to reach it
		TaskSet ts = TaskSet.union(state.getTasks(), state.getCurrentTasks());
		double total_dist = 0.0;
		
		for(Task t: ts) {
			total_dist += t.pickupCity.distanceTo(t.deliveryCity);
		}
		return total_dist * state.getCostKm();
	}
	
	public static Plan run(final State startState, Heuristic h) {
		
		
		//our priority queue needs to be sorted according to the total cost to reach each state
		class StateComparator implements Comparator<State>{ 
			@Override
			public int compare(State s1, State s2) {
				//we order our priority queue according to the total cost (currentCost + heuristic cost)
				switch(h) {
					case DISTANCE:
						return (int)Double.compare(s1.getCurrentCost() + calculateHeuristic(s1), 
								s2.getCurrentCost() + calculateHeuristic(s2));
					case DISTANCE2:
						return (int)Double.compare(s1.getCurrentCost() + calculateHeuristic2(s1), 
								s2.getCurrentCost() + calculateHeuristic2(s2));
					case DISTANCE3:
						return (int)Double.compare(s1.getCurrentCost() + calculateHeuristic2(s1), 
								s2.getCurrentCost() + calculateHeuristic2(s2));
					default:
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
		PriorityQueue<State> queue = new PriorityQueue<State>(comparator);
		Map<Integer, State> visited_states = new HashMap<Integer, State>();
		City startCity = startState.getCurrentCity();
		
		queue.add(state);
		
        while (!state.isFinalState() && !queue.isEmpty()) {
            nSteps += 1;
            state = queue.poll();
            
            //if(nSteps <30) state.carrying();
            if(nSteps <40) System.out.println("is: " + state.toPlan(startCity).toString() + " Cost: " + state.getCurrentCost() + " dist: " + state.toPlan(startCity).totalDistance());
            if (!visited_states.containsKey(state.hashCode()) || comparator.isSmaller(state, visited_states.get(state.hashCode()))) {
            	visited_states.put(state.hashCode(), state);
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

