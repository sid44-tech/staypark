package staypark.parking.beans.general;

import lombok.Data;

@Data
public class StateAirportBean implements Comparable<StateAirportBean> {
    private String code = "";
    private String title = "";
    private String link = "";
    private String text = "";
    
	@Override
	public int compareTo(StateAirportBean o) {
		int stateCompare = title.compareTo(o.getTitle());
		if (stateCompare != 0) {
			return stateCompare;
		}
		
		return code.compareTo(o.getCode());
	}
}
