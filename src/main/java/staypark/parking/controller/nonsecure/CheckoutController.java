package staypark.parking.controller.nonsecure;

import staypark.parking.beans.booking.ParkingCheckout;
import staypark.parking.beans.booking.RoomParkInput;
import staypark.parking.beans.payment.Payment;
import staypark.parking.handler.CheckoutHandler;
import staypark.parking.util.SecurityUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
                               @RequestParam(name="endHotelArrivalDateMDY", required = false) String endHotelArrivalDateMDY,
                               @RequestParam(name="rooms", required = false) String rooms,
                               @RequestParam(name="totalAdults", required = false) String totalAdults,
                               @RequestParam(name="children", required = false) String children,
                               @RequestParam(name="carDropOffDateMDY", required = false) String carDropOffDateMDY,
                               @RequestParam(name="carDropOffTime", required = false) String carDropOffTime,
                               @RequestParam(name="carPickUpDateMDY", required = false) String carPickUpDateMDY,
                               @RequestParam(name="carPickUpTime", required = false) String carPickUpTime,
                               Authentication auth, @RequestHeader HttpHeaders headers) {

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
		reqParms.setEndHotelArrivalDateMDY(endHotelArrivalDateMDY);
		reqParms.setRooms(rooms);
		reqParms.setTotalAdults(totalAdults);
		reqParms.setChildren(children);
		reqParms.setCarDropOffDateMDY(carDropOffDateMDY);
		reqParms.setCarDropOffTime(carDropOffTime);
		reqParms.setCarPickUpDateMDY(carPickUpDateMDY);
		reqParms.setCarPickUpTime(carPickUpTime);
		reqParms.setUsername(username);

        ParkingCheckout checkout;
        String template;

		if (searchType != null) {
		    if (searchType.equals("R")) {
                checkout = checkoutHandler.getRoomAndParkingCheckoutInfo(reqParms);
                checkout.setCheckoutType("R");
                template = "checkout/parking-checkout";
            } else if (searchType.equals("S")) {
                checkout = checkoutHandler.getRoomAndParkingCheckoutInfo(reqParms);
                checkout.setCheckoutType("S");
                template = "checkout/parking-checkout";
            } else if (searchType.equals("E")) {
                checkout = checkoutHandler.getRoomAndParkingCheckoutInfo(reqParms);
                checkout.setCheckoutType("E");
                template = "checkout/parking-checkout";
            } else if (searchType.equals("B")) {
                checkout = checkoutHandler.getRoomAndParkingCheckoutInfo(reqParms);
                checkout.setCheckoutType("B");
                template = "checkout/parking-checkout";
            } else {
                checkout = checkoutHandler.getParkingCheckoutInfo(reqParms);
                checkout.setCheckoutType("P");
                template = "checkout/parking-checkout";
            }
        } else {
            checkout = checkoutHandler.getParkingCheckoutInfo(reqParms);
            checkout.setCheckoutType("P");
            template = "checkout/parking-checkout";
        }

        model.addAttribute("checkout", checkout);
	    return template;
    }

    @PostMapping("/parking/checkout/{hotelId}")
    public String checkoutParkPost(Model model, @PathVariable("hotelId") int hotelId, @ModelAttribute ParkingCheckout checkout, Authentication auth, @RequestHeader HttpHeaders headers) {
	    checkout.setHotelId(String.valueOf(hotelId));

        Payment payment = checkoutHandler.handleBookingSubmit(checkout, SecurityUtil.getAuthenticatedUsername(auth));

        if (!payment.isSuccess()) {
            model.addAttribute("checkout", checkout);
            model.addAttribute("error", payment.getPaymentResponse());
            return "checkout/parking-checkout";
        }

        return "checkout/confirm-checkout";
    }

    @PostMapping("/hotel-parking/checkout/{hotelId}")
    public String checkoutHotelParkInformation(Model model, @PathVariable("hotelId") int hotelId, @ModelAttribute ParkingCheckout checkout, Authentication auth, @RequestHeader HttpHeaders headers) {
//        checkout.setHotelId(String.valueOf(hotelId));
//
//        checkoutHandler.handleBookingSubmit(checkout, SecurityUtil.getAuthenticatedUsername(auth));

        return "checkout/parking-checkout";
    }

    @GetMapping("/parking/checkout")
    public String checkoutParkPost(Model model, @RequestHeader HttpHeaders headers) {

        return "checkout/confirm-checkout";
    }

    @GetMapping("/roompark/checkout")
    public String checkoutRoomPark(Model model, @RequestHeader HttpHeaders headers) {

	    return "checkout/parking-checkout";
    }
}
