package staypark.parking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CheckoutController {

	@GetMapping("/parking/checkout")
    public String signup(Model model) {
        return "checkout";
    }
}
