package com.butent.bee.client.ui;

import com.google.common.collect.Sets;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.ValueBoxBase;

import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.HasCharacterFilter;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Set;

/**
 * Contains utility user interface creation functions like setting and getting horizontal alignment.
 */

public class UiHelper {

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

  public static boolean isSave(NativeEvent event) {
    if (event == null) {
      return false;
    }
    return EventUtils.isKeyDown(event.getType()) && event.getKeyCode() == KeyCodes.KEY_ENTER
        && EventUtils.hasModifierKey(event);
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
  
  public static void registerSave(UIObject obj) {
    Assert.notNull(obj);
    obj.sinkEvents(Event.ONKEYDOWN);
  }

  public static void setDefaultHorizontalAlignment(HasHorizontalAlignment obj, ValueType type) {
    Assert.notNull(obj);
    HorizontalAlignmentConstant align = getDefaultHorizontalAlignment(type);
    if (align != null) {
      obj.setHorizontalAlignment(align);
    }
  }

  public static void setHorizontalAlignment(HasHorizontalAlignment obj, String text) {
    Assert.notNull(obj);
    Assert.notEmpty(text);

    HorizontalAlignmentConstant align = parseHorizontalAlignment(text);
    if (align != null) {
      obj.setHorizontalAlignment(align);
    }
  }

  public static void setVerticalAlignment(HasVerticalAlignment obj, String text) {
    Assert.notNull(obj);
    Assert.notEmpty(text);

    VerticalAlignmentConstant align = parseVerticalAlignment(text);
    if (align != null) {
      obj.setVerticalAlignment(align);
    }
  }
  
  private UiHelper() {
  }
}
