package com.butent.bee.client.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Font;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.HasGridView;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.HasCharacterFilter;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasNumberBounds;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Procedure;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.ui.HasMaxLength;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;

/**
 * Contains utility user interface creation functions like setting and getting horizontal alignment.
 */

public class UiHelper {

  private static final BeeLogger logger = LogUtils.getLogger(UiHelper.class);

  private static final String ALIGN_START = "start";
  private static final String ALIGN_CENTER = "center";
  private static final String ALIGN_END = "end";

  private static final Set<HorizontalAlignmentConstant> HORIZONTAL_ALIGNMENT_CONSTANTS =
      Sets.newHashSet(HasHorizontalAlignment.ALIGN_LEFT, HasHorizontalAlignment.ALIGN_CENTER,
          HasHorizontalAlignment.ALIGN_RIGHT, HasHorizontalAlignment.ALIGN_JUSTIFY,
          HasHorizontalAlignment.ALIGN_LOCALE_START, HasHorizontalAlignment.ALIGN_LOCALE_END,
          HasHorizontalAlignment.ALIGN_DEFAULT);

  private static final Set<VerticalAlignmentConstant> VERTICAL_ALIGNMENT_CONSTANTS =
      Sets.newHashSet(HasVerticalAlignment.ALIGN_TOP, HasVerticalAlignment.ALIGN_MIDDLE,
          HasVerticalAlignment.ALIGN_BOTTOM);

  public static void add(HasWidgets container, Holder<Widget> holder,
      WidgetInitializer initializer, String name) {
    Assert.notNull(holder);
    holder.set(add(container, holder.get(), initializer, name));
  }

  public static Widget add(HasWidgets container, Widget widget, WidgetInitializer initializer,
      String name) {
    if (container == null || widget == null) {
      return null;
    }

    if (initializer == null) {
      container.add(widget);
      return widget;
    }

    Widget w = initializer.initialize(widget, name);
    if (w != null) {
      container.add(w);
    }
    return w;
  }

  public static void doEditorAction(Editor widget, String value, char charCode,
      EditorAction action) {
    Assert.notNull(widget);

    boolean acceptChar;
    if (widget instanceof HasCharacterFilter) {
      acceptChar = ((HasCharacterFilter) widget).acceptChar(charCode);
    } else {
      acceptChar = Character.isLetterOrDigit(charCode);
    }
    String charValue = acceptChar ? BeeUtils.toString(charCode) : BeeConst.STRING_EMPTY;

    String v;

    if (BeeUtils.isEmpty(value)) {
      v = charValue;
    } else if (!acceptChar) {
      v = value;
    } else if (action == null || action == EditorAction.REPLACE) {
      v = charValue;
    } else if (action == EditorAction.ADD_FIRST) {
      v = charValue + BeeUtils.trim(value);
    } else if (action == EditorAction.ADD_LAST) {
      v = BeeUtils.trimRight(value) + charValue;
    } else {
      v = value;
    }
    widget.setValue(BeeUtils.trimRight(v));

    if (widget instanceof ValueBoxBase && !BeeUtils.isEmpty(value) && action != null) {
      ValueBoxBase<?> box = (ValueBoxBase<?>) widget;
      int p = BeeConst.UNDEF;
      int len = box.getText().length();

      switch (action) {
        case ADD_FIRST:
          p = acceptChar ? 1 : 0;
          break;
        case ADD_LAST:
          p = len;
          break;
        case END:
          p = len;
          break;
        case HOME:
          p = 0;
          break;
        case REPLACE:
          p = len;
          break;
        case SELECT:
          box.selectAll();
          break;
      }
      if (p >= 0 && p <= len) {
        box.setCursorPos(p);
      }
    }
  }

  public static boolean focus(Widget target) {
    if (target instanceof HasOneWidget) {
      return focus(((HasOneWidget) target).getWidget());

    } else if (target instanceof HasWidgets) {
      for (Widget child : (HasWidgets) target) {
        if (focus(child)) {
          return true;
        }
      }
    } else if (target instanceof Focusable) {
      ((Focusable) target).setFocus(true);
      return true;
    }
    return false;
  }

