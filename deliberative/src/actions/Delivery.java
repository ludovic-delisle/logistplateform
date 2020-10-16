package actions;

import java.util.ArrayList;
import java.util.List;

import logist.task.Task;
import logist.task.TaskSet;
import model.State;

public class Delivery implements Action{
	
	private Task task;
	
	public Delivery(Task task) {
		this.task = task;
	}

	@Override
	public logist.plan.Action getResultingAction() {
		
		return new logist.plan.Action.Delivery(task);
	}

	@Override
	public State getResultingState(State state) {
		assert(state.getCurrentCity() == task.deliveryCity);
		
		List<Action> act_list = new ArrayList<Action>(state.getActionList());
		act_list.add(this);
		
		TaskSet current_tasks = state.getCurrentTasks().clone();
		current_tasks.remove(task);
		
		return new State(state.getVehicle(),
				state.getCurrentCost(),
				state.getTasks(), 
				current_tasks, 
				state.getRemainingCapacity() + task.weight,
				act_list);	
	}

	@Override
	public double getCurrentCost() {
		return 0;
	}

}
