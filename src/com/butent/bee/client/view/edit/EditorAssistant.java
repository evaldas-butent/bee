package com.butent.bee.client.view.edit;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.images.star.Stars;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.Procedure;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

public class EditorAssistant {

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

  public static void editStarCell(EditStartEvent event, final CellSource source,
      final Procedure<Integer> updater) {

    Assert.notNull(event);
    Assert.notNull(source);

    final IsRow row = event.getRowValue();
    if (row == null) {
      return;
    }

    final Integer oldValue = source.getInteger(row);

    final Element element = event.getSourceElement();

    if (event.isDelete()) {
      if (oldValue != null) {
        source.clear(row);
        if (element != null) {
          element.setInnerHTML(BeeConst.STRING_EMPTY);
        }
        
        if (updater != null) {
          updater.call(null);
        }
      }

    } else if (event.getCharCode() == BeeConst.CHAR_PLUS
        || event.getCharCode() == BeeConst.CHAR_MINUS) {

      boolean forward = (event.getCharCode() == BeeConst.CHAR_PLUS);
      int count = Stars.count();

      int newValue;
      if (oldValue == null) {
        newValue = forward ? 0 : count - 1;
      } else if (forward) {
        newValue = BeeUtils.rotateForwardExclusive(oldValue, 0, count);
      } else {
        newValue = BeeUtils.rotateBackwardExclusive(oldValue, 0, count);
      }

      source.set(row, newValue);
      if (element != null) {
        element.setInnerHTML(Stars.getHtml(newValue));
      }

      if (updater != null) {
        updater.call(newValue);
      }

    } else {
      Global.getMsgBoxen().pickStar(oldValue, element, new ChoiceCallback() {
        @Override
        public void onCancel() {
          refocus();
        }

        @Override
        public void onSuccess(int value) {
          if (oldValue == null || value != oldValue) {
            source.set(row, value);
            if (element != null) {
              element.setInnerHTML(Stars.getHtml(value));
            }

            if (updater != null) {
              updater.call(value);
            }
          }

          refocus();
        }

        private void refocus() {
          if (element != null) {
            element.focus();
          }
        }
      });
    }

    event.consume();
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
  
  private EditorAssistant() {
  }
}
