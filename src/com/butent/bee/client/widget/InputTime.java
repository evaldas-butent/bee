package com.butent.bee.client.widget;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasBeforeSelectionHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.Modality;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasIntStep;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

public class InputTime extends InputText implements HasDateTimeFormat, HasIntStep,
    HasBeforeSelectionHandlers<InputTime> {

  private static final String STYLE_POPUP = "bee-TimeBox-Popup";
  private static final String STYLE_TIME_PICKER = "bee-TimePicker";

  private static final int DEFAULT_STEP = 30;

  private DateTimeFormat format = null;

  private int stepValue = BeeConst.UNDEF;

  private int minMinutes = 0;
  private int maxMinutes = TimeUtils.MINUTES_PER_DAY;

  public InputTime() {
    super();
    addStyleName("bee-TimeBox");

    sinkEvents(Event.ONCLICK + Event.ONKEYPRESS);
  }

  @Override
  public HandlerRegistration addBeforeSelectionHandler(BeforeSelectionHandler<InputTime> handler) {
    return addHandler(handler, BeforeSelectionEvent.getType());
  }

  @Override
  public DateTimeFormat getDateTimeFormat() {
    return format;
  }

  @Override
  public EditorAction getDefaultFocusAction() {
    return null;
  }

  @Override
  public String getIdPrefix() {
    return "time-box";
  }

  public int getMaxMinutes() {
    return maxMinutes;
  }

  public int getMillis() {
    return TimeUtils.parseTime(getValue());
  }

  public int getMinMinutes() {
    return minMinutes;
  }

  public int getMinutes() {
    return getMillis() / TimeUtils.MILLIS_PER_MINUTE;
  }

  public int getNormalizedStep() {
    return BeeUtils.positive(getStepValue(), DEFAULT_STEP);
  }

  @Override
  public int getStepValue() {
    return stepValue;
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.INPUT_TIME;
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getValue());
  }

  @Override
  public void onBrowserEvent(Event event) {
    String type = event.getType();

    if (EventUtils.isClick(type)) {
      event.preventDefault();
      event.stopPropagation();
      pickTime();
      return;

    } else if (EventUtils.isKeyPress(type)) {
      if (handleChar(event.getCharCode())) {
        event.preventDefault();
        event.stopPropagation();
        return;
      }
    }

    super.onBrowserEvent(event);
  }

  @Override
  public void setDateTimeFormat(DateTimeFormat format) {
    this.format = format;
  }

  public void setMaxMinutes(int maxMinutes) {
    this.maxMinutes = maxMinutes;
  }

  public void setMinMinutes(int minMinutes) {
    this.minMinutes = minMinutes;
  }

  public void setMinutes(int minutes) {
    if (minutes < 0) {
      clearValue();
    } else {
      setValue(TimeUtils.renderMinutes(minutes, true));
    }
  }

  @Override
  public void setStepValue(int stepValue) {
    this.stepValue = stepValue;
  }

  public void setTime(DateTime dateTime) {
    if (dateTime == null) {
      clearValue();
    } else {
      setMinutes(TimeUtils.minutesSinceDayStarted(dateTime));
    }
  }
  
  protected boolean handleChar(int charCode) {
    if (charCode == '*') {
      pickTime();
      return true;
    }

    if (Character.isDigit(BeeUtils.toChar(charCode)) || charCode == DateTime.TIME_FIELD_SEPARATOR) {
      return false;
    }

    int oldMinutes = getMinutes();
    int newMinutes = oldMinutes;

    int incr;

    switch (charCode) {
      case 'h':
      case 'H':
        incr = (charCode == 'h') ? -1 : 1;
        newMinutes = oldMinutes + incr * TimeUtils.MINUTES_PER_HOUR;
        break;

      case 't':
      case 'T':
      case 'l':
      case 'L':
        newMinutes = TimeUtils.minutesSinceDayStarted(new DateTime());
        if (getStepValue() > 1) {
          newMinutes = BeeUtils.snap(newMinutes, getStepValue());
        } else {
          newMinutes = BeeUtils.snap(newMinutes, TimeUtils.MINUTES_PER_HOUR);
        }
        break;

      case '+':
      case '-':
        incr = (charCode == '+') ? 1 : -1;
        if (getStepValue() > 1) {
          incr *= getStepValue();
        }
        newMinutes = oldMinutes + incr;
        break;
    }

    if (newMinutes != oldMinutes) {
      BeforeSelectionEvent<InputTime> event = BeforeSelectionEvent.fire(this, this);
      if (event.isCanceled()) {
        return false;
      }
      
      setMinutes(clamp(newMinutes));
    }
    return true;
  }

  protected void pickTime() {
    BeforeSelectionEvent<InputTime> before = BeforeSelectionEvent.fire(this, this);
    if (before.isCanceled()) {
      return;
    }

    int step = getNormalizedStep();

    int start;
    if (getMinMinutes() > 0) {
      start = getMinMinutes();
    } else if (step <= TimeUtils.MINUTES_PER_HOUR) {
      start = getMinutes() % step;
    } else {
      start = 0;
    }

    int end;
    if (getMaxMinutes() > 0 && getMaxMinutes() > start) {
      end = getMaxMinutes();
    } else {
      end = TimeUtils.MINUTES_PER_DAY;
    }
    if (end <= start) {
      return;
    }

    final BeeListBox widget = new BeeListBox();
    widget.addStyleName(STYLE_TIME_PICKER);

    for (int i = start; i < end; i += step) {
      String item = TimeUtils.renderMinutes(i, true);
      widget.addItem(item);
    }
    widget.setVisibleItemCount(10);

    final Popup popup = new Popup(OutsideClick.CLOSE, Modality.MODAL, STYLE_POPUP);

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (widget.getSelectedIndex() >= 0) {
          String text = widget.getItemText(widget.getSelectedIndex());
          popup.hide();
          setValue(text);
        }
      }
    });

    widget.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER && widget.getSelectedIndex() >= 0) {
          String text = widget.getItemText(widget.getSelectedIndex());
          popup.hide();
          setValue(text);
        }
      }
    });

    popup.setWidget(widget);
    popup.setHideOnEscape(true);
    
    popup.setOnEscape(new ScheduledCommand() {
      @Override
      public void execute() {
        InputTime.this.setFocus(true);
      }
    });

    popup.showRelativeTo(getElement());

    if (!BeeUtils.isEmpty(getValue())) {
      final int index = widget.getItems().indexOf(getValue().trim());
      if (index > 0) {
        widget.setSelectedIndex(index);
      }
    }

    widget.setFocus(true);
  }

  private int clamp(int minutes) {
    if (getMinMinutes() >= 0 && minutes < getMinMinutes()) {
      return getMinMinutes();
    }
    if (getMaxMinutes() > 0 && getMaxMinutes() > getMinMinutes() && minutes >= getMaxMinutes()) {
      return getMaxMinutes();
    }
    return minutes;
  }
}
