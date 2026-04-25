package tn.ecocycle.ecocycletn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EcocycleTnApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcocycleTnApplication.class, args);
	}

}
