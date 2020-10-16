package actions;

import java.util.ArrayList;
import java.util.List;

import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import model.State;


public class Move implements Action{
	
	private City current_city=null;
	private City next_city=null;
	private double costPerKm=0;
	
	public Move(City current_city, City next_city, double costPerKm) {
		this.current_city = current_city;
		this.next_city = next_city;
		this.costPerKm = costPerKm;
	}

	@Override
	public logist.plan.Action getResultingAction() {
		return new logist.plan.Action.Move(next_city);
	}

	@Override
	public State getResultingState(State state) {
double additional_cost = state.getCostKm()*next_city.distanceTo(state.getCurrentCity());
		
		List<Action> actions = new ArrayList<>(state.getActionList());
	    actions.add(this);
		
		return new State(next_city, state,additional_cost, actions);
		
	}

	@Override
	public double getCurrentCost() {
		return current_city.distanceTo(next_city)*costPerKm;
	}
	

}
