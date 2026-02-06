package com.i5irin.kurabekko;

import com.i5irin.kurabekko.app.config.AppProperties;
import com.i5irin.kurabekko.line.config.LineProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = { AppProperties.class, LineProperties.class })
public class KurabekkoApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(KurabekkoApiApplication.class, args);
	}
}
