package com.butent.bee.server.modules.mail;

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.safety.Whitelist;

public class HtmlUtils {

  public static String cleanHtml(String dirtyHtml) {
    if (dirtyHtml != null) {
      return Jsoup.clean(dirtyHtml, Whitelist.relaxed());
    }
    return dirtyHtml;
  }

  public static boolean hasHtml(String content) {
    if (content != null) {
      return !Jsoup.isValid(content, Whitelist.none());
    }
    return false;
  }

  public static String stripHtml(String content) {
    if (hasHtml(content)) {
      return new HtmlToPlainText().getPlainText(Jsoup.parse(content));
    }
    return content;
  }

  private HtmlUtils() {
  }
}
