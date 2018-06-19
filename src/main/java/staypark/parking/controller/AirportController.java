package staypark.parking.controller;

import staypark.parking.beans.ContactUs;
import staypark.parking.beans.RoomParkSearch;
import staypark.parking.handler.AirportHandler;
import staypark.parking.util.AirportUtil;
import staypark.parking.util.EmailUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AirportController {

    @Autowired
    private AirportHandler airportHandler;

	@GetMapping("/airport/{airport}")
    public String airportDetails(@PathVariable("airport") String airport, Model model) {
        model.addAttribute("airport", airportHandler.getAirportDetails(airport));
        
        return "airport-detail";
    }
	
    @PostMapping("/room-park-results")
    public String roomParkResults(Model model, @ModelAttribute RoomParkSearch search) {
    	
        return "room-park-results";
    }
}
