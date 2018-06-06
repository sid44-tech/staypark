package staypark.parking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AirportController {

	@GetMapping("/airport/{state}/{airport}")
    public String airportDetails(@PathVariable("state") String state, @PathVariable("airport") String airport, Model model) {

        
        return "airport-detail";
    }
}
