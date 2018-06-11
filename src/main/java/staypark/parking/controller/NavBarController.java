package staypark.parking.controller;

import staypark.parking.beans.ContactUs;
import staypark.parking.util.AirportUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NavBarController {

	@GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
	    return "about";
    }
	
	@GetMapping("/airport-parking")
    public String airportParking(Model model) {
        return "airport-parking";
    }
	
	@GetMapping("/room-park")
    public String roomPark(Model model) {
        return "room-park";
    }
	
	@GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
	    return "faq";
    }

	@GetMapping("/contact-us")
    public String contactUs(Model model) {
        model.addAttribute("status", "");
        model.addAttribute("contactUsMessage", new ContactUs());
	    return "contact-us";
    }

    @PostMapping("/contact-us")
    public String contactUsPost(Model model, @ModelAttribute ContactUs contactUsMessage) {
	    model.addAttribute("status", "bu-sent");
        model.addAttribute("contactUsMessage", new ContactUs());
	    return "contact-us";
    }
}
