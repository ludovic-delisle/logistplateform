package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.task.Task;

public class LocalSearch {
	private List<Vehicle> vehicles;
	private NextTasks current_plan;
	private TaskSet availableTasks;
	
	public boolean checkConstraints(NextTasks nextTask) {
		
		for(Task t: availableTasks) {
			if(nextTask.get(t) == t
			|| nextTask.getTime(t) + 1 != nextTask.getTime(nextTask.get(t)) 
			|| getVehicle(nextTask.get(t)) != getVehicle(t)) {
				return false;
			}	
		}
		
		for(Vehicle v: vehicles) {
			if(nextTask.getTime(nextTask.get(v)) == 1
				|| getVehicle(nextTask.get(v)) != v) {
				return false;
			}
		}
		
		if(nextTask.size() != availableTasks.size() + vehicles.size()) {
			return false;
		}
		
		for(Vehicle v: vehicles) {
			if(v.getCurrentTasks().weightSum() > v.capacity()) {
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
	
	public List<NextTasks> choose_neighbours() {
		//petites mod pour créer slo nbh
		ArrayList<NextTasks> res_list = new ArrayList<NextTasks>();
		int i=0;
		//mettre condition d'arrèt, par ex quand le cost arrête de diminuer
		while(i < 10000) {
			++i;
			NextTasks n = new NextTasks(current_plan);
			solutionShuffle(n);
			if(checkConstraints(n)) {
				res_list.add(n);
			}
		}
		return res_list;
	}
	
	public void solutionShuffle(NextTasks n) {
		
	}
	
	public double cost(TaskSet tasks) {
		Double total_cost = 0.0;
		for(Task t: tasks) {
			total_cost += (t.deliveryCity.distanceTo(current_plan.get(t).pickupCity) + current_plan.get(t).pathLength()) * getVehicle(t).costPerKm();
		}	
		
		for(Vehicle v: vehicles) {
			total_cost += (v.getCurrentCity().distanceTo(current_plan.get(v).pickupCity) + current_plan.get(v).pathLength()) * v.costPerKm();
		}
		return total_cost;
	}
}
