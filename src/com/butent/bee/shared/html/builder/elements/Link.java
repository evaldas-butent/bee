package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.Element;
import com.butent.bee.shared.html.builder.Keywords;

public class Link extends Element {

  public enum Rel {
    ALTERNATE("alternate"),
    AUTHOR("author"),
    BOOKMARK("bookmark"),
    HELP("help"),
    ICON("icon"),
    LICENSE("license"),
    NEXT("next"),
    NO_FOLLOW("nofollow"),
    NO_REFERRER("noreferrer"),
    PREFETCH("prefetch"),
    PREV("prev"),
    SEARCH("search"),
    STYLE_SHEET("stylesheet"),
    TAG("tag");

    private final String keyword;

    private Rel(String keyword) {
      this.keyword = keyword;
    }

    public String getKeyword() {
      return keyword;
    }
  }

  public Link() {
    super();
  }

  public Link addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Link crossoriginAnonymous() {
    setAttribute(Attribute.CROSSORIGIN, Keywords.CORS_SETTINGS_ANONYMOUS);
    return this;
  }

  public Link crossoriginUseCredentials() {
    setAttribute(Attribute.CROSSORIGIN, Keywords.CORS_SETTINGS_USE_CREDENTIALS);
    return this;
  }

  

  public Link href(String value) {
    setAttribute(Attribute.HREF, value);
    return this;
  }

  public Link hrefLang(String value) {
    setAttribute(Attribute.HREFLANG, value);
    return this;
  }

  public Link id(String value) {
    setId(value);
    return this;
  }

  public Link lang(String value) {
    setLang(value);
    return this;
  }

  public Link media(String value) {
    setAttribute(Attribute.MEDIA, value);
    return this;
  }

  public Link rel(Rel rel) {
    if (rel == null) {
      removeAttribute(Attribute.REL);
    } else {
      setAttribute(Attribute.REL, rel.getKeyword());
    }
    return this;
  }

  public Link sizes(String value) {
    setAttribute(Attribute.SIZES, value);
    return this;
  }

  public Link title(String value) {
    setTitle(value);
    return this;
  }

  public Link type(String value) {
    setAttribute(Attribute.TYPE, value);
    return this;
  }
}
