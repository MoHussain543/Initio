package com.mbh.initio.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InitioDashboardProperties.class)
public class InitioDashboardConfiguration {
}
