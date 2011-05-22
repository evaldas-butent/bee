package com.butent.bee.client.composite;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.datepicker.client.DatePicker;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Date;

public class InputDate extends Composite implements Editor {

  public static class DefaultFormat implements Format {

    private final DateTimeFormat dateTimeFormat;

    public DefaultFormat(DateTimeFormat dateTimeFormat) {
      this.dateTimeFormat = dateTimeFormat;
    }

    public String format(InputDate box, Date date) {
      if (date == null) {
        return "";
      } else {
        return dateTimeFormat.format(date);
      }
    }

    public DateTimeFormat getDateTimeFormat() {
      return dateTimeFormat;
    }

    @SuppressWarnings("deprecation")
    public Date parse(InputDate dateBox, String dateText, boolean reportError) {
      Date date = null;
      try {
        if (dateText.length() > 0) {
          date = dateTimeFormat.parse(dateText);
        }
      } catch (IllegalArgumentException exception) {
        try {
          date = new Date(dateText);
        } catch (IllegalArgumentException e) {
          if (reportError) {
            dateBox.addStyleName(DATE_BOX_FORMAT_ERROR);
          }
          return null;
        }
      }
      return date;
    }

    public void reset(InputDate dateBox, boolean abandon) {
      dateBox.removeStyleName(DATE_BOX_FORMAT_ERROR);
    }
  }

  public interface Format {

    String format(InputDate dateBox, Date date);

    Date parse(InputDate dateBox, String text, boolean reportError);

    void reset(InputDate dateBox, boolean abandon);
  }

  private class DateBoxHandler implements ValueChangeHandler<Date>,
      FocusHandler, BlurHandler, ClickHandler, KeyDownHandler, CloseHandler<PopupPanel> {

    public void onBlur(BlurEvent event) {
      if (isDatePickerShowing() == false) {
        updateDateFromTextBox();
      }
    }

    public void onClick(ClickEvent event) {
      showDatePicker();
    }

    public void onClose(CloseEvent<PopupPanel> event) {
      if (allowDPShow) {
        updateDateFromTextBox();
      }
    }

    public void onFocus(FocusEvent event) {
      if (allowDPShow && isDatePickerShowing() == false) {
        showDatePicker();
      }
    }

    public void onKeyDown(KeyDownEvent event) {
      switch (event.getNativeKeyCode()) {
        case KeyCodes.KEY_ENTER:
        case KeyCodes.KEY_TAB:
          updateDateFromTextBox();
          break;
        case KeyCodes.KEY_ESCAPE:
        case KeyCodes.KEY_UP:
          hideDatePicker();
          break;
        case KeyCodes.KEY_DOWN:
          showDatePicker();
          break;
      }
    }

    public void onValueChange(ValueChangeEvent<Date> event) {
      setValue(parseDate(false), event.getValue(), true);
      hideDatePicker();
      preventDatePickerPopup();
      box.setFocus(true);
    }
  }

  private static final String DATE_BOX_FORMAT_ERROR = "dateBoxFormatError";

  public static final String DEFAULT_STYLENAME = "bee-DateBox";
  private static final DefaultFormat DEFAULT_FORMAT = 
    new DefaultFormat(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM));
  private final PopupPanel popup;
  private final InputText box = new InputText();
  private final DatePicker picker;
  private Format format;
  private boolean allowDPShow = true;

  public InputDate() {
    this(new DatePicker(), null, DEFAULT_FORMAT);
  }

  public InputDate(DatePicker picker, Date date, Format format) {
    this.picker = picker;
    this.popup = new PopupPanel(true);
    Assert.notNull(format);
    this.format = format;

    popup.addAutoHidePartner(box.getElement());
    popup.setWidget(picker);
    popup.setStyleName("dateBoxPopup");

    initWidget(box);
    setStyleName(DEFAULT_STYLENAME);

    DateBoxHandler handler = new DateBoxHandler();
    picker.addValueChangeHandler(handler);
    box.addFocusHandler(handler);
    box.addBlurHandler(handler);
    box.addClickHandler(handler);
    box.addKeyDownHandler(handler);
    popup.addCloseHandler(handler);
    setValue(date);
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return addDomHandler(handler, BlurEvent.getType());
  }

  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }
  
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public void createId() {
    DomUtils.createId(this, "date-box");
  }

  public int getCursorPos() {
    return box.getCursorPos();
  }

  public Date getDate() {
    return parseDate(true);
  }

  public DatePicker getDatePicker() {
    return picker;
  }

  public Format getFormat() {
    return this.format;
  }
  
  public String getId() {
    return DomUtils.getId(this);
  }

  public String getNormalizedValue() {
    return getBox().getNormalizedValue();
  }

  public int getTabIndex() {
    return box.getTabIndex();
  }

  public String getValue() {
    return box.getText().trim();
  }

  public void hideDatePicker() {
    popup.hide();
  }
  
  public boolean isDatePickerShowing() {
    return popup.isShowing();
  }

  public boolean isNullable() {
    return getBox().isNullable();
  }

  public void setAccessKey(char key) {
    box.setAccessKey(key);
  }

  public void setEnabled(boolean enabled) {
    box.setEnabled(enabled);
  }

  public void setFocus(boolean focused) {
    box.setFocus(focused);
  }

  public void setFormat(Format format) {
    Assert.notNull(format, "A Date box may not have a null format");
    if (this.format != format) {
      Date date = getDate();

      this.format.reset(this, true);

      this.format = format;
      setValue(date);
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setNullable(boolean nullable) {
    getBox().setNullable(nullable);
  }

  public void setTabIndex(int index) {
    box.setTabIndex(index);
  }

  public void setValue(Date date) {
    setValue(date, false);
  }

  public void setValue(Date date, boolean fireEvents) {
    setValue(picker.getValue(), date, fireEvents);
  }

  public void setValue(String value) {
    setValue(value, false);
  }

  public void setValue(String value, boolean fireEvents) {
    setValue(getFormat().parse(this, value, true), fireEvents);
  }

  public void showDatePicker() {
    Date current = parseDate(false);
    if (current == null) {
      current = new Date();
    }
    picker.setCurrentMonth(current);
    popup.showRelativeTo(this);
  }

  public void startEdit(String oldValue, char charCode) {
    setValue(BeeUtils.trim(oldValue));
  }

  public boolean validate() {
    return getBox().validate();
  }

  private InputText getBox() {
    return box;
  }
  
  private Date parseDate(boolean reportError) {
    if (reportError) {
      getFormat().reset(this, false);
    }
    String text = box.getText().trim();
    return getFormat().parse(this, text, reportError);
  }

  private void preventDatePickerPopup() {
    allowDPShow = false;
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public void execute() {
        allowDPShow = true;
      }
    });
  }

  private void setValue(Date oldDate, Date date, boolean fireEvents) {
    if (date != null) {
      picker.setCurrentMonth(date);
    }
    picker.setValue(date, false);
    format.reset(this, false);
    box.setText(getFormat().format(this, date));

    if (fireEvents) {
      ValueChangeEvent.fireIfNotEqual(this, oldDate.toString(), date.toString());
    }
  }

  private void updateDateFromTextBox() {
    Date parsedDate = parseDate(true);
    if (parsedDate != null) {
      setValue(picker.getValue(), parsedDate, true);
    }
  }
}

