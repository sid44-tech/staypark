package staypark.parking.util;

import org.springframework.security.core.Authentication;

import staypark.parking.beans.security.Principal;

public class SecurityUtil {
	
	public static String getAuthenticatedUsername(final Authentication auth) {
		if (auth == null || auth.getPrincipal() == null) {
			return null;
		}
        Principal p = (Principal) auth.getPrincipal();
        return p.getUsername();
	}
}
