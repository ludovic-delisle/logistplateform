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
		
		this.discount = agent.readProperty("discount-factor", Double.class, 0.95);

		this.numActions = 0;
		this.myAgent = agent;
		this.taskDistribution = distribution;
	
	    Map<Transition, Set<City>> actionMap = new HashMap<>();
	    /*In this double for loop, we add values to actionMap:
	      We create a Transition object for each city and for each possible situation in this city.
	    		for example:
	    			- finding no task ---> Transition(city_1, null)
	    			- finding a task that sends vehicle in any city --> Transition(city_1, city_x)
	      Then, for each of those situations, we add a list of cities that the vehicle could go to:
	        		- city where the task sends the vehicle
	        		- neighbor city if the task is refused
	    */
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
	
	    this.bestActions = findBestActions(actionMap);
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
		if(nextDestination == destinationOfTask) {
			return new Action.Pickup(availableTask);
		} else {
			return new Action.Move(nextDestination);
		}
	}
	
	/**
     * @returns a Map<Transition, City> which indicate for each situation transition, what the best choice of city is.
     * @param actionMap indicate for each situation transition, what choices of city we have.
     * Thus the agent has to choose for each Transition in actionMap the best choice and we'll have bestActions.
     */
	private Map<Transition, City> findBestActions( Map<Transition, Set<City>> actionMap){
		Double threshold= 0.0001;
		Boolean keepTraining=true;
		
		Map<Transition, City> newBestAction = new HashMap<>();
		Map<Transition, Double> v = new HashMap<>();
		
		while(keepTraining) {
			
			keepTraining=false;
			
			//we first iterate over all possible transitions. We have transitions like (city_1, null) and it just means that the agent has no task and has to go to a neighbor city.
			//for every transition, we have to find the best action (city to go)
			for(Transition transition : actionMap.keySet()) {
				
				//set the highest reward to 0, it will later on be the sum of all the consecutive rewards 
				Double best_R = 0.0;
				City bestCity = null;
				City current_start_city = transition.get_start_city();
				
				
				//we then iterate over all the possible cities => either a neighbor city or the task destination
				//All the cities that we can reach from our current position
				for(City city : actionMap.get(transition)) {
					
					Double new_R=0.0;
					//Until we are at the end city, where we will drop our task, we still have it
					Boolean has_task = (city==transition.get_end_city());
					
					//in case it should not be possible to go to a city, the cost for doing so must be very penalizing
					if(!isPossible(transition, city)) {
						new_R = Double.NEGATIVE_INFINITY;
					}else{
					//otherwise we simply calculate the net profit of going to this city
					//if the agent already has a task, the reward is zero
						new_R = netReward(current_start_city, city, has_task);
					}
					
					//we iterate over all the possible situations that could arise after arriving to the City city.
					//we add to the previously calculated net reward, the state value of that city attenuated by discount factor in order to make future rewards weight less.
					//The state value of a city is the best reward that we can hope of getting in the long run if we go to that city.
					for(Transition next_pos : actionMap.keySet()) {
						//we verify that the next possible action indeed starts from our current city
						if(next_pos.get_start_city()==city) {
							Double proba_of_task = taskDistribution.probability(next_pos.get_start_city(), next_pos.get_end_city());
							//getOrDefault gets the reward of going to this city
							new_R += discount*proba_of_task*v.getOrDefault(next_pos, 0.0);
						}
					}
					//if the newly calculated reward is better with this city than for the previous one, it means that we have found a new best city with a new best reward!
					if (new_R >= best_R) {
		                bestCity = city;
		                best_R = new_R; 
					}
				}
				//Finally, we check if there is still a noticeable improvement at each step and decide whether we keep training the agent or not.
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
	/**
     * @returns the net reward for going from a city to another 
     * @param startCity the starting city
     * @param endCity the ending city
     * @param isGain if it is to do a task or just to move to a neighboring city
     */
	private Double netReward(City startCity, City endCity, Boolean isGain) {
		double gain=0.0;
		if(isGain) {
			//if the trip isn't done for a task, there's no reward.
			gain = taskDistribution.reward(startCity, endCity);
		}
		double task_dist = startCity.distanceTo(endCity);
		double cost = task_dist*myAgent.vehicles().get(0).costPerKm();
		return gain - cost;
	}
	/**
     * @returns if for a given Transition, it is possible to go to nextCity
     * @param transition the transition that might take the vehicle to the next city
     * @param nextCity the city we control that it is possible to go to
     */
	private Boolean isPossible(Transition transition, City nextCity){
		// if nextCity is a neighbor the startCity of transition then it will be true
		Boolean next_city_is_a_neighbor=transition.get_start_city().neighbors().contains(nextCity);
		
		// if nextCity is the endCity of transition then it will be true
		Boolean next_city_is_task_destination = nextCity == transition.get_end_city();
		
		Boolean possible= next_city_is_a_neighbor || next_city_is_task_destination;
		return possible;
	}
	
}
