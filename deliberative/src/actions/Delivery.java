package actions;

import java.util.ArrayList;
import java.util.List;

import logist.task.Task;
import model.State;

public class Delivery implements Action{
	
	private Task task;
	
	public Delivery(Task task) {
		this.task = task;
	}
	
	public void run(State state, Task task) {
		
	}

	@Override
	public logist.plan.Action getResultingAction() {
		
		return new logist.plan.Action.Delivery(task);
	}

	@Override
	public State getResultingState(State state) {
		assert(state.getCurrentCity() == task.deliveryCity);
		state.getCurrentTasks().remove(task);
		state.setRemainingCapacity(state.getRemainingCapacity() + task.weight);
		List<Action> actions_list_temp = new ArrayList<>(state.getActionList());
		actions_list_temp.add(this);
		state.updateActionList(actions_list_temp);
		return state;
	}

	@Override
	public double getCurrentCost() {
		return 0;
	}

}
