package staypark.parking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavBarController {

	@GetMapping("/about")
    public String about(Model model) {
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
        return "faq";
    }
	
	@GetMapping("/contact-us")
    public String contactUs(Model model) {
        return "contact-us";
    }
}
