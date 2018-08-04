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
	public String parkingChangePost(Model model, @ModelAttribute ChangeReservation inres) {
		ChangeReservation newRes = parkingHandler.checkValidConfirmatioNumber(inres);
		boolean validOrder = newRes.isValidNumber();
		newRes.setValidNumber(validOrder);
		if (!validOrder) {
			if (newRes.getType().equals("E")) {
				model.addAttribute("message", "The reservation number you entered is invalid");
			} else if (newRes.getType().equals("C")) {
				model.addAttribute("message", "The reservation number or credit card number you entered is invalid");
			}
			newRes.setMessageClass("col change-error-message-custom visible");
		} else {
			model.addAttribute("message", "");
			newRes.setMessageClass("col change-error-message-custom gone");
		}
		model.addAttribute("res", newRes);

		return "change";
	}

	@GetMapping("/parking/cancel")
	public String parkingCancelGet(Model model) {
		model.addAttribute("res", new ChangeReservation());

		return "checkout/cancel";
	}

	@GetMapping("/parking/extend")
	public String parkingExtendGet(Model model) {
		model.addAttribute("res", new ChangeReservation());

		return "checkout/extend";
	}

	@PostMapping("/parking/cancel")
	public String parkingCancelPost(Model model, @ModelAttribute ChangeReservation inres) {
		ChangeReservation newRes = parkingHandler.checkValidConfirmatioNumber(inres);
		boolean validOrder = newRes.isValidNumber();
		newRes.setValidNumber(validOrder);
		if (!validOrder) {
			model.addAttribute("message", "The reservation number or credit card number you entered is invalid");
			newRes.setMessageClass("col change-error-message-custom visible");
		} else {
			model.addAttribute("message", "");
			newRes.setMessageClass("col change-error-message-custom gone");
		}
		model.addAttribute("res", newRes);

		return "checkout/cancel";
	}

	@PostMapping("/parking/extend")
	public String parkingExtendPost(Model model, @ModelAttribute ChangeReservation inres) {
		ChangeReservation newRes = parkingHandler.checkValidConfirmatioNumber(inres);
		boolean validOrder = newRes.isValidNumber();
		newRes.setValidNumber(validOrder);
		if (!validOrder) {
			model.addAttribute("message", "The reservation number you entered is invalid");
			newRes.setMessageClass("col change-error-message-custom visible");
		} else {
			model.addAttribute("message", "");
			newRes.setMessageClass("col change-error-message-custom gone");
		}
		model.addAttribute("res", newRes);

		return "checkout/extend";
	}
}
