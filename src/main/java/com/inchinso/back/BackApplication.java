package com.inchinso.back;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackApplication {

    public static void main(String[] args) {
        // .env 파일 로드 (Railway 환경에서는 환경변수 직접 주입되므로 ignoreIfMissing)
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        } catch (Exception ignored) {}

        SpringApplication.run(BackApplication.class, args);
    }
}
