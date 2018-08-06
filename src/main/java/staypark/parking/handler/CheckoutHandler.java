package staypark.parking.handler;

import staypark.parking.beans.SpecialParkingDate;
import staypark.parking.beans.SpecialParkingDay;
import staypark.parking.beans.booking.AppliedCoupon;
import staypark.parking.beans.booking.Coupon;
import staypark.parking.beans.booking.ParkingCheckout;
import staypark.parking.beans.payment.Payment;
import staypark.parking.beans.security.UserProfile;
import staypark.parking.util.AirportUtil;
import staypark.parking.util.DBUtil;
import staypark.parking.util.DateUtil;
import staypark.parking.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLType;
import java.sql.Types;
import java.util.*;

@Service
@Slf4j
public class CheckoutHandler {
	@Autowired
	private AccountHandler accountHandler;
	@Autowired
	private PaymentHandler paymentHandler;
	@Autowired
	private AirportHandler airportHandler;
	
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

    private String UPDATE_RESERVATION =
            "update dbstaypark.gp_reservation set extn_order_id = ? where order_id = ? ";

    private String INSERT_RESERVATION_DETAIL =
            "insert into dbstaypark.gp_reservation_dtls " +
            "(rd_htl_id, rd_st_date, rd_en_date, rd_order_id, rd_cost, rd_extn_order_id) " +
            "values (?, ?, ?, ?, ?, ?) ";

    private String GET_COUPON_INFO =
            "select * from dbstaypark.gp_voucher " +
            "where status='Active' and now() between start_range and end_range ";

    public ParkingCheckout getParkingCheckoutInfo(final int hotelId, final String dropOffDate, final String dropOffTime,
                                              final String pickUpDate, final String pickUpTime, final String username,
                                                  final String couponCode, final String orderId) {
        ParkingCheckout pc = new ParkingCheckout();
        if (orderId != null && !orderId.trim().isEmpty()) {
            pc.setExtensionOrderId(orderId);
        }
        
        UserProfile up = null;
        if (username != null) {
        	up = accountHandler.getUserProfile(username);
        }

        List<Coupon> coupons = null;
        int validCouponIndex = -1;
        if (couponCode != null && !couponCode.isEmpty()) {
            coupons = getCouponInfo();
            for (int i = 0; i < coupons.size(); i++) {
                if (couponCode.equals(coupons.get(i).getCode())) {
                    validCouponIndex = i;
                }
            }
        }

        Date checkinDate = DateUtil.stringMDYToDate(dropOffDate);
        Date checkoutDate = DateUtil.stringMDYToDate(pickUpDate);
        int daysDiff = AirportUtil.getNumberOfParkingDays(checkinDate, checkoutDate, dropOffTime, pickUpTime);
        pc.setTotalParkingTime(daysDiff + " day(s)");
        pc.setParkingDays(daysDiff);
        pc.setHotelId(String.valueOf(hotelId));
        pc.setDropOffDate(dropOffDate);
        pc.setDropOffTime(dropOffTime);
        pc.setPickUpDate(pickUpDate);
        pc.setPickUpTime(pickUpTime);
        
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

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_CHECKOUT_INFO);
            ps.setInt(1, hotelId);
            rs = ps.executeQuery();
            if (rs.next()) {
                pc.setDepartAirport(rs.getString("gdl_location"));
                pc.setDepartLocation(AirportUtil.parseDropLocation(rs.getString("drc_gpi_inn_name")));
                pc.setShuttleFrequency(rs.getString("gpi_shuttel"));
                pc.setInnAddress(rs.getString("gpi_add"));
                pc.setInnPhone(rs.getString("gpi_phone"));
                pc.setEmailFooter(rs.getString("email_footer"));
                pc.setCheckinTime(DateUtil.formatDate(checkinDate) + " " + dropOffTime);
                pc.setCheckoutTime(DateUtil.formatDate(checkoutDate) + " " + pickUpTime);
                pc.setTimezone(rs.getString("gps_t_zone"));
                pc.setStateName(rs.getString("gps_state"));
                pc.setCityName(rs.getString("gpc_city"));
                pc.setAffiliateEmail(rs.getString("gal_email"));
                pc.setAffiliateSecondaryEmail(rs.getString("gal_s_email"));
                pc.setFaxType(rs.getString("gal_fax_type"));
                pc.setFaxNumber(rs.getString("gal_fax_number"));
                pc.setConfirmationType(rs.getString("gal_confirmation_type"));
                pc.setStandardDailyFee(rs.getBigDecimal("gpi_feepday"));
                
                //Calculate Base Total Cost.  This only includes number of days * cost per day
                BigDecimal baseTotalCost = AirportUtil.calculateParkingCosts(hotelId, rs.getBigDecimal("gpi_feepday"), checkinDate, checkoutDate, daysDiff, conn);
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
                    pc.setCouponCode(couponCode);
                } else if (couponCode != null && !couponCode.isEmpty()) {
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
                    BigDecimal payNow = calculatePayNow(rs.getInt("gpi_split_payment"), baseTotalCost, serviceFee, new BigDecimal(0));
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

        return pc;
    }
    
