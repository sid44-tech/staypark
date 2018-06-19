package staypark.parking.beans;

import lombok.Data;

import java.util.List;

@Data
public class RoomParkSearchResults {
    private String hotelTitle;
    private String cityState;
    private List<String> imageLinks;
    private List<String> roomList;
    private String hotelArrivalDate;
    private String hotelDepartureDate;
    private String totalHotelNights;
    private String rooms;
    private String totalAdults;
    private String children;
    private String carDropOffDate;
    private String carPickUpDate;
    private String totalParkingTime;
    private String hotelText;
    private String googleMapLink;
}
