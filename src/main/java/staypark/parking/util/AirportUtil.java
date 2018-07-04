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
	private static List<String> usDepartureLocationsRoomPark = null;
	private static List<String> canadaDepartureLocationsRoomPark = null;
	
	private static String GET_DEPARTURE_LOCATIONS = 
			"select gdl_scode, gdl_location, gdl_country from dbstaypark.gp_deperture_location " + 
			"where gdl_country = ? and gdl_status = 'Active' " + 
			"order by gdl_country, gdl_location";

	private static String GET_ROOM_AND_PARK = "select distinct c.gpc_city, d.gdl_scode, d.gdl_logo_title, d.gdl_country " +
			"from dbstaypark.gp_htl_day_range_cost h " +
			"left join dbstaypark.gp_deperture_location d on h.drc_gpi_dep_id = d.gdl_id " +
			"left join dbstaypark.gp_city c on d.gdl_city_id= c.gpc_id " +
			"left join dbstaypark.gp_state s on d.gdl_state_id= s.gps_id " +
			"WHERE h.drc_area = 'both' and h.drc_status = 'Active' and c.gpc_city is not null " +
			"order by c.gpc_city ";

	/**
	 * This method returns bla bla bla
	 * @param in String This is the airport text
	 * @return String This is the airport code
	 */
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

	private static String buildRPDepartureLocation(final String city, final String title, final String code) {
		return city + " : " + title + " (" + code + ")";
	}

	public static String getCurrencyFromId(final int id) {
		if (id == 2) {
			return "USD";
		} else if (id == 3) {
			return "GBP";
		}

		return "USD";
	}

	public static List<String> getRoomAndParkLocations(final int countryCode) {
		if (countryCode == 1 && usDepartureLocationsRoomPark != null) {
			return usDepartureLocationsRoomPark;
		} else if (countryCode == 2 && canadaDepartureLocationsRoomPark != null) {
			return canadaDepartureLocationsRoomPark;
		}

		List<String> list = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_ROOM_AND_PARK);
			rs = ps.executeQuery();

			while (rs.next()) {
				if (rs.getInt("gdl_country") == countryCode) {
					list.add(buildRPDepartureLocation(rs.getString("gpc_city"), rs.getString("gdl_logo_title"),
							rs.getString("gdl_scode")));
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}

		if (countryCode == 1) {
			usDepartureLocationsRoomPark = list;
			return usDepartureLocationsRoomPark;
		} else if (countryCode == 2) {
			canadaDepartureLocationsRoomPark = list;
			return canadaDepartureLocationsRoomPark;
		}

		return new ArrayList<>();
	}
}
