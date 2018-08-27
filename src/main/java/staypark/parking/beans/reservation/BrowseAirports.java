package staypark.parking.beans.reservation;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class BrowseAirports {
	private String state = "";
	private List<AirportBean> airports = new ArrayList<>();
	
	public void addAirport(final AirportBean airport) {
		airports.add(airport);
	}
}
