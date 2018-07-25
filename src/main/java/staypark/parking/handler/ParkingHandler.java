package staypark.parking.handler;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import staypark.parking.comparator.HotelRecommendedComparator;
import staypark.parking.util.AirportUtil;
import org.springframework.stereotype.Service;

import staypark.parking.beans.HotelInformation;
import staypark.parking.beans.ImageLink;
import staypark.parking.beans.SpecialParkingDate;
import staypark.parking.beans.SpecialParkingDay;
import staypark.parking.comparator.HotelDistanceComparator;
import staypark.parking.comparator.HotelPriceComparator;
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

	private static String GET_RESERVATION_COUNT_FOR_RANGE_HOTEL =
			"select d.rd_st_date, count(d.rd_st_date) as count " +
			"from dbstaypark.gp_reservation_dtls as d " +
			"WHERE d.rd_htl_id = ? and d.rd_st_date >= ? AND d.rd_st_date <= ? " +
			"group by d.rd_st_date ";

	private static String GET_SPECIAL_DATE_PARKING_RANGE_INFO =
			"select sp_date, sp_cost, sp_prk_no " +
			"from dbstaypark.gp_sp_reservation " +
			"WHERE sp_htl_id = ? and sp_date >= ? and sp_date <= ? ";

	private static String GET_SPECIAL_DAY_PARKING_INFO =
			"select * " +
			"from dbstaypark.gp_sp_dy_reservation " +
			"WHERE dy_htl_id = ? ";

	private static String GET_PARKING_IMAGES_FOR_HOTEL =
			"select * " +
			"from dbstaypark.gp_htl_day_range_cost as hotel " +
			"left join dbstaypark.gp_parking_info as dep on dep.gpi_id = hotel.drc_htl_id " +
			"left join dbstaypark.gp_parking_image as img on img.gppi_p_id = dep.gpi_id " +
			"where gpi_id=? and drc_status = 'Active' ";

	public List<HotelInformation> getParkingSearchResults(final String airportCode, final String startDateStr, final String endDateStr,
			final String startTimeStr, final String endTimeStr, final String sort) {
		Date startDate = DateUtil.stringMDYToDate(startDateStr);
		Date endDate = DateUtil.stringMDYToDate(endDateStr);
		List<HotelInformation> hotels = new ArrayList<>();

		try (Connection conn = DBUtil.getConnection()) {
			int totalParkingDays = AirportUtil.getNumberOfParkingDays(startDate, endDate, startTimeStr, endTimeStr);

			hotels = getParkingLocations(airportCode, conn);

			for (HotelInformation hotel : hotels) {
				calculateParkingCost(hotel, startDate, endDate, totalParkingDays, conn);
				hotel.setParkingImageLinks(getParkingImagesForHotel(hotel.getHotelId(), conn));
			}
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}

		sortHotels(hotels, sort);

		return hotels;
	}

	private void sortHotels(final List<HotelInformation> hotels, final String sort) {
		if (sort == null || sort.equals("REC")) {
			hotels.sort(new HotelRecommendedComparator());
		} else if (sort.equals("PLH")) {
			hotels.sort(new HotelPriceComparator());
		} else if (sort.equals("PHL")) {
			hotels.sort(new HotelPriceComparator());
			Collections.reverse(hotels);
		} else if (sort.equals("DSL")) {
			hotels.sort(new HotelDistanceComparator());
		} else if (sort.equals("DLS")) {
			hotels.sort(new HotelDistanceComparator());
			Collections.reverse(hotels);
		} else {
			hotels.sort(new HotelPriceComparator());
		}
	}

	private List<HotelInformation> getParkingLocations(final String airportCode, final Connection conn) {
		List<HotelInformation> hotels = new ArrayList<>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
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
				hotel.setRecommendedSort(rs.getInt("gpi_sequence"));
				hotels.add(hotel);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(null, ps, rs);
		}

		return hotels;
	}

	private HotelInformation calculateParkingCost(final HotelInformation hotel, final Date startDate, final Date endDate, final int totalDays, final Connection conn) {
		BigDecimal totalCost = new BigDecimal(0);
		Date currentDate = startDate;

		Map<Date, Integer> bookedCountMap = AirportUtil.getReservationForRangeCountbyHotel(hotel.getHotelId(), startDate, endDate, conn);
		Map<Date, SpecialParkingDate> spdateMap = AirportUtil.checkSpecialParkingForDateRange(hotel.getHotelId(), startDate, endDate, conn);
		Map<Integer, SpecialParkingDay> spdayMap = AirportUtil.checkSpecialParkingForDay(hotel.getHotelId(), conn);

		for (int i = 0; i < totalDays; i++) {
			currentDate = DateUtil.addDaysToDate(currentDate, i);
			int dayId = DateUtil.getNumericalDayOfWeek(currentDate);
			int bookedCount = 0;
			if (bookedCountMap.get(currentDate) != null) {
				bookedCount = bookedCountMap.get(currentDate);
			}

			//Calculate cost for day if day is a special date
			if (spdateMap.get(currentDate) != null) {
				totalCost = totalCost.add(spdateMap.get(currentDate).getCost());
				if (spdateMap.get(currentDate).getNumberSpaces() < bookedCount) {
					hotel.setSoldout(true);
				}
			//Calculate cost for day if day is a special day (mon, tues, etc)
			} else if (spdayMap.get(currentDate) != null) {
				totalCost = totalCost.add(spdayMap.get(currentDate).getCost());
				if (spdayMap.get(currentDate).getNumberSpaces() < bookedCount) {
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

	private List<ImageLink> getParkingImagesForHotel(final int hotelId, final Connection conn) {
		List<ImageLink> images = new ArrayList<>();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
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
			DBUtil.closeResources(null, ps, rs);
		}

		return images;
	}
}
