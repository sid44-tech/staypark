package staypark.parking.controller.secure;

import staypark.parking.beans.security.Principal;
import staypark.parking.handler.AccountHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GeneralAccuontController {

    @Autowired
    private AccountHandler accountHandler;

    @GetMapping("/profile")
    public String getProfile(Model model, Authentication auth) {
        if (!auth.isAuthenticated()) {
            return "login";
        }
        Principal p = (Principal) auth.getPrincipal();

        if (p.getUsername().contains("@")) {
            model.addAttribute("profile", accountHandler.getAffiliateProfile(p.getUsername()));
            return "affiliate/profile";
        } else {
            model.addAttribute("profile", accountHandler.getUserProfile(p.getUsername()));
            return "user/profile";
        }
    }
}
