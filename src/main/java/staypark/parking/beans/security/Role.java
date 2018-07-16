package staypark.parking.beans.security;

import org.springframework.security.core.GrantedAuthority;

public class Role implements GrantedAuthority {
    private String role;

    public Role(final String role) {
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }
}
