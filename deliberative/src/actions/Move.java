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
		List<Action> act_list = new ArrayList<Action>(state.getActionList());
		act_list.add(this);
		
		return new State(state.getVehicle(),
				state.getCurrentCost() + this.getCurrentCost(),
				state.getTasks(), 
				state.getCurrentTasks(), 
				state.getRemainingCapacity(),
				act_list);		
	}

	@Override
	public double getCurrentCost() {
		return current_city.distanceTo(next_city)*costPerKm;
	}
	

}
