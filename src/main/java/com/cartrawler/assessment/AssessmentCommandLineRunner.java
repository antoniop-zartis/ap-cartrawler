package com.cartrawler.assessment;

import com.cartrawler.assessment.car.AssessmentRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
@Component
public class AssessmentCommandLineRunner implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AssessmentCommandLineRunner.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        AssessmentRunner.process();
    }
}