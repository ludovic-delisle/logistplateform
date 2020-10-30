package template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

import action.*;

public class NextTasks {
	private HashMap<Vehicle, LinkedList<Task>> nextTask;
	private HashMap<Vehicle, LinkedList<Action>> nextAction;
	private HashMap<Task, LinkedList<Action>> taskActionMap;
	Random rand = new Random(42);
	public NextTasks() {
		this.nextTask = new HashMap<Vehicle,LinkedList<Task>>();
	}
	
	public NextTasks(NextTasks n) {
		HashMap<Vehicle, LinkedList<Task>> n2 = new HashMap<Vehicle,LinkedList<Task>>();
		HashMap<Vehicle, LinkedList<Action>> n3 = new HashMap<Vehicle,LinkedList<Action>>();
		HashMap<Task, LinkedList<Action>> n4 = new HashMap<Task, LinkedList<Action>>();
		
		for(Vehicle v: n.nextTask.keySet()) {
			n2.put(v, new LinkedList<Task>(n.getCurrentTasks(v)));
			n3.put(v, new LinkedList<Action>(n.getCurrentActions(v)));
		}
		
		n4.putAll(n.get_map());
		this.nextTask = n2;
		this.nextAction=n3;
		this.taskActionMap=n4;
	}
	
	public NextTasks(List<Vehicle> vehicles, TaskSet tasks) {
		this.nextTask = new HashMap<Vehicle,LinkedList<Task>>();
		
		for(Vehicle v: vehicles) {
			this.nextTask.put(v, new LinkedList<Task>());
		}
		
		LinkedList<Task> ll_t = new LinkedList<Task>();
		Iterator<Vehicle> it = vehicles.iterator();
		Vehicle v = it.next();
		for(Task t: tasks) {
			if(ll_t.stream().collect(Collectors.summingInt(i -> i.weight)) + t.weight <= v.capacity()) {
				ll_t.add(t);
			} else {
				this.nextTask.put(v, new LinkedList<Task>(ll_t));
				v = it.next();
				ll_t.clear();
				ll_t.add(t);
			}
		}
		this.nextTask.put(v, new LinkedList<Task>(ll_t));
		
	}
	
	public NextTasks(List<Vehicle> vehicles, TaskSet tasks, int a) {
		this.nextTask = new HashMap<Vehicle,LinkedList<Task>>();
		this.nextAction = new HashMap<Vehicle,LinkedList<Action>>();
		this.taskActionMap= new HashMap<Task,LinkedList<Action>>();
		for(Vehicle v: vehicles) {
			this.nextTask.put(v, new LinkedList<Task>());
			this.nextAction.put(v, new LinkedList<Action>());
		}
		LinkedList<Task> task_list = new LinkedList<Task>();
		Iterator<Vehicle> it = vehicles.iterator();
		Vehicle v = it.next();
		
		for(Task t: tasks) {
			
			//Add pickup and delivery action to the map with their corresponding task
			this.taskActionMap.put(t, new LinkedList<Action>());
			Action p = new Pickup(t);
			Action d = new Delivery(t);
			LinkedList<Action> new_action_list = taskActionMap.get(t);
			new_action_list.add(p);
			new_action_list.add(d);
			taskActionMap.put(t, new_action_list);
			
			//Assigns the tasks randomly to each vehicle
			Vehicle random_vehicle;
			do {
		    random_vehicle = vehicles.get(rand.nextInt(vehicles.size()));
			}while(random_vehicle.capacity() <= (nextTask.get(random_vehicle).size()+1)*t.weight);
			this.add(random_vehicle, t);
			this.add_actions(random_vehicle,  t);
			
		}
		for(Vehicle ve: vehicles) {
			Collections.shuffle(nextTask.get(ve), rand);
			
		}
		
		
	}
	
	
	public int get_all_task_weight(List<Task> ll_t) {
		return ll_t.stream().collect(Collectors.summingInt(i -> i.weight));
	}
	
	public Task get(Task t) {
		for(Vehicle v: nextTask.keySet()) {
			LinkedList<Task> ll_t = nextTask.get(v);
			int idx = ll_t.indexOf(t);
			if(idx > -1 && idx < ll_t.size() - 1) return ll_t.get(idx + 1);
		}
		return null; 
	}
	
	public Task get(Vehicle v, Task t) {
		LinkedList<Task> ll_t = nextTask.get(v);
		int idx = ll_t.indexOf(t);
		if(idx > -1 && idx < ll_t.size() - 1) return ll_t.get(idx + 1);
		else return null;
	}
	
