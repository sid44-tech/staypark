package staypark.parking.beans;

import lombok.Data;

import java.util.List;

@Data
public class RoomParkSearchResults {
    private int hotelId;
    private String hotelTitle;
    private String cityState;
    private List<ImageLink> imageLinks;
    private List<Room> roomList;
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
