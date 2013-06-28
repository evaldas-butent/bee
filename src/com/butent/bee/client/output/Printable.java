package com.butent.bee.client.output;

import com.google.gwt.dom.client.Element;

import com.butent.bee.shared.ui.HasCaption;

public interface Printable extends HasCaption {

  Element getPrintElement();

  boolean onPrint(Element source, Element target);
}
