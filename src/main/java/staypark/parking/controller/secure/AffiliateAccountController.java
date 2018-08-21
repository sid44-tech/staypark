package staypark.parking.controller.secure;

import staypark.parking.beans.reservation.AffiliateReservation;
import staypark.parking.beans.security.Principal;
import staypark.parking.handler.AccountHandler;
import staypark.parking.handler.AirportHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AffiliateAccountController {

    @Autowired
    private AccountHandler accountHandler;

    @Autowired
    private AirportHandler airportHandler;

    @GetMapping("/affiliate/reservations")
    public String getAffiliateReservations(Model model, Authentication auth, @RequestParam(name="range", required=false) String range,
                                           @RequestParam(name="startDate", required=false) String startDate, @RequestParam(name="endDate", required=false) String endDate) {
        Principal p = (Principal) auth.getPrincipal();
        String username = p.getUsername();
        if (username == null || username.trim().isEmpty()) {
            return "login";
        }

        List<AffiliateReservation> list = accountHandler.getAffiliateReservations(username, range, startDate, endDate);

        model.addAttribute("res", list);
        if (range == null || range.trim().isEmpty()) {
            model.addAttribute("range", "PREVIOUS_WEEK");
        } else {
            model.addAttribute("range", range);
        }

        if (list == null || list.isEmpty()) {
            model.addAttribute("resCount", "No reservations in time range");
        } else if (list.size() == 1) {
            model.addAttribute("resCount", list.size() + " reservation in time range");
        } else {
            model.addAttribute("resCount", list.size() + " reservations in time range");
        }

        return "affiliate/reservations";
    }

    @GetMapping("/affiliate/reservations/{orderId}")
    public String getAffiliateReservationOrderDetail(Model model, Authentication auth, @PathVariable(name = "orderId") String orderId) {
        Principal p = (Principal) auth.getPrincipal();
        String username = p.getUsername();
        boolean validAccess = accountHandler.checkAffiliateCanAccessOrder(orderId, username);

        model.addAttribute("details", airportHandler.getCustomerReservationDetails(username, orderId, true));

        return "user/airport-parking-reservation-detail";
    }

    @GetMapping("/affiliate/financial")
    public String getFinancialReport(Model model, Authentication auth, @RequestParam(name="range", required=false) String range, @RequestParam(name="type", required=false) String type) {
        Principal p = (Principal) auth.getPrincipal();
        String username = p.getUsername();

        //model.addAttribute("details", airportHandler.getCustomerReservationDetails(username, orderId, true));

        if (range == null || range.trim().isEmpty()) {
            model.addAttribute("range", "PREVIOUS_WEEK");
        } else {
            model.addAttribute("range", range);
        }

        if (type == null || type.trim().isEmpty()) {
            model.addAttribute("type", "ALL");
        } else {
            model.addAttribute("type", type);
        }

        model.addAttribute("financials", accountHandler.getAffiliateFinancial(username, range, type));

        return "affiliate/financial-report";
    }
}
