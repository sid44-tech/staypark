package staypark.parking.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import staypark.parking.beans.general.TestimonialBean;
import staypark.parking.util.DBUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HomeHandler {
	private String GET_TESTIMONIALS = "select gt_name as name, gt_desig as job, gt_com as comment from dbstaypark.gp_testimonial ORDER BY RAND() LIMIT 10";

	public List<TestimonialBean> getTestimonials() {
		List<TestimonialBean> list = new ArrayList<TestimonialBean>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(GET_TESTIMONIALS);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(new TestimonialBean(rs.getString("comment"), "By " + rs.getString("name"), rs.getString("job")));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DBUtil.closeResources(conn, ps, rs);
		}
		
		return list;
	}
}
