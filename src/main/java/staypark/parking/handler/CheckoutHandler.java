package staypark.parking.handler;

import staypark.parking.beans.booking.ParkingCheckout;
import staypark.parking.util.DBUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Service
@Slf4j
public class CheckoutHandler {
    private String GET_CHECKOUT_INFO =
            "select * " +
            "from dbstaypark.gp_htl_day_range_cost as hotel " +
            "left join dbstaypark.gp_deperture_location as dep on dep.gdl_state_id = hotel.drc_gpi_state_id and dep.gdl_city_id = hotel.drc_gpi_city_id " +
            "where drc_id = ? and drc_status = 'Active' ";

    public ParkingCheckout getParkingCheckout(final int hotelId) {
        ParkingCheckout pc = new ParkingCheckout();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(GET_CHECKOUT_INFO);
            ps.setInt(1, hotelId);
            rs = ps.executeQuery();
            if (rs.next()) {
                pc.setDepartAirport(rs.getString(""));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, rs);
        }

        return pc;
    }
}
