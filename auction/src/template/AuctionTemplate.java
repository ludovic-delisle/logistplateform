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

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			vehicle.getCurrentTasks().add(previous);
			currentCity = previous.deliveryCity;
			System.out.println("Wiiiinnnneeer = " + winner);
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

		// Is this the right way to enforce capacity ???¿¿¿
		if (vehicle.capacity() < task.weight)
			return null;
		//System.out.println("Ttttttttttt1111111");
		State startState = new State(vehicle, agent.getTasks());
		//System.out.println("TttttttttttMarginal");

		double marginalCost = Astar.marginalCost(startState, task, Heuristic.DISTANCE);
		//System.out.println("Ttttttttttt");

		double ratio = 1.0 + (random.nextDouble() * state.getBiddingFactor() * task.reward);
		double bid = marginalCost + 
		
		//System.out.println("Task reward :" + task.reward + "  bidding price:  " + Math.round(bid) + "  marginalCost:  " + marginalCost);
		return (long) Math.round(bid);
	}
	
	
	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
		
		
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
