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
    private String googleMapLink;
    private BigDecimal carBaseRate;
    private BigDecimal carUpToSevenRate;
    private BigDecimal carEightToFourteenRate;
    private BigDecimal carOverFifteenRate;
    private String logoImage;
}
