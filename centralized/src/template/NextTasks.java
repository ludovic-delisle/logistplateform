package template;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

public class NextTasks {
	private HashMap<Vehicle, LinkedList<Task>> nextTask;
	
	public NextTasks() {
		this.nextTask = new HashMap<Vehicle,LinkedList<Task>>();
	}
	
	public NextTasks(NextTasks n) {
		this.nextTask = new HashMap<Vehicle,LinkedList<Task>>(n.nextTask);
	}
	
	public NextTasks(List<Vehicle> vehicles, TaskSet tasks) {
		LinkedList<Task> ll_t = new LinkedList<Task>();
		HashMap<Vehicle, LinkedList<Task>> hm = new HashMap<Vehicle,LinkedList<Task>>();
		
		Iterator<Vehicle> it = vehicles.iterator();
		Vehicle v = it.next();
		for(Task t: tasks) {
			if(v.getCurrentTasks().weightSum() + t.weight > v.capacity()) ll_t.add(t);
			else {
				hm.put(v, new LinkedList<Task>(ll_t));
				v = it.next();
				ll_t.clear();
			}
		}
		this.nextTask = hm;
	}
	
	public Task get(Task t) {
		for(Vehicle v: nextTask.keySet()) {
			LinkedList<Task> ll_t = nextTask.get(v);
			int idx = ll_t.indexOf(t);
			if(idx > -1) return ll_t.get(idx + 1);
		}
		return null; 
	}
	
	public Task get(Vehicle v, Task t) {
		LinkedList<Task> ll_t = nextTask.get(v);
		int idx = ll_t.indexOf(t);
		if(idx > -1) return ll_t.get(idx + 1);
		else throw new ArrayIndexOutOfBoundsException();
	}
	
	public List<Task> getCurrentTasks(Vehicle v) {
		return nextTask.get(v);
	}
	
	public Task getFirstTask(Vehicle v) {
		return nextTask.get(v).get(0);
	}
	
	public Integer getTime(Task t) {
		for(Vehicle v: nextTask.keySet()) {
			LinkedList<Task> ll_t = nextTask.get(v);
			int idx = ll_t.indexOf(t);
			if(idx > -1) return idx+1;
		}
		return 0;
	}
	
	public Integer getTime(Vehicle v, Task t) {
		LinkedList<Task> ll_t = nextTask.get(v);
		int idx = ll_t.indexOf(t);
		if(idx > -1) return idx+1;
		else return 0;
	}
	
	public Integer size() {
		int nb_tasks=0;
		for(Vehicle v: nextTask.keySet()) {
			nb_tasks += nextTask.get(v).size();
		}
		return nextTask.size() + nb_tasks;
	}
	
	public void put(Task t1, Task t2) {
		for(Vehicle v: nextTask.keySet()) {
			LinkedList<Task> ll_t = nextTask.get(v);
			int idx = ll_t.indexOf(t1);
			if(idx > -1) {
				ll_t.add(idx, t2);
			}
		}
	}
	
	public void put(Vehicle v, Task t1, Task t2) {
		LinkedList<Task> ll_t = nextTask.get(v);
		int idx = ll_t.indexOf(t1);
		if(idx > -1) {
			ll_t.add(idx, t2);
		}
	}
	
	public void swap_task_order(Task t) {
		for(Vehicle v: nextTask.keySet()) {
			LinkedList<Task> ll_t = nextTask.get(v);
			int idx = ll_t.indexOf(t);
			if(idx > -1) {
				Collections.swap(ll_t, idx, idx+1);
			}
		}
	}
	
	public NextTasks swap_task_order(Vehicle v, Task t) {
		NextTasks res = new NextTasks(this);
		LinkedList<Task> ll_t = res.nextTask.get(v);
		int idx = ll_t.indexOf(t);
		if(idx > -1) {
			Collections.swap(ll_t, idx, idx+1);
		}
		return res;
	}
	
	public Vehicle getVehicle(Task t) {
		for(Vehicle v: nextTask.keySet()) {
			LinkedList<Task> ll_t = nextTask.get(v);
			int idx = ll_t.indexOf(t);
			if(idx > -1) return v;
		}
		return null; 
	}
	
	public NextTasks swap_task_vehicle(Task t, Vehicle v1, Vehicle v2) {
		NextTasks res = new NextTasks(this);
		if(res.getCurrentTasks(v1).remove(t)) res.getCurrentTasks(v2).add(0, t);
		return res;
	}
	
}
