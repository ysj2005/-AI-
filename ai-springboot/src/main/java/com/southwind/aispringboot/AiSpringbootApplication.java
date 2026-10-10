package com.southwind.aispringboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.southwind.aispringboot.mapper")
public class AiSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiSpringbootApplication.class, args);
	}

}
