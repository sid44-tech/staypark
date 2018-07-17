package staypark.parking.beans.user;

import lombok.Data;

@Data
public class AirportReservations {
    private int orderNumber;
    private String departingAirport;
    private String dropLocation;
    private String reservationStatus;
    private String details;

}
