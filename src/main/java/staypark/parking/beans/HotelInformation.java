package staypark.parking.beans;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HotelInformation {
    private int hotelId;
    private String hotelTitle;
    private String cityState;
    private List<ImageLink> imageLinks;
    private List<Room> roomList;
    private String hotelText;
    private BigDecimal carBaseRate;
    private BigDecimal carUpToSevenRate;
    private BigDecimal carEightToFourteenRate;
    private BigDecimal carOverFifteenRate;
    private String logoImage;
    private double airportDistance;
    private String shuttleFrequency;
    private String parkingDescription;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal parkingCost;
    private String parkingCostDescription;
    private boolean soldout;
    private String currencyType;
}
