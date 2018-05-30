package staypark.parking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FooterLinksController {

	@GetMapping("/affiliates")
    public String affiliates(Model model) {
        return "affiliates";
    }
	
	@GetMapping("/affiliates-login")
    public String affiliatesLogin(Model model) {
        return "affiliates-login";
    }
	
	@GetMapping("/privacy")
    public String privacy(Model model) {
        return "privacy";
    }
	
	@GetMapping("/tac")
    public String tac(Model model) {
        return "tac";
    }
	
	@GetMapping("/browse-airports")
    public String browseAirports(Model model) {
        return "browse-airports";
    }
	
	@GetMapping("/sitemap")
    public String sitemap(Model model) {
        return "sitemap";
    }
}
