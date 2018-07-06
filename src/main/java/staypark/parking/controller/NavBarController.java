package staypark.parking.controller;

import staypark.parking.beans.AffiliateRegister;
import staypark.parking.beans.ContactUs;
import staypark.parking.handler.AirportHandler;
import staypark.parking.util.AirportUtil;
import staypark.parking.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Slf4j
public class NavBarController {

    @Autowired
    private AirportHandler airportHandler;

	@GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
	    return "about";
    }
	
	@GetMapping("/airport-parking")
    public String airportParking(Model model) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
	    return "airport-parking";
    }
	
	@GetMapping("/room-park")
    public String roomPark(Model model) {
        model.addAttribute("usDepart", AirportUtil.getRoomAndParkLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getRoomAndParkLocations(2));
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
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("status", "");
        model.addAttribute("contactUsMessage", new ContactUs());
        model.addAttribute("messageVisible", "bu-sent-div");
	    return "contact-us";
    }

    @PostMapping("/contact-us")
    public String contactUsPost(Model model, @ModelAttribute ContactUs contactUsMessage) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
	    model.addAttribute("status", "bu-sent");
        model.addAttribute("contactUsMessage", new ContactUs());
        try {
            String BODY = EmailUtil.CONTACT_US_BODY
                    .replace("[EMAIL]", contactUsMessage.getEmail())
                    .replace("[NAME]", contactUsMessage.getName())
                    .replace("[PHONE]", contactUsMessage.getPhone());
            if (contactUsMessage.getComment() != null && !contactUsMessage.getComment().trim().equals("")) {
                BODY = BODY.replace("[COMMENT]", contactUsMessage.getComment());
            }
            boolean result = EmailUtil.sendEmail(EmailUtil.INFO_EMAIL, "Contact Details from User", BODY);
            if (result) {
                model.addAttribute("messageVisible", "bu-sent-div bu-message-visible");
            } else {
                model.addAttribute("messageVisible", "bu-sent-div");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "contact-us";
    }

    @GetMapping("/affiliates")
    public String affiliates(Model model) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("countries", AirportUtil.getCountries());
        model.addAttribute("states", AirportUtil.getStates(1));
        model.addAttribute("cities", AirportUtil.getCities(1));
        model.addAttribute("affiliate", new AffiliateRegister());
        return "affiliates";
    }

    @PostMapping("/affiliates")
    public String affiliateRegister(Model model, @ModelAttribute AffiliateRegister affiliate) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("countries", AirportUtil.getCountries());
        model.addAttribute("states", AirportUtil.getStates(1));
        model.addAttribute("cities", AirportUtil.getCities(1));



        model.addAttribute("affiliate", new AffiliateRegister());

        return "affiliate";
    }
}
