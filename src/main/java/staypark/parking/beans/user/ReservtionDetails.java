package staypark.parking.beans.user;

import lombok.Data;

@Data
public class ReservtionDetails {
    private String orderId;
    private String departingAirport;
    private String dropLocation;
    private String dropTime;
    private String pickTime;
    private String baseRate;
    private String serviceFee;
    private String salesTax;
    private String totalCharge;
    private String paymentType;
    private String paymentStatus;
    private String bookingDate;
    private String parkingTime;
    private String state;
    private String city;
    private String zip;
    private String trackingNumber;
    private String billingAddress;
    private String reservationStatus;
    private String orderNumber;
    private String bookingType;
}
