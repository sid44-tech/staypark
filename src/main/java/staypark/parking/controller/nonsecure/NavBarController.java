package staypark.parking.controller.nonsecure;

import staypark.parking.beans.affiliate.AffiliateRegister;
import staypark.parking.beans.general.ContactUs;
import staypark.parking.handler.AccountHandler;
import staypark.parking.util.AirportUtil;
import staypark.parking.util.EmailUtil;
import staypark.parking.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Controller
@Slf4j
public class NavBarController {

    @Autowired
    private AccountHandler accountHandler;

	@GetMapping("/about")
    public String about(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
	    return "about";
    }
	
	@GetMapping("/airport-parking")
    public String airportParking(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
	    return "airport-parking";
    }
	
	@GetMapping("/room-park")
    public String roomPark(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getRoomAndParkLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getRoomAndParkLocations(2));
	    return "room-park";
    }
	
	@GetMapping("/faq")
    public String faq(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
	    return "faq";
    }

	@GetMapping("/contact-us")
    public String contactUs(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("status", "");
        model.addAttribute("contactUsMessage", new ContactUs());
        model.addAttribute("messageVisible", "bu-sent-div");
        model.addAttribute("captchaFailed", "false");
	    return "contact-us";
    }

    @PostMapping("/contact-us")
    public String contactUsPost(Model model, @ModelAttribute ContactUs contactUsMessage, @RequestHeader HttpHeaders headers, HttpServletRequest request) {
	    String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
	    String ip = "";
	    if (headers != null && headers.get("x-forwarded-for") != null && headers.get("x-forwarded-for").get(0) != null) {
            ip = headers.get("x-forwarded-for").get(0);
        }
        boolean captchaPassed = SecurityUtil.checkCaptcha(gRecaptchaResponse, ip);
        if (!captchaPassed) {
            model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
            model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
            model.addAttribute("contactUsMessage", contactUsMessage);
            model.addAttribute("messageVisible", "bu-sent-div");
            model.addAttribute("captchaFailed", "true");
            return "contact-us";
        } else {
            model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
            model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
            model.addAttribute("status", "bu-sent");
            model.addAttribute("contactUsMessage", new ContactUs());
        }

        try {
            String BODY = EmailUtil.CONTACT_US_BODY
                    .replace("[EMAIL]", contactUsMessage.getEmail())
                    .replace("[NAME]", contactUsMessage.getName())
                    .replace("[PHONE]", contactUsMessage.getPhone());
            if (contactUsMessage.getComment() != null && !contactUsMessage.getComment().trim().equals("")) {
                BODY = BODY.replace("[COMMENT]", contactUsMessage.getComment());
            }
            boolean result = EmailUtil.sendEmail(EmailUtil.INFO_EMAIL, "Contact Details from User", BODY);
            if (result) {
                model.addAttribute("messageVisible", "bu-sent-div bu-message-visible");
            } else {
                model.addAttribute("messageVisible", "bu-sent-div");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "contact-us";
    }

    @GetMapping("/affiliates")
    public String affiliates(Model model, @RequestHeader HttpHeaders headers) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("countries", AirportUtil.getCountries());
        model.addAttribute("states", AirportUtil.getStates(1));
        model.addAttribute("cities", AirportUtil.getCities(1));
        model.addAttribute("affiliate", new AffiliateRegister());
        model.addAttribute("success", "false");
        return "affiliates";
    }

    @PostMapping("/affiliates")
    public String affiliateRegister(Model model, @RequestPart(value = "hotelLogo", required = false) MultipartFile hotelLogo, @ModelAttribute AffiliateRegister affiliate, @RequestHeader HttpHeaders headers, HttpServletRequest request) {
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        model.addAttribute("countries", AirportUtil.getCountries());
        model.addAttribute("states", AirportUtil.getStates(1));
        model.addAttribute("cities", AirportUtil.getCities(1));
        boolean imageError = false;

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        String ip = "";
        if (headers != null && headers.get("x-forwarded-for") != null && headers.get("x-forwarded-for").get(0) != null) {
            ip = headers.get("x-forwarded-for").get(0);
        }
        boolean captchaPassed = SecurityUtil.checkCaptcha(gRecaptchaResponse, ip);
        if (!captchaPassed) {
            model.addAttribute("affiliate", affiliate);
            model.addAttribute("captchaFailed", "true");
            return "affiliates";
        }

        if (!hotelLogo.getOriginalFilename().equals("")) {
            if (hotelLogo.getSize() > 1000000) {
                imageError = true;
                model.addAttribute("imageError", "Image is too large.  Limit image size to 1 MB.");
            }

            try {
                BufferedImage img = ImageIO.read(hotelLogo.getInputStream());
                if (img == null) {
                    imageError = true;
                    model.addAttribute("imageError", "File uploaded is not an image.  Please verify logo is an image file.");
                }
            } catch (IOException e) {
                //do nothing just checking if image is really an image
            }
        }

        if (imageError) {
            model.addAttribute("imageErrorCheck", true);
            model.addAttribute("affiliate", affiliate);
            model.addAttribute("success", "false");
        } else {
            model.addAttribute("imageErrorCheck", false);
            model.addAttribute("affiliate", new AffiliateRegister());
            model.addAttribute("success", accountHandler.createAffiliateAccount(affiliate));
        }

        return "affiliates";
    }
}
