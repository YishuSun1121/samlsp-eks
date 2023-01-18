package com.nextjump.saml2;

import com.google.common.collect.Lists;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
@EntityScan({"com.nextjump.saml2.model"})
@EnableJpaRepositories({"com.nextjump.saml2.repository"})
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    @Bean
    public GroupedOpenApi apis(@Autowired
                                       AppSettings appSettings) {
        if (appSettings.isEnableLB()) {
            return GroupedOpenApi.builder().group("apis")
                    .addOpenApiCustomiser(openApi -> openApi.info(new Info().title("Management API").version("1.0.0"))
                            .servers(Lists.newArrayList(new Server(
                            ).url(appSettings.getLbSchema() + "://" + appSettings.getLbServerName() + "/"))))
                    .packagesToScan("com.nextjump.saml2.controllers")
                    .build();
        } else {
            return GroupedOpenApi.builder().group("apis")
                    .addOpenApiCustomiser(openApi -> openApi.info(new Info().title("Management API").version("1.0.0")))
                    .packagesToScan("com.nextjump.saml2.controllers")
                    .build();
        }

    }

}