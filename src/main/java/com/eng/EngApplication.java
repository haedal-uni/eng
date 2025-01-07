package com.eng;

import com.eng.service.SearchService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EngApplication {

    public static void main(String[] args) {
        SpringApplication.run(EngApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(SearchService searchService) {
        return args -> {
            searchService.addWordRedis();
        };
    }
}
