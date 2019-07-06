package staypark.parking.beans.hotel;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import staypark.parking.beans.general.ImageLink;
import staypark.parking.beans.reservation.Room;

@Data
public class HotelInformation {
    private int hotelId;
    private String hotelTitle;
    private String cityState;
    private List<ImageLink> imageLinks;
    private List<ImageLink> parkingImageLinks;
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
    private boolean soldout = false;
    private String currencyType;
    private int numberParkingSpaces;
    private String parkingPaymentType = "";
    private BigDecimal parkingStandardDailyFee;
    private BigDecimal totalCost;
    private int recommendedSort;

    private boolean minimumDaysEnabled = false;
    private String minimumDaysMessage = "";

    private boolean ad = false;

    private String cssClass = "card col-sm p-results-custom no-padding";
}
