package control.prendario.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvironmentProfilesTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(context -> {
                var environment = context.getEnvironment();
                environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
                environment.getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
                new ConfigDataApplicationContextInitializer().initialize(context);
            });

    @Test
    void defaultProfileUsesTheLocalDatabaseAndApiAddress() {
        runner.run(context -> {
            var environment = context.getEnvironment();
            assertThat(environment.getDefaultProfiles()).containsExactly("local");
            assertThat(environment.getProperty("server.address")).isEqualTo("127.0.0.1");
            assertThat(environment.getProperty("spring.datasource.url"))
                    .startsWith("jdbc:mysql://localhost:3306/controlprendario");
            assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("update");
            assertThat(environment.getProperty("app.cors.allowed-origins")).contains("http://localhost:4200");
        });
    }

    @Test
    void localProfileIgnoresDeployedConnectionVariables() {
        runner.withPropertyValues(
                "spring.profiles.active=local",
                "DB_URL=jdbc:mysql://db.example:4000/controlprendario",
                "DB_USERNAME=app-test")
                .run(context -> {
                    var environment = context.getEnvironment();
                    assertThat(environment.getProperty("spring.datasource.url"))
                            .startsWith("jdbc:mysql://localhost:3306/");
                    assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("root");
                });
    }

    @Test
    void deployedProfileUsesProvidedDatabaseAndNoCrossOriginAccessByDefault() {
        runner.withPropertyValues(
                "spring.profiles.active=prod",
                "DB_URL=jdbc:mysql://db.example:4000/controlprendario?sslMode=VERIFY_IDENTITY",
                "DB_USERNAME=app-test")
                .run(context -> {
                    var environment = context.getEnvironment();
                    assertThat(environment.getActiveProfiles()).containsExactly("prod");
                    assertThat(environment.getProperty("spring.datasource.url"))
                            .isEqualTo("jdbc:mysql://db.example:4000/controlprendario?sslMode=VERIFY_IDENTITY");
                    assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("app-test");
                    assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
                    assertThat(environment.getProperty("server.forward-headers-strategy")).isEqualTo("framework");
                    assertThat(environment.getProperty("app.cors.allowed-origins")).isEmpty();
                });
    }

    @Test
    void localProfileCanSignTokensWithoutAConfiguredSecret() {
        runner.withPropertyValues("JWT_SECRET=production-test-value")
                .run(context -> assertThat(context.getEnvironment().getProperty("jwt.secret"))
                        .hasSizeGreaterThanOrEqualTo(32)
                        .isNotEqualTo("production-test-value"));
    }

    @Test
    void localProfileUsesAnExplicitLocalSecretWhenProvided() {
        runner.withPropertyValues("LOCAL_JWT_SECRET=explicit-test-key-for-local-sessions")
                .run(context -> assertThat(context.getEnvironment().getProperty("jwt.secret"))
                        .isEqualTo("explicit-test-key-for-local-sessions"));
    }

    @Test
    void deployedProfileRequiresItsSigningSecret() {
        runner.withPropertyValues("spring.profiles.active=prod",
                                  "LOCAL_JWT_SECRET=explicit-test-key-for-local-sessions")
                .run(context -> assertThatThrownBy(() -> context.getEnvironment().getProperty("jwt.secret"))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("JWT_SECRET"));
    }

    @Test
    void deployedProfileHasNoLocalDatabaseFallback() {
        runner.withPropertyValues("spring.profiles.active=prod")
                .run(context -> assertThatThrownBy(() -> context.getEnvironment().getProperty("spring.datasource.url"))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("DB_URL"));
    }
}
