package staypark.parking.comparator;

import java.util.Comparator;

import staypark.parking.beans.hotel.HotelInformation;

public class HotelDistanceComparator implements Comparator<HotelInformation> {

    @Override
    public int compare(HotelInformation h1, HotelInformation h2) {
        return (new Double(h1.getAirportDistance())).compareTo(new Double(h2.getAirportDistance()));
    }
}
