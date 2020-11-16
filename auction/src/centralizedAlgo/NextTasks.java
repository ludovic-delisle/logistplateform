package centralizedAlgo;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

import action.*;

public class NextTasks {

	private HashMap<Vehicle, LinkedList<Task>> nextTask; // stores task assigned to each vehicle
	private HashMap<Vehicle, LinkedList<Action>> nextAction; //stores the action to perform in order
	private HashMap<Task, LinkedList<Action>> taskActionMap; //stores which actions (pickup and delivery) are associated which which task
	
	public NextTasks(NextTasks n) {
		HashMap<Vehicle, LinkedList<Task>> n2 = new HashMap<Vehicle,LinkedList<Task>>();
		HashMap<Vehicle, LinkedList<Action>> n3 = new HashMap<Vehicle,LinkedList<Action>>();
		HashMap<Task, LinkedList<Action>> n4 = new HashMap<Task, LinkedList<Action>>();
		
		for(Vehicle v: n.nextTask.keySet()) {
			n2.put(v, new LinkedList<Task>(n.getCurrentTasks(v)));
			n3.put(v, new LinkedList<Action>(n.getCurrentActions(v)));
		}
		
		n4.putAll(n.get_map());
		this.nextTask=n2;
		this.nextAction=n3;
		this.taskActionMap=n4;
	}
	
	// used for version where a vehicle can only carry one task at a time
	public NextTasks(List<Vehicle> vehicles, TaskSet tasks) {
		this.nextTask = new HashMap<Vehicle,LinkedList<Task>>();
		
		for(Vehicle v: vehicles) {
			this.nextTask.put(v, new LinkedList<Task>());
		}
		
		LinkedList<Task> ll_t = new LinkedList<Task>();
		ll_t.addAll(tasks);
		
		this.nextTask.put(vehicles.get(0), new LinkedList<Task>(ll_t));
	}
	
	public NextTasks(NextTasks n, Task new_task) {
	
		HashMap<Vehicle, LinkedList<Task>> n2 = new HashMap<Vehicle,LinkedList<Task>>();
		HashMap<Vehicle, LinkedList<Action>> n3 = new HashMap<Vehicle,LinkedList<Action>>();
		HashMap<Task, LinkedList<Action>> n4 = new HashMap<Task, LinkedList<Action>>();
		
		for(Vehicle v: n.nextTask.keySet()) {
			n2.put(v, new LinkedList<Task>(n.getCurrentTasks(v)));
			n3.put(v, new LinkedList<Action>(n.getCurrentActions(v)));
		}
		
		n4.putAll(n.get_map());
		this.nextTask=n2;
		this.nextAction=n3;
		this.taskActionMap=n4;
		
		Action p = new Pickup(new_task);
		Action d = new Delivery(new_task);
		LinkedList<Action> new_action_list = new LinkedList<Action>();
		new_action_list.add(p);
		new_action_list.add(d);
		taskActionMap.put(new_task, new_action_list);
		
		
		Map.Entry<Vehicle, LinkedList<Action>> entry = n3.entrySet().iterator().next();
		Vehicle key = entry.getKey();
		this.add(key, new_task);
		this.add_actions(key, new_task);
		
	}
	
	
	/**
	 * Creates a new NextTasks object by assigning the tasks in a random order to randomly chosen vehicles
	 * @param vehicles: 
	 * @param tasks: set of available tasks in the environment 
	 * @param rand: Random generator
	 */
	public NextTasks(List<Vehicle> vehicles, TaskSet tasks, Random rand) {
		this.nextTask = new HashMap<Vehicle,LinkedList<Task>>();
		this.nextAction = new HashMap<Vehicle,LinkedList<Action>>();
		this.taskActionMap= new HashMap<Task,LinkedList<Action>>();
		
		for(Vehicle v: vehicles) {
			this.nextTask.put(v, new LinkedList<Task>());
			this.nextAction.put(v, new LinkedList<Action>());
		}

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
			random_vehicle = vehicles.get(rand.nextInt(vehicles.size()));
			/*
			 * Probablement pas nécessaire de check la capacité comme ça pck on effectue les tasks séquentiellement -> boucle infinie!!!!!
			
			do {
				random_vehicle = vehicles.get(rand.nextInt(vehicles.size()));
			}while(random_vehicle.capacity() <= (nextTask.get(random_vehicle).size()+1)*t.weight);
			*/
			this.add(random_vehicle, t);
			this.add_actions(random_vehicle,  t);
			
		}
		
