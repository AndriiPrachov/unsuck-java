package awsm;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = { JacksonAutoConfiguration.class })
@EnableScheduling
public class AwesomeBank {

  static {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  public static void main(String[] args) {
    SpringApplication.run(AwesomeBank.class, args);
  }
}