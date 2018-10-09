package staypark.parking.controller.nonsecure;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import staypark.parking.beans.general.TestimonialBean;
import staypark.parking.handler.HomeHandler;
import staypark.parking.util.AirportUtil;

@Controller
public class HomeController {
	
	@Autowired
	private HomeHandler homeHandler;
    
	@GetMapping("/")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name,
                           @RequestParam(name="error", required=false) String error, Model model, @RequestHeader HttpHeaders headers) {
	    if (error != null) {
	        return "login";
        }

        model.addAttribute("name", name);
        List<TestimonialBean> list = homeHandler.getTestimonials();
        if (list.size() > 0) {
        	model.addAttribute("activeTestimonial", homeHandler.getTestimonials().get(0));
            model.addAttribute("testimonials", homeHandler.getTestimonials().subList(1, homeHandler.getTestimonials().size()));
        } else {
        	model.addAttribute("activeTestimonial", new TestimonialBean("", "", ""));
        	model.addAttribute("testimonials", new ArrayList<TestimonialBean>());
        }
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        return "home";
    }
}