		for(Vehicle ve: vehicles) {
			// shuffle the order of tasks that are assigned to each vehicle
			Collections.shuffle(nextTask.get(ve), rand);
			
		}
		
		
	}
	public NextTasks(List<Vehicle> vehicles, List<Task> tasks, Random rand) {
		this.nextTask = new HashMap<Vehicle,LinkedList<Task>>();
		this.nextAction = new HashMap<Vehicle,LinkedList<Action>>();
		this.taskActionMap= new HashMap<Task,LinkedList<Action>>();
		
		for(Vehicle v: vehicles) {
			this.nextTask.put(v, new LinkedList<Task>());
			this.nextAction.put(v, new LinkedList<Action>());
		}

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
			// shuffle the order of tasks that are assigned to each vehicle
			Collections.shuffle(nextTask.get(ve), rand);
			
		}
		
		
	}
	
	// get the cumulative weight of all tasks
	public int get_all_task_weight(List<Task> ll_t) {
		return ll_t.stream().collect(Collectors.summingInt(i -> i.weight));
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
	
	// swaps the current task with the next one in the list of tasks
	public NextTasks swap_task_order(Vehicle v, Task t) {
		NextTasks res = new NextTasks(this);
		LinkedList<Task> ll_t = res.nextTask.get(v);
		int idx = ll_t.indexOf(t);

		if(idx > -1 && idx < ll_t.size()-1) {
			Collections.swap(ll_t, idx, idx+1);
		}
		return res;
	}
	
	// swaps the current action with the next one in the list of actions
	public NextTasks swap_action_order(Vehicle v, Action a) {
		NextTasks res = new NextTasks(this);
		LinkedList<Action> ll_t = res.nextAction.get(v);
		int idx = ll_t.indexOf(a);

		if(idx > -1 && idx < ll_t.size()-1) {
			Collections.swap(ll_t, idx, idx+1);
		}
		return res;
	}
	
	// swaps the current task with the one 3 steps away
	public NextTasks swap_task_order_2(Vehicle v, Task t) {
		NextTasks res = new NextTasks(this);
		LinkedList<Task> ll_t = res.nextTask.get(v);
		int idx = ll_t.indexOf(t);

		if(idx > -1 && idx < ll_t.size()-3) {
			Collections.swap(ll_t, idx, idx+3);
		}
		return res;
	}
	
	// swaps the first and the last task
	public NextTasks swap_task_order_3(Vehicle v) {
		NextTasks res = new NextTasks(this);
		LinkedList<Task> ll_t = res.nextTask.get(v);
		Collections.swap(ll_t, 0, ll_t.size()-1);
		
		return res;
	}
	
	// assigns a given task to another vehicle
	public List<NextTasks> swap_task_vehicle(Vehicle v1, final List<Vehicle> vv2) {
		List<NextTasks> res_list = new LinkedList<NextTasks>();
		List<Task> l_t = new ArrayList<Task>(this.getCurrentTasks(v1));
		for(Task t1: l_t) {
			for(Vehicle v2: vv2) {
				NextTasks res = new NextTasks(this);
				 if(!v1.equals(v2))
				 	res.remove(v1, t1);
				 	res.add(v2, t1);
				 	res_list.add(res);
			}
		}
		return res_list;
	}
	
	// Same as above but for multiple tasks version
	public List<NextTasks> swap_action_vehicle(Vehicle v1, final List<Vehicle> vv2) {
		List<NextTasks> res_list = new LinkedList<NextTasks>();
		List<Task> l_t = new ArrayList<Task>(this.getCurrentTasks(v1));
		NextTasks res;
		for(Task t1: l_t) {
			for(Vehicle v2: vv2) {
				res = new NextTasks(this);
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
		Action p = taskActionMap.get(t).getFirst();
		Action d = taskActionMap.get(t).getLast();
		
		LinkedList<Action> new_action_list = nextAction.get(v);
		new_action_list.add(p);
		new_action_list.add(d);
		
		this.nextAction.put(v, new_action_list);
	}
	
	public void remove_actions(Vehicle v, Task t) {
		Action p = taskActionMap.get(t).getFirst();
		Action d = taskActionMap.get(t).getLast();
		
		LinkedList<Action> new_action_list = nextAction.get(v);
		new_action_list.remove(p);
		new_action_list.remove(d);
		
		this.nextAction.put(v, new_action_list);
	}
	public  HashMap<Vehicle, LinkedList<Task>> get_nextTask(){
		return this.nextTask;
	}
	
}
