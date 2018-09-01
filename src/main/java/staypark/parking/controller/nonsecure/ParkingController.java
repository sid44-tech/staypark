package staypark.parking.controller.nonsecure;

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
		model.addAttribute("selectedAirport", AirportUtil.getDepartureLocation(airport));
		model.addAttribute("pickUpDate", pickUpDate.replace("-", "/"));
		model.addAttribute("dropOffDate", dropOffDate.replace("-", "/"));
		model.addAttribute("pickUpTime", pickUpTime);
		model.addAttribute("dropOffTime", dropOffTime);
		model.addAttribute("sort", sort);
        
        return "parking";
    }
	
	@GetMapping("/parking/change")
    public String parkingChangeGet(Model model) {
		model.addAttribute("res", new ChangeReservation());

		return "change";
    }

	@GetMapping("/parking/cancel")
	public String parkingCancelGet(Model model, @RequestParam(name="cancelReservationNumber", required=false) String cancelReservationNumber,
								   @RequestParam(name="lastDigits", required=false) String lastDigits) {
		if (cancelReservationNumber != null && !cancelReservationNumber.isEmpty() && lastDigits != null && !lastDigits.isEmpty()) {
			ChangeReservation newRes = parkingHandler.checkValidConfirmatioNumber(cancelReservationNumber, lastDigits, "C");
			newRes.setLastDigits(lastDigits);
			newRes.setCancelReservationNumber(cancelReservationNumber);
			newRes.setType("C");
			boolean validOrder = newRes.isValidNumber();
			newRes.setValidNumber(validOrder);
			if (!validOrder) {
				if (!newRes.getMessage().equals("")) {
					model.addAttribute("message", newRes.getMessage());
				} else {
					model.addAttribute("message", "The reservation number or credit card number you entered is invalid");
				}
				newRes.setMessageClass("col change-error-message-custom visible");
			} else {
				model.addAttribute("message", "");
				newRes.setMessageClass("col change-error-message-custom gone");
			}
			model.addAttribute("res", newRes);
		} else {
			model.addAttribute("res", new ChangeReservation());
		}

		return "checkout/cancel";
	}

	@PostMapping("/parking/cancel")
	public String parkingCancelPost(Model model, @ModelAttribute ChangeReservation inres) {
		ChangeReservation newRes = parkingHandler.checkValidConfirmatioNumber(inres.getCancelReservationNumber(), inres.getLastDigits(), "C");
		boolean validOrder = newRes.isValidNumber();
		newRes.setValidNumber(validOrder);
		if (!validOrder) {
			if (!newRes.getMessage().equals("")) {
				model.addAttribute("message", newRes.getMessage());
			} else {
				model.addAttribute("message", "The reservation number or credit card number you entered is invalid");
			}
			newRes.setMessageClass("col change-error-message-custom visible");
		} else {
			parkingHandler.cancelReservation(newRes);
			return "checkout/cancel-confirm";
		}

		return "checkout/cancel";
	}

	@GetMapping("/parking/extend")
	public String parkingExtendGet(Model model, @RequestParam(name="extendReservationNumber", required=false) String extendReservationNumber) {
		if (extendReservationNumber != null && !extendReservationNumber.isEmpty()) {
			ChangeReservation newRes = parkingHandler.checkValidConfirmatioNumber(extendReservationNumber, null, "E");
			newRes.setExtendReservationNumber(extendReservationNumber);
			newRes.setType("E");
			boolean validOrder = newRes.isValidNumber();
			newRes.setValidNumber(validOrder);
			if (!validOrder) {
				if (!newRes.getMessage().equals("")) {
					model.addAttribute("message", newRes.getMessage());
				} else {
					model.addAttribute("message", "The reservation number you entered is invalid");
				}
				newRes.setMessageClass("col change-error-message-custom visible");
			} else {
				model.addAttribute("message", "");
				newRes.setMessageClass("col change-error-message-custom gone");
			}
			model.addAttribute("res", newRes);
		} else {
			model.addAttribute("res", new ChangeReservation());
		}

		return "checkout/extend";
	}

	@PostMapping("/parking/extend")
	public String parkingExtendPost(Model model, @ModelAttribute ChangeReservation inres) {
		ChangeReservation newRes = parkingHandler.checkValidConfirmatioNumber(inres.getExtendReservationNumber(), null, "E");
		boolean validOrder = newRes.isValidNumber();
		newRes.setValidNumber(validOrder);
		if (!validOrder) {
			if (!newRes.getMessage().equals("")) {
				model.addAttribute("message", newRes.getMessage());
			} else {
				model.addAttribute("message", "The reservation number you entered is invalid");
			}
			newRes.setMessageClass("col change-error-message-custom visible");
		} else {
			parkingHandler.cancelReservation(newRes);
			return "checkout/cancel-confirm";
		}

		return "checkout/extend";
	}
}
