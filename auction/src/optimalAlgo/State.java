package optimalAlgo;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import logist.plan.Action;
import logist.plan.Plan;
import logist.plan.Action.*;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import logist.simulation.Vehicle;
import logist.task.Task;

public class State{
	
	private Vehicle vehicle;
	private double current_cost=0;
	private TaskSet available_tasks;
	private TaskSet current_tasks;
	private int remainingCapacity;
	private City current_city;
	private Queue<Action> previous_actions= new LinkedList<Action>();
	private double biddingFactor = 0.05;
	
	public State(Vehicle vehicle) {
		this.vehicle=vehicle;
		this.remainingCapacity=vehicle.capacity();
		this.current_city=vehicle.getCurrentCity();
	}
	
	/**
	 * constructor called from the class DeliberativeTemplate. used to create the first state
	 * @param vehicle vehicle of the agent 
	 * @param tasks taskeset of the tasks available in the environment
	 */
	public State(Vehicle vehicle, TaskSet tasks) {
			this.vehicle=vehicle;
			this.available_tasks=tasks.clone();
			this.current_tasks=vehicle.getCurrentTasks().clone();
			this.remainingCapacity= (vehicle.capacity());
			this.current_city=vehicle.getCurrentCity();
	}
	
	public static State MakeEmptyState(State state, TaskSet tasks) {
		State resulting_state = new State(state);

		if(tasks == null) {
			resulting_state.current_tasks=null;
			resulting_state.available_tasks=null;
		}else {
			resulting_state.current_tasks=TaskSet.noneOf(tasks);
			resulting_state.available_tasks=tasks.clone();
		}
		
		return resulting_state;
	}
	
	public static State MakeEmptyState(Vehicle vehicle, TaskSet tasks) {
		State resulting_state = new State(vehicle);

		if(tasks == null) {
			resulting_state.current_tasks=null;
			resulting_state.available_tasks=null;
		}else {
			resulting_state.current_tasks=TaskSet.noneOf(tasks);
			resulting_state.available_tasks=tasks.clone();
		}
		
		return resulting_state;
	}
	
	/**
	 * Constructor used when creating a new state after an action
	 * @param state state that we are copying
	 */
	public State(State state) {
		this.vehicle = state.vehicle;
		this.current_cost=(state.current_cost);
		this.available_tasks=state.available_tasks.clone();
		this.current_tasks=state.current_tasks.clone();
		this.remainingCapacity=(state.remainingCapacity);
		this.current_city= state.current_city;
		this.previous_actions=new LinkedList<Action>(state.previous_actions);
	}
	
	public State(Vehicle vehicle, Task task) {
		this.vehicle=vehicle;
		TaskSet t = vehicle.getCurrentTasks().clone();
		t.add(task);
		this.available_tasks=t;
		this.current_tasks=TaskSet.noneOf(t);
		this.remainingCapacity= vehicle.capacity();
		this.current_city=vehicle.getCurrentCity();
	}
	/**
	 * Constructor used when creating a new state after an action
	 * @param state state that we are copying
	 */
	public State(State state, Task auctionedTask) {
		this.vehicle = state.vehicle;
		this.current_cost=(state.current_cost);
		this.current_tasks=state.current_tasks.clone();
		this.remainingCapacity=(state.remainingCapacity);
		this.current_city= state.current_city;
		this.previous_actions=new LinkedList<Action>(state.previous_actions);
		this.available_tasks= state.available_tasks.clone();
		this.available_tasks.add(auctionedTask);
	}
	
	
	/**
	 * Action method move
	 * @param next_city is the city where the agent will move
	 * @return the state expected after the action
	 */
	public State move(City next_city) {
			State resulting_state = new State(this);
			resulting_state.current_cost+=this.getCostKm()*next_city.distanceTo(this.getCurrentCity());
			resulting_state.previous_actions.add(new Move(next_city));
			resulting_state.current_city=next_city;
			return resulting_state;
	}
		
	/**
	 * Action method pickup
	 * @param task that the agent picks up
	 * @return the state expected after the action
	 */
	public State pickup(Task task) {
			State resulting_state = new State(this);
			resulting_state.previous_actions.add(new Pickup(task));
			resulting_state.available_tasks.remove(task);
			resulting_state.current_tasks.add(task);
			resulting_state.remainingCapacity-=task.weight;
			return resulting_state;
	}
		
