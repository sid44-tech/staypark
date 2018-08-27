package staypark.parking.beans.affiliate;

import lombok.Data;

@Data
public class AffiliateFinancialRecord {
    private String reservationDate;
    private String orderNumber;
    //private String hotelName;
    private String customerName;
    //private String airportName;
    private String baseFee;
    private String salesTax;
    private String type;
}
