package staypark.parking.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
@Slf4j
public class LoggingAspect {

    @Around("execution(* staypark.parking.controller.*.*(..))")
    public Object before(ProceedingJoinPoint joinPoint) throws Throwable {
            long startTime = System.currentTimeMillis();
            final Object result = joinPoint.proceed();
            long timeTaken = System.currentTimeMillis() - startTime;

            log.info(buildControllerLogOutput(joinPoint.getSignature().toShortString(), timeTaken));

            return result;
    }

    private String buildControllerLogOutput(final String endpointName, final long time) {
        return "endpoint=\"" + endpointName + "\", time-ms=\"" + time + "\"";
    }
}
