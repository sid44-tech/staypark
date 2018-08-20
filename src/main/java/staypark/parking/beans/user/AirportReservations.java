package staypark.parking.beans.user;

import lombok.Data;

@Data
public class AirportReservations {
    private String orderNumber;
    private String orderDate;
    private String departingAirport;
    private String dropLocation;
    private String reservationStatus;
    private String details;
    private String detailLink;
    private String bookingType;
}
