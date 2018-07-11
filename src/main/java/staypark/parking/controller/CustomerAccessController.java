package staypark.parking.controller;

import staypark.parking.beans.UserRegister;
import staypark.parking.handler.AccountHandler;
import staypark.parking.util.AirportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CustomerAccessController {

    @Autowired
    private AccountHandler accountHandler;

	@GetMapping("/login")
    public String login(Model model) {
	    return "login";
    }

    @PostMapping("/login")
    public String loginCheck(Model model, @ModelAttribute UserRegister user) {
        boolean credentialsValid = accountHandler.validateCredentials(user.getUsername(), user.getPassword());
        model.addAttribute("validates", credentialsValid);
        return "login";
    }
	
	@GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("user", new UserRegister());
        model.addAttribute("success", "false");
	    return "signup";
    }

    @PostMapping("/signup")
    public String signupRegistered(Model model, @ModelAttribute UserRegister user) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("user", new UserRegister());
        model.addAttribute("success", accountHandler.createCustomerAccount(user));

        return "signup";
    }

    @GetMapping("/forgot")
    public String forgot(Model model) {

        return "forgot";
    }
}
