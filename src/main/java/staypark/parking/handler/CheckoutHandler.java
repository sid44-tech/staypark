package staypark.parking.handler;

import staypark.parking.beans.booking.AppliedCoupon;
import staypark.parking.beans.booking.Coupon;
import staypark.parking.beans.booking.ParkingCheckout;
import staypark.parking.beans.booking.RoomParkInput;
import staypark.parking.beans.hotel.HotelParkingRates;
import staypark.parking.beans.payment.Payment;
import staypark.parking.beans.reservation.Room;
import staypark.parking.beans.reservation.SpecialParkingDate;
import staypark.parking.beans.reservation.SpecialParkingDay;
import staypark.parking.beans.security.UserProfile;
import staypark.parking.util.AirportUtil;
import staypark.parking.util.DBUtil;
import staypark.parking.util.DateUtil;
import staypark.parking.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class CheckoutHandler {
	@Autowired
	private AccountHandler accountHandler;
	@Autowired
	private PaymentHandler paymentHandler;
	@Autowired
	private AirportHandler airportHandler;
	@Autowired
    private HotelHandler hotelHandler;
	
    private String GET_CHECKOUT_INFO =
            "select * " +
            "from dbstaypark.gp_htl_day_range_cost as hotel " +
            "left join dbstaypark.gp_parking_info as dep on dep.gpi_id = hotel.drc_htl_id " +
            "left join dbstaypark.gp_deperture_location as loc on dep.gpi_dep_id = loc.gdl_id " +
            "left join dbstaypark.gp_state as st on st.gps_id = dep.gpi_state_id " +
            "left join dbstaypark.gp_city as city on city.gpc_id = dep.gpi_city_id " +
            "left join dbstaypark.gp_affiliate_login as aff on aff.gal_id = dep.gpi_aff_id " +
            "where gpi_id=? and gpi_status = 'Active' ";
    
    private String INSERT_RESERVATION = 
    		"insert into dbstaypark.gp_reservation " + 
    		"(gr_p_id, gr_event_id, gr_aff_id, user_id, gr_d_air, gr_d_loc, gr_shuttel, gr_d_time, gr_p_time, drop_time, pick_time, gr_t_time, gr_h_time, " + 
    		"gr_b_fee, gr_s_fee, gr_ac_fee, gr_s_tax, gr_g_total, gr_p_no, loc_state, loc_city, gr_fname, gr_lname, gr_make, gr_model, gr_license, gr_hear, " +
    		"gr_add, gr_city, gr_state, gr_zip, gr_email, gr_phone, gr_payment, gp_tracking_no, gr_transaction_key, gr_payment_reason, gr_card_lastdegits, " + 
    		"gr_payment_type, gr_holder, gr_card_type, gr_status, gr_coup_code, gr_disc, gr_reservation_date, reservation_status, order_id, extn_order_id, gr_discount, " + 
    		"gr_due_payment, gr_due_at_lot, gr_min_dy_prk_status, gr_cancel_date, ex_gen) " + 
    		"values " + 
    		"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private String INSERT_ROOM_AND_PARK_RESERVATION =
            "insert into dbstaypark.gp_hotel_parking_reservation " +
            "(bk_order_id,bk_htl_id,bk_aff_id,bk_loc_state,bk_loc_city,bk_rsvn_date,bk_chkin_date,bk_chkout_date,bk_days, " +
            "bk_room,bk_persn,bk_children,bk_rm_type,bk_rm_persn,bk_rm_cost,bk_base_rate,bk_total_cost,bk_drc_sl_tax, " +
            "bk_drc_srv_tax,bk_inn_desc,bk_location,bk_airport_lcn,bk_state,bk_room_id,bk_car_drop_off_date,bk_car_chkin_date, " +
            "bk_car_chkin_time,bk_car_days,bk_car_hours,bk_prk_cost,bk_en_days,bk_parking_type,bk_se_en_htl_chkin_date,bk_bk_se_en_htl_chkout_date, " +
            "bk_discount,bk_first_name,bk_last_name,bk_make,model,bk_licence,bk_gr_hear,bk_address,bk_prsn_state,bk_city,bk_zip,bk_email, " +
            "bk_c_email,bk_gr_phone,owner_name,card_type,card_number,cvv_cord,card_year,card_month,bk_payment_status,bk_payment_reason,bk_tracking_no, " +
            "bk_transaction_key,bk_card_lastdegits,bk_reservation_status,bk_extn_order_id,bk_due_at_lot,bk_due_payment,bk_ac_fee,extn_bk_order_id, " +
            "bk_tax_name1,bk_tax_total1,bk_tax_name2,bk_tax_total2,ex_gen, bk_drc_gb_tax, bk_cancel_date) " +
            "values " +
            "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private String UPDATE_RESERVATION =
            "update dbstaypark.gp_reservation set extn_order_id = ? where order_id = ? ";

    private String INSERT_RESERVATION_DETAIL =
            "insert into dbstaypark.gp_reservation_dtls " +
            "(rd_htl_id, rd_st_date, rd_en_date, rd_order_id, rd_cost, rd_extn_order_id) " +
            "values (?, ?, ?, ?, ?, ?) ";

    private String GET_COUPON_INFO =
            "select * from dbstaypark.gp_voucher " +
            "where status='Active' and now() between start_range and end_range ";

    public ParkingCheckout getRoomAndParkingCheckoutInfo(final RoomParkInput input) {
    	if (!input.getSearchType().equals("R")) {
    	    if (input.getSearchType().equals("E")) {
                input.setDropOffDate(input.getCarDropOffDateMDY());
                input.setDropOffTime(input.getCarDropOffTime());
                input.setPickUpDate(input.getHotelArrivalDateMDY());
                input.setPickUpTime(input.getCarDropOffTime());
            } else if (input.getSearchType().equals("B")) {
                input.setDropOffDate(input.getHotelDepartureDateMDY());
                input.setDropOffTime(input.getCarDropOffTime());
                input.setPickUpDate(input.getEndHotelArrivalDateMDY());
                input.setPickUpTime(input.getCarDropOffTime());
            } else {
                input.setDropOffDate(input.getCarDropOffDateMDY());
                input.setDropOffTime(input.getCarDropOffTime());
                input.setPickUpDate(input.getCarPickUpDateMDY());
                input.setPickUpTime(input.getCarPickUpTime());
            }
    	}

        return getParkingCheckoutInfo(input);
    }

    public ParkingCheckout getParkingCheckoutInfo(final RoomParkInput input) {
        ParkingCheckout pc = new ParkingCheckout();
        if (input.getOrderId() != null && !input.getOrderId().trim().isEmpty()) {
            pc.setExtensionOrderId(input.getOrderId());
        }
        
        UserProfile up = null;
        if (input.getUsername() != null) {
        	up = accountHandler.getUserProfile(input.getUsername());
        }

        List<Coupon> coupons = null;
        int validCouponIndex = -1;
        if (input.getCouponCode() != null && !input.getCouponCode().isEmpty()) {
            coupons = getCouponInfo();
            for (int i = 0; i < coupons.size(); i++) {
                if (input.getCouponCode().equals(coupons.get(i).getCode())) {
                    validCouponIndex = i;
                }
            }
        }

        Date checkinDate = null;
        Date checkoutDate = null;
		try {
			checkinDate = DateUtil.stringMDYToDate(input.getDropOffDate());
			checkoutDate = DateUtil.stringMDYToDate(input.getPickUpDate());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

        if (input.getSearchType() != null && input.getSearchType().equals("E")) {
            try {
				checkoutDate = DateUtil.unknownDateStringFormatToDate(input.getHotelArrivalDateMDY());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
        }
        int daysDiff = AirportUtil.getNumberOfParkingDays(checkinDate, checkoutDate, input.getDropOffTime(), input.getPickUpTime());
        if (input.getSearchType() != null && input.getSearchType().equals("S")) {
            daysDiff = AirportUtil.getNumberOfParkingDays(checkinDate, checkoutDate, input.getPickUpTime(), input.getPickUpTime());
        }
        pc.setTotalParkingTime(daysDiff + " day(s)");
        pc.setParkingDays(daysDiff);
        pc.setHotelId(String.valueOf(input.getHotelId()));
        pc.setDropOffDate(input.getDropOffDate());
        pc.setDropOffTime(input.getDropOffTime());
        pc.setPickUpDate(input.getPickUpDate());
        pc.setPickUpTime(input.getPickUpTime());
        pc.setChildren(input.getChildren());
        pc.setTotalAdults(input.getTotalAdults());
        pc.setRooms(input.getRooms());
        pc.setRoomId(input.getRoomId());
        
        if (up != null) {
        	pc.setFirstName(up.getFirstName());
        	pc.setLastName(up.getLastName());
        	pc.setAddress(up.getAddress());
        	pc.setCity(up.getCity());
        	pc.setState(up.getState());
        	pc.setZip(up.getZip());
        	pc.setEmail(up.getEmail());
        	pc.setPhone(up.getPhone());
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String roomOnlyServiceFee = null;
        String roomParkServiceFee = null;
        BigDecimal gbHotelTax = new BigDecimal(0);
        String splitPaymentStatus = null;
        String hotelSalesTax = "";
        String hotelBaseSalesTax = "";

        int splitPayment = -1;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_CHECKOUT_INFO);
            ps.setInt(1, input.getHotelId());
            rs = ps.executeQuery();
            if (rs.next()) {
                roomOnlyServiceFee = rs.getString("drc_htl_onlt_srv_tax");
                roomParkServiceFee = rs.getString("drc_srv_tax");
                gbHotelTax = rs.getBigDecimal("drc_gb_tax");
                splitPaymentStatus = rs.getString("drc_split_payment_status");
                splitPayment = rs.getInt("drc_split_payment");
                hotelSalesTax = rs.getString("drc_sl_tax");
                hotelBaseSalesTax = rs.getString("drc_bse_sl_tax");

                String airpotLocation = buildAirportLocationString(rs.getString("gpc_city"), rs.getString("gps_state"), rs.getString("gdl_location"), rs.getString("gdl_scode"));
                pc.setDepartAirport(airpotLocation);
                pc.setLongAirportLocationText(airpotLocation);

                pc.setDepartLocation(rs.getString("gpi_inn_name"));
                pc.setLongDropLocationText(rs.getString("gpi_inn_name"));

                pc.setShuttleFrequency(rs.getString("gpi_shuttel"));
                pc.setInnAddress(rs.getString("gpi_add"));
                pc.setInnPhone(rs.getString("gpi_phone"));
                pc.setEmailFooter(rs.getString("email_footer"));
                pc.setCheckinTime(DateUtil.formatDate(checkinDate) + " " + input.getDropOffTime());
                pc.setCheckoutTime(DateUtil.formatDate(checkoutDate) + " " + input.getPickUpTime());
                pc.setTimezone(rs.getString("gps_t_zone"));
                pc.setStateName(rs.getString("gps_state"));
                pc.setCityName(rs.getString("gpc_city"));
                pc.setAffiliateEmail(rs.getString("gal_email"));
                pc.setAffiliateSecondaryEmail(rs.getString("gal_s_email"));
                pc.setFaxType(rs.getString("gal_fax_type"));
                pc.setFaxNumber(rs.getString("gal_fax_number"));
                pc.setConfirmationType(rs.getString("gal_confirmation_type"));
                pc.setStandardDailyFee(rs.getBigDecimal("gpi_feepday"));
                pc.setBaseParkingFee(rs.getString("gpi_feepday"));
                
                //Calculate Base Total Cost.  This only includes number of days * cost per day
                BigDecimal baseTotalCost = AirportUtil.calculateParkingCosts(input.getHotelId(), rs.getBigDecimal("gpi_feepday"), checkinDate, checkoutDate, daysDiff, conn);
                baseTotalCost = baseTotalCost.setScale(2, RoundingMode.DOWN);
                pc.setBaseRate(String.valueOf(baseTotalCost));
                
                //Set Service Fee
                BigDecimal serviceFee = calculateServiceFee(rs.getBigDecimal("gpi_sfee").setScale(2), daysDiff);
                pc.setServiceFee(String.valueOf(serviceFee));

                //Set Sales Tax
                double salesTaxMultiplier = rs.getDouble("gpi_stax");
                String salesTaxBasePercentage = rs.getString("gpi_b_per");
                BigDecimal salesTax = calculateSalesTax(baseTotalCost.doubleValue(), salesTaxMultiplier, salesTaxBasePercentage);
                pc.setSalesTax(salesTax.toString());
                
                //Get AC Fee.  I don't really know what this is.
                BigDecimal acFee = getACFee(rs.getInt("gpi_ac_chk"), rs.getString("gpi_ac_fee"));
                pc.setAcFee(acFee.toString());
                
                BigDecimal grandTotal = getGrandTotal(baseTotalCost, serviceFee, salesTax, acFee);
                pc.setGrandTotal(grandTotal.toString());

                if (validCouponIndex != -1) {
                    AppliedCoupon ap = getCouponDiscount(grandTotal, serviceFee, coupons.get(validCouponIndex));
                    pc.setCouponMessage(ap.getMessage());
                    pc.setCouponAmount(ap.getDiscountAmount().toString());
                    BigDecimal newGrandTotal = grandTotal.subtract(ap.getDiscountAmount());
                    grandTotal = newGrandTotal;
                    pc.setGrandTotal(newGrandTotal.toString());
                    pc.setCouponValid(ap.isCouponValid());
                    pc.setCouponCode(input.getCouponCode());
                } else if (input.getCouponCode() != null && !input.getCouponCode().isEmpty()) {
                    pc.setCouponMessage("Sorry, the coupon you entered is not valid at this facility.");
                    pc.setCouponValid(false);
                }
                
                if (!pc.getCouponMessage().equals("")) {
                	if (pc.isCouponValid()) {
                		pc.setCouponClass("coupon-valid");
                	} else {
                		pc.setCouponClass("coupon-invalid");
                	}
                }

                boolean isSplitPayment = isSplitPayment(rs.getString("gpi_split_payment_status"));
                if (isSplitPayment) {
                    BigDecimal couponAmount = new BigDecimal(0);
                    if (pc.getCouponAmount() != null && !pc.getCouponAmount().isEmpty()) {
                        couponAmount = new BigDecimal(pc.getCouponAmount());
                    }
                    BigDecimal payNow = calculatePayNow(rs.getInt("gpi_split_payment"), baseTotalCost, serviceFee, couponAmount);
                    BigDecimal payAtLot = calculatePayAtLot(grandTotal, payNow);

                    pc.setTotalDueNow(payNow.toString());
                    pc.setTotalDueAtLot(payAtLot.toString());
                    pc.setSplitPayment(true);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        if (input.getSearchType() != null && !input.getSearchType().equals("") && !input.getSearchType().equals("P")) {
            HotelParkingRates rates = hotelHandler.getRoomAndParkBaseTotal(input);
            if (input.getSearchType().equals("R")) {
                pc.setBaseRate(rates.getHotelBaseRate().toString());
                Date hotelArrivalDate = DateUtil.unknownDateStringFormatToDate(input.getHotelArrivalDateMDY());
                Date hotelDepartDate = DateUtil.unknownDateStringFormatToDate(input.getHotelDepartureDateMDY());
                pc.setPrimaryHotelCheckInDate(DateUtil.formatDate(hotelArrivalDate));
                pc.setPrimaryHotelCheckOutDate(DateUtil.formatDate(hotelDepartDate));
                int days = DateUtil.getDaysDifference(hotelArrivalDate, hotelDepartDate);
                pc.setTotalHotelNights(String.valueOf(days) + " day(s)");
            } else if (input.getSearchType().equals("S") || input.getSearchType().equals("E")) {
                pc.setBaseRate(rates.getTotalBaseRate().toString());
                Date hotelArrivalDate = DateUtil.unknownDateStringFormatToDate(input.getHotelArrivalDateMDY());
                Date hotelDepartDate = DateUtil.unknownDateStringFormatToDate(input.getHotelDepartureDateMDY());
                pc.setPrimaryHotelCheckInDate(DateUtil.formatDate(hotelArrivalDate));
                pc.setPrimaryHotelCheckOutDate(DateUtil.formatDate(hotelDepartDate));
                int days = DateUtil.getDaysDifference(hotelArrivalDate, hotelDepartDate);
                pc.setTotalHotelNights(String.valueOf(days) + " day(s)");
            } else if (input.getSearchType().equals("B")) {
                pc.setBaseRate(rates.getTotalBaseRate().toString());
                Date hotelArrivalDate = DateUtil.unknownDateStringFormatToDate(input.getHotelArrivalDateMDY());
                Date hotelDepartDate = DateUtil.unknownDateStringFormatToDate(input.getHotelDepartureDateMDY());
                pc.setPrimaryHotelCheckInDate(DateUtil.formatDate(hotelArrivalDate));
                pc.setPrimaryHotelCheckOutDate(DateUtil.formatDate(hotelDepartDate));
                Date endHotelArrivalDate = DateUtil.unknownDateStringFormatToDate(input.getEndHotelArrivalDateMDY());
                Date endHotelDepartDate = DateUtil.unknownDateStringFormatToDate(input.getEndHotelDepartureDateMDY());
                pc.setSecondaryHotelCheckInDate(DateUtil.formatDate(endHotelArrivalDate));
                pc.setSecondaryHotelCheckOutDate(DateUtil.formatDate(endHotelDepartDate));
                int days = DateUtil.getDaysDifference(hotelArrivalDate, hotelDepartDate);
                days += DateUtil.getDaysDifference(endHotelArrivalDate, endHotelDepartDate);
                pc.setTotalHotelNights(String.valueOf(days) + " day(s)");
            } else {
            	pc.setBaseRate(rates.getTotalBaseRate().toString());
            }
            BigDecimal baseServiceFee = hotelHandler.getServiceFeeForRoomAndPark(input.getSearchType(), daysDiff, AirportUtil.convertStringAmount(roomOnlyServiceFee), AirportUtil.convertStringAmount(roomParkServiceFee));
            BigDecimal hotelServiceFee = hotelHandler.getHotelServiceFee(gbHotelTax, splitPaymentStatus, splitPayment, rates);
            pc.setServiceFee(baseServiceFee.add(hotelServiceFee).toString());
            BigDecimal salesTax = hotelHandler.getHotelSalesTax(hotelSalesTax, hotelBaseSalesTax, rates);
            pc.setSalesTax(salesTax.toString());
            BigDecimal gtotal = rates.getTotalBaseRate().add(baseServiceFee).add(hotelServiceFee).add(salesTax);
            pc.setGrandTotal(gtotal.toString());

            boolean isSplitPayment = isSplitPayment(splitPaymentStatus);
            if (isSplitPayment) {
                BigDecimal couponAmount = new BigDecimal(0);
                if (pc.getCouponAmount() != null && !pc.getCouponAmount().isEmpty()) {
                    couponAmount = new BigDecimal(pc.getCouponAmount());
                }
                BigDecimal payNow = calculatePayNow(splitPayment, rates.getTotalBaseRate(), baseServiceFee.add(hotelServiceFee), couponAmount);
                BigDecimal payAtLot = calculatePayAtLot(gtotal, payNow);

                pc.setTotalDueNow(payNow.toString());
                pc.setTotalDueAtLot(payAtLot.toString());
                pc.setSplitPayment(true);
            }
        }

        return pc;
    }

    private String buildAirportLocationString(final String city, final String state, final String airportName, final String airportCode) {
        return city + " , " + state + " - " + airportName + "  (" + airportCode + ")";
    }
    
    public Payment handleBookingSubmit(final ParkingCheckout checkout, final String username) {
    	RoomParkInput input = new RoomParkInput();
    	input.setHotelId(Integer.valueOf(checkout.getHotelId()));
    	input.setDropOffDate(checkout.getDropOffDate());
    	input.setDropOffTime(checkout.getDropOffTime());
    	input.setPickUpDate(checkout.getPickUpDate());
    	input.setPickUpTime(checkout.getPickUpTime());
    	input.setCouponCode(checkout.getCouponCode());
    	input.setOrderId(checkout.getExtensionOrderId());
        input.setSearchType(checkout.getCheckoutType());
        input.setRoomId(checkout.getRoomId());
        input.setRooms(checkout.getRooms());
        input.setTotalAdults(checkout.getTotalAdults());
        input.setChildren(checkout.getChildren());
        input.setSearchType(checkout.getCheckoutType());
        input.setHotelArrivalDateMDY(checkout.getPrimaryHotelCheckInDate());
        input.setHotelDepartureDateMDY(checkout.getPrimaryHotelCheckOutDate());
        input.setEndHotelArrivalDateMDY(checkout.getSecondaryHotelCheckInDate());
        input.setEndHotelDepartureDateMDY(checkout.getSecondaryHotelCheckOutDate());

        ParkingCheckout safeCheckout = getParkingCheckoutInfo(input);
        BigDecimal chargeAmount = new BigDecimal(safeCheckout.getGrandTotal());
        if (!safeCheckout.getTotalDueNow().equals("")) {
            chargeAmount = new BigDecimal(safeCheckout.getTotalDueNow());
        }

        Payment payment = paymentHandler.processPayment("USD", checkout.getCardNumber(), checkout.getCvv(), checkout.getCardType(),
                checkout.getOwnerName(), checkout.getExpirationMonth(), checkout.getExpirationYear(), chargeAmount, checkout.getFirstName(),
                checkout.getLastName(), checkout.getAddress(), checkout.getState(), checkout.getZip());

        if (!payment.isSuccess()) {
            return payment;
        }

        int min = 100000000;
        int max = 999999999;
        Random r = new Random();
        String orderId = String.valueOf(r.nextInt((max - min) + 1) + min);

        String checkoutType = "P";
        if (checkout.getCheckoutType() != null && !checkout.getCheckoutType().isEmpty() && !checkout.getCheckoutType().equals("P")) {
            if (checkout.getCheckoutType().equals("R")) {
                checkoutType = "R";
            } else {
                checkoutType = "RP";
            }
        }

        if (checkout.getCheckoutType() != null && !checkout.getCheckoutType().isEmpty() && !checkout.getCheckoutType().equals("P")) {
            boolean recordUpdated = insertHotelParkReservationRecord(checkout, safeCheckout, username, payment, orderId);
            if (!recordUpdated) {
                payment.setDbProcessError(true);
                return payment;
            }

//            boolean detailRecordsUpdated = insertHotelParkReservationDetailRecords(checkout, safeCheckout, orderId);
//            if (!detailRecordsUpdated) {
//                return;
//            }
        } else {
            boolean recordUpdated = insertReservationRecord(checkout, safeCheckout, username, payment, orderId);
            if (!recordUpdated) {
                payment.setDbProcessError(true);
                return payment;
            }

            boolean detailRecordsUpdated = insertReservationDetailRecords(checkout, safeCheckout, orderId);
            if (!detailRecordsUpdated) {
                payment.setDbProcessError(true);
                return payment;
            }

            if (checkout.getExtensionOrderId() != null && !checkout.getExtensionOrderId().trim().isEmpty()) {
                updateExtensionReservationRecord(checkout.getExtensionOrderId(), orderId);
            }
        }

        sendBookingEmails(checkout, safeCheckout, username, payment, orderId, checkout.getExtensionOrderId(), checkoutType);

        return payment;
    }
    
    private BigDecimal calculateServiceFee(final BigDecimal baseServiceFee, final int totalDays) {
    	if(totalDays <= 9) {
    		return baseServiceFee;
    	} else {
    		int multiplier = (totalDays/10) + 1;
    		return baseServiceFee.multiply(new BigDecimal(multiplier)).setScale(2);
    	}
    }

    private AppliedCoupon getCouponDiscount(final BigDecimal grandTotal, final BigDecimal serviceFee, final Coupon coupon) {
        AppliedCoupon ap = new AppliedCoupon();

        if(("FLAT".equals(coupon.getDiscountType())) && (coupon.getCouponAmount().compareTo(serviceFee) < 0)) {
            ap.setDiscountAmount(coupon.getCouponAmount());
            ap.setMessage("Congratulations! You got a discount of " + coupon.getCouponAmount().toString() + " USD");
            ap.setCouponValid(true);
        } else if(("PERCENT").equals(coupon.getDiscountType())) {
            double pDiscount = grandTotal.doubleValue() * (coupon.getCouponAmount().doubleValue()/100d);
            if (pDiscount < serviceFee.doubleValue()) {
                ap.setDiscountAmount(new BigDecimal(pDiscount).setScale(2));
                ap.setMessage("Congratulations! You got a discount of " + ap.getDiscountAmount().toString() + " USD");
                ap.setCouponValid(true);
            }
        } else{
            ap.setMessage("Sorry, this coupon is not valid for your length of stay.");
        }

        return ap;
    }

    private boolean isSplitPayment(final String status) {
        if (status != null && status.trim().equals("1")) {
            return true;
        }
        return false;
    }

    private BigDecimal calculatePayNow(final int splitPaymentPercent, final BigDecimal baseRate, final BigDecimal serviceFee, final BigDecimal discount) {
        BigDecimal totalDue = new BigDecimal(baseRate.doubleValue() * ((double)splitPaymentPercent/100d)).setScale(2, RoundingMode.DOWN);
        return serviceFee.add(totalDue).subtract(discount).setScale(2, RoundingMode.DOWN);
    }

    private BigDecimal calculatePayAtLot(final BigDecimal grandTotal, final BigDecimal totalDueNow) {
        return grandTotal.subtract(totalDueNow).setScale(2, RoundingMode.DOWN);
    }
    
    private BigDecimal getGrandTotal(final BigDecimal baseCost, final BigDecimal serviceFee, 
    		final BigDecimal salesTax, final BigDecimal acFee) {
    	return baseCost.add(serviceFee).add(salesTax).add(acFee);
    }
    
    private BigDecimal calculateSalesTax(final double totalCost, final double salesTaxMultiplier, final String salesTaxBasePercentageStr) {
    	double salesTaxBasePercentage = 0;
    	
    	 if (salesTaxBasePercentageStr != null && !salesTaxBasePercentageStr.trim().equals("")) {
    		salesTaxBasePercentage = Double.valueOf(salesTaxBasePercentageStr);
    	 } else {
    		 salesTaxBasePercentage = 75; 
    	 }
    	 
    	 double salesTaxDub = ((((totalCost*salesTaxBasePercentage)/100)*salesTaxMultiplier)/100);
    	 BigDecimal salesTax = new BigDecimal(salesTaxDub).setScale(2, RoundingMode.DOWN);
    	
    	 return salesTax;
    }
    
    private BigDecimal getACFee(final int acFlag, final String acAmountStr) {
    	if (acFlag == 1 && acAmountStr != null && acAmountStr.trim().length() == 0) {
    		return new BigDecimal(Double.parseDouble(acAmountStr)).setScale(2, RoundingMode.DOWN);
    		
    	}
    	
    	return new BigDecimal(0).setScale(2, RoundingMode.DOWN);
    }

    private boolean updateExtensionReservationRecord(final String originalReservation, final String newReservation) {
        Connection conn = null;
        PreparedStatement ps = null;
        int updated = -1;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(UPDATE_RESERVATION);
            ps.setString(1, newReservation);
            ps.setString(2, originalReservation);

            updated = ps.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, null);
        }

        if (updated > 0) {
            return true;
        }
        return false;
    }
    
    private boolean insertReservationRecord(final ParkingCheckout checkout, final ParkingCheckout safeCheckout, final String username, final Payment payment,
                                            final String orderId) {
    	 Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         int updated = -1;
         int affId = airportHandler.getAffiliateIdByParkingId(Integer.valueOf(checkout.getHotelId()));
         
         UserProfile up = null;
         int userId = 0;
         if (username != null) {
         	up = accountHandler.getUserProfile(username);
         	userId = up.getUserId();
         }

         String discountAmount = "";
         if (safeCheckout.getCouponCode() != null && !safeCheckout.getCouponCode().isEmpty()) {
            discountAmount = getDiscountFromCode(safeCheckout.getCouponCode());
         }

//      1-gr_p_id, 2-gr_event_id, 3-gr_aff_id, 4-user_id, 5-gr_d_air, 6-gr_d_loc, 7-gr_shuttel, 8-gr_d_time, 9-gr_p_time, 10-drop_time,
//      11-pick_time, 12-gr_t_time, 13-gr_h_time, 14-gr_b_fee, 15-gr_s_fee, 16-gr_ac_fee, 17-gr_s_tax, 18-gr_g_total, 19-gr_p_no, 20-loc_state,
//      21-loc_city, 22-gr_fname, 23-gr_lname, 24-gr_make, 25-gr_model, 26-gr_license, 27-gr_hear, 28-gr_add, 29-gr_city,
//      30-gr_state, 31-gr_zip, 32-gr_email, 33-gr_phone, 34-gr_payment, 35-gp_tracking_no, 36-gr_transaction_key, 37-gr_payment_reason, 38-gr_card_lastdegits, 39-gr_payment_type, 40-gr_holder,
//      41-gr_card_type, 42-gr_status, 43-gr_coup_code, 44-gr_disc, 45-gr_reservation_date, 46-reservation_status, 47-order_id, 48-extn_order_id, 49-gr_discount, " +
// 		50-gr_due_payment, 51-gr_due_at_lot, 52-gr_min_dy_prk_status, 53-gr_cancel_date, 54-ex_gen
 		
         try {
             conn = DBUtil.getConnection();
             ps = conn.prepareStatement(INSERT_RESERVATION);
             ps.setInt(1, Integer.valueOf(checkout.getHotelId()));
             ps.setInt(2, 0);
             ps.setInt(3,  affId);
             ps.setInt(4, userId);
             ps.setString(5, safeCheckout.getLongAirportLocationText());
             ps.setString(6, safeCheckout.getLongDropLocationText());
             ps.setString(7, "");
             ps.setString(8, safeCheckout.getCheckinTime());
             ps.setString(9, safeCheckout.getCheckoutTime());
             ps.setDate(10, DateUtil.longDateToDate(safeCheckout.getCheckinTime()));
             ps.setDate(11, DateUtil.longDateToDate(safeCheckout.getCheckoutTime()));
             ps.setInt(12, safeCheckout.getParkingDays());
             ps.setInt(13, 0);
             ps.setString(14, safeCheckout.getBaseRate());
             ps.setString(15, safeCheckout.getServiceFee());
             ps.setString(16, safeCheckout.getAcFee());
             ps.setString(17, safeCheckout.getSalesTax());
             ps.setString(18, safeCheckout.getGrandTotal());
             ps.setString(19, checkout.getNumberPeopleTravelling());
             ps.setString(20, safeCheckout.getStateName());
             ps.setString(21, safeCheckout.getCityName());
             ps.setString(22, checkout.getFirstName());
             ps.setString(23, checkout.getLastName());
             ps.setString(24, checkout.getCarMake());
             ps.setString(25, checkout.getCarModel());
             ps.setString(26, checkout.getCarLicensePlate());
             ps.setString(27, checkout.getHearAboutUs());
             ps.setString(28, checkout.getAddress());
             ps.setString(29, checkout.getCity());
             ps.setString(30, checkout.getState());
             ps.setString(31, checkout.getZip());
             ps.setString(32, checkout.getEmail());
             ps.setString(33, checkout.getPhone());
             ps.setString(34, "Paid");
             ps.setString(35, payment.getTrackingNumber());
             ps.setString(36, payment.getTransactionKey());
             ps.setString(37, payment.getPaymentResponse());
             ps.setString(38, payment.getCardLastDigits());
             ps.setString(39, "Credit");
             ps.setString(40, checkout.getOwnerName());
             ps.setString(41, checkout.getCardType());
             ps.setString(42, "Active");
             ps.setString(43, checkout.getCouponCode());
             ps.setString(44, "0");
             ps.setDate(45, new java.sql.Date(new Date().getTime()));
             ps.setString(46, "A");
             ps.setString(47, orderId);
             ps.setString(48, "");
             ps.setString(49, discountAmount);
             ps.setString(50, safeCheckout.getTotalDueNow());
             ps.setString(51, safeCheckout.getTotalDueAtLot());
             ps.setBoolean(52, false);
             ps.setNull(53, Types.NULL);
             ps.setInt(54, 0);
             
             updated = ps.executeUpdate();
         } catch (Exception e) {
             log.error(e.getMessage(), e);
         } finally {
             DBUtil.closeResources(conn, ps, rs);
         }
         
         if (updated > 0) {
        	 return true;
         }
         return false;
    }

    private String getDiscountFromCode(final String couponCode) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int updated = -1;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_COUPON_INFO);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("cpn_coad") != null && rs.getString("cpn_coad").equals(couponCode)) {
                    return rs.getString("discount");
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return "";
    }

    private boolean insertHotelParkReservationRecord(final ParkingCheckout checkout, final ParkingCheckout safeCheckout, final String username, final Payment payment,
                                            final String orderId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int updated = -1;
        int affId = airportHandler.getAffiliateIdByParkingId(Integer.valueOf(checkout.getHotelId()));
        
        Room room = hotelHandler.getRoomInfo(Integer.valueOf(checkout.getRoomId()));
        int days = 0;
        Date hotelArrivalDate = null;
        Date hotelDepartDate = null;
        Date endHotelArrivalDate = null;
        Date endHotelDepartDate = null;

        if (checkout.getCheckoutType() == null || checkout.getCheckoutType().isEmpty()) {
            days = 0;
        } else if (checkout.getCheckoutType().equals("R")) {
            hotelArrivalDate = DateUtil.unknownDateStringFormatToDate(checkout.getPrimaryHotelCheckInDate());
            hotelDepartDate = DateUtil.unknownDateStringFormatToDate(checkout.getPrimaryHotelCheckOutDate());
            days = DateUtil.getDaysDifference(hotelArrivalDate, hotelDepartDate);
        } else if (checkout.getCheckoutType().equals("S") || checkout.getCheckoutType().equals("E")) {
            hotelArrivalDate = DateUtil.unknownDateStringFormatToDate(checkout.getPrimaryHotelCheckInDate());
            hotelDepartDate = DateUtil.unknownDateStringFormatToDate(checkout.getPrimaryHotelCheckOutDate());
            days = DateUtil.getDaysDifference(hotelArrivalDate, hotelDepartDate);
        } else if (checkout.getCheckoutType().equals("B")) {
            hotelArrivalDate = DateUtil.unknownDateStringFormatToDate(checkout.getPrimaryHotelCheckInDate());
            hotelDepartDate = DateUtil.unknownDateStringFormatToDate(checkout.getPrimaryHotelCheckOutDate());
            endHotelArrivalDate = DateUtil.unknownDateStringFormatToDate(checkout.getSecondaryHotelCheckInDate());
            endHotelDepartDate = DateUtil.unknownDateStringFormatToDate(checkout.getSecondaryHotelCheckOutDate());
            days = DateUtil.getDaysDifference(hotelArrivalDate, hotelDepartDate);
            days += DateUtil.getDaysDifference(endHotelArrivalDate, endHotelDepartDate);
        }

        String discountAmount = "";
        if (safeCheckout.getCouponCode() != null && !safeCheckout.getCouponCode().isEmpty()) {
            discountAmount = getDiscountFromCode(safeCheckout.getCouponCode());
        }

//        1-bk_order_id, 2-bk_htl_id, 3-bk_aff_id, 4-bk_loc_state, 5-bk_loc_city, 6-bk_rsvn_date, 7-bk_chkin_date, 8-bk_chkout_date, 9-bk_days, 10-bk_room,
//        11-bk_persn, 12-bk_children, 13-bk_rm_type, 14-bk_rm_persn, 15-bk_rm_cost, 16-bk_base_rate, 17-bk_total_cost, 18-bk_drc_sl_tax, 19-bk_drc_srv_tax,
//        20-bk_inn_desc, 21-bk_location, 22-bk_airport_lcn, 23-bk_state, 24-bk_room_id, 25-bk_car_drop_off_date, 26-bk_car_chkin_date, 27-bk_car_chkin_time, 28-bk_car_days, 29-bk_car_hours,
//        30-bk_prk_cost, 31-bk_en_days, 32-bk_parking_type, 33-bk_se_en_htl_chkin_date, 34-bk_bk_se_en_htl_chkout_date, 35-bk_discount, 36-bk_first_name, 37-bk_last_name, 38-bk_make,model, 39-bk_licence,
//        40-bk_gr_hear, 41-bk_address, 42-bk_prsn_state, 43-bk_city, 44-bk_zip, 45-bk_email, 46-bk_c_email, 47-bk_gr_phone, 48-owner_name, 49-card_type,
//        50-card_number, 51-cvv_cord, 52-card_year, 53-card_month, 54-bk_payment_status, 55-bk_payment_reason, 56-bk_tracking_no, 57-bk_transaction_key, 58-bk_card_lastdegits, 59-bk_reservation_status,
//        60-bk_extn_order_id, 61-bk_due_at_lot, 62-bk_due_payment, 63-bk_ac_fee, 64-extn_bk_order_id, 65-bk_tax_name1, 66-bk_tax_total1, 67-bk_tax_name2, 68-bk_tax_total2, 69-ex_gen,
//        70-bk_drc_gb_tax, 71-bk_cancel_date

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(INSERT_ROOM_AND_PARK_RESERVATION);
            ps.setString(1, orderId);
            ps.setInt(2, Integer.valueOf(checkout.getHotelId()));
            ps.setInt(3, affId);
            ps.setString(4, safeCheckout.getStateName());
            ps.setString(5, safeCheckout.getCityName());
            ps.setDate(6, new java.sql.Date(new Date().getTime()));
            ps.setDate(7, new java.sql.Date(hotelArrivalDate.getTime()));
            ps.setDate(8, new java.sql.Date(hotelDepartDate.getTime()));

            ps.setInt(9, days);
            ps.setInt(10, Integer.valueOf(checkout.getRooms()));
            ps.setInt(11, Integer.valueOf(checkout.getNumberPeopleTravelling()));
            ps.setInt(12, Integer.valueOf(checkout.getChildren()));
            ps.setString(13, room.getRoomDescription());
            ps.setInt(14, room.getMaxOccupancy());
            ps.setString(15, room.getBaseCost().toString());
            ps.setString(16, safeCheckout.getBaseRate());
            ps.setString(17, safeCheckout.getGrandTotal());
            ps.setString(18, safeCheckout.getSalesTax());
            ps.setString(19, safeCheckout.getServiceFee());
            ps.setString(20, room.getHotelDescription());
            ps.setString(21, room.getLocation());
            ps.setString(22, room.getAirport());
            ps.setInt(23, room.getStateId());
            ps.setInt(24, room.getRoomId());
            ps.setDate(25, DateUtil.longDateToDate(safeCheckout.getCheckinTime()));
            ps.setDate(26, DateUtil.longDateToDate(safeCheckout.getCheckoutTime()));
            ps.setString(27, "0");
            ps.setString(28, String.valueOf(safeCheckout.getParkingDays()));
            ps.setString(29, "0");
            ps.setString(30, safeCheckout.getBaseParkingFee());
            ps.setString(31, "");
            ps.setString(32, getCheckoutType(checkout.getCheckoutType()));

            if (endHotelArrivalDate == null) {
                ps.setString(33, "");
            } else {
                ps.setString(33, endHotelArrivalDate.toString().substring(0, 19));
            }

            if (endHotelDepartDate != null) {
                ps.setDate(34, new java.sql.Date(endHotelDepartDate.getTime()));
            } else {
            	ps.setNull(34, Types.NULL);
            }

            ps.setString(35, discountAmount);
            ps.setString(36, checkout.getFirstName());
            ps.setString(37, checkout.getLastName());
            ps.setString(38, checkout.getCarMake());
            ps.setString(39, checkout.getCarModel());
            ps.setString(40, checkout.getCarLicensePlate());
            ps.setString(41, checkout.getHearAboutUs());
            ps.setString(42, checkout.getAddress());
            ps.setString(43, checkout.getState());
            ps.setString(44, checkout.getCity());
            ps.setString(45, checkout.getZip());
            ps.setString(46, checkout.getEmail());
            ps.setString(47, checkout.getEmail());
            ps.setString(48, checkout.getPhone());
            ps.setString(49, checkout.getOwnerName());
            ps.setString(50, checkout.getCardType());
            ps.setString(51, payment.getCardLastDigits());
            ps.setString(52, checkout.getCvv());
            ps.setString(53, checkout.getExpirationYear());
            ps.setString(54, checkout.getExpirationMonth());
            ps.setString(55, "Paid");
            ps.setString(56, "Transaction Approved");
            ps.setString(57, payment.getTrackingNumber());
            ps.setString(58, payment.getTransactionKey());
            ps.setString(59, payment.getCardLastDigits());
            ps.setString(60, "A");
            ps.setString(61, "");
            ps.setString(62, safeCheckout.getTotalDueAtLot());
            ps.setString(63, safeCheckout.getTotalDueNow());
            ps.setString(64, safeCheckout.getAcFee());
            ps.setString(65, "");
            ps.setString(66, "");
            ps.setString(67, "0.00");
            ps.setString(68, "");
            ps.setString(69, "0.00");
            ps.setInt(70, 0);
            ps.setString(71, "0.00");
            ps.setNull(72, Types.NULL);

            updated = ps.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        if (updated > 0) {
            return true;
        }
        return false;
    }

    private boolean insertReservationDetailRecords(final ParkingCheckout checkout, final ParkingCheckout safeCheckout, final String orderId) {
        Date startDate = null;;
        Date endDate = null;;
		try {
			startDate = DateUtil.stringMDYToDate(safeCheckout.getDropOffDate());
			endDate = DateUtil.stringMDYToDate(safeCheckout.getPickUpDate());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
        int totalDays = AirportUtil.getNumberOfParkingDays(startDate, endDate, safeCheckout.getDropOffTime(), safeCheckout.getPickUpTime());
        int hotelId = Integer.valueOf(safeCheckout.getHotelId());

        Map<String, SpecialParkingDate> spdateMap;
        Map<Integer, SpecialParkingDay> spdayMap;
        try (Connection conn = DBUtil.getConnection()) {
            spdateMap = AirportUtil.checkSpecialParkingForDateRange(hotelId, startDate, endDate, conn);
            spdayMap = AirportUtil.checkSpecialParkingForDay(hotelId, conn);

            Date currentDate = startDate;
            for (int i = 0; i < totalDays; i++) {
                currentDate = DateUtil.addDaysToDate(currentDate, i);

                BigDecimal currentDayCost;
                //Calculate cost for day if day is a special date
                if (spdateMap.get(currentDate) != null) {
                    currentDayCost = spdateMap.get(currentDate).getCost();
                    //Calculate cost for day if day is a special day (mon, tues, etc)
                } else if (spdayMap.get(currentDate) != null) {
                    currentDayCost = spdayMap.get(currentDate).getCost();
                    //Calculate cost for day if it doesn't have any special circumstances
                } else {
                    currentDayCost = safeCheckout.getStandardDailyFee();
                }

                insertReservationDetailRecord(conn, hotelId, currentDate, DateUtil.addDaysToDate(startDate, totalDays), orderId, currentDayCost);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    private void insertReservationDetailRecord(final Connection conn, final int hotelId, final Date startDate, final Date endDate,
                                               final String orderId, final BigDecimal cost) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(INSERT_RESERVATION_DETAIL);
            ps.setInt(1, hotelId);
            ps.setDate(2, new java.sql.Date(startDate.getTime()));
            ps.setDate(3, new java.sql.Date(endDate.getTime()));
            ps.setInt(4, Integer.valueOf(orderId));
            ps.setBigDecimal(5, cost);
            ps.setString(6, "");
            ps.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(ps);
        }
    }

    private void sendBookingEmails(final ParkingCheckout checkout, final ParkingCheckout safeCheckout, final String username, final Payment payment,
                                   final String orderId, final String extensionOrderId, final String parkingOnly) {
        String subjectPrefix = "";
        if (parkingOnly.equals("RP")) {
            subjectPrefix = "ROOM + PARK - ";
        } else if (parkingOnly.equals("R")) {
            subjectPrefix = "HOTEL ROOM - ";
        }
        String subject = subjectPrefix + "Confirmation for Reservation : " + orderId;
        if (extensionOrderId != null && !extensionOrderId.trim().isEmpty()) {
            subject = "Confirmation for Reservation : " + orderId + " (EXTENDED)";
        }
        String body = buildBodyEmail(checkout, safeCheckout, orderId, extensionOrderId);

        try {
            EmailUtil.sendEmail(checkout.getEmail(), subject, body);
            EmailUtil.sendEmail(EmailUtil.SUPPORT_EMAIL, subject, body);

            if (safeCheckout.getFaxType().equals("parking_only") || safeCheckout.getFaxType().equals("both")) {
                EmailUtil.sendEmail(safeCheckout.getFaxNumber() + "@fax.tc", subject, body);
                if (safeCheckout.getConfirmationType().equals("both")) {
                    if (!safeCheckout.getAffiliateEmail().trim().equals("")) {
                        EmailUtil.sendEmail(safeCheckout.getAffiliateEmail(), subject, body);
                    }
                }
            } else {
                if (!safeCheckout.getAffiliateEmail().trim().equals("")) {
                    EmailUtil.sendEmail(safeCheckout.getAffiliateEmail(), subject, body);
                }
                if (!safeCheckout.getAffiliateSecondaryEmail().trim().equals("")) {
                    EmailUtil.sendEmail(safeCheckout.getAffiliateSecondaryEmail(), subject, body);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private String buildBodyEmail(final ParkingCheckout checkout, final ParkingCheckout safeCheckout, final String orderId, final String extensionOrderId) {
        String body = EmailUtil.CONFIRM_BOOKING_EMAIL;
        if (extensionOrderId != null && !extensionOrderId.trim().isEmpty()) {
            body = body.replace("[CONFIRMATION]", "Confirmation of Order Id " + orderId + " Extend from Order " + extensionOrderId);
        } else {
            body = body.replace("[CONFIRMATION]", "Confirmation");
        }
        body = body.replace("[FIRST_NAME]", checkout.getFirstName());
        body = body.replace("[LAST_NAME]", checkout.getLastName());
        body = body.replace("[INN_NAME]", safeCheckout.getDepartLocation());
        body = body.replace("[STATE]", safeCheckout.getStateName());
        body = body.replace("[INN_ADDRESS]", safeCheckout.getInnAddress());
        body = body.replace("[INN_PHONE]", safeCheckout.getInnPhone());
        body = body.replace("[SHUTTLE_INFO]", safeCheckout.getShuttleFrequency());
        body = body.replace("[DROP_TIME]", safeCheckout.getCheckinTime() + " " + safeCheckout.getTimezone());
        body = body.replace("[PICK_UP_TIME]", safeCheckout.getCheckoutTime() + " " + safeCheckout.getTimezone());
        body = body.replace("[ORDER_ID]", orderId);
        body = body.replace("[NUMBER_PASSENGERS]", checkout.getNumberPeopleTravelling());
        body = body.replace("[BASE_RATE]", safeCheckout.getBaseRate() + " USD");
        body = body.replace("[SERVICE_FEE]", safeCheckout.getServiceFee() + " USD");

        if (safeCheckout.getAcFee() != null && safeCheckout.getAcFee().trim().equals("0.00")) {
            body = body.replace("[CONFIRM_ACCESS_FEE]", EmailUtil.CONFIRM_ACCESS_FEE);
            body = body.replace("[AC_FEE]", safeCheckout.getAcFee() + " USD");
        } else {
            body = body.replace("[CONFIRM_ACCESS_FEE]", "");
        }

        body = body.replace("[SALES_TAX]", safeCheckout.getSalesTax() + " USD");

        if (safeCheckout.isCouponValid()) {
        	body = body.replace("[CONFIRM_COUPON_DISCOUNT]", EmailUtil.CONFIRM_COUPON_DISCOUNT);
        	body = body.replace("[DISCOUNT_AMOUNT]", safeCheckout.getCouponAmount() + " USD");
        } else {
            body = body.replace("[CONFIRM_COUPON_DISCOUNT]", "");
        }

        body = body.replace("[GRAND_TOTAL]", safeCheckout.getGrandTotal() + " USD");

        if (safeCheckout.isSplitPayment()) {
            body = body.replace("[SPLIT_PAYMENT]", EmailUtil.SPLIT_PAYMENT);
            body = body.replace("[TOTAL_DUE_NOW]", safeCheckout.getTotalDueNow() + " USD");
            body = body.replace("[TOTAL_DUE_AT_LOT]", safeCheckout.getTotalDueAtLot() + " USD");
        } else {
            body = body.replace("[SPLIT_PAYMENT]", "");
        }

        body = body.replace("[CAR_MAKE]", checkout.getCarMake());
        body = body.replace("[CAR_MODEL]", checkout.getCarModel());
        body = body.replace("[CAR_LICENSE_PLATE]", checkout.getCarLicensePlate());

        if (safeCheckout.getEmailFooter().trim().equals("")) {
            body = body.replace("[EMAIL_FOOTER]", EmailUtil.EMAIL_FOOTER_DEFAULT);
        } else {
            body = body.replace("[EMAIL_FOOTER]", safeCheckout.getEmailFooter());
        }

        return body;
    }

    private List<Coupon> getCouponInfo() {
        List<Coupon> list = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_COUPON_INFO);
            rs = ps.executeQuery();

            while (rs.next()) {
                Coupon coupon = new Coupon();
                coupon.setAmount(rs.getString("discount"));
                coupon.setCode(rs.getString("cpn_coad"));
                coupon.setCouponType(rs.getString("cpn_type"));
                coupon.setDiscountType(rs.getString("discount_type"));
                list.add(coupon);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return list;
    }
    
    private String getCheckoutType(final String type) {
    	if (type == null || type.equals("")) {
    		return "parking_only";
    	} else if (type.equals("S")) {
    		return "start_trip";
    	} else if (type.equals("E")) {
    		return "end_trip";
    	} else if (type.equals("B")) {
    		return "start_and_end_trip";
    	} else if (type.equals("R")) {
    		return "room_only";
    	}
    	
    	return "";
    }
}
