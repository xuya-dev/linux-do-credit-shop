package dev.xuya.ldcshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LDC Shop 启动类 / LDC Shop Application Entry Point
 *
 * @author xuya
 */
@SpringBootApplication
@MapperScan("dev.xuya.ldcshop.mapper")
public class LdcShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(LdcShopApplication.class, args);
    }
}
