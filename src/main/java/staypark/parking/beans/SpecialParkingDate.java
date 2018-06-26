package staypark.parking.beans;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class SpecialParkingDate {
	private int hotelId;
	private Date date;
	private BigDecimal cost;
	private int numberSpaces;
}
