package com.butent.bee.client.modules.tasks;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.edit.SimpleEditorHandler;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Verslo Aljansas TID 25505.
 */
abstract class CustomTaskDialog extends DialogBox {

  private static final String STYLE_DIALOG = CRM_STYLE_PREFIX + "taskDialog";
  private static final String STYLE_CELL = "Cell";

  // Verslo Aljansas TID 25515.
  private static final String DATA_LABEL_ID = "-label-id";

  /**
   * Verslo Aljansas TID 25505.
   */
  String addMileage() {
    HtmlTable table = getContainer();
    int row =  table.getRowCount(); //BeeUtils.max(table.getRowCount() - 1, 0);
    int col =  0; //2;

    String styleName = STYLE_DIALOG + "-va-mileageLabel";
    Label label = new Label(Localized.dictionary().mileage());
    label.addStyleName(styleName);

    table.setWidget(row, col, label);
    table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);
    col++;

    styleName = STYLE_DIALOG + "-va-mileageInput";
    InputNumber input = new InputNumber();
    String color = input.getElement().getStyle().getBorderColor();
    input.addStyleName(styleName);
    input.setNumberFormat(NumberFormat.getDecimalFormat());
    input.setMinValue(BeeConst.STRING_ZERO);

    input.addValueChangeHandler(event -> {
      input.normalizeDisplay(input.getNormalizedValue());

      if (BeeUtils.isNegative(input.getNumber())) {
        StyleUtils.setBorderColor(input, "red");
      } else {
        StyleUtils.setBorderColor(input, color);
      }

      input.validate(false);
    });

    SimpleEditorHandler.observe(Localized.dictionary().mileage(), input);

    table.setWidget(row, col, input);
    table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);

    // Verslo Aljansas TID 25515.
    DomUtils.setDataProperty(input.getElement(), DATA_LABEL_ID, label.getId());

    return input.getId();
  }

  /**
   * Verslo Aljansas TID 25505.
   */
  String getMileage(String id) {
    Widget child = DomUtils.getChildQuietly(getContent(), id);
    if (child instanceof InputNumber) {
      return ((InputNumber) child).getNormalizedValue();
    } else {
      return null;
    }
  }

  /**
   * Verslo Aljansas TID 25515.
   */
  void setRequiredMileage(String id, boolean required) {
    Widget child = DomUtils.getChildQuietly(getContent(), id);
    if (child instanceof InputNumber) {
      Widget label = DomUtils.getChildQuietly(getContent(),
          DomUtils.getDataProperty(child.getElement(), DATA_LABEL_ID));

      if (label instanceof Label) {
        label.setStyleName(StyleUtils.NAME_REQUIRED, required);
      }
    }
  }

  /**
   * Verslo Aljansas TID 25505.
   */
  protected CustomTaskDialog(String caption, String style) {
    super(caption, style);
  }

  protected abstract HtmlTable getContainer();
}
