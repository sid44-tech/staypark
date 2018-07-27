package staypark.parking.handler;

import staypark.parking.beans.booking.ParkingCheckout;
import staypark.parking.beans.payment.Payment;
import staypark.parking.beans.security.UserProfile;
import staypark.parking.util.AirportUtil;
import staypark.parking.util.DBUtil;
import staypark.parking.util.DateUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLType;
import java.sql.Types;
import java.util.Date;

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
            "where gpi_id=? and drc_status = 'Active' ";
    
    private String INSERT_RESERVATION = 
    		"insert into dbstaypark.gp_reservation " + 
    		"(gr_p_id, gr_event_id, gr_aff_id, user_id, gr_d_air, gr_d_loc, gr_shuttel, gr_d_time, gr_p_time, drop_time, pick_time, gr_t_time, gr_h_time, " + 
    		"gr_b_fee, gr_s_feegr_ac_fee, gr_s_tax, gr_g_total, gr_p_no, loc_state, loc_city, gr_fname, gr_lname, gr_make, gr_model, gr_license, gr_hear, " + 
    		"gr_add, gr_city, gr_state, gr_zip, gr_email, gr_phone, gr_payment, gp_tracking_no, gr_transaction_key, gr_payment_reason, gr_card_lastdegits, " + 
    		"gr_payment_type, gr_holder, gr_card_type, gr_status, gr_coup_code, gr_disc, gr_reservation_date, reservation_status, order_id, extn_order_id, gr_discount, " + 
    		"gr_due_payment, gr_due_at_lot, gr_min_dy_prk_status, gr_cancel_date, ex_gen) " + 
    		"values " + 
    		"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    

    public ParkingCheckout getParkingCheckoutInfo(final int hotelId, final String dropOffDate, final String dropOffTime,
                                              final String pickUpDate, final String pickUpTime, final String username) {
        ParkingCheckout pc = new ParkingCheckout();
        
        UserProfile up = null;
        if (username != null) {
        	up = accountHandler.getUserProfile(username);
        }

        Date checkinDate = DateUtil.stringMDYToDate(dropOffDate);
        Date checkoutDate = DateUtil.stringMDYToDate(pickUpDate);
        int daysDiff = AirportUtil.getNumberOfParkingDays(checkinDate, checkoutDate, dropOffTime, pickUpTime);
        pc.setTotalParkingTime(daysDiff + " day(s)");
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
                pc.setCheckinTime(DateUtil.formatDate(checkinDate) + " " + dropOffTime + " " + rs.getString("gps_t_zone"));
                pc.setCheckoutTime(DateUtil.formatDate(checkoutDate) + " " + pickUpTime + " " + rs.getString("gps_t_zone"));
                
                //Calculate Base Total Cost.  This only includes number of days * cost per day
                BigDecimal baseTotalCost = AirportUtil.calculateParkingCosts(hotelId, rs.getBigDecimal("gpi_feepday"), checkinDate, checkoutDate, daysDiff, conn);
                baseTotalCost = baseTotalCost.setScale(2, RoundingMode.DOWN);
                pc.setBaseRate(baseTotalCost + " USD");
                
                //Set Service Fee
                BigDecimal serviceFee = rs.getBigDecimal("gpi_sfee").setScale(2);
                pc.setServiceFee(serviceFee + " USD");

                //Set Sales Tax
                double salesTaxMultiplier = rs.getDouble("gpi_stax");
                String salesTaxBasePercentage = rs.getString("gpi_b_per");
                BigDecimal salesTax = calculateSalesTax(baseTotalCost.doubleValue(), salesTaxMultiplier, salesTaxBasePercentage);
                pc.setSalesTax(salesTax.toString() + " USD");
                
                //Get AC Fee.  I don't really know what this is.
                BigDecimal acFee = getACFee(rs.getInt("gpi_ac_chk"), rs.getString("gpi_ac_fee"));
                pc.setAcFee(acFee.toString());
                
                BigDecimal grandTotal = getGrandTotal(baseTotalCost, serviceFee, salesTax, acFee);
                pc.setGrandTotal(grandTotal.toString() + " USD");

                boolean isSplitPayment = isSplitPayment(rs.getString("gpi_split_payment_status"));
                if (isSplitPayment) {
                    BigDecimal payNow = calculatePayNow(rs.getInt("gpi_split_payment"), baseTotalCost, serviceFee, new BigDecimal(0));
                    BigDecimal payAtLot = calculatePayAtLot(grandTotal, payNow);

                    pc.setTotalDueNow(payNow.toString() + " USD");
                    pc.setTotalDueAtLot(payAtLot.toString() + " USD");
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
    	
    	insertReservationRecord(checkout, username, payment);
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
    
    private boolean insertReservationRecord(final ParkingCheckout checkout, final String username, final Payment payment) {
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
         
         ParkingCheckout safeCheckout = getParkingCheckoutInfo(Integer.valueOf(checkout.getHotelId()), checkout.getDropOffDate(), 
        		 checkout.getDropOffTime(), checkout.getPickUpDate(), checkout.getPickUpTime(), null);
        
         
//      "1-gr_p_id, 2-gr_event_id, 3-gr_aff_id, 4-user_id, 5-gr_d_air, 6-gr_d_loc, 7-gr_shuttel, 8-gr_d_time, 9-gr_p_time, 10-drop_time, 11-pick_time, 12-gr_t_time, 13-gr_h_time, " + 
// 		"14-gr_b_fee, 15-gr_s_fee, 16-gr_ac_fee, 17-gr_s_tax, 18-gr_g_total, 19-gr_p_no, 20-loc_state, 21-loc_city, 22-gr_fname, 23-gr_lname, 24-gr_make, 25-gr_model, 26-gr_license, 27-gr_hear, " + 
// 		"28-gr_add, 29-gr_city, 30-gr_state, 31-gr_zip, 32-gr_email, 33-gr_phone, 34-gr_payment, gp_tracking_no, gr_transaction_key, gr_payment_reason, gr_card_lastdegits, " + 
// 		"gr_payment_type, gr_holder, gr_card_type, gr_status, gr_coup_code, gr_disc, gr_reservation_date, reservation_status, order_id, extn_order_id, gr_discount, " + 
// 		"gr_due_payment, gr_due_at_lot, gr_min_dy_prk_status, gr_cancel_date, ex_gen
 		
         try {
             conn = DBUtil.getConnection();
             ps = conn.prepareStatement(INSERT_RESERVATION);
             ps.setInt(1, Integer.valueOf(checkout.getHotelId()));
             ps.setInt(2, 0);
             ps.setInt(3,  affId);
             ps.setInt(4, userId);
             ps.setString(5, "");
             ps.setString(6, "");
             ps.setString(7, "");
             ps.setString(8, checkout.getCheckinTime());
             ps.setString(9, checkout.getCheckoutTime());
             ps.setDate(10, DateUtil.utilDateToSqlDate(DateUtil.stringMDYToDate(checkout.getDropOffDate())));
             ps.setDate(11, DateUtil.utilDateToSqlDate(DateUtil.stringMDYToDate(checkout.getPickUpDate())));
             ps.setInt(12, 0);
             ps.setInt(13, 0);
             ps.setString(14, safeCheckout.getBaseRate());
             ps.setString(15, safeCheckout.getServiceFee());
             ps.setString(16, safeCheckout.getAcFee());
             ps.setString(17, safeCheckout.getSalesTax());
             ps.setString(18, safeCheckout.getGrandTotal());
             ps.setString(19, checkout.getNumberPeopleTravelling());
             ps.setString(20, "");
             ps.setString(21, "");
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
}
