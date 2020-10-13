package model;

import java.util.ArrayList;
import java.util.List;

import logist.plan.Action;
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
	private TaskSet current_tasks;
	private TaskSet tasks_taken;
	private double cost_km;
	
	public State(Vehicle vehicle, TaskSet tasks) {
		this.vehicle=vehicle;
		this.available_tasks=tasks;
		this.current_tasks=vehicle.getCurrentTasks();
		this.cost_km=vehicle.costPerKm();
	}
	
	// called for next state after an action move
	public State(State state, City next_city) {
		double additional_cost = state.get_cost_km()*next_city.distanceTo(state.get_current_city());
		this.current_cost= state.get_current_cost()+additional_cost;
		this.vehicle=state.get_vehicle();
		this.available_tasks=state.get_tasks();
		this.tasks_taken=state.get_taken_task();
		this.current_tasks=state.get_current_tasks();
	}
	
	public List<Action> get_possible_actions(){
		List<Action> possible_actions = new ArrayList<>();
		
		return possible_actions; 
	}
	
	public City get_current_city(){
		return vehicle.getCurrentCity();
	}
	public TaskSet get_tasks() {
		return available_tasks;
	}
	public TaskSet get_current_tasks() {
		return current_tasks;
	}
	public double get_current_cost() {
		return current_cost;
	}
	public double get_cost_km() {
		return cost_km;
	}
	public Vehicle get_vehicle() {
		return vehicle;
	}
	public TaskSet get_taken_task() {
		return tasks_taken;
	}
	public boolean 
}
