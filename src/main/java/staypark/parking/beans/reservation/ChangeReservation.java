package staypark.parking.beans.reservation;

import lombok.Data;

import java.util.Date;

@Data
public class ChangeReservation {

    private String extendReservationNumber = "";
    private String cancelReservationNumber = "";
    private boolean validNumber = false;
    private String messageClass;
    private String lastDigits;
    private String type;
    private String departingAirport;
    private String dropOffLocation;
    private String dropOffTime;
    private String pickUpTime;
    private String name;
    private String message = "";
    private String email = "";
    private String newPickUpDate;
    private String newPickUpTime;
    private Date extendMinPickUpDate;
    private String hotelId;
    private String originalPickUpDate;
    private String originalPickUpTime;


}
