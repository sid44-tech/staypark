package staypark.parking.beans;

import lombok.Data;

@Data
public class UserRegister {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String country;
    private String city;
    private String state;
    private String zip;
    private String address;
}
