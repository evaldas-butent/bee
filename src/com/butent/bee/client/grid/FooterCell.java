package com.butent.bee.client.grid;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements footer cells rendering and behavior management.
 */

public class FooterCell extends AbstractCell<String> {

  /**
   * Specifies footer cell's templates for safeHtml usage.
   */

  public interface Template extends SafeHtmlTemplates {
    @Template("<input type=\"search\" value=\"{0}\" tabindex=\"-1\" class=\"bee-FooterCell\"></input>")
    SafeHtml input(String value);
  }

  private static Template template;

  private boolean hasFocus = false;

  public FooterCell() {
    super(EventUtils.EVENT_TYPE_FOCUS, EventUtils.EVENT_TYPE_BLUR, EventUtils.EVENT_TYPE_CHANGE,
        EventUtils.EVENT_TYPE_KEY_DOWN, EventUtils.EVENT_TYPE_KEY_UP);
    init();
  }

  @Override
  public boolean isEditing(Context context, Element parent, String value) {
    return hasFocus;
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event,
      ValueUpdater<String> valueUpdater) {
    EventTarget target = event.getEventTarget();
    InputElement input;
    if (EventUtils.isInputElement(target)) {
      input = InputElement.as(Element.as(target));
    } else {
      input = DomUtils.getInputElement(parent);
    }

    String type = event.getType();

    if (BeeUtils.same(type, EventUtils.EVENT_TYPE_FOCUS)) {
      hasFocus = true;
    } else if (BeeUtils.same(type, EventUtils.EVENT_TYPE_BLUR)) {
      hasFocus = false;
    } else if (BeeUtils.same(type, EventUtils.EVENT_TYPE_CHANGE)) {
      if (input != null && valueUpdater != null) {
        valueUpdater.update(input.getValue());
      }
    }
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.append(template.input(value));
    } else {
      sb.appendHtmlConstant("<input type=\"search\" tabindex=\"-1\" class=\"bee-FooterCell\"></input>");
    }
  }

  private void init() {
    if (template == null) {
      template = GWT.create(Template.class);
    }
  }
}