  public static String getCaption(Class<? extends Enum<?>> clazz, int index) {
    Assert.notNull(clazz);
    if (!BeeUtils.isOrdinal(clazz, index)) {
      return null;
    }

    Enum<?> constant = clazz.getEnumConstants()[index];
    if (constant instanceof HasCaption) {
      return ((HasCaption) constant).getCaption();
    } else {
      return BeeUtils.proper(constant);
    }
  }

  public static List<String> getCaptions(Class<? extends Enum<?>> clazz) {
    Assert.notNull(clazz);
    List<String> result = Lists.newArrayList();

    for (Enum<?> constant : clazz.getEnumConstants()) {
      if (constant instanceof HasCaption) {
        result.add(((HasCaption) constant).getCaption());
      } else {
        result.add(BeeUtils.proper(constant));
      }
    }
    return result;
  }

  public static DataView getDataView(Widget widget) {
    if (widget == null) {
      return null;
    }

    Widget p = widget;
    for (int i = 0; i < DomUtils.MAX_GENERATIONS; i++) {
      if (p instanceof DataView) {
        return (DataView) p;
      }

      p = p.getParent();
      if (p == null) {
        break;
      }
    }
    return null;
  }

  public static HorizontalAlignmentConstant getDefaultHorizontalAlignment(ValueType type) {
    if (type == null) {
      return null;
    }

    HorizontalAlignmentConstant align;
    switch (type) {
      case BOOLEAN:
        align = HasHorizontalAlignment.ALIGN_CENTER;
        break;
      case DECIMAL:
      case INTEGER:
      case LONG:
      case NUMBER:
        align = HasHorizontalAlignment.ALIGN_LOCALE_END;
        break;
      default:
        align = null;
    }
    return align;
  }

  public static FormView getForm(Widget widget) {
    if (widget == null) {
      return null;
    }

    Widget p = widget;
    for (int i = 0; i < DomUtils.MAX_GENERATIONS; i++) {
      if (p instanceof FormView) {
        return (FormView) p;
      }

      p = p.getParent();
      if (p == null) {
        break;
      }
    }
    return null;
  }

  public static GridView getGrid(Widget widget) {
    DataView dataView = getDataView(widget);

    if (dataView == null) {
      return null;
    } else if (dataView instanceof GridView) {
      return (GridView) dataView;
    } else if (dataView.getViewPresenter() instanceof HasGridView) {
      return ((HasGridView) dataView.getViewPresenter()).getGridView();
    } else {
      return null;
    }
  }

  public static int getMaxLength(IsColumn column) {
    if (column == null) {
      return BeeConst.UNDEF;
    }

    ValueType type = column.getType();
    int precision = column.getPrecision();
    int scale = Math.max(column.getScale(), 0);

    if (precision <= 0) {
      switch (type) {
        case BOOLEAN:
          precision = 1;
          break;

        case DATE:
          precision = 10;
          break;

        case DATETIME:
          precision = 23;
          break;

        case INTEGER:
          precision = Integer.toString(Integer.MAX_VALUE).length();
          break;

        case LONG:
          precision = Long.toString(Long.MAX_VALUE).length();
          break;

        case NUMBER:
          precision = 20;
          break;

        case DECIMAL:
        case TEXT:
        case TIMEOFDAY:
      }
    }

    if (precision <= 0) {
      return BeeConst.UNDEF;
    } else if (ValueType.isNumeric(type)) {
      return precision + (precision - scale) / 3 + ((scale > 0) ? 2 : 1);
    } else {
      return precision;
    }
  }

  public static String getValue(Widget widget) {
    if (widget instanceof Editor) {
      return ((Editor) widget).getValue();
    } else if (widget instanceof TextBoxBase) {
      return ((TextBoxBase) widget).getValue();
    } else if (widget instanceof HasStringValue) {
      return ((HasStringValue) widget).getString();
    } else {
      return null;
    }
  }

  public static Widget initialize(Widget widget, WidgetInitializer initializer, String name) {
    if (widget == null) {
      return null;
    }
    if (initializer == null) {
      return widget;
    }
    return initializer.initialize(widget, name);
  }

