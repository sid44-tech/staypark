package staypark.parking.controller.nonsecure;

import staypark.parking.handler.AirportHandler;
import staypark.parking.util.AirportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class FooterLinksController {

    @Autowired
    private AirportHandler airportHandler;
	
	@GetMapping("/affiliates-login")
    public String affiliatesLogin(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
	    return "affiliates-login";
    }
	
	@GetMapping("/privacy")
    public String privacy(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
	    return "privacy";
    }
	
	@GetMapping("/tac")
    public String tac(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
	    return "tac";
    }
	
	@GetMapping("/browse-airports")
    public String browseAirports(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("stateAirports", airportHandler.getBrowseAirportsPageInfo());
	    return "browse-airports";
    }
	
	@GetMapping("/sitemap")
    public String sitemap(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
	    return "sitemap";
    }

    @GetMapping("/contact")
    public String contact(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        return "contact";
    }
    
    @GetMapping("/cancellation")
    public String cancel(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        return "cancellation";
    }
}
