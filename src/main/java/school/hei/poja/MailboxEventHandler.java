package school.hei.poja;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import school.hei.poja.endpoint.event.EventConf;
import school.hei.poja.endpoint.event.EventConsumer;
import software.amazon.awssdk.services.sqs.SqsClient;

@Slf4j
@PojaGenerated
public class MailboxEventHandler implements RequestHandler<SQSEvent, String> {

  @Override
  public String handleRequest(SQSEvent event, Context context) {
    log.info("Received: event={}, awsReqId={}", event, context.getAwsRequestId());
    List<SQSEvent.SQSMessage> messages = event.getRecords();
    log.info("SQS messages: {}", messages);

    ConfigurableApplicationContext applicationContext = applicationContext();
    EventConsumer eventConsumer = applicationContext.getBean(EventConsumer.class);
    EventConf eventConf = applicationContext.getBean(EventConf.class);
    SqsClient sqsClient = applicationContext.getBean(SqsClient.class);

    eventConsumer.accept(EventConsumer.toAcknowledgeableEvent(eventConf, sqsClient, messages));

    applicationContext.close();
    return "ok";
  }

  private ConfigurableApplicationContext applicationContext(String... args) {
    SpringApplication application = new SpringApplication(PojaApplication.class);
    application.setDefaultProperties(Map.of("spring.flyway.enabled", "false"));
    return application.run(args);
  }
}
