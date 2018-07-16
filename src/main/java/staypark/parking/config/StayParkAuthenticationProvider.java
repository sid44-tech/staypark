package staypark.parking.config;

import staypark.parking.beans.security.AuthProfile;
import staypark.parking.beans.security.Principal;
import staypark.parking.beans.security.Role;
import staypark.parking.handler.AccountHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StayParkAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AccountHandler accountHandler;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        Principal p = new Principal();
        p.setUsername(authentication.getName());
        String password = authentication.getCredentials().toString();

        AuthProfile profile = accountHandler.validateCredentials(p.getUsername(), password);

        if (profile != null && profile.isAuthenticated()) {
            p.setFirstName(profile.getFirstName());
            List<Role> roles = new ArrayList<>();
            roles.add(new Role(profile.getRole()));

            return new UsernamePasswordAuthenticationToken(p, password, roles);
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
