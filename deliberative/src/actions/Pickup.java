package actions;

import logist.task.Task;
import model.State;

public class Pickup implements Action{
	
	private Task task;
	
	public Pickup(Task task) {
		this.task = task;
	}

	@Override
	public logist.plan.Action getResultingAction() {
		return new logist.plan.Action.Pickup(task);
	}

	@Override
	public State getResultingState(State state) {
		assert(state.getCurrentCity() == task.pickupCity);
		state.getTasks().remove(task);
		state.getCurrentTasks().add(task);
		state.setRemainingCapacity(state.getRemainingCapacity() + task.weight);
		
		return state;
	}

	@Override
	public double getCurrentCost() {
		return 0;
	}
	
	

}
