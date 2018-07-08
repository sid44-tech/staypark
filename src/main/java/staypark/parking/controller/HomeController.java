package staypark.parking.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import staypark.parking.beans.TestimonialBean;
import staypark.parking.handler.HomeHandler;
import staypark.parking.util.AirportUtil;

@Controller
public class HomeController {
	
	@Autowired
	private HomeHandler homeHandler;
    
	@GetMapping("/")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
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

    @GetMapping("/1")
    public String greeting1(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
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
        return "home1";
    }

    @GetMapping("/2")
    public String greeting2(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
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
        return "home2";
    }

    @GetMapping("/3")
    public String greeting3(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
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
        return "home3";
    }
}
