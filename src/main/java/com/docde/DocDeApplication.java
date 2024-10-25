package com.docde;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DocDeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocDeApplication.class, args);
    }

}
