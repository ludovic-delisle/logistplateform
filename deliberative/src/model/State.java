package model;

import java.util.ArrayList;
import java.util.List;

import actions.Action;
import logist.plan.Plan;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import logist.simulation.Vehicle;
import logist.task.Task;

public class State {
	private Vehicle vehicle;
	private double current_cost=0;
	private TaskSet available_tasks;
	private TaskSet current_tasks=null;
	private Task priority_task=null; //the Task that it is currently executing
	private City destination_city=null;
	private int remainingCapacity;
	private boolean isFinalState=false;
	List<Action> previous_actions=null;
	
	public State(Vehicle vehicle, TaskSet tasks, boolean isFinalState1) {
		this.vehicle=vehicle;
		this.available_tasks=tasks;
		this.current_tasks=vehicle.getCurrentTasks();
		this.setRemainingCapacity(vehicle.capacity());
		this.isFinalState = isFinalState1;
	}
	
	
	public void changePriorityTask(Task newPriorityTask) {
		
		this.priority_task = newPriorityTask;
		this.destination_city = newPriorityTask.deliveryCity;
	}
	

	// C'est la même chose que get tasks non? On veut les tasks qu'il reste à accomplir non?
	public List<Action> get_possible_actions(){
				
		return null; 
	}
	
	public City getCurrentCity(){
		return vehicle.getCurrentCity();
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
	public Task getPriorityTask() {
		return priority_task;
	}
	public City getDestinationCity() {
		return destination_city;
	}
	public boolean isFinalState() {
		return isFinalState;
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
	public void updateActionList(List<Action> act_list) {
		this.previous_actions = act_list;
	}
	
	public List<Action> getActionList() {
		return this.previous_actions;
	}
	
	

}
