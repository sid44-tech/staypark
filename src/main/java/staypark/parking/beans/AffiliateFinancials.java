package staypark.parking.beans;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AffiliateFinancials {
    private List<AffiliateFinancialRecord> details = new ArrayList<>();
    private String allTimeTotal = "0.00 USD";
    private String filterTotal = "0.00 USD";

    public void addDetailRecord(final AffiliateFinancialRecord record) {
        details.add(record);
    }

}
