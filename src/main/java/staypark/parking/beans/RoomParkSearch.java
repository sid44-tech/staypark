package staypark.parking.beans;

import java.util.Date;

import lombok.Data;

@Data
public class RoomParkSearch {
	private String city;
	private String hotelOptions;
	private String hotelTimeOptions;
	private Date primaryCheckInDate;
	private Date primaryCheckOutDate;
	private Date secondaryCheckInDate;
	private Date secondaryCheckOutDate;
	private int rooms;
	private int adults;
	private int children;
	private Date dropOffDate;
	private String dropOffTime;
	private Date pickUpDate;
	private String pickUpTime;
}
