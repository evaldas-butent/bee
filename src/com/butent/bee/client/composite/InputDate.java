package com.butent.bee.client.composite;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.datepicker.client.DatePicker;

import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.AbstractDate;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.Date;

public class InputDate extends Composite implements Editor, HasDateTimeFormat {

  public static final String DEFAULT_STYLENAME = "bee-DateBox";

  private final InputText box;
  private final Popup popup;
  private final DatePicker datePicker;

  private final ValueType dateType;
  private DateTimeFormat format;

  public InputDate(AbstractDate date) {
    this(date, null);
  }

  public InputDate(AbstractDate date, DateTimeFormat format) {
    this(date, date.getType(), format);
  }

  public InputDate(ValueType type) {
    this(type, null);
  }

  public InputDate(ValueType type, DateTimeFormat format) {
    this(null, type, format);
  }

  private InputDate(AbstractDate date, ValueType type, DateTimeFormat format) {
    Assert.notNull(type, "input date: type not specified");
    Assert.isTrue(type == ValueType.DATE || type == ValueType.DATETIME,
        "input date: invalid type " + type.getTypeCode());

    this.box = new InputText();
    this.datePicker = new DatePicker();
    this.popup = new Popup(true);

    this.format = format;
    this.dateType = type;

    popup.setWidget(datePicker);
    popup.setStyleName("dateBoxPopup");

    initWidget(box);
    setStyleName(DEFAULT_STYLENAME);

    datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
      public void onValueChange(ValueChangeEvent<Date> event) {
        AbstractDate newValue = AbstractDate.fromJava(event.getValue(), getDateType());
        if (newValue instanceof DateTime) {
          AbstractDate oldValue = getDate();
          if (oldValue != null) {
            newValue = TimeUtils.combine(newValue, oldValue.getDateTime());
          }
        }
        setValue(newValue);

        hideDatePicker();
        getBox().setFocus(true);
        fireEvent(new EditStopEvent(State.CHANGED));
      }
    });

    popup.addAutoHidePartner(getBox().getElement());
    popup.addCloseHandler(new CloseHandler<PopupPanel>() {
      public void onClose(CloseEvent<PopupPanel> event) {
        if (event.isAutoClosed()) {
          getBox().setFocus(true);
        }
      }
    });

    sinkEvents(Event.ONCLICK + Event.ONKEYPRESS);

    setValue(date);
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return addDomHandler(handler, BlurEvent.getType());
  }

  public HandlerRegistration addEditStopHandler(EditStopEvent.Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
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

  public DateTimeFormat getDateTimeFormat() {
    return this.format;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getNormalizedValue() {
    AbstractDate date = getDate();
    if (date == null) {
      return null;
    }
    return date.serialize();
  }

  public int getTabIndex() {
    return getBox().getTabIndex();
  }

  public String getValue() {
    return getBox().getValue();
  }

  public boolean isNullable() {
    return getBox().isNullable();
  }

  @Override
  public void onBrowserEvent(Event event) {
    boolean dp = getPopup().isShowing();
    String type = event.getType();

    if (dp && EventUtils.isBlur(type)) {
      return;
    }
    if (EventUtils.isClick(type)) {
      EventUtils.eatEvent(event);
      if (dp) {
        hideDatePicker();
      } else if (checkValue()) {
        showDatePicker();
      }
      return;
    }

    if (dp && EventUtils.isKeyDown(type)) {
      hideDatePicker();
    }

    if (EventUtils.isKeyPress(type) && handleChar(event.getCharCode())) {
      EventUtils.eatEvent(event);
      return;
    }

    super.onBrowserEvent(event);
  }

  public void setAccessKey(char key) {
    getBox().setAccessKey(key);
  }

  public void setDateTimeFormat(DateTimeFormat format) {
    this.format = format;
  }

  public void setFocus(boolean focused) {
    getBox().setFocus(focused);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setNullable(boolean nullable) {
    getBox().setNullable(nullable);
  }

  public void setTabIndex(int index) {
    getBox().setTabIndex(index);
  }

  public void setValue(String value) {
    setValue(value, false);
  }

  public void setValue(String value, boolean fireEvents) {
    setValue(AbstractDate.restore(value, getDateType()));
  }

  public void startEdit(String oldValue, char charCode) {
    setValue(oldValue);
    handleChar(charCode);
  }

  public String validate() {
    String v = getBox().getValue();
    if (BeeUtils.isEmpty(v)) {
      return null;
    }

    if (getDateTimeFormat() == null) {
      if (AbstractDate.parse(v, getDateType()) == null) {
        return "error parsing " + v.trim();
      } else {
        return null;
      }
    }

    String msg = null;
    try {
      Date date = getDateTimeFormat().parse(v.trim());
      if (date == null) {
        msg = "cannot parse " + v.trim();
      }
    } catch (IllegalArgumentException ex) {
      msg = "format " + getDateTimeFormat().getPattern() + " cannot parse " + v.trim();
    }
    return msg;
  }

  private boolean checkValue() {
    String msg = validate();
    if (BeeUtils.isEmpty(msg)) {
      return true;
    }

    fireEvent(new EditStopEvent(State.ERROR, msg));
    return false;
  }

  private InputText getBox() {
    return box;
  }

  private AbstractDate getDate() {
    String v = getBox().getValue();
    if (BeeUtils.isEmpty(v)) {
      return null;
    }
    if (getDateTimeFormat() == null) {
      return AbstractDate.parse(v, getDateType());
    }
    return AbstractDate.fromJava(TimeUtils.parseQuietly(getDateTimeFormat(), v), getDateType());
  }

  private DatePicker getDatePicker() {
    return datePicker;
  }

  private ValueType getDateType() {
    return dateType;
  }

  private Date getJava() {
    String v = getBox().getValue();
    if (BeeUtils.isEmpty(v)) {
      return null;
    }
    if (getDateTimeFormat() == null) {
      return AbstractDate.parse(v, getDateType()).getJava();
    }
    return TimeUtils.parseQuietly(getDateTimeFormat(), v);
  }

  private Popup getPopup() {
    return popup;
  }

  private boolean handleChar(int charCode) {
    if (!Character.isLetter(BeeUtils.toChar(charCode))
        && !BeeUtils.inList(charCode, BeeConst.CHAR_PLUS, BeeConst.CHAR_MINUS)) {
      return false;
    }

    AbstractDate oldDate = getDate();
    JustDate baseDate =
        (oldDate == null) ? new JustDate() : new JustDate(oldDate.getDate().getDay());
    AbstractDate newDate = null;

    switch (charCode) {
      case 'a':
      case 'A':
      case 'p':
      case 'P':
        newDate = TimeUtils.getDate(baseDate, 2);
        break;

      case 'b':
      case 'B':
      case 'u':
      case 'U':
        newDate = TimeUtils.getDate(baseDate, -2);
        break;

      case 'd':
        newDate = new JustDate();
        break;

      case 'D':
        newDate = TimeUtils.today(1);
        break;

      case 'e':
      case 'E':
      case 'v':
      case 'V':
        newDate = TimeUtils.today(-1);
        break;

      case 'f':
        newDate = TimeUtils.endOfPreviousMonth(baseDate);
        break;

      case 'F':
        newDate = TimeUtils.endOfMonth(baseDate);
        if (TimeUtils.sameDate(newDate, oldDate)) {
          newDate = TimeUtils.endOfMonth(TimeUtils.nextMonth(oldDate));
        }
        break;

      case 'h':
      case 'H':
        if (!ValueType.DATETIME.equals(getDateType())) {
          newDate = new JustDate(baseDate.getDay());
        } else if (oldDate == null) {
          DateTime now = new DateTime();
          newDate = new DateTime(now.getYear(), now.getMonth(), now.getDom(), now.getHour(), 0, 0);
        } else {
          int incr = (charCode == 'h') ? -1 : 1;
          newDate =
              new DateTime(oldDate.getDateTime().getTime() + incr * TimeUtils.MILLIS_PER_HOUR);
        }
        break;

      case 'i':
      case 'I':
        if (!ValueType.DATETIME.equals(getDateType())) {
          newDate = new JustDate(baseDate.getDay());
        } else if (oldDate == null) {
          DateTime now = new DateTime();
          newDate = new DateTime(now.getYear(), now.getMonth(), now.getDom(), now.getHour(),
              now.getMinute(), 0);
        } else {
          int incr = (charCode == 'i') ? -1 : 1;
          newDate =
              new DateTime(oldDate.getDateTime().getTime() + incr * TimeUtils.MILLIS_PER_MINUTE);
        }
        break;
        
      case 'm':
        newDate = TimeUtils.startOfMonth(baseDate, 0);
        if (TimeUtils.sameDate(newDate, oldDate)) {
          newDate = TimeUtils.startOfMonth(oldDate, -1);
        }
        break;

      case 'M':
        newDate = TimeUtils.nextMonth(baseDate);
        break;

      case 'n':
        int step = (baseDate.getDom() == 1) ? -2 : -1;
        newDate = TimeUtils.startOfMonth(baseDate, step);
        break;

      case 'N':
        newDate = TimeUtils.startOfMonth(baseDate, 2);
        break;

      case 'q':
      case 'k':
        newDate = TimeUtils.startOfQuarter(baseDate, 0);
        if (TimeUtils.sameDate(newDate, oldDate)) {
          newDate = TimeUtils.startOfQuarter(oldDate, -1);
        }
        break;

      case 'Q':
      case 'K':
        newDate = TimeUtils.startOfQuarter(baseDate, 1);
        break;

      case 't':
      case 'T':
      case 'l':
      case 'L':
        newDate = new DateTime();
        break;

      case 'w':
      case 's':
        newDate = TimeUtils.startOfWeek(baseDate, 0);
        if (TimeUtils.sameDate(newDate, oldDate)) {
          newDate = TimeUtils.startOfWeek(oldDate, -1);
        }
        break;

      case 'W':
      case 'S':
        newDate = TimeUtils.startOfWeek(baseDate, 1);
        break;

      case 'y':
        newDate = TimeUtils.startOfYear(baseDate, 0);
        if (TimeUtils.sameDate(newDate, oldDate)) {
          newDate = TimeUtils.startOfYear(oldDate, -1);
        }
        break;

      case 'Y':
        newDate = TimeUtils.startOfYear(baseDate, 1);
        break;

      case 'x':
      case 'X':
        if (!ValueType.DATETIME.equals(getDateType())) {
          newDate = new JustDate(baseDate.getDay());
        } else if (oldDate == null) {
          DateTime now = new DateTime();
          newDate = new DateTime(now.getYear(), now.getMonth(), now.getDom(), now.getHour(),
              now.getMinute(), now.getSecond());
        } else {
          int incr = (charCode == 'x') ? -1 : 1;
          newDate =
              new DateTime(oldDate.getDateTime().getTime() + incr * TimeUtils.MILLIS_PER_SECOND);
        }
        break;

      case '+':
      case '-':
        int incr = (charCode == '+') ? 1 : -1;
        if (oldDate == null) {
          newDate = TimeUtils.today(incr);
        } else if (oldDate instanceof JustDate) {
          newDate = new JustDate(oldDate.getDate().getDay() + incr);
        } else if (oldDate instanceof DateTime) {
          newDate = new DateTime(oldDate.getDateTime().getTime() + TimeUtils.MILLIS_PER_DAY * incr);
        }
    }

    if (newDate == null) {
      return false;
    }

    switch (getDateType()) {
      case DATE:
        if (!TimeUtils.sameDate(newDate, oldDate)) {
          setValue(newDate.getDate());
        }
        break;
      case DATETIME:
        if (!TimeUtils.sameDateTime(newDate, oldDate)) {
          setValue(newDate.getDateTime());
        }
        break;
      default:
        Assert.untouchable();
    }
    return true;
  }

  private void hideDatePicker() {
    getPopup().hide();
  }

  private void setValue(AbstractDate value) {
    String text;
    if (value == null) {
      text = BeeConst.STRING_EMPTY;
    } else if (getDateTimeFormat() == null) {
      text = value.toString();
    } else {
      text = getDateTimeFormat().format(value.getJava());
    }
    getBox().setValue(text);
  }

  private void showDatePicker() {
    Date date = getJava();
    if (date == null) {
      date = new Date();
    }
    getDatePicker().setCurrentMonth(date);
    getDatePicker().setValue(date);

    StyleUtils.setZIndex(getPopup(), StyleUtils.getZIndex(getBox()) + 1);
    getPopup().showRelativeTo(getBox());
  }
}
