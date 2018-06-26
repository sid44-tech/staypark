package staypark.parking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import staypark.parking.handler.ParkingHandler;

@Controller
public class ParkingController {
	
	@Autowired
	private ParkingHandler parkingHandler;
	
	@GetMapping("/parking/{airport}")
    public String parkingSearchRequest(@PathVariable("airport") String airport, @RequestParam(name="dropOffDate") String dropOffDate, 
    		@RequestParam(name="dropOffTime") String dropOffTime, @RequestParam(name="pickUpDate") String pickUpDate,
    		@RequestParam(name="pickUpTime") String pickUpTime, Model model) {
		model.addAttribute("hotels", parkingHandler.getParkingSearchResults(airport, dropOffDate, pickUpDate, dropOffTime, pickUpTime));
        
        return "parking";
    }
	
	@GetMapping("/parking/cancel")
    public String parkingCancelRequest(Model model) {
        return "cancel";
    }
}
