package fr.univ.orleans.ws.wsalakon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@SpringBootApplication
public class WsAlakonApplication {

    public static void main(String[] args) {
        SpringApplication.run(WsAlakonApplication.class, args);
    }

}
