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
public class dummy_avg implements AuctionBehavior {

	Random rand=new Random(9);
	
	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	
	private List<Long> our_bids= new ArrayList<Long>();
	private List<Double> not_our_bids= new ArrayList<Double>();
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
	private Double avg_dist=0.0;
	

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
		int nb=0;
		double tot_d=0;
		for(City c : topology.cities()) {
			for(City n : c.neighbors()) {
				nb+=1;
				tot_d+= c.distanceTo(n);
			}
		}
		avg_dist=tot_d/nb;
		System.out.println(avg_dist);
		
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		
		if (winner == agent.id()) {
			our_bids.add(bids[winner]);
			tot_reward+=bids[winner];
		}
		else not_our_bids.add((double) bids[winner]);
		
	}
	
	
	
	@Override
	public Long askPrice(Task task) {
		Double avg=10000.0;
		if(not_our_bids.size()>0) avg = not_our_bids.stream().mapToDouble(Double::doubleValue).sum() / not_our_bids.size();
		
		return (long) Math.round(avg);
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
