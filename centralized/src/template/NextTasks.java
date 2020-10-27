package template;

import java.util.HashMap;
import java.util.List;

import logist.simulation.Vehicle;
import logist.task.Task;

public class NextTasks {
	private HashMap<Task,Task> nextTask;
	private HashMap<Vehicle,Task> nextTaskVehicle;
	private HashMap<Task,Integer> timeMap;
	
	public NextTasks() {
		this.nextTask = new HashMap<Task,Task>();
		this.nextTaskVehicle = new HashMap<Vehicle,Task>();
		this.timeMap = new HashMap<Task, Integer>();
	}
	
	public NextTasks(nextTask n) {
		this.nextTask = new HashMap<Task,Task>(n.nextTask);
		this.nextTaskVehicle = new HashMap<Vehicle,Task>(n.nextTaskVehicle);
		this.timeMap = new HashMap<Task, Integer>(n.timeMap);
	}
	
	public NextTasks(List<Vehicle> vehicles) {
		this.nextTask = new HashMap<Task,Task>();
		this.nextTaskVehicle = new HashMap<Vehicle,Task>();
		this.timeMap = new HashMap<Task, Integer>();
		for(Vehicle v: vehicles) {
			nextTaskVehicle.put(v, null);
		}
	}
	
	public Task get(Task t) {
		return nextTask.get(t);
	}
	
	public Task get(Vehicle v) {
		return nextTaskVehicle.get(v);
	}
	
	public Integer getTime(Task t) {
		return timeMap.get(t);
	}
	
	public Integer size() {
		return nextTask.size() + nextTaskVehicle.size();
	}
}
