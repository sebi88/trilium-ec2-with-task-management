package xxx.uk.tasks.fetching;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import xxx.uk.tasks.fetching.HtmlParser.NavbarAnchor;

public class HtmlParserTest {

  @Test
  void should_find_navbar_anchors() throws IOException {
    String html = getResource("Tasks.html");

    assertThat(HtmlParser.findNavbarAnchors(Jsoup.parse(html))).containsExactlyInAnyOrder(
        new NavbarAnchor("Tasks.html", "./bBX8wwwwww"),
        new NavbarAnchor("Tasks.js", "./UGy6nwwwwwwD"),
        new NavbarAnchor("2023-10-14 Do it once sample", "./RqFPmwwwwww"),
        new NavbarAnchor("xxxx-10-26 Yearly exmaple", "./yaDEwwwwww"),
        new NavbarAnchor("2024-10-26 Another sample", "./fY4wwwwww"),
        new NavbarAnchor("Group1 Tasks", "./O5dwwwwww"),
        new NavbarAnchor("xxxx-xx-01 Group1 task1", "./4zJJ5wwwwww"),
        new NavbarAnchor("xxxx-xx-27 Group1 task2", "./Pp1NWS0FEpqu"),
        new NavbarAnchor("xxxx-04-21 Group1 task3", "./pecMwwwwww"),
        new NavbarAnchor("xxxx-12-26 Group1 task4", "./HmKKwwwwww"),
        new NavbarAnchor("Group2 Tasks", "./8w7U8wwwwww"),
        new NavbarAnchor("xxxx-xx-08 Group2 task1", "./brzkwwwwww"),
        new NavbarAnchor("xxxx-xx-15 Group2 task2", "./SFKSwwwwww"),
        new NavbarAnchor("xxx-02/08-11 Group2 task3", "./8ejVwwwwww"),
        new NavbarAnchor("xxxx-04-01 Group2 task4", "./edsNwwwwww"),
        new NavbarAnchor("xxxx-05-17 Group2 task5", "./8g3Ywwwwww"),
        new NavbarAnchor("xxxx-06-17 Group2 task6", "./bX76wwwwww"),
        new NavbarAnchor("xxxx-09-25 Group2 task7", "./oQLVwwwwww"),
        new NavbarAnchor("xxxx-11-01 Group2 task8", "./H9QGwwwwww"));
  }
	
    @Test
    void should_get_empty_content() throws IOException {
      String html = getResource("noContent.html");
      assertThat(HtmlParser.getContentHtml(Jsoup.parse(html))).isEqualTo("");
    }
	
	@Test
	void should_get_content() throws IOException {
		String html = getResource("hasContent.html");
		assertThat(HtmlParser.getContentHtml(Jsoup.parse(html)))
			.isEqualTo("<p><strong>TODO:</strong></p>\n"
					+ "<ul>\n"
					+ " <li>todo1</li>\n"
					+ " <li>todo2</li>\n"
					+ " <li>todo3</li>\n"
					+ "</ul>");
	}

    private String getResource(String fileName) throws IOException {
      return IOUtils.toString(getClass().getResourceAsStream(fileName), Charset.defaultCharset());
    }
}
