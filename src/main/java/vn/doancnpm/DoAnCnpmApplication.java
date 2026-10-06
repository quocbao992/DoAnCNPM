package vn.doancnpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan (basePackages = "vn.doancnpm.domain")
public class DoAnCnpmApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoAnCnpmApplication.class, args);
    }
}
