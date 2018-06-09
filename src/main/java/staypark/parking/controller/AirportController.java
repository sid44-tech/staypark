package staypark.parking.controller;

import staypark.parking.handler.AirportHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AirportController {

    @Autowired
    private AirportHandler airportHandler;

	@GetMapping("/airport/{airport}")
    public String airportDetails(@PathVariable("airport") String airport, Model model) {
        model.addAttribute("airport", airportHandler.getAirportDetails(airport));
        
        return "airport-detail";
    }
}
