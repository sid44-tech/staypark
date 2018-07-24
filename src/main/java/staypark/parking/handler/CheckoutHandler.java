package staypark.parking.handler;

import staypark.parking.beans.booking.ParkingCheckout;
import staypark.parking.util.AirportUtil;
import staypark.parking.util.DBUtil;
import staypark.parking.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
                BigDecimal totalCost = AirportUtil.calculateParkingCosts(hotelId, rs.getBigDecimal("gpi_feepday"), checkinDate, checkoutDate, daysDiff, conn);
                pc.setBaseRate(totalCost.setScale(2).toString() + " USD");
                pc.setServiceFee(rs.getBigDecimal("gpi_sfee").setScale(2).toString() + " USD");
                pc.setGrandTotal(totalCost.setScale(2).toString() + " USD");
                String gpiper = rs.getString("gpi_b_per");
                BigDecimal weirdVariable = new BigDecimal(75);
                if (gpiper != null && gpiper.trim().length() != 0) {
                    weirdVariable = new BigDecimal(gpiper).setScale(2);
                }
                double salesTaxMultiplier = AirportUtil.getSalesTaxMultiplier(rs.getBigDecimal("gpi_stax"));
                BigDecimal salesTax = new BigDecimal(totalCost.doubleValue() * salesTaxMultiplier);
                pc.setSalesTax(salesTax.toString() + " USD");
                $sales_tax=(((($cost*$gpi_b_per)/100)*$sales_tax_temp)/100);

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return pc;
    }
}
