package staypark.parking.beans.reservation;

import lombok.Data;

@Data
public class ChangeReservation {

    private String reservationNumber = "";
    private boolean validNumber = false;
    private String messageClass;
}
