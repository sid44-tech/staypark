package staypark.parking.aspect;

import staypark.parking.beans.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
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

            log.info(buildControllerLogOutput(joinPoint.getSignature().toShortString(), timeTaken));

            return result;
    }

    @Around("execution(* staypark.parking.controller.secure.*.*(..))")
    public Object secureControllers(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        final Object result = joinPoint.proceed();
        long timeTaken = System.currentTimeMillis() - startTime;

        String username = getUsername(joinPoint.getArgs());
        if (username != null) {
            log.info(buildSecureControllerLogOutput(joinPoint.getSignature().toShortString(), username, timeTaken));
        } else {
            log.info(buildControllerLogOutput(joinPoint.getSignature().toShortString(), timeTaken));
        }

        return result;
    }

    private String buildControllerLogOutput(final String endpointName, final long time) {
        return "endpoint=\"" + endpointName + "\", time-ms=\"" + time + "\"";
    }

    private String buildSecureControllerLogOutput(final String endpointName, final String username, final long time) {
        return "endpoint=\"" + endpointName + "\", time-ms=\"" + time + "\""+ ", username=\"" + username + "\"";
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
}
