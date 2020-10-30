package action;

import logist.task.Task;
import logist.topology.Topology.City;

public interface Action {

    public City city();
    
    public Task task();
    
    public boolean isDelivery();
    
    public boolean isPickup();
    
    public boolean right_order(Action a); 
    
    logist.plan.Action getAction();
}