package actions;

import java.util.ArrayList;
import java.util.List;

import logist.task.Task;
import logist.task.TaskSet;
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

		TaskSet new_current_task_set = state.getCurrentTasks().clone();
		new_current_task_set.add(task);

		TaskSet new_available_task_set = state.getTasks().clone();
		new_available_task_set.remove(task);
		
		Integer weight = task.weight;
		
		List<Action> actions = new ArrayList<>(state.getActionList());
		actions.add(this);
		
		return new State(state, new_available_task_set, new_current_task_set, weight, actions);
	}

	@Override
	public double getCurrentCost() {
		return 0;
	}
	
	

}
