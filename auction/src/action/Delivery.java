package action;

import logist.task.Task;
import logist.topology.Topology.City;

public class Delivery implements Action {

	private Task task;
	private City city;
	
	public Delivery(Task task) {
        this.task = task;
        this.city= task.deliveryCity;
    }
	@Override
	public City city() {
		return city;
	}

	@Override
	public Task task() {
		return task;
	}

	/*
	 * if it is not the same task, the order cannot be wrong.
	 * we only check if this.action is a pickup  because when we use that 
	 * function, we only compare this action to the actions that come after it in 
	 * the array. Thus if it is a pickup, the other action coming from the same task
	 * can only be a delivery and the order is right, 
	 */
	@Override
	public boolean right_order(Action a) {
		if(a.task().id == this.task.id){
			if(a.isPickup() || isDelivery()) {
				return false;
			}
			if(a.isDelivery() || isPickup()){
				return true;
			}
		}
		return true;
	}
	@Override
	public boolean isDelivery() {
		return true;
	}
	@Override
	public boolean isPickup() {
		return false;
	}
	@Override
	public logist.plan.Action getAction() {
		 return new logist.plan.Action.Delivery(task);
	}

}
