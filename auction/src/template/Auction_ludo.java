package template;

//the list of imports
import java.util.ArrayList;
import java.util.Arrays;
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
	private List<Task> my_tasks = new ArrayList<Task>();
	private List<Task> my_maybe_tasks = new ArrayList<Task>();
	private NextTasks current_sol;
	private NextTasks on_wait_sol;
	private Double tot_reward=0.0;
	private Double current_cost=0.0;
	private Double fiction_cost=0.0;

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
		
		if (winner == agent.id()) {
			System.out.print(bids[winner]);
			current_sol=on_wait_sol;
			tot_reward+=our_bids.get(our_bids.size()-1);
			current_cost=fiction_cost;
			
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
		}
		else {
			on_wait_sol=current_sol;
			my_tasks.remove(my_tasks.size()-1);
			fiction_cost=current_cost;
		}
		
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
		Double dest_city_value = agent.readProperty("city_value_factor",  Double.class, 0.0)*(1 - distribution.probability(task.deliveryCity, null));
		tasks.add(task);
		
		my_tasks.add(task);
		
		if(current_sol==null) {
			on_wait_sol = new NextTasks(vehicles, my_tasks , rand);
		}
		else {
			on_wait_sol = new NextTasks(on_wait_sol, task);
		}
		
		LocalSearch SLS = new LocalSearch(vehicles, my_tasks);
		
		on_wait_sol = SLS.SLSAlgo_no_random(3000, on_wait_sol, 3);
		System.out.println("not here = ");
		LinkedList<Plan> plans = SLS.create_plan(on_wait_sol);
		double marginalCost=0.0;
		
		for(int i=0 ; i< plans.size(); i++) {
			marginalCost+=vehicles.get(i).costPerKm()*plans.get(i).totalDistance();
			
		}
		fiction_cost=marginalCost;
		//State startState = new State(vehicles.get(0), agent.getTasks());
		//double marginalCost = Astar.marginalCost(startState, task, Heuristic.DISTANCE);
		

		double ratio = 1.0 + (random.nextDouble() * state.getBiddingFactor() * task.reward);
		double bid = fiction_cost-tot_reward;
		
		return (long) Math.round(bid);
	}
	
	
	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		System.out.println("Agent " + agent.name() + " has "+ + tasks.size() +" tasks ");
		
		
		LocalSearch SLS = new LocalSearch(vehicles, tasks);
		System.out.println("Agent ok construction" + vehicles.size());
        NextTasks final_solution = SLS.SLSAlgo();
        
        System.out.println("SLS A marché");
        List<Plan> plans = SLS.create_plan(final_solution);
        System.out.println("Plans préetsxs");
        
		return plans;
	}

	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);
			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}
}
