package staypark.parking.beans;

import lombok.Data;

@Data
public class RoomParkSearch {
	private String city;
	private String hotelOptions;
	private String hotelTimeOptions;
	private String primaryCheckInDate;
	private String primaryCheckOutDate;
	private String secondaryCheckInDate;
	private String secondaryCheckOutDate;
	private int rooms;
	private int adults;
	private int children;
	private String dropOffDate;
	private String dropOffTime;
	private String pickUpDate;
	private String pickUpTime;
}
