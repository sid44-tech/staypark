package staypark.parking.controller.nonsecure;

import staypark.parking.beans.RoomParkSearch;
import staypark.parking.handler.AirportHandler;
import staypark.parking.handler.HotelHandler;

import staypark.parking.util.AirportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AirportController {

    @Autowired
    private AirportHandler airportHandler;

    @Autowired
    private HotelHandler hotelHandler;

	@GetMapping("/airport/{airport}")
    public String airportDetails(@PathVariable("airport") String airport, Model model) {
        model.addAttribute("airport", airportHandler.getAirportDetails(airport));
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));
        
        return "airport-detail";
    }
	
    @PostMapping("/room-park-results")
    public String roomParkResults(Model model, @ModelAttribute RoomParkSearch search) {
        model.addAttribute("results", hotelHandler.getHotelslForSearch(search));
        model.addAttribute("usDepart", AirportUtil.getDepartureLocations(1));
        model.addAttribute("canadaDepart", AirportUtil.getDepartureLocations(2));

        return "room-park-results";
    }
}
