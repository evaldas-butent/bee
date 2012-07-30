package com.butent.bee.client.output;

import com.google.gwt.dom.client.Element;

public interface Printable {

  Element getPrintElement();

  boolean onPrint(Element source, Element target);
}
