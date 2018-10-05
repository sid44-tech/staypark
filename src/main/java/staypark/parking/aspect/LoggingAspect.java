package staypark.parking.aspect;

import staypark.parking.beans.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

@Aspect
@Configuration
@Slf4j
public class LoggingAspect {

    @Around("execution(* staypark.parking.controller.nonsecure.*.*(..))")
    public Object nonsecureControllers(ProceedingJoinPoint joinPoint) throws Throwable {
            long startTime = System.currentTimeMillis();
            final Object result = joinPoint.proceed();
            long timeTaken = System.currentTimeMillis() - startTime;

            String headers = getHeaders(joinPoint.getArgs());
            log.info(buildControllerLogOutput(joinPoint.getSignature().toShortString(), headers, timeTaken));

            return result;
    }

    @Around("execution(* staypark.parking.controller.secure.*.*(..))")
    public Object secureControllers(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        final Object result = joinPoint.proceed();
        long timeTaken = System.currentTimeMillis() - startTime;

        String username = getUsername(joinPoint.getArgs());
        String headers = getHeaders(joinPoint.getArgs());
        if (username != null) {
            log.info(buildSecureControllerLogOutput(joinPoint.getSignature().toShortString(), username, headers, timeTaken));
        } else {
            log.info(buildControllerLogOutput(joinPoint.getSignature().toShortString(), headers, timeTaken));
        }

        return result;
    }

    private String buildControllerLogOutput(final String endpointName, final String headers, final long time) {
        return "endpoint=\"" + endpointName + "\", time-ms=\"" + time + "\", " + headers;
    }

    private String buildSecureControllerLogOutput(final String endpointName, final String username, final String headers, final long time) {
        return "endpoint=\"" + endpointName + "\", time-ms=\"" + time + "\""+ ", username=\"" + username + "\", " + headers;
    }

    private String getUsername(final Object[] objs) {
        if (objs == null) {
            return null;
        }

        for (int i = 0; i < objs.length; i++) {
            if (objs[i] instanceof Authentication) {
                Principal p = (Principal) ((Authentication) objs[i]).getPrincipal();
                String username = p.getUsername();
                return username;
            }
        }

        return null;
    }

    private String getHeaders(final Object[] objs) {
        if (objs == null) {
            return "";
        }

        for (int i = 0; i < objs.length; i++) {
            if (objs[i] instanceof HttpHeaders) {
                return objs[i].toString();
            }
        }

        return "";
    }
}
