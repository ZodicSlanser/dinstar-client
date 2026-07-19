package io.github.zodicslanser.dinstar.autoconfigure;

import io.github.zodicslanser.dinstar.DinstarClient;
import io.github.zodicslanser.dinstar.DinstarConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Optional Spring Boot auto-configuration that wires a single {@link DinstarClient} from
 * {@code dinstar.*} properties, for the common single-gateway case.
 *
 * <p><strong>This is a convenience, not the primary API.</strong> It activates only when
 * {@code dinstar.base-url} is set, and backs off if the app already defines a {@link DinstarClient}
 * bean. An app running several gateways should ignore this and construct one
 * {@link DinstarClient} per gateway directly (see {@link DinstarConfig#builder()}). The bean is
 * created via {@code destroyMethod = "close"} so its pooled connections are released on shutdown.
 */
@AutoConfiguration
@EnableConfigurationProperties(DinstarProperties.class)
@ConditionalOnProperty(prefix = "dinstar", name = "base-url")
public class DinstarAutoConfiguration {

    /**
     * Builds the single {@link DinstarClient} bean from the bound properties.
     *
     * @param properties the {@code dinstar.*} properties
     * @return a configured client (closed automatically on context shutdown)
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public DinstarClient dinstarClient(DinstarProperties properties) {
        DinstarConfig config = DinstarConfig.builder()
                .baseUrl(properties.getBaseUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .authScheme(properties.getAuthScheme())
                .connectTimeout(properties.getConnectTimeout())
                .responseTimeout(properties.getResponseTimeout())
                .verifyTls(properties.isVerifyTls())
                .build();
        return new DinstarClient(config);
    }
}
