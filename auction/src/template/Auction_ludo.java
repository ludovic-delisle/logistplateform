package template;

//the list of imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

import centralizedAlgo.LocalSearch;
import centralizedAlgo.NextTasks;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import optimalAlgo.Astar;
import optimalAlgo.Astar.Heuristic;
import optimalAlgo.State;
import centralizedAlgo.*;
import prediction.Predictions;


/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class Auction_ludo implements AuctionBehavior {

	Random rand=new Random(9);
	
	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private List<Vehicle> vehicles=new ArrayList<Vehicle>(); // A remplacer par une Liste de véhicules 
	private City currentCity;
	private State state;
	private List<Long> opponent_bids= new ArrayList<Long>();
	private List<Long> our_bids= new ArrayList<Long>();
	private List<Task> tasks = new ArrayList<Task>();
	private List<Double> tasks_dist = new ArrayList<Double>();
	private List<Task> my_tasks = new ArrayList<Task>();
	private List<Task> opponent_tasks = new ArrayList<Task>();
	private HashMap<Vehicle, TaskSet> vehicle_tasks_map=new HashMap<Vehicle, TaskSet>();
	private List<Double> city_vals = new ArrayList<Double>();
	private NextTasks current_sol;
	private NextTasks on_wait_sol;
	private Double tot_reward=0.0;
	private Double current_cost=0.0;
	private Double fiction_cost=0.0;
	private boolean give_to_one;
	private boolean first_win=true;
	private TaskSet new_task_list_1;
	private TaskSet new_task_list_2;
	private int SLS_limit=10;
	private double addaptive_coeff=0.99;
	private City opponent_start_city_guess;

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		this.topology = topology;
		this.distribution = distribution;

		this.agent = agent;
		//System.out.println("Ttttttttttt");
		this.vehicles.add(agent.vehicles().get(0));
		this.vehicles.add(agent.vehicles().get(1));
		this.currentCity = vehicles.get(0).homeCity();
		//System.out.println("Ttttttttttt");

		this.state = State.MakeEmptyState(agent.vehicles().get(0), agent.getTasks());
		//System.out.println("Ttttttttttt");
		
		this.vehicle_tasks_map.put(vehicles.get(0), null);
		
		
		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if(agent.id()==0) {
			opponent_bids.add(bids[1]);
			our_bids.add(bids[0]);
		}
		else {
			opponent_bids.add(bids[0]);
			our_bids.add(bids[1]);
		}
		
		if(tasks.size()==1) {
			opponent_start_city_guess=guess_opponent_city(opponent_bids.get(0), previous);
			System.out.println(opponent_start_city_guess.name);
		}
		
		if (winner == agent.id()) {
			if(first_win) {
				first_win=false;
				new_task_list_1 = TaskSet.noneOf(agent.getTasks());
				new_task_list_2 = TaskSet.noneOf(agent.getTasks());
			}
			
			HashMap<Vehicle, LinkedList<Task>> nt = on_wait_sol.get_nextTask();
			
			System.out.println("before " + new_task_list_1.size()+" "+new_task_list_2.size());
			System.out.println(nt.get(vehicles.get(0)).size());
			System.out.println(nt.get(vehicles.get(1)).size());
			if(nt.get(vehicles.get(0)).size()>new_task_list_1.size()) {
				new_task_list_1.add(previous);
				System.out.println("size of 1 increased");
			}
			else {
				new_task_list_2.add(previous);
				System.out.println("size of 2 increased");
			}
			System.out.println("after " + new_task_list_1.size()+" "+new_task_list_2.size());
			current_sol=on_wait_sol;
			tot_reward+=our_bids.get(our_bids.size()-1);
			current_cost+=fiction_cost;
			//vehicle_tasks_map=current_sol.get_nextTask();
			vehicles.get(0).getCurrentTasks().add(previous);
			currentCity = previous.deliveryCity;
			System.out.println("Wiiiinnnneeer = " + agent.name());
			System.out.println("winner bid " +our_bids.get(our_bids.size()-1));
			System.out.println("looser bid " +opponent_bids.get(opponent_bids.size()-1));
			//update biddingFactor depending on previous auctions results
			double avg = Arrays.stream(bids).mapToInt(i -> (int) i.intValue()).sum();
			
			// plafonner pour ne pas attendre 0 ou changer de signe
			// faire un ghangment proportionnel à la difff
			if(bids[winner] < avg) {
				state.updateBiddingFactor(-0.01);
			}else {
				state.updateBiddingFactor(+0.01);
			}
			if(my_tasks.size()>SLS_limit) {
				if(give_to_one) {
					new_task_list_1.add(previous);
					vehicle_tasks_map.put(vehicles.get(0),  new_task_list_1);
				}
				else {
					new_task_list_2.add(previous);
					vehicle_tasks_map.put(vehicles.get(1),  new_task_list_2);
				}
			}
		}
		else {
			on_wait_sol=current_sol;
			my_tasks.remove(my_tasks.size()-1);
			fiction_cost=current_cost;
		}
		
	}
	public City guess_opponent_city(double opponent_bid, Task task) {
		
		Double task_dist= task.pickupCity.distanceTo(task.deliveryCity);
		Double task_expenses = vehicles.get(0).costPerKm()*task_dist;
		Double cost_to_go_to_task = opponent_bid-task_expenses;
		
		City opponent_start_city = null;
		Double smallest_diff= Double.POSITIVE_INFINITY;
		
		for(City city : topology.cities()) {
			double diff = Math.abs(cost_to_go_to_task - vehicles.get(0).costPerKm()*task.pickupCity.distanceTo(city));
			
			if(diff<smallest_diff) {
				smallest_diff=diff;
				opponent_start_city=city;
			}
		}
		
		return opponent_start_city;
	}
	
	/*
	public Long askPriceTemplate(Task task) {

		if (vehicle.capacity() < task.weight)
			return null;

		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
		long distanceSum = distanceTask
				+ currentCity.distanceUnitsTo(task.pickupCity);
		double marginalCost = Measures.unitsToKM(distanceSum
				* vehicle.costPerKm());

		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
		double bid = ratio * marginalCost;

		return (long) Math.round(bid);
	}
	*/
	
	@Override
	public Long askPrice(Task task) {
		Predictions pred= new Predictions();
		
		double bid=0;
		Double dest_city_value = agent.readProperty("city_value_factor",  Double.class, 0.0)*(1 - distribution.probability(task.deliveryCity, null));
		city_vals.add(dest_city_value);
		tasks.add(task);
		tasks_dist.add(task.pickupCity.distanceTo(task.deliveryCity));
		
		double marginalCost=0.0;
		my_tasks.add(task);
		if(my_tasks.size()<=SLS_limit) {
			if(current_sol==null) {
				on_wait_sol = new NextTasks(vehicles, my_tasks , rand);
			}
			else {
				on_wait_sol = new NextTasks(on_wait_sol, task);
			}
			
			LocalSearch SLS = new LocalSearch(vehicles, my_tasks);
			
			on_wait_sol = SLS.SLSAlgo_no_random(3000, on_wait_sol, 2, 5);
			LinkedList<Plan> plans = SLS.create_plan(on_wait_sol);
			
			
			for(int i=0 ; i< plans.size(); i++) {
				marginalCost+=vehicles.get(i).costPerKm()*plans.get(i).totalDistance();
				
			}
			fiction_cost = marginalCost-current_cost;
			
			bid =fiction_cost;
		}
		
		else if(my_tasks.size()>SLS_limit) {
			
			TaskSet tasks_1= TaskSet.copyOf(new_task_list_1);
			tasks_1.add(task);
			State startState = new State(vehicles.get(0), tasks_1);
			Double marginalCost_1 = Astar.marginalCost(startState, task, Heuristic.DISTANCE);
			
			TaskSet tasks_2= TaskSet.copyOf(new_task_list_2);
			tasks_2.add(task);
			State startState_2 = new State(vehicles.get(1), tasks_2);
			Double marginalCost_2 = Astar.marginalCost(startState_2, task, Heuristic.DISTANCE);
			
			if(marginalCost_1<marginalCost_2) {
				bid=marginalCost_1*addaptive_coeff;
				give_to_one=true;
			}
			else {
				bid=marginalCost_2*addaptive_coeff;
				give_to_one=false;
			}
			
		}
	

		return (long) Math.round(bid);
	}
	
	
	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAH!");
		
		if(tasks.size()==0) {
			System.out.println("AH!");
			List<Plan> p = new ArrayList<Plan>();
			for(int i=0; i<vehicles.size(); i++) {
				p.add(Plan.EMPTY);
			}
			return p;
		}
		
		System.out.println("Agent " + agent.name() + " has "+ + tasks.size() +" tasks ");
		
		
		LocalSearch SLS = new LocalSearch(vehicles, tasks);
		System.out.println("Agent ok construction" + vehicles.size());
        NextTasks final_solution = SLS.SLSAlgo();
        
        System.out.println("SLS A march�");
        List<Plan> plans = SLS.create_plan(final_solution);
        System.out.println("Plans prets");
        
		return plans;
	}

}
