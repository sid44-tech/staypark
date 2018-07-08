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
    private String country = "";
    private String city = "";
    private String state = "";
    private String address = "";
    private MultipartFile hotelLogo;
    private String rooms = "";
    private String parking = "";
    private String info = "";

}
