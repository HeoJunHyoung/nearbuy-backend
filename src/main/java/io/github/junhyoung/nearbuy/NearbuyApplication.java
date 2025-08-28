package io.github.junhyoung.nearbuy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NearbuyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NearbuyApplication.class, args);
    }

}