    public void handleBookingSubmit(final ParkingCheckout checkout, final String username) {
    	Payment payment = paymentHandler.processPayment("USD", checkout.getCardNumber(), checkout.getCvv(), checkout.getCardType(), 
    			checkout.getOwnerName(), checkout.getExpirationMonth(), checkout.getExpirationYear());
    	
    	//TODO: Do something if payment fails.  Update current page to show there was a payment error?
    	if (!payment.isSuccess()) {
    		return;
    	}

        ParkingCheckout safeCheckout = getParkingCheckoutInfo(Integer.valueOf(checkout.getHotelId()), checkout.getDropOffDate(),
                checkout.getDropOffTime(), checkout.getPickUpDate(), checkout.getPickUpTime(), null, checkout.getCouponCode(), checkout.getExtensionOrderId());

        int min = 100000000;
        int max = 999999999;
        Random r = new Random();
        String orderId = String.valueOf(r.nextInt((max - min) + 1) + min);
    	
    	boolean recordUpdated = insertReservationRecord(checkout, safeCheckout, username, payment, orderId);
    	if (!recordUpdated) {
    	    return;
        }

        boolean detailRecordsUpdated = insertReservationDetailRecords(checkout, safeCheckout, orderId);
        if (!detailRecordsUpdated) {
            return;
        }

        if (checkout.getExtensionOrderId() != null && !checkout.getExtensionOrderId().trim().isEmpty()) {
            updateExtensionReservationRecord(checkout.getExtensionOrderId(), orderId);
        }

        sendBookingEmails(checkout, safeCheckout, username, payment, orderId, checkout.getExtensionOrderId());
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
             ps.setString(5, safeCheckout.getDepartAirport());
             ps.setString(6, safeCheckout.getDepartLocation());
             ps.setString(7, "");
             ps.setString(8, safeCheckout.getCheckinTime());
             ps.setString(9, safeCheckout.getCheckoutTime());
             ps.setDate(10, DateUtil.utilDateToSqlDate(DateUtil.stringMDYToDate(checkout.getDropOffDate())));
             ps.setDate(11, DateUtil.utilDateToSqlDate(DateUtil.stringMDYToDate(checkout.getPickUpDate())));
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
             //TODO: Set discount in payment calc
             ps.setString(49, safeCheckout.getDiscount());
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

    private boolean insertReservationDetailRecords(final ParkingCheckout checkout, final ParkingCheckout safeCheckout, final String orderId) {
        Date startDate = DateUtil.stringMDYToDate(safeCheckout.getDropOffDate());
        Date endDate = DateUtil.stringMDYToDate(safeCheckout.getPickUpDate());
        int totalDays = AirportUtil.getNumberOfParkingDays(startDate, endDate, safeCheckout.getDropOffTime(), safeCheckout.getPickUpTime());
        int hotelId = Integer.valueOf(safeCheckout.getHotelId());

        Map<Date, SpecialParkingDate> spdateMap;
        Map<Integer, SpecialParkingDay> spdayMap;
        try (Connection conn = DBUtil.getConnection()) {
            spdateMap = AirportUtil.checkSpecialParkingForDateRange(hotelId, startDate, endDate, conn);
            spdayMap = AirportUtil.checkSpecialParkingForDay(hotelId, conn);

            Date currentDate = startDate;
            for (int i = 0; i < totalDays; i++) {
                currentDate = DateUtil.addDaysToDate(currentDate, i);
                int dayId = DateUtil.getNumericalDayOfWeek(currentDate);

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
                                   final String orderId, final String extensionOrderId) {
        String subject = "Confirmation for Reservation : " + orderId;
        if (extensionOrderId != null && !extensionOrderId.trim().isEmpty()) {
            subject = "Confirmation for Reservation : " + orderId + " (EXTENDED)";
        }
        String body = buildBodyEmail(checkout, safeCheckout, orderId, extensionOrderId);

        try {
            EmailUtil.sendEmail(checkout.getEmail(), subject, body);
            EmailUtil.sendEmail(EmailUtil.SUPPORT_EMAIL, subject, body);

            //TODO: Uncomment this section to send emails to affiliates
//            if (safeCheckout.getFaxType().equals("parking_only") || safeCheckout.getFaxType().equals("both")) {
//                EmailUtil.sendEmail(safeCheckout.getFaxNumber() + "@fax.tc", subject, body);
//                if (safeCheckout.getConfirmationType().equals("both")) {
//                    if (!safeCheckout.getAffiliateEmail().trim().equals("")) {
//                        EmailUtil.sendEmail(safeCheckout.getAffiliateEmail(), subject, body);
//                    }
//                }
//            } else {
//                if (!safeCheckout.getAffiliateEmail().trim().equals("")) {
//                    EmailUtil.sendEmail(safeCheckout.getAffiliateEmail(), subject, body);
//                }
//                if (!safeCheckout.getAffiliateSecondaryEmail().trim().equals("")) {
//                    EmailUtil.sendEmail(safeCheckout.getAffiliateSecondaryEmail(), subject, body);
//                }
//            }
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

}
