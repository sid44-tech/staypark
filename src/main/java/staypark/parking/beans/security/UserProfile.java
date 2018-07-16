package staypark.parking.beans.security;

        import lombok.Data;

@Data
public class UserProfile {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String address;
    private String state;
    private String city;
    private String zip;
}