  public static boolean isSave(NativeEvent event) {
    if (event == null) {
      return false;
    }
    return EventUtils.isKeyDown(event.getType()) && event.getKeyCode() == KeyCodes.KEY_ENTER
        && EventUtils.hasModifierKey(event);
  }

  public static Procedure<InputText> getTextBoxResizer(final int reserve) {
    return new Procedure<InputText>() {
      @Override
      public void call(InputText input) {
        String value = input.getValue();

        int oldWidth = input.getOffsetWidth();
        int newWidth = reserve;

        if (value != null && value.length() > 0) {
          if (value.contains(BeeConst.STRING_SPACE)) {
            value = value.replace(BeeConst.STRING_SPACE, BeeConst.HTML_NBSP);
          }
          Font font = Font.getComputed(input.getElement());
          newWidth += Rulers.getAreaWidth(font, value, true);
        }

        if (newWidth != oldWidth) {
          StyleUtils.setWidth(input, newWidth);
        }
      }
    };
  }

  public static boolean moveFocus(Widget parent, UIObject currentObject, boolean forward) {
    if (currentObject == null) {
      return false;
    } else {
      return moveFocus(parent, currentObject.getElement(), forward);
    }
  }

  public static boolean moveFocus(Widget parent, Element currentElement, boolean forward) {
    if (parent == null || currentElement == null) {
      return false;
    }

    List<Focusable> children = DomUtils.getFocusableChildren(parent);
    if (children == null || children.size() <= 1) {
      return false;
    }

    int index = BeeConst.UNDEF;
    for (int i = 0; i < children.size(); i++) {
      if (children.get(i) instanceof Widget
          && ((Widget) children.get(i)).getElement().isOrHasChild(currentElement)) {
        index = i;
        break;
      }
    }
    if (BeeConst.isUndef(index)) {
      return false;
    }

    if (forward) {
      index++;
    } else {
      index--;
    }

    if (index >= 0 && index < children.size()) {
      children.get(index).setFocus(true);
      return true;
    } else {
      return false;
    }
  }

  public static HorizontalAlignmentConstant parseHorizontalAlignment(String text) {
    if (BeeUtils.isEmpty(text)) {
      return null;
    }
    HorizontalAlignmentConstant align = null;

    if (BeeUtils.same(text, ALIGN_START)) {
      align = HasHorizontalAlignment.ALIGN_LOCALE_START;
    } else if (BeeUtils.same(text, ALIGN_CENTER)) {
      align = HasHorizontalAlignment.ALIGN_CENTER;
    } else if (BeeUtils.same(text, ALIGN_END)) {
      align = HasHorizontalAlignment.ALIGN_LOCALE_END;
    } else {
      for (HorizontalAlignmentConstant hac : HORIZONTAL_ALIGNMENT_CONSTANTS) {
        if (BeeUtils.same(text, hac.getTextAlignString())) {
          align = hac;
          break;
        }
      }
    }
    return align;
  }

  public static VerticalAlignmentConstant parseVerticalAlignment(String text) {
    if (BeeUtils.isEmpty(text)) {
      return null;
    }

    VerticalAlignmentConstant align = null;

    if (BeeUtils.same(text, ALIGN_START)) {
      align = HasVerticalAlignment.ALIGN_TOP;
    } else if (BeeUtils.same(text, ALIGN_CENTER)) {
      align = HasVerticalAlignment.ALIGN_MIDDLE;
    } else if (BeeUtils.same(text, ALIGN_END)) {
      align = HasVerticalAlignment.ALIGN_BOTTOM;
    } else {
      for (VerticalAlignmentConstant vac : VERTICAL_ALIGNMENT_CONSTANTS) {
        if (BeeUtils.same(text, vac.getVerticalAlignString())) {
          align = vac;
          break;
        }
      }
    }
    return align;
  }

