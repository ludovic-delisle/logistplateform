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

public class State implements Comparable<State>{
	private Vehicle vehicle;
	private double current_cost=0;
	private TaskSet available_tasks;
	private TaskSet current_tasks=null;
	private int remainingCapacity;
	private City current_city;
	List<Action> previous_actions=new ArrayList<Action>();
	
	public State(Vehicle vehicle, double current_cost, TaskSet available_tasks, TaskSet current_tasks, int remainingCapacity,
			List<Action> previous_actions) {
		this.vehicle = vehicle;
		this.current_cost = current_cost;
		this.available_tasks = available_tasks;
		this.current_tasks = current_tasks;
		this.remainingCapacity = remainingCapacity;
		this.previous_actions = previous_actions;
	}
	
	//constructor initial state
	public State(Vehicle vehicle, TaskSet tasks) {
			this.vehicle=vehicle;
			this.available_tasks=tasks;
			this.current_tasks=vehicle.getCurrentTasks();
			this.setRemainingCapacity(vehicle.capacity());
			this.current_city=vehicle.getCurrentCity();
			this.previous_actions = new ArrayList<>();
	}
	
	//constructor for action move
	public State(City city, State old_state,Double cost, List<Action> actions) {
			this.vehicle=old_state.getVehicle();
			this.available_tasks=old_state.getTasks();
			this.current_tasks= old_state.getCurrentTasks();
			this.setRemainingCapacity(vehicle.capacity());
			this.current_city=city;
			this.current_cost+=cost;
			this.previous_actions=actions;
	}
		
	//constructor for action pickup
	public State(State old_state, TaskSet available_tasks, TaskSet current_tasks, Integer weight_to_add, List<Action> actions) {
			this.vehicle=old_state.getVehicle();
			this.available_tasks=available_tasks;
			this.current_tasks= current_tasks;
			this.setRemainingCapacity(vehicle.capacity()+ weight_to_add);
			this.current_city=old_state.getCurrentCity();
			this.previous_actions=actions;
			this.current_cost=old_state.getCurrentCost();
	}
		
		//constructor for action delivery
	public State(State old_state, TaskSet current_tasks, Integer weight_to_drop, List<Action> actions) {
			this.vehicle=old_state.getVehicle();
			this.available_tasks=old_state.getTasks();
			this.current_tasks= current_tasks;
			this.setRemainingCapacity(vehicle.capacity() - weight_to_drop);
			this.current_city=old_state.getCurrentCity();
			this.previous_actions=actions;
			this.current_cost=old_state.getCurrentCost();
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
                	// If we cannot pickup a task in that city, we can simply move to it
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
		return current_city;
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
		return this.current_tasks.isEmpty() && this.available_tasks.isEmpty();
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
	@Override
	public int compareTo(State state) {
		return (int) this.getCurrentCost() - (int)state.getCurrentCost();
	}
	
	public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State that = (State) o;

        if (remainingCapacity != that.getRemainingCapacity()) return false;
        if (current_city != null ? !current_city.equals(that.current_city) : that.current_city != null) return false;
        if (current_tasks != null ? !current_tasks.equals(that.current_tasks) : that.current_tasks != null) return false;
        return available_tasks != null ? available_tasks.equals(that.available_tasks) : that.available_tasks == null;
    }
	
	@Override
    public int hashCode() {
        int result = current_city != null ? current_city.hashCode() : 0;
        result = 31 * result + (current_tasks != null ? current_tasks.hashCode() : 0);
        result = 31 * result + remainingCapacity;
        result = 31 * result + (available_tasks != null ? available_tasks.hashCode() : 0);
        return result;
    }
	public boolean same_state(State state) {
		if(this.remainingCapacity != state.getRemainingCapacity()
				||this.current_city != state.getCurrentCity()
				||this.available_tasks!= state.getTasks()
				||this.current_tasks!= state.getCurrentTasks()) {
			return false;
		}
		return true;
	}
}
