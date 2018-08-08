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

    //These fields will be hidden input fields on the web client.  They are intended to be passed to the checkout page
    //as request parameters.
    private String hotelId;
    private String roomId;
    private String carDropOffDateMDY;
    private String carDropOffTime;
    private String carPickUpDateMDY;
    private String carPickUpTime;
    private String hotelArrivalDateMDY;
    private String hotelDepartureDateMDY;
    private String endHotelArrivalDateMDY;
    private String endHotelDepartureDateMDY;
}
