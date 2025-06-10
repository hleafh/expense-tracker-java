// src/main/java/com/expensetracker/ExpenseTrackerApplication.java
package com.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// 引入 CORS 配置所需的類別
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull; // 引入 @NonNull 註解

@SpringBootApplication
// 讓主應用程式類別直接實現 WebMvcConfigurer 介面，以配置 CORS
public class ExpenseTrackerApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }

    /**
     * 直接在這裡配置 CORS 策略。
     * 確保 CORS 設定被載入。
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**") // 將 CORS 應用於所有 /api/ 開頭的路徑
                .allowedOrigins("*")   // 允許所有來源 (開發階段使用，生產環境不推薦)
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 允許的 HTTP 方法
                .allowedHeaders("*");   // 允許所有請求頭
               // .allowCredentials(true); // 允許發送認證資訊 (如 Cookies)
    }
}