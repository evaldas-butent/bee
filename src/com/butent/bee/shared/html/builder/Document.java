package com.butent.bee.shared.html.builder;

import com.butent.bee.shared.html.builder.elements.Body;
import com.butent.bee.shared.html.builder.elements.Head;
import com.butent.bee.shared.html.builder.elements.Html;

public class Document {

  private final Doctype doctype;
  private final Html html;

  private final Head head;
  private final Body body;

  public Document() {
    this.doctype = new Doctype();
    this.html = new Html();

    this.head = new Head();
    this.body = new Body();
    
    html.appendChild(head);
    html.appendChild(body);
  }

  public String build() {
    StringBuilder sb = new StringBuilder();
    sb.append(doctype.build());
    sb.append(html.build());
    return sb.toString();
  }

  public Body getBody() {
    return body;
  }

  public Head getHead() {
    return head;
  }

  @Override
  public String toString() {
    return build();
  }
}
