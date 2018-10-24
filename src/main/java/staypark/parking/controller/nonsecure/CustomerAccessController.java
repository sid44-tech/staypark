package staypark.parking.controller.nonsecure;

import staypark.parking.beans.security.AuthProfile;
import staypark.parking.beans.user.UserRegister;
import staypark.parking.handler.AccountHandler;
import staypark.parking.util.AirportUtil;
import staypark.parking.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class CustomerAccessController {

    @Autowired
    private AccountHandler accountHandler;

	@GetMapping("/login")
    public String login(Model model, @RequestHeader HttpHeaders headers) {
	    return "login";
    }

    @GetMapping("/logout")
    public String logout(Model model, HttpServletRequest request, HttpServletResponse response, @RequestHeader HttpHeaders headers) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";//You can redirect wherever you want, but generally it's a good practice to show login screen again.
    }

    @PostMapping("/login")
    public String loginCheck(Model model, @ModelAttribute UserRegister user, @RequestHeader HttpHeaders headers) {
        AuthProfile profile = accountHandler.validateCredentials(user.getUsername(), user.getPassword());
        model.addAttribute("validates", profile.isAuthenticated());
        return "login";
    }
	
	@GetMapping("/signup")
    public String signup(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("user", new UserRegister());
        model.addAttribute("success", "false");
	    return "signup";
    }

    @PostMapping("/signup")
    public String signupRegistered(Model model, @ModelAttribute UserRegister user, @RequestHeader HttpHeaders headers, HttpServletRequest request) {
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        String ip = "";
        if (headers != null && headers.get("x-forwarded-for") != null && headers.get("x-forwarded-for").get(0) != null) {
            ip = headers.get("x-forwarded-for").get(0);
        }
        boolean captchaPassed = SecurityUtil.checkCaptcha(gRecaptchaResponse, ip);
        if (!captchaPassed) {
            model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
            model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
            model.addAttribute("user", user);
            model.addAttribute("captchaFailed", "true");
            return "signup";
        }
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("user", new UserRegister());
        model.addAttribute("success", accountHandler.createCustomerAccount(user));

        return "signup";
    }

    @GetMapping("/forgot")
    public String forgot(Model model, @RequestHeader HttpHeaders headers) {

        return "forgot";
    }
}
