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
public class Auction_jerome_reglin implements AuctionBehavior {

	Random rand=new Random(9);
	
	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	
	private List<Long> our_bids= new ArrayList<Long>();
	private List<ArrayList<Long>> bids_table= new ArrayList<ArrayList<Long>>();
	private List<ArrayList<Long>> estimate_table= new ArrayList<ArrayList<Long>>();
	private List<Boolean> estimatable_with_reg = new ArrayList<Boolean>();
	private Boolean all_estimables=true;
	private List<Long> estimate_avg_inacuracy = new ArrayList<Long>();
	
	private NextTasks current_sol;
	private NextTasks on_wait_sol;
	
	private List<Double> tasks_costs = new ArrayList<Double>();
	private Double tot_reward=0.0;
	private Double current_cost=0.0;
	private Double newCost=0.0;
	private boolean first_win=true;
	private boolean first_bid=true;
	double expected_profit = 1.0;
	Double last_bid=0.0;
	
	private int vehicle_index;
	private int nb_successive_losses=0;
	
	private int SLS_limit=5;
	private double addaptive_coeff=0.99;
	private long timeoutSetup, timeoutBid, timeoutPlan;
	private int use_reglin=0; // 0 = not initialised, 1 = will not be used, 2 = is used

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
				estimate_table.add(new ArrayList<Long>());
				estimate_table.get(i).add(null);
				estimate_table.get(i).add(null);
				estimatable_with_reg.add(false);
				estimate_avg_inacuracy.add((long) 0.0);
			}
		}
		// add bids to data struct
		for(int i=0; i<bids.length; i++) {
			bids_table.get(i).add(bids[i]);
			if(agent.id() == i) {
				our_bids.add(bids[i]);
			}else System.out.println("true bid "+bids[i]);
		}
		// in case of win
		if (winner == agent.id()) {
			agent.getTasks().add(previous);
			nb_successive_losses=0;
			HashMap<Vehicle, LinkedList<Task>> nt = on_wait_sol.get_nextTask();
			current_cost = newCost;
			// current sol becomes the hypotheticalWinSol
			current_sol=on_wait_sol;
			tot_reward+=our_bids.get(our_bids.size()-1);
			
			
			System.out.println("Winner = " + agent.name());
			System.out.println("winner bid " + bids[winner]);
			expected_profit = expected_profit*1.2 + 10;
			addaptive_coeff = min(1.7, addaptive_coeff+0.1);
			//update biddingFactor depending on previous opponent bids		
			
		} else {
			nb_successive_losses+=1;
			on_wait_sol=current_sol;
			int[] opponentBids = IntStream.range(0, bids.length) 
		            .filter(i -> i != agent.id()) 
		            .map(i -> bids[i].intValue()) 
		            .toArray(); 
			double avg = Arrays.stream(opponentBids).sum() / opponentBids.length;
			expected_profit = expected_profit * avg / (bids[agent.id()] * nb_successive_losses * nb_successive_losses + 1);
			addaptive_coeff = max(0.7, addaptive_coeff-0.1);
		}
		System.out.println("Expected_profit  " + expected_profit + "  addaptive_coeff" + addaptive_coeff);
	}
	private double min(double d, double e) {
		if(d<e) return d;
		else return e;
	}
	
	private double max(double d, double e) {
		if(d>e) return d;
		else return e;
	}
	
	
	@Override
	public Long askPrice(Task task) {
		final long startTime = System.currentTimeMillis();
		
		tasks_costs.add(task.pathLength()*agent.vehicles().get(0).costPerKm());
		
		double bid=0;
		double dest_city_value = (1 - distribution.probability(task.deliveryCity, null)); // proba that there is another task to pickup in the delivery city of the current task
		double marginalCost=Integer.MAX_VALUE;
		
		double astarMarginalCost = AstarEstimation(task);
		
		
		final long remaingTime = timeoutBid - (System.currentTimeMillis() - startTime);
		// If we do not have much time, we just use Astar
		if(remaingTime < 3000) return (long) Math.round(astarMarginalCost +expected_profit * dest_city_value * 2);
		
		else if (use_reglin == 0 || use_reglin == 1) { // if reglin not initialised or not being used
			double marginalCostSLS = SLSEstimation(task, remaingTime);
			//if we have a lot of tasks, SLS takes longer to reach it's optimal solution, so we anticipate this and slightly reduce it's estimation
			if(our_bids.size()>4) marginalCost = 0.8*marginalCostSLS + 0.2*marginalCostSLS/(1+our_bids.size());
			else marginalCost = marginalCostSLS;
			//If the marginal cost is negative, wed divide it by 2 in order to mitigate our losses
			if(marginalCost < 0) bid = marginalCost/2 +expected_profit * dest_city_value;
			else bid = marginalCost + expected_profit * dest_city_value;
			last_bid=bid;
		}	
		if(use_reglin == 0 || use_reglin == 2) {
			//If we have guessed correctly with reglin for 3 out of the 5 first rounds -> we use reg_lin
			if(our_bids.size()>2) {
				
				Predictions p=new Predictions();
				for(int i=0; i<bids_table.size(); i++) {
					ArrayList<Long> o=bids_table.get(i);
					estimate_table.get(i).add((long) Math.round(p.estimated_bid(o, our_bids, bid)));
					
				}
				if(bids_table.get(0).size()>4 ) {
					double smallest_bid= Integer.MAX_VALUE;
					for(int i=0; i<estimatable_with_reg.size(); i++) {
						if(estimatable_with_reg.get(i) && i!=agent.id()) {
							long e = estimate_table.get(i).get( estimate_table.get(i).size()-1);
							long e_safe = e-estimate_avg_inacuracy.get(i);
							if(e_safe<smallest_bid)smallest_bid=e_safe;
							if((bid > e_safe && e_safe>marginalCost) || (bid<e_safe && e_safe==smallest_bid)) {
								bid=0.95*e_safe;
								System.out.println("Reglin estimate   "+ e_safe); 
							}
						}
					}
				}
				
			}
		}
		if(our_bids.size() > 2 && bids_table.get(0).size() == 5) 
			if(check_reg()) use_reglin = 2;
			else use_reglin = 1;
		return (long) Math.round(bid);
	}
	
	public double AstarEstimation(Task task) { 
		double astarMarginalCost = Integer.MAX_VALUE;
		double current_marge_cost = Integer.MAX_VALUE;
		for(int i=0; i<agent.vehicles().size(); i++) {
			State startState= new State(agent.vehicles().get(i), agent.getTasks());
			current_marge_cost = Astar.marginalCost(startState, task, Heuristic.DISTANCE);
			if(current_marge_cost<astarMarginalCost) {
				astarMarginalCost=current_marge_cost;
				this.vehicle_index=i;
			}
		}
	return astarMarginalCost;
	}	
	
	public double SLSEstimation(Task task, long RemaingTime) {
		TaskSet hypotheticalWinTaskSet = agent.getTasks().clone();
		hypotheticalWinTaskSet.add(task);
		if(current_sol==null) {
			on_wait_sol = new NextTasks(agent.vehicles(), hypotheticalWinTaskSet , rand);
		} else {
			on_wait_sol = new NextTasks(on_wait_sol, task);
		}
		
		LocalSearch SLS = new LocalSearch(agent.vehicles(), hypotheticalWinTaskSet);
		long SLSTime = RemaingTime - 6000;
		if(use_reglin == 1) SLSTime = RemaingTime - 2000;
		
		on_wait_sol = SLS.SLSAlgoConsolidation(SLSTime, on_wait_sol, 1, 10);
		LinkedList<Plan> plans = SLS.create_plan(on_wait_sol);
		newCost=0.0;
		for(int i=0 ; i< plans.size(); i++) {
			newCost+=agent.vehicles().get(i).costPerKm()*plans.get(i).totalDistance();
		}
		return newCost-current_cost;
	}
	
	
	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		
		System.out.println("Agent " + agent.name() + " has "+ tasks.size() +" tasks ");
		if(tasks.size() <= 0) return emptyPlans();
		else {
			NextTasks startingPoint = new NextTasks(vehicles, tasks);
			LocalSearch SLS = new LocalSearch(vehicles, tasks);
	        NextTasks final_solution = SLS.SLSAlgo(timeoutPlan - 1000);
	        
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
	public void printm( List<ArrayList<Long>> arr) {
		for (int row = 0; row < arr.size(); row++)//Cycles through rows
		{
		  for (int col = 0; col < arr.get(row).size(); col++)//Cycles through columns
		  {
		    System.out.print(" " + arr.get(row).get(col)); //change the %5d to however much space you want
		  }
		  System.out.println(); //Makes a new row
		}
	}
	
	public List<Plan> emptyPlans() {
		List<Plan> res = new ArrayList<Plan>();
		for(int i=0; i < agent.vehicles().size(); ++i) {
			res.add(Plan.EMPTY);
		}
		return res;
	}
	
	private boolean check_reg(){
		int count_correct_estimates = 0;
		all_estimables=true;
		for(int i=0; i< bids_table.size(); i++) {
			Double diff=0.0;
			for(int j=bids_table.get(i).size()-3; j< bids_table.get(i).size(); j++) {
				diff+=Math.abs(bids_table.get(i).get(j)-estimate_table.get(i).get(j))/3;
				estimate_avg_inacuracy.set(i, (bids_table.get(i).get(j)-estimate_table.get(i).get(j))/3);
			}
			if(diff<300) {	
				++count_correct_estimates;
				estimatable_with_reg.set(i, true);
			} else {
				if(agent.id()!=i) all_estimables=false;
				estimatable_with_reg.set(i, false);
			}
		}
		return count_correct_estimates >= 3;
	}
	private TaskSet task_list_to_set(List<Task> task_list) {
		TaskSet t = TaskSet.noneOf(agent.getTasks());
		for(Task ta: task_list) {
			t.add(ta);
		}
		return t;
	}

}
