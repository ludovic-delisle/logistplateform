package centralizedAlgo;


import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import action.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import logist.task.Task;

public class LocalSearch {
	private List<Vehicle> vehicles;
	private TaskSet availableTasks;
	Random rand = new Random(42);
	
	public LocalSearch(List<Vehicle> vehicles1, TaskSet availableTasks1){
		this.vehicles = vehicles1;
		this.availableTasks = availableTasks1;
	}
	
	/**
	 * Executes the Stochastic Local Search Algorithm with greedy descent and random restart
	 * @return The Set of tasks with the smallest cost (not optimal)
	 */	
	public NextTasks SLSAlgoConsolidation(long timeout, NextTasks solution, int n, int iter) {
		
		final long startTime = System.currentTimeMillis();
		NextTasks best_solution = new NextTasks(solution);
		int n_iter=0;
		int n_step=0; //current degree of neighbors of neighbors that we compute
		int n_step_threshold=n; //threshold above which we stop increasing n_steps and do a random restart
		//System.out.println(200);
		
		while(System.currentTimeMillis() - startTime < timeout) {
			//System.out.println(300);
			NextTasks candidate_solution = new NextTasks(solution);
			HashSet<NextTasks> sol_set = new HashSet<NextTasks>();
			sol_set.add(candidate_solution);
			Set<NextTasks> A = choose_neighbour_actions(sol_set);
			
			for(int i =0; i<n_step; i++) {
				//here we search for all the neighbors up to i transformations away from the initial solution
				A=choose_neighbour_actions(A);
			}
			//System.out.println(400);
			candidate_solution = local_choice_actions(A);
			//System.out.println(401);
			if(cost_action(candidate_solution) < cost_action(solution)) {
				n_step=0;
				//System.out.println(500);
				solution = candidate_solution;
				if(cost_action(candidate_solution) < cost_action(best_solution)) {
					//System.out.println("Best global cost: "+ cost_action(candidate_solution));
					best_solution=candidate_solution;
					
				}
			// if we exceeded a given amount of steps, we restart the search with another random initial guess (same seed)
			}else if(n_step>=n_step_threshold && n_iter<iter) {
				//System.out.println(401);
				solution=new NextTasks(vehicles, availableTasks, rand);
				n_step=0;
				n_iter+=1;
			// otherwise we just increase the steps counter
			}else if(n_step>=n_step_threshold) {
				return best_solution;
			}
			else n_step+=1;	
		}
		//System.out.println(600);
		return best_solution;
	}
	
	public NextTasks SLSAlgo(long timeout) {
		NextTasks solution = new NextTasks(vehicles, availableTasks, rand);
		NextTasks best_solution = new NextTasks(solution);

		
		int n_step=0; //current degree of neighbors of neighbors that we compute
		int n_step_threshold=1; //threshold above which we stop increasing n_steps and do a random restart
		final long startTime = System.currentTimeMillis();

		while(System.currentTimeMillis() - startTime < timeout) {

			NextTasks candidate_solution = new NextTasks(solution);
			HashSet<NextTasks> sol_set = new HashSet<NextTasks>();
			sol_set.add(candidate_solution);
			Set<NextTasks> A = choose_neighbour_actions(sol_set);
			
			for(int i =0; i<n_step; i++) {
				//here we search for all the neighbors up to i transformations away from the initial solution
				A=choose_neighbour_actions(A);
			}
			
			candidate_solution = local_choice_actions(A);
			
			if(cost_action(candidate_solution) < cost_action(solution)) {
				n_step=0;
				
				solution = candidate_solution;
				if(cost_action(candidate_solution) < cost_action(best_solution)) {
					//System.out.println("Best global cost: "+ cost_action(candidate_solution));
					best_solution=candidate_solution;
					
				}
			// if we exceeded a given amount of steps, we restart the search with another random initial guess (same seed)
			}else if(n_step>=n_step_threshold) {
				solution=new NextTasks(vehicles, availableTasks, rand);
				n_step=0;
			// otherwise we just increase the steps counter
			} else n_step+=1;	
		}
		return best_solution;
	}
	