	public List<Task> getCurrentTasks(Vehicle v) {
		return nextTask.get(v);
	}
	
	public List<Action> getCurrentActions(Vehicle v){
		return nextAction.get(v);
	}
	public HashMap<Task, LinkedList<Action>> get_map(){
		return taskActionMap;
	}
	
	public Task getFirstTask(Vehicle v) {
		List<Task> l_t = nextTask.get(v);
		if(l_t.size() <= 0) return null;
		else return l_t.get(0);
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
	
	public Integer getSize() {
		int nb_tasks=0;
		for(Vehicle v: nextTask.keySet()) {
			nb_tasks += nextTask.get(v).size() + 1;
			
		}
		
		return nb_tasks;
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
	
	public NextTasks swap_task_order(Vehicle v, Task t) {
		NextTasks res = new NextTasks(this);
		LinkedList<Task> ll_t = res.nextTask.get(v);
		int idx = ll_t.indexOf(t);

		if(idx > -1 && idx < ll_t.size()-1) {
			Collections.swap(ll_t, idx, idx+1);
		}
		return res;
	}
	
	public NextTasks swap_action_order(Vehicle v, Action a) {
		NextTasks res = new NextTasks(this);
		LinkedList<Action> ll_t = res.nextAction.get(v);
		int idx = ll_t.indexOf(a);

		if(idx > -1 && idx < ll_t.size()-1) {
			Collections.swap(ll_t, idx, idx+1);
		}
		return res;
	}
	
	public NextTasks swap_task_order_2(Vehicle v, Task t) {
		NextTasks res = new NextTasks(this);
		LinkedList<Task> ll_t = res.nextTask.get(v);
		int idx = ll_t.indexOf(t);

		if(idx > -1 && idx < ll_t.size()-3) {
			Collections.swap(ll_t, idx, idx+3);
		}
		return res;
	}
	
	public NextTasks swap_task_order_3(Vehicle v) {
		NextTasks res = new NextTasks(this);
		LinkedList<Task> ll_t = res.nextTask.get(v);
		Collections.swap(ll_t, 0, ll_t.size()-1);
		
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
	
	public List<NextTasks> swap_task_vehicle(Vehicle v1, final List<Vehicle> vv2) {
		List<NextTasks> res_list = new LinkedList<NextTasks>();
		List<Task> l_t = new ArrayList<Task>(this.getCurrentTasks(v1));
		for(Task t1: l_t) {
			for(Vehicle v2: vv2) {
				NextTasks res = new NextTasks(this);
				 if(get_all_task_weight(res.getCurrentTasks(v2)) + t1.weight <= v2.capacity() && !v1.equals(v2))
				 	res.remove(v1, t1);
				 	res.add(v2, t1);
				 	res_list.add(res);
			}
		}
		return res_list;
	}
	public List<NextTasks> swap_action_vehicle(Vehicle v1, final List<Vehicle> vv2) {
		List<NextTasks> res_list = new LinkedList<NextTasks>();
		List<Task> l_t = new ArrayList<Task>(this.getCurrentTasks(v1));
		for(Task t1: l_t) {
			for(Vehicle v2: vv2) {
				NextTasks res = new NextTasks(this);
				res.remove(v1, t1);
			 	res.add(v2, t1);
				res.remove_actions(v1, t1);
				res.add_actions(v2, t1);
				res_list.add(res);
			}
		}
		return res_list;
	}
	
	public void add(Vehicle v, Task t) {
		LinkedList<Task> new_task_list = nextTask.get(v);
		new_task_list.add(t);
		nextTask.put(v, new_task_list);
	}
	
	public void remove(Vehicle v, Task t) {
		LinkedList<Task> new_task_list = nextTask.get(v);
		new_task_list.remove(t);
		nextTask.put(v, new_task_list);
	}
	public void add_actions(Vehicle v, Task t) {
		Action p = new Pickup(t);
		Action d = new Delivery(t);
		
		LinkedList<Action> new_action_list = nextAction.get(v);
		new_action_list.add(p);
		new_action_list.add(d);
		
		this.nextAction.put(v, new_action_list);
	}
	
	public void remove_actions(Vehicle v, Task t) {
		Action p = new Pickup(t);
		Action d = new Delivery(t);
		
		LinkedList<Action> new_action_list = nextAction.get(v);
		new_action_list.remove(p);
		new_action_list.remove(d);
		
		nextAction.put(v, new_action_list);
	}
	
}
