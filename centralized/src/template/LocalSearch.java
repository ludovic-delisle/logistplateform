package template;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
		while(System.currentTimeMillis() - startTime < 10000) {
			NextTasks candidate_solution = new NextTasks(solution);

			List<NextTasks> A = new ArrayList<NextTasks>(choose_neighbours(candidate_solution));
	    	System.out.println("gggggggggggggggggggAsize:  " + A.size());
			candidate_solution = local_choice(A);
	    	System.out.println("bbbbbbbbbbbbbb");

			if(cost(candidate_solution) < cost(solution)) {
				solution = candidate_solution;
			}
		}
		return solution;
	}
	
	public boolean checkConstraints(NextTasks nextTask) {
		for(Task t: availableTasks) {
			if(nextTask.get(t) != null 
			&& (nextTask.get(t) == t
			|| nextTask.getTime(t) + 1 != nextTask.getTime(nextTask.get(t)) 
			|| nextTask.getVehicle(nextTask.get(t)) != nextTask.getVehicle(t))) {
				return false;
			}	
		}
		System.out.println("rrrrrrrrrrrrrrrrrr    " + nextTask.size() + "  " +  availableTasks.size() + "   " + vehicles.size());
		if(nextTask.size() != availableTasks.size() + vehicles.size()) {
			System.out.println("faaaalse constraint");
			return false;
		}

		for(Vehicle v: vehicles) {
			double weight_sum = nextTask.getCurrentTasks(v).stream().collect(Collectors.summingInt(i -> i.weight));
			if(weight_sum > v.capacity()) {
				return false;
			}
		}
		System.out.println("constraints true");
		return true;
	}
	
	public Vehicle getVehicle(Task t) {
		for(Vehicle v: vehicles) {
			if(v.getCurrentTasks().contains(t))
				return v;
		}
		return null;
	}
	
	public NextTasks local_choice(List<NextTasks> task_list) {
		System.out.println("cooost:  " + task_list.size());
		for(NextTasks n: task_list) {
			System.out.println("cooost:  " + cost(n));
		}
		task_list.stream().collect(Collectors.minBy(Comparator.comparing(i->cost(i)))).get();
		return task_list.stream().collect(Collectors.minBy(Comparator.comparing(i->cost(i)))).get();
	}
	
	public List<NextTasks> choose_neighbours(NextTasks initialSol) {
		List<NextTasks> res_list = new ArrayList<NextTasks>();
		for(Vehicle v: vehicles) {
			System.out.println("looop v:  " + res_list.size());
			for(Task t: initialSol.getCurrentTasks(v)) {
				System.out.println("looop 1:  " + res_list.size());
				NextTasks n = initialSol.swap_task_order(v, t);
				System.out.println("check consta:  " + checkConstraints(n) + "   " + res_list.size());

				if(checkConstraints(n)) res_list.add(n);
				
				//for(Vehicle v2: vehicles) {
				//	NextTasks n2 = initialSol.swap_task_vehicle(t, v, v2);
				//	if(checkConstraints(n2)) res_list.add(n2);
				//}
			}
		}
		System.out.println("res siiize:  " + res_list.size());
		return res_list;
	}

	
	public double cost(NextTasks n) {
		Double total_cost = 0.0;
		for(Task t: availableTasks) {
			total_cost += (t.deliveryCity.distanceTo(n.get(t).pickupCity) + n.get(t).pathLength()) 
					* getVehicle(t).costPerKm();
		}	
		
		for(Vehicle v: vehicles) {
			total_cost += (v.getCurrentCity().distanceTo(n.getFirstTask(v).pickupCity)
					+ n.getFirstTask(v).pathLength()) * v.costPerKm();
		}
		return total_cost;
	}
}








