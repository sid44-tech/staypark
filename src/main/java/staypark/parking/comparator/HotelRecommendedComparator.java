package staypark.parking.comparator;

import staypark.parking.beans.HotelInformation;

import java.util.Comparator;

public class HotelRecommendedComparator implements Comparator<HotelInformation> {

    @Override
    public int compare(HotelInformation h1, HotelInformation h2) {
        return (new Integer(h1.getRecommendedSort())).compareTo(h2.getRecommendedSort());
    }
}
