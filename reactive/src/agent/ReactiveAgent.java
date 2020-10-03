package agent;

import logist.simulation.Vehicle;

import java.util.Random;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import transitions.Transition;

public class ReactiveAgent implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;
	private TaskDistribution taskDistribution;
	private Double discount;
	
	
	
	private Map<Transition, City> bestActions;
	
	
	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		
		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		
		this.discount = agent.readProperty("discount-factor", Double.class,
						0.95);

		this.numActions = 0;
		this.myAgent = agent;
		this.taskDistribution = distribution;
		
	
		
	    Map<Transition, Set<City>> actionMap = new HashMap<>();
	    Map<Transition, Double> state_values = new HashMap<>();
	    for (City startCity : topology.cities()) {
	    
	        for (City endCity : topology.cities()) {
	        	Set<City> possible_move = new HashSet<>();
	            		for (City neighbor_city : startCity.neighbors()) {
	            			possible_move.add(neighbor_city);
	            		}
	            if (startCity.name==endCity.name) {
	                actionMap.put(new Transition(startCity, null), possible_move);
	            } else {
	            	
	            	possible_move.add(endCity);
	                actionMap.put(new Transition(startCity, endCity), possible_move);
	            }

	        }
	    }
	
	    
	    bestActions = findBestActions(actionMap);
	    
	    
	}
	

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		
		numActions++;
		City destinationOfTask=null;
		City currentCityName=vehicle.getCurrentCity();
	
		if(availableTask != null) {
			destinationOfTask =  availableTask.deliveryCity;
		}
		Transition currentState = new Transition(currentCityName, destinationOfTask);
		for(Transition i : bestActions.keySet()) {
			if(i.sameTransition(currentState)){
				currentState=i;
			}
		}
		
		City nextDestination = bestActions.get(currentState);
		System.out.println(currentState);
		System.out.println("a");
		if(nextDestination == destinationOfTask) {
			
			
			return new Action.Pickup(availableTask);
		}
		else {
			System.out.println("olol7");
			return new Action.Move(nextDestination);
		}
	}
	
	private Map<Transition, City> findBestActions( Map<Transition, Set<City>> actionMap){
		Double threshold= 0.001;
		Boolean keepTraining=true;
		
		Map<Transition, City> newBestAction = new HashMap<>();
		Map<Transition, Double> v = new HashMap<>();
		
		while(keepTraining) {
			
			keepTraining=false;
			
			
			for(Transition transition : actionMap.keySet()) {
				Double best_R = 0.0;
				City bestCity = null;
				City current_start_city = transition.get_start_city();
				
				
				for(City city : actionMap.get(transition)) {
					
					Double new_R=0.0;
					Boolean has_task = (city==transition.get_end_city());
					
					if(!isPossible(transition, city)) {
						new_R = Double.NEGATIVE_INFINITY;
						
					}
					else{
						new_R = netReward(current_start_city, city, has_task);
						}
					
					
					for(Transition next_pos : actionMap.keySet()) {
						if(next_pos.get_start_city()==city) {
							Double proba_of_task = proba_of_task(transition, next_pos, city);
							new_R += discount*proba_of_task*v.getOrDefault(next_pos, 0.0);
							
						}
					}
					if (new_R >= best_R) {
		                bestCity = city;
		                best_R = new_R;
		                
					}
				}
				if (best_R > v.getOrDefault(transition, 0.0)) {
	                if (best_R - v.getOrDefault(transition, 0.0) > threshold) {
	                	keepTraining=true;
	                } 
	                v.put(transition, best_R);
                    newBestAction.put(transition, bestCity);
	            }
			}
	}
		return newBestAction;
	}
	private Double netReward(City startCity, City endCity, Boolean isGain) {
		double gain=0.0;
		if(isGain) {
			gain = taskDistribution.reward(startCity, endCity);
		}
		double task_dist = startCity.distanceTo(endCity);
		double cost = task_dist*myAgent.vehicles().get(0).costPerKm();
		return gain - cost;
	}
	
	private Boolean isPossible(Transition transition, City nextCity){
		Boolean next_city_is_a_neighbor=transition.get_start_city().neighbors().contains(nextCity);
		Boolean next_city_is_task_destination = nextCity == transition.get_end_city();
		Boolean possible= next_city_is_a_neighbor || next_city_is_task_destination;
		return possible;
	}
	
	private double proba_of_task(Transition transition, Transition nextTransition, City nextCity) {
		
		return taskDistribution.probability(nextTransition.get_start_city(), nextTransition.get_end_city());
	}

}
