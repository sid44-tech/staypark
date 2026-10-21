package staypark.parking.handler;

import staypark.parking.util.DBUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.StringJoiner;

@Slf4j
@Service
public class ReportHandler {

    public String getMonthlyReportCsv(final int year, final int month) {
        Date[] dates = getMonthStartAndEnd(year, month);
        return runMonthlyReportQuery(dates[0], dates[1]);
    }

    private String runMonthlyReportQuery(final Date startDate, final Date endDate) {
        Connection conn = null;
        PreparedStatement ps = null;
        StringBuilder csv = new StringBuilder();

        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(MONTHLY_REPORT);
            ps.setDate(1, startDate);
            ps.setDate(2, endDate);

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                // ✅ Header row
                for (int i = 1; i <= columnCount; i++) {
                    csv.append(meta.getColumnLabel(i));
                    if (i < columnCount) csv.append(",");
                }
                csv.append("\n");

                // ✅ Data rows
                while (rs.next()) {
                    StringJoiner joiner = new StringJoiner(",");
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        joiner.add(escapeCsv(value));
                    }
                    csv.append(joiner.toString()).append("\n");
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            DBUtil.closeResources(conn, ps, null);
        }

        return csv.toString();
    }

    private static String escapeCsv(Object value) {
        if (value == null) return "";
        String s = value.toString();
        // Escape commas, quotes, and newlines safely
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");  // escape internal quotes
            s = "\"" + s + "\"";
        }
        return s;
    }

    private Date[] getMonthStartAndEnd(int year, int month) {
        // Create a YearMonth object for the given year and month
        YearMonth yearMonth = YearMonth.of(year, month);

        // Get the first and last day of the month
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        return new Date[]{Date.valueOf(startOfMonth), Date.valueOf(endOfMonth)};
    }

    private static String MONTHLY_REPORT =
            "select distinct aff.gal_com_name as hotel, loc.gdl_location as airport, CONCAT(res.gr_fname, res.gr_lname) as username, " +
            "res.gr_b_fee, res.gr_reservation_date, res.pick_time, res.drop_time, res.gr_b_fee, res.gr_s_fee , res.gr_ac_fee , res.gr_s_tax, res.gr_g_total, " +
            "p.gpi_b_per " +
            "from dbstaypark.gp_reservation as res " +
            "left join dbstaypark.gp_parking_info as p on p.gpi_aff_id = res.gr_aff_id " +
            "left join dbstaypark.gp_deperture_location as loc on loc.gdl_id = p.gpi_dep_id " +
            "left join dbstaypark.gp_affiliate_login as aff on aff.gal_id = p.gpi_aff_id " +
            "where res.drop_time >= ? and res.drop_time <= ? " +
            "order by aff.gal_com_name, res.drop_time asc ";
}
