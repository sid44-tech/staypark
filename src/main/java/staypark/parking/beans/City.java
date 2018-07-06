package staypark.parking.beans;

import lombok.Data;

@Data
public class City {
    private int countryId;
    private int stateId;
    private int cityId;
    private String cityName;
}
