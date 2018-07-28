package staypark.parking.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import staypark.parking.beans.*;
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

	private static String GET_SPECIAL_DATE_PARKING_RANGE_INFO =
			"select sp_date, sp_cost, sp_prk_no " +
					"from dbstaypark.gp_sp_reservation " +
					"WHERE sp_htl_id = ? and sp_date >= ? and sp_date <= ? ";

	private static String GET_SPECIAL_DAY_PARKING_INFO =
			"select * " +
					"from dbstaypark.gp_sp_dy_reservation " +
					"WHERE dy_htl_id = ? ";

	private static String GET_RESERVATION_COUNT_FOR_RANGE_HOTEL =
			"select d.rd_st_date, count(d.rd_st_date) as count " +
					"from dbstaypark.gp_reservation_dtls as d " +
					"WHERE d.rd_htl_id = ? and d.rd_st_date >= ? AND d.rd_st_date <= ? " +
					"group by d.rd_st_date ";

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

	public static String parseDepartingLocation(final String location) {
		if (location == null) {
			return "";
		}
		String depAirport = location.replace("�", " ");

		if (depAirport.indexOf("-") != -1) {
			depAirport = depAirport.split("-")[1].trim();
		}
		return depAirport;
	}

	public static String parseDropLocation(final String dropLocation) {
		String location = dropLocation;

		if (location != null && location.indexOf("-") != -1) {
			location = location.split("-")[0].trim();
		}

		return location;
	}

	public static BigDecimal calculateParkingCosts(final int hotelId, final BigDecimal dailyFee, final Date startDate,
											 final Date endDate, final int totalDays, final Connection conn) {
		BigDecimal totalCost = new BigDecimal(0);
		Date currentDate = startDate;

		Map<Date, Integer> bookedCountMap = getReservationForRangeCountbyHotel(hotelId, startDate, endDate, conn);
		Map<Date, SpecialParkingDate> spdateMap = checkSpecialParkingForDateRange(hotelId, startDate, endDate, conn);
		Map<Integer, SpecialParkingDay> spdayMap = checkSpecialParkingForDay(hotelId, conn);

		for (int i = 0; i < totalDays; i++) {
			currentDate = DateUtil.addDaysToDate(currentDate, i);

			//Calculate cost for day if day is a special date
			if (spdateMap.get(currentDate) != null) {
				totalCost = totalCost.add(spdateMap.get(currentDate).getCost());
				//Calculate cost for day if day is a special day (mon, tues, etc)
			} else if (spdayMap.get(currentDate) != null) {
				totalCost = totalCost.add(spdayMap.get(currentDate).getCost());
				//Calculate cost for day if it doesn't have any special circumstances
			} else {
				totalCost = totalCost.add(dailyFee);
			}
		}

		totalCost = totalCost.setScale(2);

		return totalCost;
	}

	public static Map<Date, Integer> getReservationForRangeCountbyHotel(final int hotelId, final Date startDate, final Date endDate, final Connection conn) {
		Map<Date, Integer> map = new HashMap<>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement(GET_RESERVATION_COUNT_FOR_RANGE_HOTEL);
			ps.setInt(1,  hotelId);
			ps.setDate(2, new java.sql.Date(startDate.getTime()));
			ps.setDate(3, new java.sql.Date(endDate.getTime()));
			rs = ps.executeQuery();
			while (rs.next()) {
				map.put(rs.getDate("rd_st_date"), rs.getInt("count"));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(null, ps, rs);
		}

		return map;
	}

	/**
	 * Checks if given day for a hotel is considered a special parking day with different rates and number of spaces available.
	 * @param hotelId int
	 * @param startDate Date
	 * @return SpecialParkingDate - null if not special rates for given hotel and date
	 */
	public static Map<Date, SpecialParkingDate> checkSpecialParkingForDateRange(final int hotelId, final Date startDate, final Date endDate, final Connection conn) {
		Map<Date, SpecialParkingDate> map = new HashMap<>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement(GET_SPECIAL_DATE_PARKING_RANGE_INFO);
			ps.setInt(1,  hotelId);
			ps.setDate(2, new java.sql.Date(startDate.getTime()));
			ps.setDate(3, new java.sql.Date(endDate.getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				SpecialParkingDate spday = new SpecialParkingDate();
				spday.setHotelId(hotelId);
				spday.setDate(rs.getDate("sp_date"));
				spday.setNumberSpaces(rs.getInt("sp_prk_no"));
				spday.setCost(rs.getBigDecimal("sp_cost"));
				map.put(rs.getDate("sp_date"), spday);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(null, ps, rs);
		}

		return map;
	}

	public static Map<Integer, SpecialParkingDay> checkSpecialParkingForDay(final int hotelId, final Connection conn) {
		Map<Integer, SpecialParkingDay> map = new HashMap<>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = conn.prepareStatement(GET_SPECIAL_DAY_PARKING_INFO);
			ps.setInt(1,  hotelId);
			rs = ps.executeQuery();
			if (rs.next()) {
				SpecialParkingDay spday = new SpecialParkingDay();
				spday.setHotelId(hotelId);
				spday.setDayId(rs.getInt("dy_day_id"));
				spday.setNumberSpaces(rs.getInt("dy_room"));
				spday.setCost(rs.getBigDecimal("dy_cost"));
				map.put(rs.getInt("dy_day_id"), spday);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(null, ps, rs);
		}

		return map;
	}

	public static int getNumberOfParkingDays(final Date startDate, final Date endDate,
									   final String startTimeStr, final String endTimeStr) {
		long diff = endDate.getTime() - startDate.getTime();
		long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

		if (days == 0) {
			return 1;
		}

		LocalTime startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("KK:mm a"));
		LocalTime endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("KK:mm a"));

		if (endTime.isAfter(startTime)) {
			days++;
		}

		return (int)days;
	}
}
