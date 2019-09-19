package com.cs6310.OsMowSis;

import com.cs6310.Controller.ApiController;
import com.cs6310.Services.SimulationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
@ComponentScan(basePackageClasses = ApiController.class)
public class OsMowSisApplication {

	private static String fileName;

	public static void main(String[] args) {
		fileName = args[0];
		SpringApplication.run(OsMowSisApplication.class, args);
	}

	@Bean(name = "SimulationService")
	@Scope("singleton")
	public SimulationService getSimulationService() {
		SimulationService srv = new SimulationService();
		srv.readScenario(fileName);
		return srv;
	}
}
