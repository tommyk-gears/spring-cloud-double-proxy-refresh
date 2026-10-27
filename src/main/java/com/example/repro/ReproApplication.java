package com.example.repro;

import jakarta.validation.constraints.NotBlank;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@SpringBootApplication
@EnableConfigurationProperties(ReproApplication.DemoProperties.class)
public class ReproApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReproApplication.class, args);
    }

    /**
     * {@code @Validated} makes the method validation post-processor proxy this bean.
     */
    @Validated
    @ConfigurationProperties("demo")
    public static class DemoProperties {

        @NotBlank
        private String message = "default";

        public String getMessage() {
            return this.message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Matches every {@code @Validated} class, so the auto-proxy creator wraps the method validation
     * proxy of {@link DemoProperties} in a second proxy.
     */
    @Aspect
    @Component
    public static class ValidatedAspect {

        @Around("execution(public * (@org.springframework.validation.annotation.Validated *).*(..))")
        public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
            return joinPoint.proceed();
        }
    }
}
