package com.butent.bee.client.composite;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
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
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.EditStopEvent.Handler;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.State;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Manages a user interface component for selecting text values from a cell list.
 */

public class StringPicker extends CellList<String> implements Editor, HasItems, BlurHandler {

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

    @Override
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
  
  private final List<String> data = Lists.newArrayList();
  private String value = null;

  private boolean nullable = true;
  private boolean editing = false;
  private boolean enabled = true;

  private final Map<HandlerRegistration, BlurHandler> blurHandlers = Maps.newHashMap();
  private HandlerRegistration blurRegistration = null;

  private boolean selectionPending = false;

  private String options = null;

  private boolean handlesTabulation = false;
  
  public StringPicker() {
    super(new DefaultCell());

    DomUtils.createId(this, getIdPrefix());
    setStyleName(STYLE_CONTAINER);
    sinkEvents(Event.ONKEYDOWN | Event.ONKEYPRESS | Event.ONMOUSEDOWN | Event.ONBLUR);
  }

  @Override
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
  
  @Override
  public HandlerRegistration addEditStopHandler(Handler handler) {
    return addHandler(handler, EditStopEvent.getType());
  }

  @Override
  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return addDomHandler(handler, FocusEvent.getType());
  }

  @Override
  public void addItem(String item) {
    Assert.notEmpty(item);
    data.add(item);
    refresh();
  }

  @Override
  public void addItems(Collection<String> items) {
    Assert.notNull(items);
    if (!items.isEmpty()) {
      data.addAll(items);
      refresh();
    }
  }

  @Override
  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return addDomHandler(handler, KeyDownEvent.getType());
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }
  
  @Override
  public StringPicker asWidget() {
    return this;
  }

  @Override
  public void clearValue() {
    if (isNullable()) {
      setValue(null);
    }
  }

  @Override
  public EditorAction getDefaultFocusAction() {
    return null;
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "string-picker";
  }

  @Override
  public int getItemCount() {
    return data.size();
  }
  
  @Override
  public List<String> getItems() {
    return data;
  }
  
  @Override
  public String getNormalizedValue() {
    String v = getValue();
    if (BeeUtils.isEmpty(v) && isNullable()) {
      return null;
    } else {
      return BeeUtils.trimRight(v);
    }
  }

  @Override
  public String getOptions() {
    return options;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.STRING_PICKER;
  }

  @Override
  public boolean handlesKey(int keyCode) {
    return BeeUtils.inList(keyCode, KeyCodes.KEY_UP, KeyCodes.KEY_DOWN, KeyCodes.KEY_ENTER);
  }

  @Override
  public boolean handlesTabulation() {
    return handlesTabulation;
  }

  @Override
  public boolean isEditing() {
    return editing;
  }
  
  @Override
  public boolean isEmpty() {
    return getItemCount() <= 0;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean isIndex(int index) {
    return index >= 0 && index < getItemCount();
  }
  
  @Override
  public boolean isNullable() {
    return nullable;
  }

  @Override
  public boolean isOrHasPartner(Node node) {
    return node != null && getElement().isOrHasChild(node);
  }
  
  @Override
  public void normalizeDisplay(String normalizedValue) {
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
    if (!isEnabled()) {
      return;
    }

    String type = event.getType();
    if (EventUtils.isKeyDown(type)) {
      int keyCode = event.getKeyCode();
      if (navigate(keyCode)) {
        event.preventDefault();
        return;
      }

      if (keyCode == KeyCodes.KEY_ENTER) {
        event.preventDefault();
        fireEvent(new EditStopEvent(State.CHANGED));
      }

    } else if (EventUtils.isKeyPress(type)) {
      char charCode = (char) event.getCharCode();
      if (selectByChar(charCode, getSelectedIndex())) {
        event.preventDefault();
      }

    } else if (EventUtils.isMouseDown(type)) {
      EventTarget target = event.getEventTarget();
      for (int i = 0; i < getChildContainer().getChildCount(); i++) {
        if (EventUtils.equalsOrIsChild(getChildElement(i), target)) {
          event.preventDefault();
          setValue(getItemValue(i));
          fireEvent(new EditStopEvent(State.CHANGED));
          break;
        }
      }
    }
  }
  
  @Override
  public void setEditing(boolean editing) {
    this.editing = editing;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void setHandlesTabulation(boolean handlesTabulation) {
    this.handlesTabulation = handlesTabulation;
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  @Override
  public void setItems(Collection<String> items) {
    Assert.notNull(items);
    data.clear();
    data.addAll(items);
    refresh();
  }

  @Override
  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  @Override
  public void setValue(String value) {
    if (BeeUtils.equalsTrimRight(value, getValue())) {
      return;
    }

    setSelected(false);
    this.value = value;
    setSelected(true);
  }

  @Override
  public void startEdit(String oldValue, char charCode, EditorAction onEntry,
      Element sourceElement) {
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

  @Override
  public List<String> validate(boolean checkForNull) {
    return Collections.emptyList();
  }

  @Override
  public List<String> validate(String normalizedValue, boolean checkForNull) {
    return Collections.emptyList();
  }
  
  @Override
  protected boolean isKeyboardNavigationSuppressed() {
    return true;
  }

  @Override
  protected void onUnload() {
    getBlurHandlers().clear();
    super.onUnload();
  }

  @Override
  protected void renderRowValues(SafeHtmlBuilder sb, List<String> values, int start,
      SelectionModel<? super String> selectionModel) {

    int length = values.size();
    int end = start + length;

    int tabIdx = getTabIndex();

    for (int i = start; i < end; i++) {
      String item = values.get(i - start);
      String displayValue = BeeUtils.notEmpty(BeeUtils.getSuffix(item, displaySeparator), item);

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

  private String getItemData(String item) {
    return BeeUtils.notEmpty(BeeUtils.getPrefix(item, displaySeparator), item);
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

    newIndex = BeeUtils.clamp(newIndex, 0, itemCount - 1);
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

  private void refresh() {
    setRowData(data);
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
