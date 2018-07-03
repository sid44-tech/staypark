package staypark.parking.beans;

import lombok.Data;

@Data
public class AffiliateRegister {
    private String username = "";
    private String email = "";
    private String password = "";

    private String companyName = "";
    private String contactName = "";
    private String phoneNumber = "";
    private String hotelName = "";
    private String country = "";
    private String city = "";
    private String state = "";
    private String hotelLogo = "";
    private String numberRooms = "";
    private String numberParkingSpaces = "";
    private String address = "";
    private String companyInfo = "";

}
