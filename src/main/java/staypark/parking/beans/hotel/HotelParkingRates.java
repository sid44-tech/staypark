package staypark.parking.beans.hotel;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HotelParkingRates {
    private BigDecimal baseRate;
    private BigDecimal oneToSevenDaysRate;
    private BigDecimal eightToFourteenDaysRate;
    private BigDecimal upToFifteenDaysRate;
    private BigDecimal hotelBaseRate;
    private BigDecimal parkingBaseRate;
    private BigDecimal totalBaseRate;
}
