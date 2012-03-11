package com.butent.bee.client.grid;

import com.google.common.collect.Sets;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Set;

public class FooterCell extends AbstractCell<String> {

  public interface Template extends SafeHtmlTemplates {
    @Template("<input type=\"search\" value=\"{0}\" tabindex=\"-1\" class=\"bee-FooterCell\"></input>")
    SafeHtml input(String value);
  }

  private static final Collection<String> requiredEvents = 
      Sets.newHashSet(EventUtils.EVENT_TYPE_INPUT);
  private static final Collection<String> defaultUpdaterEvents =
      Sets.newHashSet(EventUtils.EVENT_TYPE_KEY_DOWN);

  private static Template template = null;

  private static Set<String> prepareEvents(Collection<String> updaterEvents) {
    Set<String> events = Sets.newHashSet(requiredEvents);

    if (updaterEvents == null || updaterEvents.isEmpty()) {
      events.addAll(defaultUpdaterEvents);
    } else {
      events.addAll(updaterEvents);
    }

    return events;
  }

  private final Collection<String> updaterEvents;

  private String oldValue = null;
  private String newValue = null;

  public FooterCell(Collection<String> updaterEvents) {
    super(prepareEvents(updaterEvents));
    this.updaterEvents = BeeUtils.isEmpty(updaterEvents) ? defaultUpdaterEvents : updaterEvents;
    init();
  }

  public String getNewValue() {
    return newValue;
  }

  public String getOldValue() {
    return oldValue;
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event,
      ValueUpdater<String> valueUpdater) {
    EventTarget target = event.getEventTarget();
    String type = event.getType();
    
    InputElement input;
    if (EventUtils.isInputElement(target)) {
      input = InputElement.as(Element.as(target));
    } else {
      input = DomUtils.getInputElement(parent);
    }
    if (input == null) {
      return;
    }

    String v = getInputValue(input);
    if (EventUtils.isInputEvent(type)) {
      setNewValue(v);
    }
    
    if (valueUpdater == null || !isUpdaterEvent(type)) {
      return;
    }

    if (EventUtils.isChange(type)) {
      if (!BeeUtils.equalsTrim(getOldValue(), v)) {
        setOldValue(v);
        valueUpdater.update(v);
      }
    } else if (EventUtils.isKeyEvent(type) && event.getKeyCode() == KeyCodes.KEY_ENTER) {
      event.preventDefault();
      setOldValue(v);
      valueUpdater.update(v);
    }
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    sb.append(template.input(BeeUtils.trim(getNewValue())));
  }

  private String getInputValue(InputElement input) {
    if (input == null) {
      return null;
    } else {
      return BeeUtils.trim(input.getValue());
    }
  }

  private void init() {
    if (template == null) {
      template = GWT.create(Template.class);
    }
  }

  private boolean isUpdaterEvent(String type) {
    if (this.updaterEvents == null) {
      return false;
    } else {
      return this.updaterEvents.contains(type);
    }
  }

  private void setNewValue(String newValue) {
    this.newValue = newValue;
  }

  private void setOldValue(String oldValue) {
    this.oldValue = oldValue;
  }
}
