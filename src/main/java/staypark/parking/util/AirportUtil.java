package staypark.parking.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AirportUtil {
	private static List<String> usDepartureLocations = null;
	private static List<String> canadaDepartureLocations = null;
	
	private static String GET_DEPARTURE_LOCATIONS = 
			"select gdl_scode, gdl_location, gdl_country from dbstaypark.gp_deperture_location " + 
			"where gdl_country = ? and gdl_status = 'Active' " + 
			"order by gdl_country, gdl_location";

	public static String getAirportCodeFromSearchCityString(final String in) {
		String[] str = in.split("\\(");
		if (str.length == 2) {
			String[] codearr = str[1].split("\\)");
			if (codearr.length == 1) {
				return codearr[0];
			}
		}

		return "";
	}

	public static List<String> getDepartureLocations(final int countryCode) {
		if (countryCode == 1 && usDepartureLocations != null) {
			return usDepartureLocations;
		} else if (countryCode == 2 && canadaDepartureLocations != null) {
			return canadaDepartureLocations;
		}
		List<String> list = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_DEPARTURE_LOCATIONS);
			ps.setInt(1, countryCode);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(buildDepartureLocation(rs.getString("gdl_location"), rs.getString("gdl_scode")));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}
		
		if (countryCode == 1) {
			usDepartureLocations = list;
			return usDepartureLocations;
		} else if (countryCode == 2) {
			canadaDepartureLocations = list;
			return canadaDepartureLocations;
		}
		
		return new ArrayList<String>();
	}
	
	private static String buildDepartureLocation(final String airportName, final String airportCode) {
		return airportName + " (" + airportCode + ")"; 
	}

	public static String getCurrencyFromId(final int id) {
		if (id == 2) {
			return "CAD";
		} else if (id == 3) {
			return "GBP";
		}

		return "USD";
	}

}
