package staypark.parking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ParkingController {
	
	@GetMapping("/parking/{airport}")
    public String parkingSearchRequest(@PathVariable("airport") String airport, @RequestParam(name="dropOffDate") String dropOffDate, 
    		@RequestParam(name="dropOffTime") String dropOffTime, @RequestParam(name="pickUpDate") String pickUpDate,
    		@RequestParam(name="pickUpTime") String pickUpTime, Model model) {
        model.addAttribute("airport", airport);
        model.addAttribute("dropOffDate", dropOffDate);
        model.addAttribute("dropOffTime", dropOffTime);
        model.addAttribute("pickUpDate", pickUpDate);
        model.addAttribute("pickUpTime", pickUpTime);
        
        return "parking";
    }
	
	@GetMapping("/parking/cancel")
    public String parkingCancelRequest(Model model) {
        return "cancel";
    }
}
