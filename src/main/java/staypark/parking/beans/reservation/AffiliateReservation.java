package staypark.parking.beans.reservation;

import lombok.Data;

@Data
public class AffiliateReservation {
    private String date;
    private String order;
    private String name;
    private String email;
    private String payment;
    private String detail;
    private String type;
    private String range;
}
