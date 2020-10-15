package actions;

import model.State;


public interface Action {
	
	logist.plan.Action getResultingAction();
	
	State getResultingState(State state);
	
	double getCurrentCost();
	
}