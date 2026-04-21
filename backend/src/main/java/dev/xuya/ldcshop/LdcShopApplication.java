package dev.xuya.ldcshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * LDC Shop 启动类 / LDC Shop Application Entry Point
 *
 * @author xuya
 */
@SpringBootApplication
@MapperScan("dev.xuya.ldcshop.mapper")
@EnableScheduling
public class LdcShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(LdcShopApplication.class, args);
    }
}
