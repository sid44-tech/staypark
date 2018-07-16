package staypark.parking.beans.security;

import lombok.Data;

@Data
public class AuthProfile {
    private String username;
    private String password;
    private String role;
    private String firstName;
    private boolean authenticated = false;
}
