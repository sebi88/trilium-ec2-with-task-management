package xxx.uk.tasks.fetching;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import xxx.uk.tasks.Task;
import xxx.uk.tasks.fetching.HtmlParser.NavbarAnchor;

public class Fetcher {

  private static final Logger LOG = LogManager.getLogger(Fetcher.class);

  private static final Duration TIMEOUT = Duration.ofSeconds(25);

  private final String taskPageUrl;
  private final String user;
  private final String password;
  private final LocalDate today;

  public Fetcher(String url, String user, String password, LocalDate today) {
    this.taskPageUrl = url;
    this.user = user;
    this.password = password;
    this.today = today;
  }

  public List<Task> listTodayTasks() throws IOException, URISyntaxException {
    Document taskPage = download("Tasks", taskPageUrl);
    Collection<NavbarAnchor> allAnchors = HtmlParser.findNavbarAnchors(taskPage);
    LOG.debug("All anchors: {}",
        allAnchors.stream().map(NavbarAnchor::title).collect(Collectors.joining("\n\t")));
    List<NavbarAnchor> todayAnchors =
        allAnchors.stream().filter(anchor -> DatePattern.isPattern(anchor.title()))
            .filter(anchor -> DatePattern.matchesToday(today, anchor.title())).toList();

    List<Task> tasks = new ArrayList<Task>();
    for (NavbarAnchor anchor : todayAnchors) {
      tasks.add(toTask(anchor));
    }
    return tasks;
  }

  private Task toTask(NavbarAnchor anchor) throws IOException, URISyntaxException {
    Document subPage = download(anchor.title(), taskPageUrl + "/." + anchor.href());
    return new Task(anchor.title(), HtmlParser.getContentHtml(subPage));
  }

  private Document download(String title, String fullUrl) throws IOException, URISyntaxException {
    LOG.info("Downloading page: {} {}", title, fullUrl);
    HttpResponse<String> response = Unirest.get(new URI(fullUrl).normalize().toString())
        .basicAuth(user, password).connectTimeout((int) TIMEOUT.toMillis()).asString();
    if (!response.isSuccess()) {
      if (response.getStatus() >= 400 && response.getStatus() < 500) {
        throw new RuntimeException(
            "Failed response " + response.getStatus() + " " + response.getStatusText());
      } else {
        throw new IOException(
            "Failed response " + response.getStatus() + " " + response.getStatusText());
      }
    }
    LOG.info("Downloading page: {} {}(finished)", title, fullUrl);
    return Jsoup.parse(response.getBody());
  }
}
