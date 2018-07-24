package staypark.parking.controller;

import staypark.parking.handler.CheckoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CheckoutController {

    @Autowired
    private CheckoutHandler checkoutHandler;

	@GetMapping("/parking/checkout/{hotelId}")
    public String checkoutPark(Model model, @PathVariable("hotelId") int hotelId, @RequestParam(name="dropOffDate") String dropOffDate,
                               @RequestParam(name="dropOffTime") String dropOffTime, @RequestParam(name="pickUpDate") String pickUpDate,
                               @RequestParam(name="pickUpTime") String pickUpTime) {
        model.addAttribute("checkout", checkoutHandler.getParkingCheckout(hotelId, dropOffDate, dropOffTime, pickUpDate, pickUpTime));

	    return "checkout/parking-checkout";
    }

    @PostMapping("/parking/checkout")
    public String checkoutParkPost(Model model) {

        return "checkout/parking-checkout";
    }

    @GetMapping("/roompark/checkout")
    public String checkoutRoomPark(Model model) {

	    return "checkout/parking-checkout";
    }
}
