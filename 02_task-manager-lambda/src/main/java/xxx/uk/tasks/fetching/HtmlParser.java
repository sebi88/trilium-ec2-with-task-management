package xxx.uk.tasks.fetching;

import java.util.Collection;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class HtmlParser {

  static Collection<NavbarAnchor> findNavbarAnchors(Document document) {
    return document.select("#menu a").stream()
        .map(e -> new NavbarAnchor(e.text(), e.attr("href")))
        .toList();
  }

  /**
   * Sample: <b>Sample html</>
   */
  static String getContentHtml(Document document) {
    Element element = document.select("#content").first();
    return element != null ? element.html() : "";
  }

  /**
   * Sample: title: e.g xxxx-10-26 Yearly exmaple href: ./yaDEwwwww
   */
  record NavbarAnchor(String title, String href) {
  }
}
