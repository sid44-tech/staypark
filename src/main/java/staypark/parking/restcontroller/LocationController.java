package staypark.parking.restcontroller;

import staypark.parking.beans.City;
import staypark.parking.beans.Country;
import staypark.parking.beans.State;
import staypark.parking.util.AirportUtil;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LocationController {

    @RequestMapping("/countries")
    public List<Country> getCountries() {
        return AirportUtil.getCountries();
    }

    @RequestMapping("/countries/{countryId}/states")
    public List<State> getStates(@PathVariable("countryId") int countryId) {
        return AirportUtil.getStates(countryId);
    }

    @RequestMapping("/states/{stateId}/cities")
    public List<City> getCities(@PathVariable("stateId") int stateId) {
        return AirportUtil.getCities(stateId);
    }
}
