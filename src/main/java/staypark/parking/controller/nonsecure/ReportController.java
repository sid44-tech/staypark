package staypark.parking.controller.nonsecure;

import staypark.parking.handler.ReportHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
public class ReportController {

    @Autowired
    ReportHandler reportHandler;

    @GetMapping("/reports/monthly/{year}/{month}")
    public ResponseEntity<byte[]> getMonthlyReport(@PathVariable("year") int year, @PathVariable("month") int month) {
        // Create CSV content

        String csv = reportHandler.getMonthlyReportCsv(year, month);

        // Convert to bytes (UTF-8 encoding)
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);

        String filename = "attachment; filename=\"monthly-" + year + "-" + month + ".csv\"";

        // Set headers for download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, filename);
        headers.setContentLength(csvBytes.length);

        // Return the response entity
        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }
}
