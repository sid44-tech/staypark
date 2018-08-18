package staypark.parking.controller.secure;

import staypark.parking.beans.security.Principal;
import staypark.parking.handler.AccountHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AffiliateAccountController {

    @Autowired
    private AccountHandler accountHandler;

    @GetMapping("/affiliate/reservations")
    public String getAffiliateReservations(Model model, Authentication auth, @RequestParam(name="range", required=false) String range) {
        Principal p = (Principal) auth.getPrincipal();
        String username = p.getUsername();
        if (username == null || username.trim().isEmpty()) {
            return "affiliate/reservations";
        }

        model.addAttribute("res", accountHandler.getAffiliateReservations(username, range));
        if (range == null || range.trim().isEmpty()) {
            model.addAttribute("range", "TODAY");
        } else {
            model.addAttribute("range", range);
        }
        return "affiliate/reservations";
    }
}
