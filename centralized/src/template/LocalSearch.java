package template;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.task.Task;

public class LocalSearch {
	private List<Vehicle> vehicles;
	private TaskSet availableTasks;
	
	LocalSearch(List<Vehicle> vehicles1, TaskSet availableTasks1){
		this.vehicles = vehicles1;
		this.availableTasks = availableTasks1;
	}
	
	public NextTasks SLSAlgo() {

		NextTasks solution = new NextTasks(vehicles, availableTasks);
		

		final long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - startTime < 50000) {
			NextTasks candidate_solution = new NextTasks(solution);
			//System.out.println(candidate_solution.getSize());
			Set<NextTasks> A = choose_neighbours(candidate_solution);
			
			candidate_solution = local_choice(A);
			
			
			if(cost(candidate_solution) < cost(solution)) {
				
				solution = candidate_solution;
			}
			System.out.println(candidate_solution.getSize());
		}
		return solution;
	}
	
	public boolean checkConstraints(NextTasks nextTask) {
		if(nextTask.getSize() != availableTasks.size() + vehicles.size()) {
			
			
			return false;
		}
		
		for(Vehicle v: vehicles) {
			for(Task t: nextTask.getCurrentTasks(v)) {
				if((nextTask.get(v, t) != null && t != null) && (nextTask.get(v, t) == t)) {
					
					return false;
				}	
			}
		
			double weight_sum = nextTask.getCurrentTasks(v).stream().collect(Collectors.summingInt(i -> i.weight));
			if(weight_sum > v.capacity()) {
				
				return false;
			}
		}
		
		return true;
	}
	
	public Vehicle getVehicle(Task t) {
		for(Vehicle v: vehicles) {
			if(v.getCurrentTasks().contains(t))
				return v;
		}
		return null;
	}
	
	public NextTasks local_choice(Set<NextTasks> task_list) {
		return task_list.stream().collect(Collectors.minBy(Comparator.comparing(i->cost(i)))).get();
	}
	
	public Set<NextTasks> choose_neighbours(NextTasks initialSol) {
		Set<NextTasks> next_tasks_set = new HashSet<NextTasks>();
		for(Vehicle v: vehicles) {
			
			final List<Task> l_t = new LinkedList<Task>(initialSol.getCurrentTasks(v));
			for(Task t: l_t) {
				NextTasks n = new NextTasks(initialSol.swap_task_order(v, t));
				NextTasks n2 = new NextTasks(initialSol.swap_task_order_2(v, t));
				NextTasks n3 = new NextTasks(initialSol.swap_task_order_3(v));
				
				if(checkConstraints(n)) next_tasks_set.add(n);
				if(checkConstraints(n2)) next_tasks_set.add(n2);
				if(checkConstraints(n2)) next_tasks_set.add(n3);
			}
			
			List<NextTasks> swaped_vehicles_tasks = new LinkedList<NextTasks>(initialSol.swap_task_vehicle(v, vehicles));
			swaped_vehicles_tasks.stream().filter(i -> checkConstraints(i));
			next_tasks_set.addAll(swaped_vehicles_tasks);
		}

		return next_tasks_set;
	}

	
	public double cost(NextTasks n) {
		Double total_cost = 0.0;
		
		for(Vehicle v: vehicles) {
			// dist to pickup the task
			Task first_task = n.getFirstTask(v);
			if(first_task != null) total_cost += v.getCurrentCity().distanceTo(n.getFirstTask(v).pickupCity) * v.costPerKm();
			// dist to complete the remaining tasks
			List<Task> ll_t = n.getCurrentTasks(v);
			for(int i=0; i < ll_t.size()-1; ++i) {
				Task t1 = ll_t.get(i);
				Task t2 = ll_t.get(i+1);
				total_cost += t1.pathLength() * v.costPerKm();
				total_cost += t1.deliveryCity.distanceTo(t2.pickupCity) * v.costPerKm();
			}
			if(ll_t.size() > 0) total_cost += ll_t.get(ll_t.size()-1).pathLength() * v.costPerKm();
			
		}
		return total_cost;
	}
}








