package template;

import java.io.File;
//the list of imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
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
public class dummy_no_profit implements AuctionBehavior {

	Random rand=new Random(9);
	
	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	
	private List<Long> our_bids= new ArrayList<Long>();
	private List<ArrayList<Long>> bids_table= new ArrayList<ArrayList<Long>>();
	
	private NextTasks current_sol;
	private NextTasks on_wait_sol;
	
	private Double tot_reward=0.0;
	private Double current_cost=0.0;
	private Double newCost=0.0;
	private boolean first_win=true;
	private boolean first_bid=true;
	double expected_profit = 1000.0;
	private double last_bid = 0.0;
	
	private int vehicle_index;
	private int nb_successive_losses=0;
	
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
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		// int the data struct that stores the bid history
		if(first_bid) {
			first_bid=false;
			for(int i=0; i<bids.length; i++) {
				bids_table.add(new ArrayList<Long>());
			}
		}
		// add bids to data struct
		for(int i=0; i<bids.length; i++) {
			bids_table.get(i).add(bids[i]);
			if(agent.id() == i) {
				our_bids.add(bids[i]);
			}
		}
		// in case of win
		if (winner == agent.id()) {
			nb_successive_losses=0;
			HashMap<Vehicle, LinkedList<Task>> nt = on_wait_sol.get_nextTask();
			current_cost = newCost;
			// current sol becomes the hypotheticalWinSol
			current_sol=on_wait_sol;
			tot_reward+=our_bids.get(our_bids.size()-1);
			
			expected_profit = expected_profit*1.2 + 100;
			//update biddingFactor depending on previous opponent bids		
			
		} else {
			nb_successive_losses+=1;
			on_wait_sol=current_sol;
			int[] opponentBids = IntStream.range(0, bids.length) 
		            .filter(i -> i != agent.id()) 
		            .map(i -> bids[i].intValue()) 
		            .toArray(); 
			double avg = Arrays.stream(opponentBids).sum() / opponentBids.length;
			
			expected_profit = expected_profit * avg / (bids[agent.id()] * nb_successive_losses + 1);
			
		}
	}
	
	
	
	@Override
	public Long askPrice(Task task) {
		final long startTime = System.currentTimeMillis();
		double bid=0;
		double dest_city_value = (1 - distribution.probability(task.deliveryCity, null)); // proba that there is another task to pickup in the delivery city of the current task
		double marginalCost=Integer.MAX_VALUE;
		double astarMarginalCost = Integer.MAX_VALUE;
		double current_marge_cost=0.0;
		
		
		for(int i=0; i<agent.vehicles().size(); i++) {
			State startState= new State(agent.vehicles().get(i), agent.vehicles().get(i).getCurrentTasks().clone());
			current_marge_cost = Astar.marginalCost(startState, task, Heuristic.DISTANCE);
			if(current_marge_cost<astarMarginalCost) {
				astarMarginalCost=current_marge_cost;
				this.vehicle_index=i;
			}
		}
		//System.out.println(8);
		final long RemaingTime = timeoutBid - (System.currentTimeMillis() - startTime);
		if(RemaingTime < 2000) return (long) Math.round(astarMarginalCost + expected_profit);
		else {
			TaskSet hypotheticalWinTaskSet = agent.getTasks().clone();
			hypotheticalWinTaskSet.add(task);
			if(current_sol==null) {
				on_wait_sol = new NextTasks(agent.vehicles(), hypotheticalWinTaskSet , rand);
			} else {
				on_wait_sol = new NextTasks(on_wait_sol, task);
			}
			
			//System.out.println(10);
			LocalSearch SLS = new LocalSearch(agent.vehicles(), hypotheticalWinTaskSet);
			//System.out.println(11);
			on_wait_sol = SLS.SLSAlgoConsolidation(RemaingTime - 3000, on_wait_sol, 1, 10);
			//System.out.println(12);
			LinkedList<Plan> plans = SLS.create_plan(on_wait_sol);
			//System.out.println(13);
			newCost=0.0;
			for(int i=0 ; i< plans.size(); i++) {
				newCost+=agent.vehicles().get(i).costPerKm()*plans.get(i).totalDistance();
			}
			double marginalCostSLS = newCost-current_cost;
			
			//In case of win the newCost becomes the current cost
			marginalCost = (marginalCostSLS * RemaingTime + astarMarginalCost * (timeoutBid - RemaingTime)) / timeoutBid; // weighted avg
			if(marginalCost < 0) bid = 0;
			else bid = marginalCost;
			//System.out.println(11);
			last_bid=bid;
			return (long) Math.round(bid);
		}
	}
	
	
	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		System.out.println("Agent " + agent.name() + " has "+ tasks.size() +" tasks ");
		if(tasks.size() <= 0) return emptyPlans();
		else {
			NextTasks startingPoint = new NextTasks(vehicles, tasks);
			LocalSearch SLS = new LocalSearch(vehicles, tasks);
			System.out.println("SLS object constructed for " + vehicles.size() + " vehicles");
	        NextTasks final_solution = SLS.SLSAlgo(20000);
	        
	        System.out.println("SLS finished");
	        List<Plan> plans = SLS.create_plan(final_solution);
	        double pl =0.0;
	        for(Plan pli : plans) {
	        	pl+= pli.totalDistance()*vehicles.get(0).costPerKm();
	        }
	        if(tasks.size()>our_bids.size()) {
	        	tot_reward+=last_bid;
	        }
	        System.out.println("Plans created");
	        System.out.println("bids sum = " + tot_reward + "total cost = "+ pl + " profit = "+ (tot_reward-pl));
	        
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