	/**
	 * Same as above but for multiple tasks carried by a single vehicle
	 * @param task_list
	 * @return
	 */
	public NextTasks local_choice_actions(Set<NextTasks> task_list) {
		return task_list.stream().collect(Collectors.minBy(Comparator.comparing(i->cost_action(i)))).get();
	}
	
	
	/**
	 * Computes the cost of a plan
	 * @param n NextTasks object to be evaluated
	 * @return
	 */
	public double cost_action(NextTasks n) {
		Double total_cost = 0.0;
		
		for(Vehicle v: vehicles) {
			if(n.getCurrentTasks(v).size()!=0) {
				Task first_task = n.getFirstTask(v);
				City new_city;
				City old_city= first_task.pickupCity;
				double total_dist=0.0;
				for(Action a : n.getCurrentActions(v)) {
					new_city=a.city();
					total_dist+= new_city.distanceTo(old_city);
					old_city=new_city;
				}

				total_cost += total_dist*v.costPerKm();
			}
			
		}
		return total_cost;
	}
	
	/**
	 * Computes a set of all the neighboring plans that we can obtain with a single transformation
	 * @param one_step_Sol
	 * @return Set of neighboring NextTasks objects
	 */
	public Set<NextTasks> choose_neighbour_actions(Set<NextTasks> one_step_Sol) {
		Set<NextTasks> next_tasks_set = new HashSet<NextTasks>();
		for(NextTasks initialSol:one_step_Sol) {
			for(Vehicle v: vehicles) {
				final List<Action> l_t = new LinkedList<Action>(initialSol.getCurrentActions(v));
				for(Action a: l_t) {
					NextTasks n = new NextTasks(initialSol.swap_action_order(v, a));
					if(checkConstraints_actions(n)) next_tasks_set.add(n);	
				}
				List<NextTasks> swaped_vehicles_tasks = new LinkedList<NextTasks>(initialSol.swap_action_vehicle(v, vehicles));
				swaped_vehicles_tasks.stream().filter(i -> checkConstraints_actions(i));
				next_tasks_set.addAll(swaped_vehicles_tasks);
			}
		}
		return next_tasks_set;
	}
	
	/**
	 * Check if a proposal of NextTasks respects the constraints
	 * Note: This version allows a vehicle to carry multiple tasks at a time
	 * @param nextTask the NextTasks object to be evaluated
	 * @return true if the constraints are respected
	 */
	public boolean checkConstraints_actions(NextTasks nextTask) {
		if(nextTask.getSize() != availableTasks.size() + vehicles.size()) {
			return false;
		}
		
		for(Vehicle v: vehicles) {
			//check for capacity
			int w=0;
			for(int i = 0; i < nextTask.getCurrentActions(v).size()-1; i++) {
				Action a = nextTask.getCurrentActions(v).get(i);
				if(a.isDelivery()) {
					w-=a.task().weight;
				}
				else {
					w+=a.task().weight;
				}
				//dynamically check if the capacity of the given vehicle is never exceeded
				if(w>v.capacity()) {
					return false;
				}
				
				//check for right order
				for(int j = i+1; j < nextTask.getCurrentActions(v).size(); j++) {
					Action ac = nextTask.getCurrentActions(v).get(j);
					if(!a.right_order(ac)) {
						return false;
					}
				}
			}
		}	
		return true;
	}
	
	// Converts a NextTasks object to a plan
	public LinkedList<Plan> create_plan(NextTasks nt){
		LinkedList<Plan> plans = new LinkedList<Plan>();
		
		for(Vehicle v : vehicles) {
			
			City start_city=v.getCurrentCity();
			City current_city= v.getCurrentCity();
			Plan plan= new Plan(start_city);
			for(Action a: nt.getCurrentActions(v)) {
				
				while(current_city!=a.city()) {
					plan.appendMove(current_city.pathTo(a.city()).get(0));
					current_city=current_city.pathTo(a.city()).get(0);
				}
				
				plan.append(a.getAction());
				current_city=a.city();
			}
			//System.out.println(plan);
			plans.add(plan);
		}
		return plans;
	}
}







