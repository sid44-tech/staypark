package staypark.parking.controller;

import staypark.parking.beans.RoomParkSearch;
import staypark.parking.handler.AirportHandler;
import staypark.parking.handler.HotelHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserAccountController {

    @GetMapping("/s/hello")
    public String airportDetails(@PathVariable("airport") String airport, Model model) {

        return "helloe";
    }
}
