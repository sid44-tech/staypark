package staypark.parking.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import staypark.parking.beans.City;
import staypark.parking.beans.Country;
import staypark.parking.beans.State;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AirportUtil {
	private static List<String> usDepartureLocations = null;
	private static List<String> canadaDepartureLocations = null;
	private static List<String> usDepartureLocationsRoomPark = null;
	private static List<String> canadaDepartureLocationsRoomPark = null;
	private static List<Country> countries  = null;
	//key is country id and list is states
	private static Map<Integer, List<State>> states = null;
	//key is state id and list is cities
	private static Map<Integer, List<City>> cities = null;
	
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

	private static String GET_COUNTRIES = "select * from dbstaypark.gp_countries order by country_name desc ";
	private static String GET_STATES = "select * from dbstaypark.gp_state where gps_status = 'Active'  order by gps_state ";
	private static String GET_CITIES = "select * from dbstaypark.gp_city where gpc_status = 'Active'  order by gpc_city ";

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

	public static List<Country> getCountries() {
		if (countries != null) {
			return countries;
		}

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_COUNTRIES);
			rs = ps.executeQuery();
			countries = new ArrayList<>();

			while (rs.next()) {
				if (rs.getInt("id") < 3) {
					Country country = new Country();
					country.setCountryId(rs.getInt("id"));
					country.setCountryName(rs.getString("country_name"));
					countries.add(country);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}

		return countries;
	}

	public static List<State> getStates(int countryId) {
		if (states != null) {
			return states.get(countryId);
		}

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_STATES);
			rs = ps.executeQuery();
			states = new HashMap<>();
			states.put(1, new ArrayList<State>());
			states.put(2, new ArrayList<State>());

			while (rs.next()) {
				String stateName = rs.getString("gps_state");
				if (stateName.contains("CANADA")) {
					State state = new State();
					state.setStateId(rs.getInt("gps_id"));
					state.setCountryId(2);
					state.setStateName(stateName.replace("CANADA", "").trim());
					states.get(2).add(state);
				} else if (stateName.contains("ENGLAND") || stateName.contains("SCOTLAND")) {
					//Do nothing.  Don't want to do UK anymore.  Maybe because of Brexit?  Or Maybe because of tariffs.
				} else {
					State state = new State();
					state.setStateId(rs.getInt("gps_id"));
					state.setCountryId(1);
					state.setStateName(stateName.trim());
					states.get(1).add(state);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}

		return states.get(countryId);
	}

	public static List<City> getCities(final int stateId) {
		if (cities != null) {
			return cities.get(stateId);
		}

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_CITIES);
			rs = ps.executeQuery();
			cities = new HashMap<>();

			while (rs.next()) {
				int sid = rs.getInt("gpc_state_id");
				if (cities.get(sid) == null) {
					cities.put(sid, new ArrayList<>());
				}
				City city = new City();
				city.setStateId(sid);
				city.setCityId(rs.getInt("gpc_id"));
				city.setCityName(rs.getString("gpc_city"));
				cities.get(sid).add(city);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}

		return cities.get(stateId);
	}
}
