package com.erokin.strove;

import com.erokin.strove.config.properties.AppSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppSecurityProperties.class})
public class StroveApplication {

    public static void main(String[] args) {
        SpringApplication.run(StroveApplication.class, args);
    }
}
