package pers.wjx.ojsb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OjsbApplication {

    public static void main(String[] args) {
        SpringApplication.run(OjsbApplication.class, args);
    }

}
