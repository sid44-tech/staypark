package staypark.parking.controller.secure;

import staypark.parking.beans.security.ChangePassword;
import staypark.parking.beans.security.Principal;
import staypark.parking.beans.security.UserProfile;
import staypark.parking.handler.AccountHandler;
import javafx.scene.control.TextFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserAccountController {

    @Autowired
    private AccountHandler accountHandler;

    @GetMapping("/user/profile")
    public String getUserProfile(Model model, Authentication auth) {
        Principal p = (Principal) auth.getPrincipal();

        model.addAttribute("profile", accountHandler.getUserProfile(p.getUsername()));

        return "user/profile";
    }

    @GetMapping("/user/password")
    public String changeUserPasswordGet(Model model) {
        return "change-password";
    }

    @PostMapping("/user/password")
    public String changeUserPassword(Model model, Authentication auth, @ModelAttribute ChangePassword pw) {
        Principal p = (Principal) auth.getPrincipal();
        boolean success = accountHandler.changePassword(p.getUsername(), pw.getCurrentPassword(), pw.getNewPassword());
        if (success) {
            model.addAttribute("message", "Password Successfully Updated");
        } else {
            model.addAttribute("message", "Error Updating Password");
        }

        return "change-password";
    }

    @PostMapping("/user/profile")
    public String updateUserProfile(Model model, @ModelAttribute UserProfile profile, Authentication auth) {
        if (!auth.isAuthenticated()) {
            return "login";
        }

        Principal p = (Principal) auth.getPrincipal();
        model.addAttribute("profile", accountHandler.updateUserProfile(p.getUsername(), profile));


        return "user/profile";
    }

    @GetMapping("/affiliate/profile")
    public String getAffiliateProfile(Model model, Authentication auth) {
        Principal p = (Principal) auth.getPrincipal();

        model.addAttribute("profile", accountHandler.getUserProfile(p.getUsername()));

        return "user/profile";
    }

    @PostMapping("/affiliate/profile")
    public String updateAffiliateProfile(Model model, @ModelAttribute UserProfile profile, Authentication auth) {
        if (!auth.isAuthenticated()) {
            return "login";
        }

        Principal p = (Principal) auth.getPrincipal();
        model.addAttribute("profile", accountHandler.updateUserProfile(p.getUsername(), profile));


        return "user/profile";
    }
}
