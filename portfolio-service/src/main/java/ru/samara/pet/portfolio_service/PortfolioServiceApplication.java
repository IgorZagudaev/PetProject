package ru.samara.pet.portfolio_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class PortfolioServiceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(PortfolioServiceApplication.class, args);
        String applicationName = context.getEnvironment()
                .getProperty("spring.application.name", "portfolio-service-default");
        System.out.println("✓✓✓✓✓ " + applicationName + " started successfully! ✓✓✓✓✓");
    }

}
