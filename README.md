# Config refresh with a double-proxied `@ConfigurationProperties` bean

Reproducer for a config refresh failing on a `@ConfigurationProperties` bean whose AOP proxy wraps
another AOP proxy. Spring Boot 4.1.2, Java 17.

## Run

```shell
./gradlew test                                   # Spring Cloud 2025.1.4 (spring-cloud-context 5.0.4)
./gradlew test -PspringCloudVersion=2025.1.2     # spring-cloud-context 5.0.2
SPRING_CLOUD_COMPATIBILITY_VERIFIER_ENABLED=false ./gradlew test -PspringCloudVersion=2025.1.1
                                                 # spring-cloud-context 5.0.1, which does not support Boot 4.1
```

A locally installed spring-cloud-context, e.g. built from a pull request, can be tested with
`-PspringCloudContextVersion=<version>`.

## The setup

- `DemoProperties` is `@Validated`, so the method validation post-processor proxies it.
- `ValidatedAspect` matches every `@Validated` class, so the AspectJ auto-proxy creator wraps that proxy
  in a second proxy.

`DoubleProxyRefreshTest` asserts that the bean is double-proxied, changes `demo.message` and calls
`ContextRefresher.refresh()`.

## What happens

| spring-cloud-context | Result                       |
|----------------------|------------------------------|
| 5.0.1                | The property is rebound      |
| 5.0.2 to 5.0.4       | The refresh throws (below)   |

```
org.springframework.beans.factory.BeanCreationException: Error creating bean with name
'com.example.repro.ReproApplication$DemoProperties$$SpringCGLIB$$0': Cannot invoke
"org.springframework.cglib.proxy.Dispatcher.loadObject()" ...
Caused by: java.lang.NullPointerException: Cannot invoke "org.springframework.cglib.proxy.Dispatcher.loadObject()"
	at com.example.repro.ReproApplication$DemoProperties$$SpringCGLIB$$0.isFrozen(<generated>)
	at org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor.postProcessAfterInitialization(...)
	...
	at org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder.rebind(...)
```

For an AOP-proxied bean, `ConfigurationPropertiesRebinder` creates a fresh instance with
`createBean(targetClass)`, where `targetClass` is `AopUtils.getTargetClass(bean)`. That unwraps one level,
so for a double proxy it is the class of the inner proxy. The fresh instance is a CGLIB proxy without its
callbacks, and the first call on it fails.
