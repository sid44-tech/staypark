package staypark.parking.beans.booking;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomParkInput {
	 private int hotelId;
	 private String dropOffDate;
	 private String dropOffTime;
	 private String pickUpDate;
	 private String pickUpTime;
	 private String couponCode;
     private String searchType;
     private String roomId;
     private String hotelArrivalDateMDY;
     private String hotelDepartureDateMDY;
     private String orderId;
     private String endHotelDepartureDateMDY;
     private String endHotelArrivalDateMDY;
     private String rooms;
     private String totalAdults;
     private String children;
     private String carDropOffDateMDY;
     private String carDropOffTime;
     private String carPickUpDateMDY;
     private String carPickUpTime;
     private String username;
}
