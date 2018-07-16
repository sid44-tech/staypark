package staypark.parking.beans.security;

import lombok.Data;

@Data
public class ChangePassword {
    private String currentPassword;
    private String newPassword;
}
