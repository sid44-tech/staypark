package staypark.parking.controller.secure;

import staypark.parking.beans.reservation.AffiliateReservation;
import staypark.parking.beans.security.AffiliateProfile;
import staypark.parking.beans.security.ChangePassword;
import staypark.parking.beans.security.Principal;
import staypark.parking.beans.security.UserProfile;
import staypark.parking.handler.AccountHandler;
import staypark.parking.handler.AirportHandler;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
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

        String rangeParm = range;
        String typeParm = type;
        if (range == null || range.trim().isEmpty()) {
            model.addAttribute("range", "PREVIOUS_WEEK");
            rangeParm = "PREVIOUS_WEEK";
        } else {
            model.addAttribute("range", range);
        }

        if (type == null || type.trim().isEmpty()) {
            model.addAttribute("type", "ALL");
            typeParm = "ALL";
        } else {
            model.addAttribute("type", type);
        }

        model.addAttribute("financials", accountHandler.getAffiliateFinancial(username, rangeParm, typeParm));

        return "affiliate/financial-report";
    }

    @GetMapping(value = "/affiliate/financial/report.csv", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] getFinancialReportSpreadsheet(Authentication auth) throws IOException {
        Principal p = (Principal) auth.getPrincipal();
        String username = p.getUsername();

        return accountHandler.getFinancialReportSpreadsheet(username);
    }

    @GetMapping("/affiliate/financial/{affId}")
    public String getFinancialReportSpreadsheet(Model model, Authentication auth) {
        Principal p = (Principal) auth.getPrincipal();
        String username = p.getUsername();

        model.addAttribute("financials", accountHandler.getFinancialReportSpreadsheet(username));

        return "affiliate/financial-report";
    }
    
    @GetMapping("/affiliate/profile")
    public String getAffiliateProfile(Model model, Authentication auth) {
        if (!auth.isAuthenticated()) {
            return "login";
        }
        Principal p = (Principal) auth.getPrincipal();

        model.addAttribute("profile", accountHandler.getAffiliateProfile(p.getUsername()));

        return "affiliate/profile";
    }

    @PostMapping("/affiliate/profile")
    public String updateAffiliateProfile(Model model, @ModelAttribute AffiliateProfile profile, Authentication auth) {
        if (!auth.isAuthenticated()) {
            return "login";
        }

        Principal p = (Principal) auth.getPrincipal();
        model.addAttribute("profile", accountHandler.updateAffiliateProfile(p.getUsername(), profile));


        return "affiliate/profile";
    }
    
    @GetMapping("/affiliate/rooms")
    public String getAffiliateRooms(Model model, Authentication auth) {
        if (!auth.isAuthenticated()) {
            return "login";
        }

        Principal p = (Principal) auth.getPrincipal();

        model.addAttribute("rooms", accountHandler.getAffiliateRoomInfo(p.getUsername()));

        return "affiliate/rooms";
    }
    
    @GetMapping("/affiliate/password")
    public String changeAffiliatePasswordGet(Model model) {
        return "affiliate/change-password";
    }

    @PostMapping("/affiliate/password")
    public String changeAffiliatePassword(Model model, Authentication auth, @ModelAttribute ChangePassword pw) {
        Principal p = (Principal) auth.getPrincipal();
        boolean success = accountHandler.changePassword(p.getUsername(), pw.getCurrentPassword(), pw.getNewPassword());
        if (success) {
            model.addAttribute("message", "success");
        } else {
            model.addAttribute("message", "error");
        }

        return "affiliate/change-password";
    }
}
