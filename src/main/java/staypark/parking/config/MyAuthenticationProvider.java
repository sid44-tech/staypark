package staypark.parking.config;

import staypark.parking.beans.security.Principal;
import staypark.parking.handler.AccountHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class MyAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private AccountHandler accountHandler;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        Principal p = new Principal();
        p.setUsername(authentication.getName());
        p.setFirstName("Sid");
        p.setLastName("Bhat");
        String password = authentication.getCredentials().toString();

        boolean validated = accountHandler.validateCredentials(p.getUsername(), password);

        if (validated) {
            return new UsernamePasswordAuthenticationToken(p, password, new ArrayList<>());
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
