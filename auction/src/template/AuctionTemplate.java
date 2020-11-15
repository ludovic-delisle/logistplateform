package template;

//the list of imports
import java.util.ArrayList;
import java.util.Arrays;
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
public class AuctionTemplate implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle; // A remplacer par une Liste de véhicules 
	private City currentCity;
	private State state;
	private List<Long> opponent_bids= new ArrayList<Long>();
	private List<Long> our_bids= new ArrayList<Long>();
	private List<Double> city_vals= new ArrayList<Double>();
	private List<Double> task_dists= new ArrayList<Double>();
	private Predictions pred = new Predictions();
	private int id;

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		this.topology = topology;
		this.distribution = distribution;

		this.agent = agent;
		//System.out.println("Ttttttttttt");
		this.vehicle = agent.vehicles().get(0);

		this.currentCity = vehicle.homeCity();
		//System.out.println("Ttttttttttt");

		this.state = State.MakeEmptyState(agent.vehicles().get(0), agent.getTasks());
		//System.out.println("Ttttttttttt");
		
		this.id=agent.id();
		
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
			
			vehicle.getCurrentTasks().add(previous);
			currentCity = previous.deliveryCity;
			System.out.println("Wiiiinnnneeer = " + agent.name());
			System.out.println(our_bids.get(our_bids.size()-1));
			System.out.println(opponent_bids.get(opponent_bids.size()-1));
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
		this.city_vals.add(dest_city_value);
		this.task_dists.add(task.pickupCity.distanceTo(task.deliveryCity));
		if(opponent_bids.size()>5) {
			double e_opponent_bid= 0.0;//pred.estimated_bid(opponent_bids, our_bids, task_dists, city_vals);
			if(id==0) {
				//System.out.println("estimated bid: " + e_opponent_bid);
			}
		}
		
		State startState = new State(vehicle, agent.getTasks());
		double marginalCost = Astar.marginalCost(startState, task, Heuristic.DISTANCE);
		

		double ratio = 1.0 + (random.nextDouble() * state.getBiddingFactor() * task.reward);
		double bid = marginalCost - dest_city_value;
		if(id==1) {
			bid= task.pickupCity.distanceTo(task.deliveryCity);
			//System.out.println("real bid: " + bid);
		}
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
