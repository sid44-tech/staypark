package staypark.parking.controller;

import staypark.parking.beans.booking.ParkingCheckout;
import staypark.parking.beans.booking.RoomParkInput;
import staypark.parking.handler.CheckoutHandler;
import staypark.parking.util.SecurityUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CheckoutController {

    @Autowired
    private CheckoutHandler checkoutHandler;

	@GetMapping("/parking/checkout/{hotelId}")
    public String checkoutPark(Model model,
                               @PathVariable("hotelId") int hotelId,
                               @RequestParam(name="dropOffDate", required = false) String dropOffDate,
                               @RequestParam(name="dropOffTime", required = false) String dropOffTime,
                               @RequestParam(name="pickUpDate", required = false) String pickUpDate,
                               @RequestParam(name="pickUpTime", required = false) String pickUpTime,
                               @RequestParam(name="couponCode", required = false) String couponCode,
                               @RequestParam(name="searchType", required = false) String searchType,
                               @RequestParam(name="roomId", required = false) String roomId,
                               @RequestParam(name="hotelArrivalDateMDY", required = false) String hotelArrivalDateMDY,
                               @RequestParam(name="hotelDepartureDateMDY", required = false) String hotelDepartureDateMDY,
                               @RequestParam(name="endHotelArrivalDateMDY", required = false) String orderId,
                               @RequestParam(name="endHotelDepartureDateMDY", required = false) String endHotelDepartureDateMDY,
                               @RequestParam(name="rooms", required = false) String rooms,
                               @RequestParam(name="totalAdults", required = false) String totalAdults,
                               @RequestParam(name="children", required = false) String children,
                               @RequestParam(name="carDropOffDateMDY", required = false) String carDropOffDateMDY,
                               @RequestParam(name="carDropOffTime", required = false) String carDropOffTime,
                               @RequestParam(name="carPickUpDateMDY", required = false) String carPickUpDateMDY,
                               @RequestParam(name="carPickUpTime", required = false) String carPickUpTime,
                               Authentication auth) {

		String username = SecurityUtil.getAuthenticatedUsername(auth);
		
		RoomParkInput reqParms = new RoomParkInput();
		reqParms.setHotelId(hotelId);
		reqParms.setDropOffDate(dropOffDate);
		reqParms.setDropOffTime(dropOffTime);
		reqParms.setPickUpDate(pickUpDate);
		reqParms.setPickUpTime(pickUpTime);
		reqParms.setCouponCode(couponCode);
		reqParms.setSearchType(searchType);
		reqParms.setRoomId(roomId);
		reqParms.setHotelArrivalDateMDY(hotelArrivalDateMDY);
		reqParms.setHotelDepartureDateMDY(hotelDepartureDateMDY);
		reqParms.setOrderId(orderId);
		reqParms.setEndHotelDepartureDateMDY(endHotelDepartureDateMDY);
		reqParms.setRooms(rooms);
		reqParms.setTotalAdults(totalAdults);
		reqParms.setChildren(children);
		reqParms.setCarDropOffDateMDY(carDropOffDateMDY);
		reqParms.setCarDropOffTime(carDropOffTime);
		reqParms.setCarPickUpDateMDY(carPickUpDateMDY);
		reqParms.setCarPickUpTime(carPickUpTime);
		reqParms.setUsername(username);
		
		if (searchType != null) {
		    if (searchType.equals("R")) {
                model.addAttribute("checkout", checkoutHandler.getRoomAndParkingCheckoutInfo(reqParms));
                return "checkout/hotel-park-checkout";
            } else if (searchType.equals("S")) {
                model.addAttribute("checkout", checkoutHandler.getRoomAndParkingCheckoutInfo(reqParms));
                return "checkout/hotel-park-checkout";
            } else if (searchType.equals("E")) {
                model.addAttribute("checkout", checkoutHandler.getRoomAndParkingCheckoutInfo(reqParms));
                return "checkout/hotel-park-checkout";
            } else if (searchType.equals("B")) {
                model.addAttribute("checkout", checkoutHandler.getRoomAndParkingCheckoutInfo(reqParms));
                return "checkout/hotel-park-checkout";
            }
        }

        model.addAttribute("checkout", checkoutHandler.getParkingCheckoutInfo(reqParms));
	    return "checkout/parking-checkout";
    }

    @PostMapping("/parking/checkout/{hotelId}")
    public String checkoutParkPost(Model model, @PathVariable("hotelId") int hotelId, @ModelAttribute ParkingCheckout checkout, Authentication auth) {
	    checkout.setHotelId(String.valueOf(hotelId));

        checkoutHandler.handleBookingSubmit(checkout, SecurityUtil.getAuthenticatedUsername(auth));

        return "checkout/confirm-checkout";
    }

    @PostMapping("/hotel-parking/checkout/{hotelId}")
    public String checkoutHotelParkInformation(Model model, @PathVariable("hotelId") int hotelId, @ModelAttribute ParkingCheckout checkout, Authentication auth) {
//        checkout.setHotelId(String.valueOf(hotelId));
//
//        checkoutHandler.handleBookingSubmit(checkout, SecurityUtil.getAuthenticatedUsername(auth));

        return "checkout/parking-checkout";
    }

    @GetMapping("/parking/checkout")
    public String checkoutParkPost(Model model) {

        return "checkout/confirm-checkout";
    }

    @GetMapping("/roompark/checkout")
    public String checkoutRoomPark(Model model) {

	    return "checkout/parking-checkout";
    }
}
