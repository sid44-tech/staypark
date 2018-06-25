package staypark.parking.beans;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoomParkSearchResults {
    private RoomParkSearchOptions search;
    private List<HotelInformation> hotels = new ArrayList<>();
}
