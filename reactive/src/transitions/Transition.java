package transitions;

import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.*;

public class Transition {
	
	City startCity;
	City endCity;
	
	public Transition(City start_city, City end_city) {
		this.startCity = start_city;
		this.endCity = end_city;
	}
	
	
	public City get_start_city() {
		return startCity;
	}
	public City get_end_city() {
		return endCity;
	}
	public boolean sameTransition(Transition t2) {
		if(!this.get_start_city().name.equals(t2.get_start_city().name)) {
			return false;
		}
		else if(this.get_end_city()==null && t2.get_end_city()==null) {
			return true;
		}
		else if(this.get_end_city()==null || t2.get_end_city()==null) {
			return false;
		}
		else if(this.get_end_city().name.equals(t2.get_end_city().name)) {
			return true;
		}
		return false;
	}

}
