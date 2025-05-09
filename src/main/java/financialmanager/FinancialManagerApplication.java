package financialmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FinancialManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialManagerApplication.class, args);
	}

}
