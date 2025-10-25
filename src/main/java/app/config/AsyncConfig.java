package app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // minimal thread aktif
        executor.setMaxPoolSize(10);       // maksimal thread
        executor.setQueueCapacity(50);    // antrean task sebelum ditolak
        executor.setThreadNamePrefix("Async-"); // biar keliatan di log
        executor.initialize();
        return executor;
    }
    
}
