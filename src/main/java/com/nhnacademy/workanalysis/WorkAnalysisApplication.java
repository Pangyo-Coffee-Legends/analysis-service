package com.nhnacademy.workanalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class WorkAnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkAnalysisApplication.class, args);
	}

}
