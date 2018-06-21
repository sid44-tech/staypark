package staypark.parking.beans;

import lombok.Data;

@Data
public class Room {
    private int roomId;
    private ImageLink image;
    private String roomDescription;
    private String occupancy;
    private String cost;
}
