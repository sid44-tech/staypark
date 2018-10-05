package staypark.parking.controller.secure;

import staypark.parking.beans.security.ChangePassword;
import staypark.parking.beans.security.Principal;
import staypark.parking.beans.security.UserProfile;
import staypark.parking.beans.user.AirportReservations;
import staypark.parking.handler.AccountHandler;
import staypark.parking.handler.AirportHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserAccountController {

    @Autowired
    private AccountHandler accountHandler;

    @Autowired
    private AirportHandler airportHandler;

    @GetMapping("/user/reservations")
    public String getUserReservations(Model model, Authentication auth, @RequestHeader HttpHeaders headers) {
        Principal p = (Principal) auth.getPrincipal();
        String username = p.getUsername();

        List<AirportReservations> list = airportHandler.getCustomerReservations(username);
        model.addAttribute("reservations", list);

        if (list.isEmpty()) {
            model.addAttribute("empty", "true");
        } else {
            model.addAttribute("empty", "false");
        }

        return "user/airport-parking-reservations";
    }

    @GetMapping("/user/reservations/{orderId}")
    public String getUserReservationsDetails(Model model, @PathVariable("orderId") String orderId, Authentication auth, @RequestHeader HttpHeaders headers) {
        Principal p = (Principal) auth.getPrincipal();
        String username = p.getUsername();

        model.addAttribute("details", airportHandler.getCustomerReservationDetails(username, orderId, false));

        return "user/airport-parking-reservation-detail";
    }

    @GetMapping("/user/profile")
    public String getUserProfile(Model model, Authentication auth) {
        Principal p = (Principal) auth.getPrincipal();

        model.addAttribute("profile", accountHandler.getUserProfile(p.getUsername()));

        return "user/profile";
    }

    @GetMapping("/user/password")
    public String changeUserPasswordGet(Model model, @RequestHeader HttpHeaders headers) {
        return "user/change-password";
    }

    @PostMapping("/user/password")
    public String changeUserPassword(Model model, Authentication auth, @ModelAttribute ChangePassword pw, @RequestHeader HttpHeaders headers) {
        Principal p = (Principal) auth.getPrincipal();
        boolean success = accountHandler.changePassword(p.getUsername(), pw.getCurrentPassword(), pw.getNewPassword());
        if (success) {
            model.addAttribute("message", "success");
        } else {
            model.addAttribute("message", "error");
        }

        return "user/change-password";
    }

    @PostMapping("/user/profile")
    public String updateUserProfile(Model model, @ModelAttribute UserProfile profile, Authentication auth, @RequestHeader HttpHeaders headers) {
        if (!auth.isAuthenticated()) {
            return "login";
        }

        Principal p = (Principal) auth.getPrincipal();
        model.addAttribute("profile", accountHandler.updateUserProfile(p.getUsername(), profile));


        return "user/profile";
    }
}
