package staypark.parking.beans;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Room {
    private int roomId;
    private ImageLink image;
    private String roomDescription;
    private int occupancy;
    private int maxOccupancy;
    private BigDecimal baseCost;
    private BigDecimal totalCost;
    private BigDecimal extraPersonCost;
    private BigDecimal extraChildCost;
}
