package staypark.parking.beans.reservation;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SpecialParkingDay {
	private int hotelId;
	private int dayId;
	private BigDecimal cost;
	private int numberSpaces;
}