  public static void pressKey(ValueBoxBase<?> widget, char key) {
    Assert.notNull(widget);

    String oldText = BeeUtils.nvl(widget.getText(), BeeConst.STRING_EMPTY);

    int pos = widget.getCursorPos();
    int len = widget.getSelectionLength();

    if (len <= 0 && widget instanceof HasMaxLength) {
      int maxLength = ((HasMaxLength) widget).getMaxLength();
      if (maxLength > 0 && BeeUtils.hasLength(oldText, maxLength)) {
        return;
      }
    }

    String newText;
    if (len > 0) {
      newText = BeeUtils.replace(oldText, pos, pos + len, key);
    } else {
      newText = BeeUtils.insert(oldText, pos, key);
    }

    widget.setText(newText);
    widget.setCursorPos(pos + 1);
  }

  public static void registerSave(UIObject obj) {
    Assert.notNull(obj);
    obj.sinkEvents(Event.ONKEYDOWN);
  }

  public static void selectDeferred(final ValueBoxBase<?> widget) {
    Assert.notNull(widget);
    final String text = widget.getText();
    if (BeeUtils.isEmpty(text)) {
      return;
    }

    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        if (text.equals(widget.getText())) {
          widget.selectAll();
        }
      }
    });
  }

  public static void setDefaultHorizontalAlignment(HasHorizontalAlignment obj, ValueType type) {
    Assert.notNull(obj);
    HorizontalAlignmentConstant align = getDefaultHorizontalAlignment(type);
    if (align != null) {
      obj.setHorizontalAlignment(align);
    }
  }

  public static void setHorizontalAlignment(Element elem, String text) {
    Assert.notNull(elem);

    HorizontalAlignmentConstant align = parseHorizontalAlignment(text);
    if (align != null) {
      StyleUtils.setTextAlign(elem, align);
    }
  }

  public static void setHorizontalAlignment(HasHorizontalAlignment obj, String text) {
    Assert.notNull(obj);

    HorizontalAlignmentConstant align = parseHorizontalAlignment(text);
    if (align != null) {
      obj.setHorizontalAlignment(align);
    }
  }

  public static void setNumberBounds(HasNumberBounds obj, String min, String max) {
    Assert.notNull(obj);
    if (BeeUtils.isDouble(min)) {
      obj.setMinValue(BeeUtils.toDoubleOrNull(min));
    }
    if (BeeUtils.isDouble(max)) {
      obj.setMaxValue(BeeUtils.toDoubleOrNull(max));
    }
  }

  public static void setVerticalAlignment(Element elem, String text) {
    Assert.notNull(elem);
    if (BeeUtils.isEmpty(text)) {
      return;
    }

    VerticalAlign align = StyleUtils.parseVerticalAlign(text);
    if (align != null) {
      elem.getStyle().setVerticalAlign(align);
    }
  }

  public static void setVerticalAlignment(HasVerticalAlignment obj, String text) {
    Assert.notNull(obj);

    VerticalAlignmentConstant align = parseVerticalAlignment(text);
    if (align != null) {
      obj.setVerticalAlignment(align);
    }
  }

  public static void setWidget(HasOneWidget container, Holder<Widget> holder,
      WidgetInitializer initializer, String name) {
    Assert.notNull(holder);
    holder.set(setWidget(container, holder.get(), initializer, name));
  }

  public static Widget setWidget(HasOneWidget container, Widget widget,
      WidgetInitializer initializer, String name) {
    if (container == null || widget == null) {
      return null;
    }

    if (initializer == null) {
      container.setWidget(widget);
      return widget;
    }

    Widget w = initializer.initialize(widget, name);
    if (w != null) {
      container.setWidget(w);
    }
    return w;
  }

  public static void updateForm(String widgetId, String columnId, String value) {
    Assert.notEmpty(widgetId);
    Assert.notEmpty(columnId);

    Widget widget = DomUtils.getWidget(widgetId);
    if (widget == null) {
      logger.severe("update form:", widgetId, "widget not found");
      return;
    }

    FormView form = getForm(widget);
    if (form == null) {
      logger.severe("update form:", widgetId, columnId, value, "form not found");
      return;
    }

    form.updateCell(columnId, value);
  }

  private UiHelper() {
  }
}
