package staypark.parking.handler;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import staypark.parking.util.AirportUtil;
import org.springframework.stereotype.Service;

import staypark.parking.beans.HotelInformation;
import staypark.parking.beans.ImageLink;
import staypark.parking.beans.SpecialParkingDate;
import staypark.parking.beans.SpecialParkingDay;
import staypark.parking.util.DBUtil;
import staypark.parking.util.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ParkingHandler {
	private static String GET_PARKING_LOCATIONS = 
			"select p.*, d.*, c.gpc_id, c.gpc_state_id, c.gpc_city, c.gpc_status, s.gps_id, s.gps_state, s.gps_status " + 
			"from dbstaypark.gp_parking_info as p " + 
			"left join dbstaypark.gp_deperture_location as d on p.gpi_dep_id = d.gdl_id " + 
			"left join dbstaypark.gp_city as c on d.gdl_city_id= c.gpc_id " + 
			"left join dbstaypark.gp_state as s on d.gdl_state_id=s.gps_id " + 
			"where d.gdl_scode=? and p.gpi_status='Active' ";

	private static String GET_RESERVATION_COUNT_FOR_DAY_HOTEL =
			"select count(*) as count " +
			"from dbstaypark.gp_reservation_dtls as d " +
			"WHERE d.rd_htl_id = ? and d.rd_st_date = ? ";
	
	private static String GET_SPECIAL_DATE_PARKING_INFO = 
			"select sp_cost, sp_prk_no " +
			"from dbstaypark.gp_sp_reservation " + 
			"WHERE sp_htl_id = ? and sp_date = ? ";
	
	private static String GET_SPECIAL_DAY_PARKING_INFO = 
			"select * " + 
			"from dbstaypark.gp_sp_dy_reservation " +
			"WHERE dy_htl_id = ? and dy_day_id = ? ";
	
	private static String GET_PARKING_IMAGES_FOR_HOTEL =
			"select gppi_timage, gppi_limage " + 
			"from dbstaypark.gp_parking_image " + 
			"where gppi_p_id = ? ";
	
	public List<HotelInformation> getParkingSearchResults(final String airportCode, final String startDateStr, final String endDateStr,
			final String startTimeStr, final String endTimeStr) {
		Date startDate = DateUtil.stringMDYToDate(startDateStr);
		Date endDate = DateUtil.stringMDYToDate(endDateStr);
		
		int totalParkingDays = getNumberOfParkingDays(startDate, endDate, startTimeStr, endTimeStr);
		
		List<HotelInformation> hotels = getParkingLocations(airportCode);
		
		for (HotelInformation hotel : hotels) {
			calculateParkingCost(hotel, startDate, totalParkingDays);
			hotel.setParkingImageLinks(getParkingImagesForHotel(hotel.getHotelId()));
		}
		
		return hotels;
	}

	private List<HotelInformation> getParkingLocations(final String airportCode) {
		List<HotelInformation> hotels = new ArrayList<>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_PARKING_LOCATIONS);
			ps.setString(1,  airportCode);
			rs = ps.executeQuery();
			while (rs.next()) {
				HotelInformation hotel = new HotelInformation();
				hotel.setHotelId(rs.getInt("gpi_id"));
				hotel.setLogoImage("/images/hotel-logo/" + rs.getString("gpi_logo"));
				hotel.setHotelTitle(rs.getString("gpi_inn_name"));
				hotel.setAirportDistance(rs.getDouble("gpi_distence"));
				hotel.setShuttleFrequency(rs.getString("gpi_shuttel"));
				hotel.setParkingDescription(rs.getString("gpi_desc"));
				hotel.setLatitude(rs.getBigDecimal("gpi_latitude"));
				hotel.setLongitude(rs.getBigDecimal("gpi_longitude"));
				hotel.setCurrencyType(AirportUtil.getCurrencyFromId(rs.getInt("gdl_country")));
				hotel.setParkingCost(new BigDecimal(0.00));
				hotel.setParkingCostDescription("Based on some number of days");
				hotel.setSoldout(false);
				hotel.setNumberParkingSpaces(rs.getInt("gpi_pno"));
				hotel.setParkingPaymentType(rs.getString("payment_type"));
				hotel.setParkingStandardDailyFee(rs.getBigDecimal("gpi_feepday"));
				hotels.add(hotel);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}
		
		return hotels;
	}
	
	private HotelInformation calculateParkingCost(final HotelInformation hotel, final Date startDate, final int totalDays) {
		BigDecimal totalCost = new BigDecimal(0);
		Date currentDate = startDate;

		for (int i = 0; i < totalDays; i++) {
			currentDate = DateUtil.addDaysToDate(currentDate, i);
			int dayId = DateUtil.getNumericalDayOfWeek(currentDate);

			int bookedCount = getReservationForDayCountbyHotel(hotel.getHotelId(), currentDate);
			SpecialParkingDate spdate = checkSpecialParkingForDate(hotel.getHotelId(), currentDate);
			SpecialParkingDay spday = checkSpecialParkingForDay(hotel.getHotelId(), dayId);

			//Calculate cost for day if day is a special date
			if (spdate != null) {
				totalCost = totalCost.add(spdate.getCost());
				if (spdate.getNumberSpaces() < bookedCount) {
					hotel.setSoldout(true);
				}
			//Calculate cost for day if day is a special day (mon, tues, etc)
			} else if (spday != null) {
				totalCost = totalCost.add(spday.getCost());
				if (spday.getNumberSpaces() < bookedCount) {
					hotel.setSoldout(true);
				}
			//Calculate cost for day if it doesn't have any special circumstances
			} else {
				totalCost = totalCost.add(hotel.getParkingStandardDailyFee());
				if (hotel.getNumberParkingSpaces() < bookedCount) {
					hotel.setSoldout(true);
				}
			}
		}
		
		hotel.setTotalCost(totalCost.setScale(2));
		
		return hotel;
	}
	
	private int getReservationForDayCountbyHotel(final int hotelId, final Date date) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;

		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_RESERVATION_COUNT_FOR_DAY_HOTEL);
			ps.setInt(1,  hotelId);
			ps.setDate(2, new java.sql.Date(date.getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				count = rs.getInt("count");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}

		return count;
	}

	
	/**
	 * Checks if given day for a hotel is considered a special parking day with different rates and number of spaces available.
	 * @param hotelId int
	 * @param date Date
	 * @return SpecialParkingDate - null if not special rates for given hotel and date
	 */
	private SpecialParkingDate checkSpecialParkingForDate(final int hotelId, final Date date) {
		SpecialParkingDate spday = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_SPECIAL_DATE_PARKING_INFO);
			ps.setInt(1,  hotelId);
			ps.setDate(2, new java.sql.Date(date.getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				spday = new SpecialParkingDate();
				spday.setHotelId(hotelId);
				spday.setDate(date);
				spday.setNumberSpaces(rs.getInt("sp_prk_no"));
				spday.setCost(rs.getBigDecimal("sp_cost"));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}

		return spday;
	}
	
	private SpecialParkingDay checkSpecialParkingForDay(final int hotelId, final int dayId) {
		SpecialParkingDay spday = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_SPECIAL_DAY_PARKING_INFO);
			ps.setInt(1,  hotelId);
			ps.setInt(2, dayId);
			rs = ps.executeQuery();
			if (rs.next()) {
				spday = new SpecialParkingDay();
				spday.setHotelId(hotelId);
				spday.setDayId(dayId);
				spday.setNumberSpaces(rs.getInt("dy_room"));
				spday.setCost(rs.getBigDecimal("dy_cost"));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}

		return spday;
	}
	
	private List<ImageLink> getParkingImagesForHotel(final int hotelId) {
		List<ImageLink> images = new ArrayList<>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_PARKING_IMAGES_FOR_HOTEL);
			ps.setInt(1,  hotelId);
			rs = ps.executeQuery();
			while (rs.next()) {
				ImageLink il = new ImageLink();
				il.setSmallImageLink("/images/hotel-images/" + rs.getString("gppi_timage"));
				il.setLargeImageLink("/images/hotel-images/" + rs.getString("gppi_limage"));
				images.add(il);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}

		return images;
	}
	
	private int getNumberOfParkingDays(final Date startDate, final Date endDate,
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
