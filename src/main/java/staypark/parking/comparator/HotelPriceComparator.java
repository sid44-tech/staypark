package staypark.parking.comparator;

import java.util.Comparator;

import staypark.parking.beans.HotelInformation;

public class HotelPriceComparator implements Comparator<HotelInformation> {

	@Override
	public int compare(HotelInformation h1, HotelInformation h2) {
		return (h1.getTotalCost()).compareTo(h2.getTotalCost());
	}

}
