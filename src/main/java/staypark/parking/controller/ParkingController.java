package staypark.parking.controller;

import staypark.parking.beans.reservation.ChangeReservation;
import staypark.parking.util.AirportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import staypark.parking.handler.ParkingHandler;

@Controller
public class ParkingController {
	
	@Autowired
	private ParkingHandler parkingHandler;
	
	@GetMapping("/parking/{airport}")
    public String parkingSearchRequest(@PathVariable("airport") String airport, @RequestParam(name="dropOffDate") String dropOffDate, 
    		@RequestParam(name="dropOffTime") String dropOffTime, @RequestParam(name="pickUpDate") String pickUpDate,
    		@RequestParam(name="pickUpTime") String pickUpTime, @RequestParam(name="sort", required = false) String sort, Model model) {
		if (sort == null) {
			sort = "REC";
		}
		model.addAttribute("hotels", parkingHandler.getParkingSearchResults(airport, dropOffDate, pickUpDate, dropOffTime, pickUpTime, sort));
		model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
		model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
		model.addAttribute("sort", sort);
        
        return "parking";
    }
	
	@GetMapping("/parking/change")
    public String parkingChangeGet(Model model) {
		model.addAttribute("res", new ChangeReservation());

		return "change";
    }

	@PostMapping("/parking/change")
	public String parkingChangePost(Model model, @ModelAttribute ChangeReservation res) {
		boolean validOrder = parkingHandler.checkValidConfirmatioNumber(res.getReservationNumber());
		res.setValidNumber(validOrder);
		model.addAttribute("res", res);
		if (!validOrder) {
			res.setMessageClass("col change-error-message-custom visible");
			model.addAttribute("message", "The reservation number you entered is invalid");
		} else {
			res.setMessageClass("col change-error-message-custom gone");
		}

		return "change";
	}
}
