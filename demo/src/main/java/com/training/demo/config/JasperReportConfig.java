package com.training.demo.config;

import com.training.demo.helpers.Reports.JasperReportGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for JasperReport
 * Tạo singleton bean để compile templates 1 lần duy nhất khi app start
 */
@Configuration
@Slf4j
public class JasperReportConfig {

    @Bean
    public JasperReportGenerator jasperReportGenerator() {
        log.info("[JasperReportConfig] Initializing JasperReportGenerator bean...");
        long startTime = System.currentTimeMillis();

        JasperReportGenerator generator = new JasperReportGenerator();

        long endTime = System.currentTimeMillis();
        log.info("[JasperReportConfig] JasperReportGenerator initialized in {}ms", (endTime - startTime));

        return generator;
    }
}

