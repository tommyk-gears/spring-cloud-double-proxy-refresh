package com.example.repro;

import com.example.repro.ReproApplication.DemoProperties;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A config refresh should rebind a {@code @ConfigurationProperties} bean whose proxy wraps another
 * proxy.
 */
@SpringBootTest(properties = "demo.message=before")
class DoubleProxyRefreshTest {

    @Autowired
    ContextRefresher contextRefresher;

    @Autowired
    ConfigurableEnvironment environment;

    @Autowired
    DemoProperties properties;

    @Test
    void doubleProxiedPropertiesAreRebound() throws Exception {
        assertThat(AopUtils.isAopProxy(this.properties)).as("outer proxy").isTrue();
        Object inner = ((Advised) this.properties).getTargetSource().getTarget();
        assertThat(AopUtils.isAopProxy(inner)).as("the outer proxy's target is a proxy too").isTrue();
        assertThat(this.properties.getMessage()).isEqualTo("before");

        TestPropertyValues.of("demo.message=after").applyTo(this.environment);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThatCode(this.contextRefresher::refresh).as("refresh").doesNotThrowAnyException();
        softly.assertThat(this.properties.getMessage()).as("message after the refresh").isEqualTo("after");
        softly.assertAll();
    }
}
