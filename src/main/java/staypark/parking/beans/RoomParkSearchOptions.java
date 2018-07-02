package staypark.parking.beans;

import lombok.Data;

@Data
public class RoomParkSearchOptions {
    private String hotelArrivalDate;
    private String hotelDepartureDate;
    private String endHotelArrivalDate;
    private String endHotelDepartureDate;
    private int totalHotelNights;
    private int rooms;
    private int totalAdults;
    private int children;
    private String carDropOffDate;
    private String carPickUpDate;
    private int totalParkingTime;
    private String searchType;
}
