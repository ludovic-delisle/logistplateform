package model;

import java.util.ArrayList;
import java.util.List;

import actions.Action;
import actions.Pickup;
import actions.Delivery;
import actions.Move;
import logist.plan.Plan;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import logist.simulation.Vehicle;
import logist.task.Task;

public class State {
	private Vehicle vehicle;
	private double current_cost=0;
	private TaskSet available_tasks;
	private TaskSet current_tasks=null;
	private int remainingCapacity;
	private boolean isFinalState=false;
	List<Action> previous_actions=null;
	
	public State(Vehicle vehicle, TaskSet tasks, boolean isFinalState1) {
		this.vehicle=vehicle;
		this.available_tasks=tasks;
		this.current_tasks=vehicle.getCurrentTasks();
		this.setRemainingCapacity(vehicle.capacity());
		this.isFinalState = isFinalState1;
	}
	
	public Plan toPlan() {
		List<Action> temp_act_list = this.getActionList();
		List<logist.plan.Action> act_list = new ArrayList<logist.plan.Action>();
		for(Action a : temp_act_list) {
			act_list.add(a.getResultingAction());
		}
		
		return new Plan(this.getCurrentCity(), act_list);
	}
	
	public List<State> getNextStates() {
		List<Action> act_list = new ArrayList<>(this.getPossibleActions());
		List<State> state_list = new ArrayList<State>();
		for(Action a : act_list) {
			state_list.add(a.getResultingState(this));
		}
		return state_list;
    }
	
	public List<Action> getPossibleActions() {

        List<Action> possibleActions = new ArrayList<>();
        List<City> moveActions = new ArrayList<>();
        
        //first we get all the cities where we could deliver a task
        for (Task task : current_tasks) {
            if (task.deliveryCity == this.getCurrentCity()) {
                possibleActions.add(new Delivery(task));
                return possibleActions;
            } else { 
            	// If we cannot deliver a task in that city, we can simply move to it
            	moveActions.add(this.getCurrentCity().pathTo(task.deliveryCity).get(0));
            }
        }

        for (Task task : available_tasks) {
            if (task.weight <= remainingCapacity) {
            	//then we get all the cities where we could pickup a task
                if (task.pickupCity == this.getCurrentCity()) { 
                    possibleActions.add(new Pickup(task));
                } else { 
                	// If we cannot deliver a task in that city, we can simply move to it
                    moveActions.add(this.getCurrentCity().pathTo(task.pickupCity).get(0));
                }
            }
        }

        // Finally, we create the move actions for all the remaining cities
        for (Topology.City city : moveActions) {
            possibleActions.add(new Move(this.getCurrentCity(), city, this.getCostKm()));
        }

        return possibleActions;
    }
	
	public City getCurrentCity(){
		return vehicle.getCurrentCity();
	}
	public TaskSet getTasks() {
		return available_tasks;
	}
	public TaskSet getCurrentTasks() {
		return current_tasks;
	}
	public double getCurrentCost() {
		return current_cost;
	}
	public void setCurrentCost(double cost) {
		this.current_cost = cost;
	}
	public Vehicle getVehicle() {
		return vehicle;
	}
	public boolean isFinalState() {
		return isFinalState;
	}
	public int getRemainingCapacity() {
		return remainingCapacity;
	}
	public void setRemainingCapacity(int remainingCapacity) {
		this.remainingCapacity = remainingCapacity;
	}
	public int getCostKm() {
		return this.vehicle.costPerKm();
	}
	public double getSpeed() {
		return this.vehicle.speed();
	}
	public void updateActionList(List<Action> act_list) {
		this.previous_actions = act_list;
	}
	public List<Action> getActionList() {
		return this.previous_actions;
	}
	
}
