package staypark.parking.controller;

import com.sun.tools.javac.comp.Check;
import staypark.parking.handler.CheckoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CheckoutController {

    @Autowired
    private CheckoutHandler checkoutHandler;

	@GetMapping("/parking/checkout")
    public String checkoutPark(Model model) {
        model.addAttribute("checkout", checkoutHandler.getParkingCheckout());

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
