package staypark.parking.beans.security;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CaptchaResponse {
    private Boolean success;

    @SerializedName(value = "challenge_ts")
    private String challengeTs;

    private String hostname;
//
//    @SerializedName(value = "error-codes")
//    private String errorCodes;
}
