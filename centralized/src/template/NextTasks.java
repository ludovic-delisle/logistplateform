package template;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class NextTasks {
	private HashMap<Task,Task> nextTask;
	private HashMap<Task, Vehicle> nextTaskVehicle;
	private HashMap<Task,Integer> timeMap;
	
	public NextTasks() {
		this.nextTask = new HashMap<Task,Task>();
		this.nextTaskVehicle = new HashMap<Task, Vehicle>();
		this.timeMap = new HashMap<Task, Integer>();
	}
	
	public NextTasks(NextTasks n) {
		this.nextTask = new HashMap<Task,Task>(n.nextTask);
		this.nextTaskVehicle = new HashMap<Task, Vehicle>(n.nextTaskVehicle);
		this.timeMap = new HashMap<Task, Integer>(n.timeMap);
	}
	
	public NextTasks(List<Vehicle> vehicles, TaskSet tasks) {
		this.nextTask = new HashMap<Task,Task>();
		this.nextTaskVehicle = new HashMap<Task, Vehicle>();
		this.timeMap = new HashMap<Task, Integer>();
		
		Iterator<Vehicle> it = vehicles.iterator();
		Vehicle v = it.next();
		for(Task t: tasks) {
			if(v.getCurrentTasks().weightSum() + t.weight > v.capacity())	v = it.next();
			nextTaskVehicle.put(t, v);
		}
	}
	
	public Task get(Task t) {
		return nextTask.get(t);
	}
	
	public Set<Task> get(Vehicle v) {
		return nextTaskVehicle.entrySet()
	              .stream()
	              .filter(entry -> Objects.equals(entry.getValue(), v))
	              .map(Map.Entry::getKey)
	              .collect(Collectors.toSet());
	}
	
	public Integer getTime(Task t) {
		return timeMap.get(t);
	}
	
	public Integer size() {
		return nextTask.size() + nextTaskVehicle.size();
	}
	
	public void remove(Task t) {
		nextTask.remove(t);
	}
	
	public void put(Task t1, Task t2) {
		nextTask.put(t1, t2);
	}
	
	public void swap_task_order(Task t) {
		Task old_next_task = nextTask.remove(t);
		nextTask.put(old_next_task, t);
	}
	
	public Vehicle getVehicle(Task t) {
		return nextTaskVehicle.get(t);
	}
	
	public void swap_task_vehicle(Task t, Vehicle v1, Vehicle v2) {
		if(nextTaskVehicle.remove(t, v1)) nextTaskVehicle.put(t, v2);
	}
	
}
