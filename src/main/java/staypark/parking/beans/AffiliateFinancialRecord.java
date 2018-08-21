package staypark.parking.beans;

import lombok.Data;

@Data
public class AffiliateFinancialRecord {
    private String reservationDate;
    private String orderNumber;
    private String hotelName;
    private String customerName;
    private String airportName;
    private String baseFee;
    private String salesTax;
}