	/**
	 * Action method delivery
	 * @param task that the agent delivers
	 * @return the state expected after the action
	 */
	public State delivery(Task task) {
			State resulting_state = new State(this);
			resulting_state.previous_actions.add(new Delivery(task));
			resulting_state.current_tasks.remove(task);
			resulting_state.remainingCapacity+=task.weight;
			return resulting_state;
		}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((available_tasks == null) ? 0 : available_tasks.hashCode());
		result = prime * result + ((current_city == null) ? 0 : current_city.hashCode());
		result = prime * result + ((current_tasks == null) ? 0 : current_tasks.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (available_tasks == null) {
			if (other.available_tasks != null)
				return false;
		} else if (!available_tasks.equals(other.available_tasks))
			return false;
		if (current_city == null) {
			if (other.current_city != null)
				return false;
		} else if (!current_city.equals(other.current_city))
			return false;
		if (current_tasks == null) {
			if (other.current_tasks != null)
				return false;
		} else if (!current_tasks.equals(other.current_tasks))
			return false;
		if (remainingCapacity != other.remainingCapacity)
			return false;
		return true;
	}
	
	
	/**
	 * Method that finds all the possible next states that can come after this state
	 * @return  all the possible next states that can come after this state
	 */
	public List<State> getPossibleStates() {

        List<State> possibleStates = new ArrayList<>();
        //first we get all the cities where we could deliver a task
        for (Task task : current_tasks) {
        	
            if (task.deliveryCity == this.getCurrentCity()) {
            	possibleStates.add(delivery(task));
            }
        }
        for (Task task : available_tasks) {
            if (task.weight <= this.remainingCapacity && task.pickupCity == this.getCurrentCity()) {
            	//then we get all the cities where we could pickup a task
              possibleStates.add(pickup(task));
            }
        } 
        for(City neighbor : this.getCurrentCity().neighbors()) {
        	possibleStates.add(move(neighbor));
        }

        return possibleStates;
    }
	
	/**
	 * Converts the list of actions of this state and the city where it all begun into a plan
	 * @param startCity the city where the agent starts the plan
	 * @return the plan that logist can use
	 */
	public Plan toPlan(City startCity) {
		List<Action> act_list = new ArrayList<Action>();
		act_list.addAll(previous_actions);
		
		return new Plan(startCity, act_list);
	}
	
	public City getCurrentCity(){
		return current_city;
	}
	public TaskSet getTasks() {
		return available_tasks;
	}
	public TaskSet getCurrentTasks() {
		return current_tasks;
	}
	public double getCurrentCost() {
		return current_cost;
	}
	public void setCurrentCost(double cost) {
		this.current_cost = cost;
	}
	public Vehicle getVehicle() {
		return vehicle;
	}
	/**
	 * A final state means that:
	 * 	- there are no more available tasks in the environment
	 * 	- the agent is carrying no task
	 * thus any additional action would just increase the cost
	 * @return if this state is a final state
	 */
	public boolean isFinalState() {
		
		return this.current_tasks.isEmpty() && this.available_tasks.isEmpty();
	}
	public int getRemainingCapacity() {
		return remainingCapacity;
	}
	public void setRemainingCapacity(int remainingCapacity) {
		this.remainingCapacity = remainingCapacity;
	}
	public int getCostKm() {
		return this.vehicle.costPerKm();
	}
	public double getSpeed() {
		return this.vehicle.speed();
	}
	public Action getLastAction() {
			return previous_actions.peek();
	}
	public Queue<Action> getActions(){
		return previous_actions;
	}
	
	public double getBiddingFactor() {
		return biddingFactor;
	}
	
	public void updateBiddingFactor(double increment) {
		this.biddingFactor += increment;
	}
	
	public void print_carrying() {
		double res=0.0;
		System.out.print("Tasks ");
		for(Task t:this.getCurrentTasks()) {
			res+=t.weight;
			System.out.print("id: "+t.id+ " wght: "+t.weight+ "  ");
		}
		System.out.println("");
		System.out.println(" Wght carr: " + res + " check: " + this.getCurrentTasks().weightSum()+ " rem: "+this.getRemainingCapacity());
	}
}
