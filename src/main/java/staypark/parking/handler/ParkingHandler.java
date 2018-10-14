package staypark.parking.handler;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import staypark.parking.beans.reservation.ChangeReservation;
import staypark.parking.beans.reservation.SpecialParkingDate;
import staypark.parking.beans.reservation.SpecialParkingDay;
import staypark.parking.comparator.HotelRecommendedComparator;
import staypark.parking.util.AirportUtil;
import staypark.parking.util.EmailUtil;
import org.springframework.stereotype.Service;

import staypark.parking.beans.general.ImageLink;
import staypark.parking.beans.hotel.HotelInformation;
import staypark.parking.comparator.HotelDistanceComparator;
import staypark.parking.comparator.HotelPriceComparator;
import staypark.parking.util.DBUtil;
import staypark.parking.util.DateUtil;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Email;

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

	private static String GET_PARKING_IMAGES_FOR_HOTEL =
			"select * " +
			"from dbstaypark.gp_htl_day_range_cost as hotel " +
			"left join dbstaypark.gp_parking_info as dep on dep.gpi_id = hotel.drc_htl_id " +
			"left join dbstaypark.gp_parking_image as img on img.gppi_p_id = dep.gpi_id " +
			"where gpi_id=? and drc_status = 'Active' ";

	private static String CHECK_ORDER_ID =
			"select * from dbstaypark.gp_reservation as res " +
			"left join dbstaypark.gp_parking_info as park on res.gr_p_id = park.gpi_id " +
			"where res.order_id = ? and res.reservation_status = 'A' ";

	private static String CANCEL_ORDER =
			"update dbstaypark.gp_reservation set reservation_status = 'C' where order_id = ? or extn_order_id = ?";

	public List<HotelInformation> getParkingSearchResults(final String airportCode, final String startDateStr, final String endDateStr,
			final String startTimeStr, final String endTimeStr, final String sort) {
		Date startDate = null;
		Date endDate = null;
		try {
			startDate = DateUtil.stringMDYToDate(startDateStr);
			endDate = DateUtil.stringMDYToDate(endDateStr);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
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
				hotel.setNumberParkingSpaces(rs.getInt("gpi_pno"));
				if (hotel.getNumberParkingSpaces() <= 0) {
					hotel.setSoldout(true);
				} else {
					hotel.setSoldout(false);
				}
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
		Map<String, SpecialParkingDate> spdateMap = AirportUtil.checkSpecialParkingForDateRange(hotel.getHotelId(), startDate, endDate, conn);
		Map<Integer, SpecialParkingDay> spdayMap = AirportUtil.checkSpecialParkingForDay(hotel.getHotelId(), conn);
		int minimumDays = AirportUtil.getMinimumParkingDays(hotel.getHotelId(), conn);

		for (int i = 0; i < totalDays; i++) {
			currentDate = DateUtil.addDaysToDate(currentDate, i);
			int bookedCount = 0;
			if (bookedCountMap.get(currentDate) != null) {
				bookedCount = bookedCountMap.get(currentDate);
				if (hotel.getNumberParkingSpaces() <= bookedCount) {
					hotel.setSoldout(true);
				}
			}

			//Calculate cost for day if day is a special date
			String strCurDate = DateUtil.formatDBDate(currentDate);
			if (spdateMap.get(strCurDate) != null) {
				totalCost = totalCost.add(spdateMap.get(strCurDate).getCost());
				if (spdateMap.get(strCurDate).getNumberSpaces() < bookedCount) {
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

		if (minimumDays != -1 && totalDays < minimumDays) {
			hotel.setSoldout(true);
			hotel.setMinimumDaysMessage("Minimum " + minimumDays + " days booking required");
			hotel.setMinimumDaysEnabled(true);
		}

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

	public ChangeReservation checkValidConfirmatioNumber(final String orderId, final String lastDigits, final String type) {
		ChangeReservation res = new ChangeReservation();
		res.setCancelReservationNumber(orderId);
		res.setLastDigits(lastDigits);
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(CHECK_ORDER_ID);
			ps.setString(1, orderId);
			rs = ps.executeQuery();
			if (rs.next()) {
				if ("C".equals(type)) {
					String cc = rs.getString("gr_card_lastdegits");
					if (cc != null & cc.replace("X", "").equals(res.getLastDigits())) {
						res.setValidNumber(true);
						res.setDepartingAirport(rs.getString("gr_d_air"));
						res.setDropOffLocation(rs.getString("gr_d_loc"));
						res.setDropOffTime(rs.getString("gr_d_time"));
						res.setPickUpTime(rs.getString("gr_p_time"));
						res.setName(rs.getString("gr_fname") + " " + rs.getString("gr_lname"));
						res.setEmail(rs.getString("gr_email"));
						res.setHotelId(rs.getString("gr_p_id"));
						res.setOriginalPickUpDate(DateUtil.parseDateFromDateTimeString(rs.getString("pick_time")));
						res.setOriginalPickUpTime(DateUtil.parseTimeFromDateTimeString(rs.getString("gr_p_time")));
						res.setAddress(rs.getString("gpi_add"));
						res.setBaseFee(getAmountString(rs.getString("gr_b_fee")));
						res.setServiceFee(getAmountString(rs.getString("gr_s_fee")));
						res.setAcFee(getAmountString(rs.getString("gr_ac_fee")));
						res.setSalesTax(getAmountString(rs.getString("gr_s_tax")));
						res.setGrandTotal(getAmountString(rs.getString("gr_g_total")));
						res.setDiscount(getAmountString(rs.getString("gr_discount")));
						res.setPaymentDueAtLot(getAmountString(rs.getString("gr_due_payment")));
						res.setPaymentDueNow(getAmountString(rs.getString("gr_due_payment")));
					}
					Date dropDate = rs.getDate("drop_time");
					if (dropDate.compareTo(new Date()) <= 0) {
						res.setValidNumber(false);
						res.setMessage("Your reservation cannot be cancelled because the drop off date has already passed.");
					}
				} else {
					res.setValidNumber(true);
					res.setDepartingAirport(rs.getString("gr_d_air"));
					res.setDropOffLocation(rs.getString("gr_d_loc"));
					res.setDropOffTime(rs.getString("gr_d_time"));
					res.setPickUpTime(rs.getString("gr_p_time"));
					res.setName(rs.getString("gr_fname") + " " + rs.getString("gr_lname"));
					res.setEmail(rs.getString("gr_email"));
					res.setHotelId(rs.getString("gr_p_id"));
					res.setOriginalPickUpDate(DateUtil.parseDateFromDateTimeString(rs.getString("pick_time")));
					res.setOriginalPickUpTime(DateUtil.parseTimeFromDateTimeString(rs.getString("gr_p_time")));
					res.setAddress(rs.getString("gpi_add"));
					res.setBaseFee(getAmountString(rs.getString("gr_b_fee")));
					res.setServiceFee(getAmountString(rs.getString("gr_s_fee")));
					res.setAcFee(getAmountString(rs.getString("gr_ac_fee")));
					res.setSalesTax(getAmountString(rs.getString("gr_s_tax")));
					res.setGrandTotal(getAmountString(rs.getString("gr_g_total")));
					res.setDiscount(getAmountString(rs.getString("gr_discount")));
					res.setPaymentDueAtLot(getAmountString(rs.getString("gr_due_payment")));
					res.setPaymentDueNow(getAmountString(rs.getString("gr_due_payment")));

					Date pickUpDate = rs.getDate("pick_time");
					Date newMinPickUpDate = DateUtil.addDaysToDate(pickUpDate, 1);
					res.setExtendMinPickUpDate(newMinPickUpDate);

					if (pickUpDate.compareTo(new Date()) <= 0) {
						res.setValidNumber(false);
						res.setMessage("Your reservation cannot be extended because the pick up date has already passed.");
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}

		return res;
	}

	public boolean extendReservation(final ChangeReservation res) {
		Connection conn = null;
		PreparedStatement ps = null;
		boolean success = false;

		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(CANCEL_ORDER);
			ps.setString(1, res.getCancelReservationNumber());
			ps.setString(2, res.getCancelReservationNumber());
			int updated = ps.executeUpdate();
			if (updated > 0) {
				success = true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, null);
		}

		if (success) {
			sendCancelEmails(res);
		}

		return false;
	}

	public boolean cancelReservation(final ChangeReservation res) {
		Connection conn = null;
		PreparedStatement ps = null;
		boolean success = false;

		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(CANCEL_ORDER);
			ps.setString(1, res.getCancelReservationNumber());
			ps.setString(2, res.getCancelReservationNumber());
			int updated = ps.executeUpdate();
			if (updated > 0) {
				success = true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, null);
		}

		if (success) {
			sendCancelEmails(res);
		}

		return false;
	}

	private void sendCancelEmails(final ChangeReservation res) {
		String body = EmailUtil.CANCEL_BOOKING_EMAIL;
		body = body.replace("[ORDER_ID]", res.getCancelReservationNumber());
		body = body.replace("[NAME]", res.getName());
		body = body.replace("[AIRPORT]", res.getDepartingAirport());
		body = body.replace("[LOCATION]", res.getDropOffLocation());
		body = body.replace("[DROP_OFF_TIME]", res.getDropOffTime());
		body = body.replace("[PICK_UP_TIME]", res.getPickUpTime());
		body = body.replace("[ADDRESS]", res.getAddress());
		body = body.replace("[BASE_FEE]", res.getBaseFee());
		body = body.replace("[SERVICE_FEE]", res.getServiceFee());
		body = body.replace("[SALES_TAX]", res.getSalesTax());
		body = body.replace("[GRAND_TOTAL]", res.getGrandTotal());
		if (!res.getAcFee().equals("")) {
			body = body.replace("[CANCEL_BOOKING_ACFEE]", EmailUtil.CANCEL_BOOKING_ACFEE).replace("[AC_FEE]", res.getAcFee());
		} else {
			body = body.replace("[CANCEL_BOOKING_ACFEE]", "");
		}
		if (!res.getDiscount().equals("")) {
			body = body.replace("[CANCEL_BOOKING_DISCOUNT]", EmailUtil.CANCEL_BOOKING_DISCOUNT).replace("[DISCOUNT]", res.getDiscount());
		} else {
			body = body.replace("[CANCEL_BOOKING_DISCOUNT]", "");
		}
		if (!res.getPaymentDueAtLot().equals("")) {
			body = body.replace("[CANCEL_BOOKING_SPLIT_PAY]", EmailUtil.CANCEL_BOOKING_SPLIT_PAY).replace("[PAYMENT_DUE_NOW]", res.getPaymentDueNow().replace("[PAYMENT_DUE_AT_LOT]", res.getPaymentDueNow()));
		} else {
			body = body.replace("[CANCEL_BOOKING_SPLIT_PAY]", "");
		}

		try {
			EmailUtil.sendEmail(res.getEmail(), "RESERVATION CANCELED", body);
			EmailUtil.sendEmail(EmailUtil.SUPPORT_EMAIL, "RESERVATION CANCELED", body);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private String getAmountString(final String amount) {
		if (amount == null || amount.trim().isEmpty()) {
			return "";
		}
		try {
			double amountd = Double.valueOf(amount);
			if (amountd == 0) {
				return "";
			}
		} catch (Exception e) {}

		return amount + " USD";
	}
}
