package template;

import java.io.File;
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
import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.config.Parsers;
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
public class Auction_rendu implements AuctionBehavior {

	Random rand=new Random(9);
	
	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	
	private List<Vehicle> vehicles=new ArrayList<Vehicle>(); // A remplacer par une Liste de véhicules 
	private List<Long> our_bids= new ArrayList<Long>();
	private List<ArrayList<Long>> bids_table= new ArrayList<ArrayList<Long>>();
	private List<Task> tasks = new ArrayList<Task>();
	private List<Task> my_tasks = new ArrayList<Task>();
	private List<TaskSet> vehicle_tasks=new ArrayList<TaskSet>();
	
	private NextTasks current_sol;
	private NextTasks on_wait_sol;
	
	
	private Double tot_reward=0.0;
	private Double current_cost=0.0;
	private Double fiction_cost=0.0;
	private boolean first_win=true;
	private boolean first_bid=true;
	
	private int vehicle_index;
	
	private int SLS_limit=5;
	private double addaptive_coeff=0.99;
	private long timeoutSetup, timeoutBid, timeoutPlan;
	

	@Override
	public void setup(Topology topology, TaskDistribution distribution, Agent agent) {
		LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_auction.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        timeoutSetup = ls.get(LogistSettings.TimeoutKey.SETUP);
        timeoutBid = ls.get(LogistSettings.TimeoutKey.BID);
        timeoutPlan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
		this.topology = topology;
		this.distribution = distribution;

		this.agent = agent;
		this.vehicles.addAll(agent.vehicles());
		
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		
		if(first_bid) {
			first_bid=false;
			for(int i=0; i<bids.length; i++) {
				bids_table.add(new ArrayList<Long>());
			}
		}
		for(int i=0; i<bids.length; i++) {
			bids_table.get(i).add(bids[i]);
			if(agent.id() == i) {
				our_bids.add(bids[i]);
			}
		}
		
		if (winner == agent.id()) {
			SLS_limit=8-vehicles.size();
			if(first_win) {
				first_win=false;
				for(int i=0; i<vehicles.size() ; i++) {
					vehicle_tasks.add(TaskSet.noneOf(agent.getTasks()));
				}
			}
			HashMap<Vehicle, LinkedList<Task>> nt = on_wait_sol.get_nextTask();
			
			if(my_tasks.size()>SLS_limit) {
				vehicle_tasks.get(vehicle_index).add(previous);
			}else {
				for(int i=0; i<vehicles.size(); i++) {
					if(nt.get(vehicles.get(i)).size()>vehicle_tasks.get(i).size()) {
						vehicle_tasks.get(i).add(previous);
						break;
					}
				}
			}
			System.out.println(4);
			current_sol=on_wait_sol;
			
			tot_reward+=our_bids.get(our_bids.size()-1);
			
			current_cost+=fiction_cost;
			
			
			System.out.println("Winner = " + agent.name());
			System.out.println("bids " + bids[0] + "  " + bids[1]);
			//update biddingFactor depending on previous auctions results
			double avg = Arrays.stream(bids).mapToInt(i -> (int) i.intValue()).sum();
			System.out.println(5);
			
		}
		else {
			on_wait_sol=current_sol;
			my_tasks.remove(my_tasks.size()-1);
			fiction_cost=current_cost;
		}
		
	}
	
	
	
	@Override
	public Long askPrice(Task task) {
		final long startTime = System.currentTimeMillis();
		double bid=0;
		Double dest_city_value = agent.readProperty("city_value_factor",  Double.class, 0.0)*(1 - distribution.probability(task.deliveryCity, null));
		TaskSet hypotheticalWinTaskSet = agent.getTasks().clone();
		hypotheticalWinTaskSet.add(task);
		tasks.add(task);
		
		double marginalCost=0.0;
		my_tasks.add(task);
		if(my_tasks.size()<=SLS_limit) {
			if(current_sol==null) {
				on_wait_sol = new NextTasks(vehicles, hypotheticalWinTaskSet , rand);
			}
			else {
				on_wait_sol = new NextTasks(on_wait_sol, task);
			}
			LocalSearch SLS = new LocalSearch(vehicles, hypotheticalWinTaskSet);
			int n_steps = 7 - my_tasks.size();
			on_wait_sol = SLS.SLSAlgoConsolidation(timeoutBid - startTime - 1000, on_wait_sol, n_steps, 10);
			LinkedList<Plan> plans = SLS.create_plan(on_wait_sol);
			
			for(int i=0 ; i< plans.size(); i++) {
				marginalCost+=vehicles.get(i).costPerKm()*plans.get(i).totalDistance();
				
			}
			fiction_cost = marginalCost-current_cost;
			
			bid =fiction_cost;
			
		}else if(my_tasks.size()>SLS_limit) {
			
			double best_marge_cost=99999999;
			double current_marge_cost;
			
			for(int i=0; i<vehicles.size(); i++) {
				TaskSet ts= TaskSet.copyOf(vehicle_tasks.get(i));
				//ts.add(task);
				State startState= new State(vehicles.get(i), ts);
				current_marge_cost = Astar.marginalCost(startState, task, Heuristic.DISTANCE);
				if(current_marge_cost<best_marge_cost) {
					best_marge_cost=current_marge_cost;
					this.vehicle_index=i;
					
				}
			}
			bid=best_marge_cost*addaptive_coeff;
					
		}
	
		return (long) Math.round(bid);
	}
	
	
	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		System.out.println("Agent " + agent.name() + " has "+ + tasks.size() +" tasks ");
		if(tasks.size() <= 0) return emptyPlans();
		else {
		
			LocalSearch SLS = new LocalSearch(vehicles, tasks);
			System.out.println("Agent ok construction" + vehicles.size());
	        NextTasks final_solution = SLS.SLSAlgo(timeoutPlan - 1000);
	        
	        System.out.println("SLS A marché");
	        List<Plan> plans = SLS.create_plan(final_solution);
	        System.out.println("Plans préetsxs");
	        
			return plans;
		}
	}

	public List<Plan> emptyPlans() {
		List<Plan> res = new ArrayList<Plan>();
		for(int i=0; i < agent.vehicles().size(); ++i) {
			res.add(Plan.EMPTY);
		}
		return res;
	}
}
