package staypark.parking.util;

import com.google.gson.Gson;
import staypark.parking.beans.GoogleResponse;
import staypark.parking.beans.security.CaptchaResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.security.core.Authentication;

import staypark.parking.beans.security.Principal;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SecurityUtil {
	private static String G_CAPTCHA_SECRET = "";
	
	public static String getAuthenticatedUsername(final Authentication auth) {
		if (auth == null || auth.getPrincipal() == null) {
			return null;
		}
        Principal p = (Principal) auth.getPrincipal();
        return p.getUsername();
	}

	public static boolean checkCaptcha(final String gRecaptchaResponse, final String ip) {

		try {
			HttpClient httpclient = HttpClients.createDefault();
			HttpPost httppost = new HttpPost("https://www.google.com/recaptcha/api/siteverify");

			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>(2);
			params.add(new BasicNameValuePair("secret", ""));
			params.add(new BasicNameValuePair("response", gRecaptchaResponse));
			params.add(new BasicNameValuePair("remoteip", ip));
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			//Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				String jsonString = EntityUtils.toString(entity);
				CaptchaResponse cr = new Gson().fromJson(jsonString, CaptchaResponse.class);

				if (cr != null) {
					return cr.getSuccess();
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}

		return false;
	}
}
