package com.butent.bee.client.composite;

import com.google.common.collect.Maps;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Event;
import com.google.gwt.view.client.SelectionModel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.EditStopEvent.Handler;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.State;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Manages a user interface component for selecting text values from a cell list.
 */

public class StringPicker extends CellList<String> implements Editor, HasItems,
    BlurHandler {

  /**
   * Contains {@code SafeHtml} templates for string picker screen presentations.
   */

  public interface RenderTemplate extends SafeHtmlTemplates {
    @Template("<div id=\"{0}\" class=\"{1}\" tabindex=\"{2}\">{3}</div>")
    SafeHtml divItem(String id, String classes, int tabIndex, SafeHtml cellContent);

    @Template("<div id=\"{0}\" class=\"{1}\" tabindex=\"{2}\">{3}</div>")
    SafeHtml divSelected(String id, String classes, int tabIndex, SafeHtml cellContent);
  }

  private class BlurHandlerRegistration implements HandlerRegistration {
    private BlurHandlerRegistration() {
    }

    public void removeHandler() {
      getBlurHandlers().remove(this);
    }
  }

  /**
   * Manages a single cell in a string picker component.
   */
  private static class DefaultCell extends AbstractCell<String> {
    private DefaultCell() {
    }

    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
      if (!BeeUtils.isEmpty(value)) {
        sb.append(SafeHtmlUtils.fromTrustedString(value));
      }
    }
  }

  public static String displaySeparator = ";";

  private static final RenderTemplate RENDER_TEMPLATE = GWT.create(RenderTemplate.class);

  private static final String STYLE_CONTAINER = "bee-StringPicker";
  private static final String STYLE_ITEM = "bee-StringPicker-item";
  private static final String STYLE_SELECTED = "bee-StringPicker-selected";

  private String value = null;

  private boolean nullable = true;

  private boolean editing = false;

  private final Map<HandlerRegistration, BlurHandler> blurHandlers = Maps.newHashMap();
  private HandlerRegistration blurRegistration = null;

  private boolean selectionPending = false;

  public StringPicker() {
    super(new DefaultCell());

    createId();
    setStyleName(STYLE_CONTAINER);
    sinkEvents(Event.ONKEYDOWN + Event.ONKEYPRESS + Event.ONMOUSEDOWN + Event.ONBLUR);
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    Assert.notNull(handler);
    if (getBlurHandlers().values().contains(handler)) {
      return null;
    }
    if (getBlurRegistration() == null) {
      setBlurRegistration(addDomHandler(this, BlurEvent.getType()));
    }

    BlurHandlerRegistration reg = new BlurHandlerRegistration();
    getBlurHandlers().put(reg, handler);
    return reg;
  }

  public HandlerRegistration addEditStopHandler(Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  @Override
  public StringPicker asWidget() {
    return this;
  }

  public void createId() {
    DomUtils.createId(this, "string-picker");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getNormalizedValue() {
    String v = getValue();
    if (BeeUtils.isEmpty(v) && isNullable()) {
      return null;
    } else {
      return BeeUtils.trimRight(v);
    }
  }

  public String getValue() {
    return value;
  }

  public boolean handlesKey(int keyCode) {
    return BeeUtils.inList(keyCode, KeyCodes.KEY_UP, KeyCodes.KEY_DOWN, KeyCodes.KEY_ENTER);
  }

  public boolean isEditing() {
    return editing;
  }

  public boolean isNullable() {
    return nullable;
  }

  @Override
  public void onBlur(BlurEvent event) {
    if (isSelectionPending()) {
      setSelectionPending(false);
    } else {
      for (BlurHandler handler : getBlurHandlers().values()) {
        handler.onBlur(event);
      }
    }
  }

  @Override
  public void onBrowserEvent2(Event event) {
    String type = event.getType();
    if (EventUtils.isKeyDown(type)) {
      int keyCode = event.getKeyCode();
      if (navigate(keyCode)) {
        EventUtils.eatEvent(event);
        return;
      }

      if (keyCode == KeyCodes.KEY_ENTER) {
        EventUtils.eatEvent(event);
        fireEvent(new EditStopEvent(State.CHANGED));
      }

    } else if (EventUtils.isKeyPress(type)) {
      char charCode = (char) event.getCharCode();
      if (selectByChar(charCode, getSelectedIndex())) {
        EventUtils.eatEvent(event);
      }

    } else if (EventUtils.isMouseDown(type)) {
      EventTarget target = event.getEventTarget();
      for (int i = 0; i < getChildContainer().getChildCount(); i++) {
        if (EventUtils.equalsOrIsChild(getChildElement(i), target)) {
          EventUtils.eatEvent(event);
          setValue(getItemValue(i));
          fireEvent(new EditStopEvent(State.CHANGED));
          break;
        }
      }
    }
  }

  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setItems(Collection<String> items) {
    setRowData(0, new ArrayList<String>(items));
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public void setValue(String value) {
    setValue(value, false);
  }

  public void setValue(String value, boolean fireEvents) {
    if (BeeUtils.equalsTrimRight(value, getValue())) {
      return;
    }

    setSelected(false);
    this.value = value;
    setSelected(true);

    if (fireEvents) {
      ValueChangeEvent.fire(this, value);
    }
  }

  public void startEdit(String oldValue, char charCode, EditorAction onEntry) {
    String v;

    if (selectByChar(charCode, BeeConst.UNDEF)) {
      v = null;
    } else if (contains(oldValue)) {
      v = BeeUtils.trimRight(oldValue);
    } else {
      v = getItemValue(0);
    }

    if (!BeeUtils.isEmpty(v)) {
      setValue(v);
    }

    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public void execute() {
        refocus();
        setSelectionPending(false);
      }
    });
  }

  public String validate() {
    return null;
  }

  @Override
  protected boolean isKeyboardNavigationSuppressed() {
    return true;
  }

  @Override
  protected void onUnload() {
    super.onUnload();
    getBlurHandlers().clear();
  }

  @Override
  protected void renderRowValues(SafeHtmlBuilder sb, List<String> values, int start,
      SelectionModel<? super String> selectionModel) {

    int length = values.size();
    int end = start + length;

    int tabIdx = getTabIndex();

    for (int i = start; i < end; i++) {
      String item = values.get(i - start);
      String displayValue = BeeUtils.ifString(BeeUtils.getSuffix(item, displaySeparator), item);

      SafeHtmlBuilder cellBuilder = new SafeHtmlBuilder();
      Context context = new Context(i, 0, getValueKey(item));
      getCell().render(context, displayValue, cellBuilder);
      SafeHtml cellContent = cellBuilder.toSafeHtml();

      String id = DomUtils.createUniqueId("picker-cell");

      if (BeeUtils.same(getItemData(item), getValue())) {
        sb.append(RENDER_TEMPLATE.divSelected(id, STYLE_SELECTED, tabIdx, cellContent));
      } else {
        sb.append(RENDER_TEMPLATE.divItem(id, STYLE_ITEM, tabIdx, cellContent));
      }
    }
  }

  private boolean contains(String item) {
    return getItemIndex(item) >= 0;
  }

  private String findByChar(int startIndex, int endIndex, char charCode) {
    for (int i = startIndex; i < endIndex; i++) {
      String item = getItemValue(i);
      if (BeeUtils.startsWith(item, charCode)) {
        return item;
      }
    }
    return null;
  }

  private Map<HandlerRegistration, BlurHandler> getBlurHandlers() {
    return blurHandlers;
  }

  private HandlerRegistration getBlurRegistration() {
    return blurRegistration;
  }

  private Element getChildElement(int index) {
    if (index >= 0 && index < getChildContainer().getChildCount()) {
      return Element.as(getChildContainer().getChild(index));
    } else {
      return null;
    }
  }

  private String getItemData(String item) {
    return BeeUtils.ifString(BeeUtils.getPrefix(item, displaySeparator), item);
  }

  private int getItemIndex(String item) {
    int index = BeeConst.UNDEF;
    if (BeeUtils.isEmpty(item)) {
      return index;
    }

    for (int i = 0; i < getVisibleItemCount(); i++) {
      if (BeeUtils.same(getItemValue(i), item)) {
        index = i;
        break;
      }
    }
    return index;
  }

  private String getItemValue(int index) {
    return getItemData(getVisibleItem(index));
  }

  private int getSelectedIndex() {
    return getItemIndex(getValue());
  }

  private boolean isSelectionPending() {
    return selectionPending;
  }

  private boolean navigate(int keyCode) {
    boolean ok = false;
    int itemCount = getVisibleItemCount();
    if (itemCount <= 1) {
      return ok;
    }
    int oldIndex = getSelectedIndex();
    int newIndex = oldIndex;

    switch (keyCode) {
      case KeyCodes.KEY_HOME:
      case KeyCodes.KEY_PAGEUP:
        newIndex = 0;
        break;
      case KeyCodes.KEY_END:
      case KeyCodes.KEY_PAGEDOWN:
        newIndex = itemCount - 1;
        break;
      case KeyCodes.KEY_DOWN:
      case KeyCodes.KEY_RIGHT:
        if (newIndex < itemCount - 1) {
          newIndex++;
        } else {
          newIndex = 0;
        }
        break;
      case KeyCodes.KEY_LEFT:
      case KeyCodes.KEY_UP:
        if (newIndex > 0) {
          newIndex--;
        } else {
          newIndex = itemCount - 1;
        }
        break;
    }

    newIndex = BeeUtils.limit(newIndex, 0, itemCount - 1);
    if (newIndex != oldIndex) {
      setValue(getItemValue(newIndex));
      ok = true;
    }
    return ok;
  }

  private void refocus() {
    Element childElement = getChildElement(getSelectedIndex());
    if (childElement != null) {
      setSelectionPending(true);
      childElement.focus();
    }
  }

  private boolean selectByChar(char charCode, int currentIndex) {
    boolean ok = false;
    if (charCode <= BeeConst.CHAR_SPACE) {
      return ok;
    }
    int itemCount = getVisibleItemCount();
    if (itemCount <= 0) {
      return ok;
    }

    String item = null;
    if (currentIndex >= 0 && currentIndex < itemCount - 1) {
      item = findByChar(currentIndex + 1, itemCount, charCode);
      if (BeeUtils.isEmpty(item) && currentIndex > 0) {
        item = findByChar(0, currentIndex, charCode);
      }
    } else {
      item = findByChar(0, itemCount, charCode);
    }

    if (!BeeUtils.isEmpty(item)) {
      setValue(item);
      ok = true;
    }
    return ok;
  }

  private void setBlurRegistration(HandlerRegistration blurRegistration) {
    this.blurRegistration = blurRegistration;
  }

  private void setSelected(boolean selected) {
    Element childElement = getChildElement(getSelectedIndex());
    if (childElement == null) {
      return;
    }
    if (selected) {
      childElement.setClassName(STYLE_SELECTED);
      setSelectionPending(true);
      childElement.focus();
    } else {
      childElement.setClassName(STYLE_ITEM);
    }
  }

  private void setSelectionPending(boolean selectionPending) {
    this.selectionPending = selectionPending;
  }
}
