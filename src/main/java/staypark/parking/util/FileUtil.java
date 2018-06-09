package staypark.parking.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.Charset;

@Slf4j
public class FileUtil {

    public static String getImageAsEncodedString(final String file) {
        String encodedImage = null;
        try {
            ClassPathResource classPathResource = new ClassPathResource(file);
            InputStream inputStream = classPathResource.getInputStream();
            encodedImage = IOUtils.toString(inputStream, Charset.forName("ISO-8859-1"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return encodedImage;
    }
}
