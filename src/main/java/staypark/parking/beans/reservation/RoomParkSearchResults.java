package staypark.parking.beans.reservation;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import staypark.parking.beans.hotel.HotelInformation;

@Data
public class RoomParkSearchResults {
    private RoomParkSearchOptions search;
    private List<HotelInformation> hotels = new ArrayList<>();
}
