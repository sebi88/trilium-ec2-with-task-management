package xxx.uk.tasks.sending;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import xxx.uk.tasks.Task;

public class Sender {

  private static final Logger LOG = LogManager.getLogger(Sender.class);

  private final String sender;
  private final String recipient;
  private final String awsRegion;
  private final AwsCredentialsProvider credentialsProvider;

  public Sender(String sender, String recipient, String awsProfile, String awsRegion) {
    this.sender = sender;
    this.recipient = recipient;
    this.awsRegion = awsRegion;
    this.credentialsProvider = Strings.isBlank(awsProfile) ? DefaultCredentialsProvider.builder().build()
            : ProfileCredentialsProvider.create(awsProfile);
  }

  public void send(List<Task> tasks) {
    var builder = SesClient.builder().credentialsProvider(credentialsProvider);
    if (!Strings.isBlank(awsRegion)) {
      builder = builder.region(Region.of(awsRegion));
    }
    try (SesClient client = builder.build()) {
      for (Task task : tasks) {
        sendMail(client, task.title(), task.html());
      }
    }
  }

  private void sendMail(SesClient client, String subject, String bodyHTML) {
    Destination destination = Destination.builder().toAddresses(recipient).build();
    Content content = Content.builder().data(bodyHTML).build();
    Content sub = Content.builder().data(subject).build();
    Body body = Body.builder().html(content).build();
    Message msg = Message.builder().subject(sub).body(body).build();
    SendEmailRequest emailRequest =
        SendEmailRequest.builder().destination(destination).message(msg).source(sender).build();

    LOG.info("Sending: {}", subject);
    client.sendEmail(emailRequest);
  }
}
