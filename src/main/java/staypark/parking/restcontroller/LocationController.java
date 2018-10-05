package staypark.parking.restcontroller;

import staypark.parking.beans.general.City;
import staypark.parking.beans.general.Country;
import staypark.parking.beans.general.State;
import staypark.parking.util.AirportUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LocationController {

    @RequestMapping("/countries")
    public List<Country> getCountries(@RequestHeader HttpHeaders headers) {
        return AirportUtil.getCountries();
    }

    @RequestMapping("/countries/{countryId}/states")
    public List<State> getStates(@PathVariable("countryId") int countryId, @RequestHeader HttpHeaders headers) {
        return AirportUtil.getStates(countryId);
    }

    @RequestMapping("/states/{stateId}/cities")
    public List<City> getCities(@PathVariable("stateId") int stateId, @RequestHeader HttpHeaders headers) {
        return AirportUtil.getCities(stateId);
    }
}
