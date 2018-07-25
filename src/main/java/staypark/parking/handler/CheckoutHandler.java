package staypark.parking.handler;

import staypark.parking.beans.booking.ParkingCheckout;
import staypark.parking.util.AirportUtil;
import staypark.parking.util.DBUtil;
import staypark.parking.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

@Service
@Slf4j
public class CheckoutHandler {
    private String GET_CHECKOUT_INFO =
            "select * " +
            "from dbstaypark.gp_htl_day_range_cost as hotel " +
            "left join dbstaypark.gp_parking_info as dep on dep.gpi_id = hotel.drc_htl_id " +
            "left join dbstaypark.gp_deperture_location as loc on dep.gpi_dep_id = loc.gdl_id " +
            "left join dbstaypark.gp_state as st on st.gps_id = dep.gpi_state_id " +
            "where gpi_id=? and drc_status = 'Active' ";

    public ParkingCheckout getParkingCheckout(final int hotelId, final String dropOffDate, final String dropOffTime,
                                              final String pickUpDate, final String pickUpTime) {
        ParkingCheckout pc = new ParkingCheckout();

        Date checkinDate = DateUtil.stringMDYToDate(dropOffDate);
        Date checkoutDate = DateUtil.stringMDYToDate(pickUpDate);
        int daysDiff = AirportUtil.getNumberOfParkingDays(checkinDate, checkoutDate, dropOffTime, pickUpTime);
        pc.setTotalParkingTime(daysDiff + " day(s)");

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
}
