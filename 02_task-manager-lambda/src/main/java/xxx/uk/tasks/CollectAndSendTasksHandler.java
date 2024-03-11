package xxx.uk.tasks;


import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import xxx.uk.tasks.fetching.Fetcher;
import xxx.uk.tasks.sending.Sender;

public class CollectAndSendTasksHandler implements RequestHandler<Object, String> {

  private static final Logger LOG = LogManager.getLogger(CollectAndSendTasksHandler.class);

  private static final String SES_SENDER = "SES_SENDER";
  private static final String SES_RECIPIENT = "SES_RECIPIENT";
  private static final String SES_AWS_PROFILE = "SES_AWS_PROFILE";
  private static final String SES_AWS_REGION = "SES_AWS_REGION";

  private static final String TRILIUM_URL = "TRILIUM_URL";
  private static final String TRILIUM_USER = "TRILIUM_USER";
  private static final String TRILIUM_PASSWORD = "TRILIUM_PASSWORD";

  @Override
  public String handleRequest(Object event, Context context) {
    String url = System.getenv(TRILIUM_URL);
    String user = System.getenv(TRILIUM_USER);
    String password = System.getenv(TRILIUM_PASSWORD);
    String sesSender = System.getenv(SES_SENDER);
    String sesRecipient = System.getenv(SES_RECIPIENT);
    String sesProfile = System.getenv(SES_AWS_PROFILE);
    String sesRegion = System.getenv(SES_AWS_REGION);
    if (StringUtils.isAnyBlank(url, user, password, sesSender, sesRecipient)) {
      LOG.error("Url, user, password, sender, region or recipient is not set.");
      throw new RuntimeException("Url, user, password, sender, region or recipient is not set.");
    }
    LOG.info("Processing started on url: {} with user: {}", url, user);

    try {
      Fetcher fetcher = new Fetcher(url, user, password, LocalDate.now());
      List<Task> tasks = fetcher.listTodayTasks();
      LOG.info("{} task to send out", tasks.size());
      Sender sender = new Sender(sesSender, sesRecipient, sesProfile, sesRegion);
      sender.send(tasks);
      LOG.info("Processing successful.");
      return "";
    } catch (Exception e) {
      LOG.error("Processing failed.", e);
      throw new RuntimeException(e);
    }
  }
}
