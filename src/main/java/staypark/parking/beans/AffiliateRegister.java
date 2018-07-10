package staypark.parking.beans;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AffiliateRegister {
    private String username = "";
    private String email = "";
    private String password = "";
    private String company = "";
    private String contact = "";
    private String phone = "";
    private String hotel = "";
    private int country;
    private int city;
    private int state;
    private String address = "";
    private MultipartFile hotelLogo;
    private int rooms;
    private int parking;
    private String info = "";
}
