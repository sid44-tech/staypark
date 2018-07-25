package staypark.parking.beans.booking;

import lombok.Data;

@Data
public class ParkingCheckout {
    private String departAirport;
    private String departLocation;
    private String checkinTime;
    private String checkoutTime;
    private String totalParkingTime;
    private String shuttleFrequency;
    private String baseRate;
    private String serviceFee;
    private String salesTax;
    private String grandTotal;
    private String totalDueNow;
    private String totalDueAtLot;
    private boolean splitPayment = false;
    private String driverInformation;
    private String numberPeopleTravelling;
    private String firstName;
    private String lastName;
    private String carMake;
    private String carModel;
    private String carLicensePlate;
    private String hearAboutUs;
    private String address;
    private String state;
    private String city;
    private String zip;
    private String email;
    private String phone;
    private String paymentMethod;
    private String ownerName;
    private String cardType;
    private String cardNumber;
    private String cvv;
    private String expirationDate;
    private boolean agreeTac;
    private boolean agreeCancelPolicy;
}
