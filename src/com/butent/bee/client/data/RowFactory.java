package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputWidgetCallback;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class RowFactory {
  
  public abstract static class NewRowCallback extends Queries.RowCallback {
    public void onCancel() {
    }

    public void onTimeout() {
    }
  }

  public static final String OK = "Išsaugoti";
  public static final String CANCEL = "Atšaukti";

  public static final String DIALOG_STYLE = "bee-NewRow";

  private static final String DEFAULT_CAPTION = "Naujas";

  private static final String STYLE_NEW_ROW_TABLE = "bee-NewRow-table";
  private static final String STYLE_NEW_ROW_LABEL_CELL = "bee-NewRow-labelCell";
  private static final String STYLE_NEW_ROW_LABEL = "bee-NewRow-label";
  private static final String STYLE_NEW_ROW_INPUT_CELL = "bee-NewRow-inputCell";
  private static final String STYLE_NEW_ROW_INPUT = "bee-NewRow-input";

  public static BeeRow createEmptyRow(DataInfo dataInfo, boolean defaults) {
    BeeRow row = DataUtils.createEmptyRow(dataInfo.getColumnCount());
    if (defaults) {
      setDefaults(row, dataInfo);
    }
    return row;
  }

  public static void createRelatedRow(final DataSelector selector) {
    Assert.notNull(selector);

    DataInfo dataInfo = selector.getOracle().getDataInfo();

    String formName = selector.getNewRowForm();
    String caption = selector.getNewRowCaption();

    BeeRow row = createEmptyRow(dataInfo, true);

    String value = selector.getDisplayValue();
    if (!BeeUtils.isEmpty(value) || !selector.getChoiceColumns().isEmpty()) {
      BeeColumn column = dataInfo.getColumn(selector.getChoiceColumns().get(0));
      if (column != null && column.isWritable() && ValueType.isString(column.getType())) {
        Data.setValue(dataInfo.getViewName(), row, column.getId(), value.trim());
      }
    }

    if (BeeUtils.isEmpty(formName)) {
      List<BeeColumn> columns = getColumns(dataInfo, selector.getNewRowColumns(),
          selector.getChoiceColumns());
      if (columns.isEmpty()) {
        return;
      }

      formName = generateFormName(dataInfo, columns);

      FormDescription formDescription = FormFactory.getFormDescription(formName);
      if (formDescription == null) {
        formDescription = createFormDescription(formName, dataInfo, columns);
        FormFactory.putFormDescription(formName, formDescription);
      }
    }
    
    selector.setAdding(true);

    createRow(formName, caption, dataInfo, row, selector, new NewRowCallback() {
      @Override
      public void onCancel() {
        selector.setAdding(false);
        selector.setFocus(true);
      }

      @Override
      public void onSuccess(BeeRow result) {
        selector.setAdding(false);
        selector.setSelection(result);
      }

      @Override
      public void onTimeout() {
        onCancel();
      }
    });
  }

  public static void createRow(String formName, String caption, DataInfo dataInfo, BeeRow row,
      UIObject target, FormCallback formCallback, NewRowCallback newRowCallback) {
    Assert.notEmpty(formName);

    Assert.notNull(dataInfo);
    Assert.notNull(row);

    getForm(formName, caption, formCallback, dataInfo, row, target, newRowCallback);
  }

  public static void createRow(String formName, String caption, DataInfo dataInfo, BeeRow row,
      NewRowCallback newRowCallback) {
    createRow(formName, caption, dataInfo, row, null, null, newRowCallback);
  }

  public static void createRow(String formName, String caption, DataInfo dataInfo, BeeRow row,
      UIObject target, NewRowCallback newRowCallback) {
    createRow(formName, caption, dataInfo, row, target, null, newRowCallback);
  }

  public static int setDefaults(BeeRow row, DataInfo dataInfo) {
    if (row == null || dataInfo == null) {
      return BeeConst.UNDEF;
    }

    List<String> colNames = Lists.newArrayList();
    for (BeeColumn column : dataInfo.getColumns()) {
      if (column.hasDefaults()) {
        colNames.add(column.getId());
      }
    }
    if (colNames.isEmpty()) {
      return 0;
    }

    return DataUtils.setDefaults(row, colNames, dataInfo.getColumns(), Global.getDefaults())
        + RelationUtils.setDefaults(dataInfo, row, colNames, dataInfo.getColumns());
  }

  private static int countValues(IsRow row, List<BeeColumn> columns) {
    int cnt = 0;
    for (int i = 0; i < columns.size(); i++) {
      if (!BeeUtils.isEmpty(row.getString(i)) && columns.get(i).isWritable()) {
        cnt++;
      }
    }
    return cnt;
  }

  private static FormDescription createFormDescription(String formName, DataInfo dataInfo,
      List<BeeColumn> columns) {
    Document doc = XMLParser.createDocument();
    Element form = doc.createElement(FormFactory.TAG_FORM);

    form.setAttribute(UiConstants.ATTR_NAME, formName);
    form.setAttribute(UiConstants.ATTR_VIEW_NAME, dataInfo.getViewName());

    Element table = doc.createElement(FormWidget.FLEX_TABLE.getTagName());
    table.setAttribute(UiConstants.ATTR_CLASS, STYLE_NEW_ROW_TABLE);

    for (BeeColumn column : columns) {
      Element row = doc.createElement(UiConstants.TAG_ROW);

      Element labelCell = doc.createElement(UiConstants.TAG_CELL);
      labelCell.setAttribute(UiConstants.ATTR_CLASS, STYLE_NEW_ROW_LABEL_CELL);

      Element label = doc.createElement(FormWidget.LABEL.getTagName());
      label.setAttribute(UiConstants.ATTR_HTML,
          BeeUtils.ifString(column.getLabel(), column.getId()));

      String labelClass;
      if (column.hasDefaults()) {
        labelClass = StyleUtils.NAME_HAS_DEFAULTS;
      } else if (!column.isNullable()) {
        labelClass = StyleUtils.NAME_REQUIRED;
      } else {
        labelClass = null;
      }
      label.setAttribute(UiConstants.ATTR_CLASS,
          StyleUtils.buildClasses(STYLE_NEW_ROW_LABEL, labelClass));

      labelCell.appendChild(label);
      row.appendChild(labelCell);

      Element inputCell = doc.createElement(UiConstants.TAG_CELL);
      inputCell.setAttribute(UiConstants.ATTR_CLASS, STYLE_NEW_ROW_INPUT_CELL);

      FormWidget widgetType = dataInfo.hasRelation(column.getId()) 
          ? FormWidget.DATA_SELECTOR : FormFactory.getWidgetType(column);
      Element input = doc.createElement(widgetType.getTagName());
      input.setAttribute(UiConstants.ATTR_SOURCE, column.getId());
      input.setAttribute(UiConstants.ATTR_CLASS, STYLE_NEW_ROW_INPUT);

      inputCell.appendChild(input);
      row.appendChild(inputCell);

      table.appendChild(row);
    }

    form.appendChild(table);
    return new FormDescription(form);
  }

  private static String generateFormName(DataInfo dataInfo, List<BeeColumn> columns) {
    int hash = 0;
    for (int i = 0; i < columns.size(); i++) {
      hash += columns.get(i).getId().hashCode() * (i + 1);
    }
    return dataInfo.getViewName().toLowerCase() + "-new-row-" + BeeUtils.toString(Math.abs(hash));
  }

  private static List<BeeColumn> getColumns(DataInfo dataInfo, String specified,
      List<String> preferred) {
    List<BeeColumn> result = Lists.newArrayList();

    List<String> colNames = Lists.newArrayList();
    if (!BeeUtils.isEmpty(specified)) {
      List<String> list = DataUtils.parseColumns(specified, dataInfo.getColumns(), null, null);
      if (list != null) {
        colNames.addAll(list);
      }
    }

    if (colNames.isEmpty() && !BeeUtils.isEmpty(preferred)) {
      List<String> list = DataUtils.parseColumns(preferred, dataInfo.getColumns(), null, null);
      if (list != null) {
        colNames.addAll(list);
      }
    }

    if (!colNames.isEmpty()) {
      for (String colName : colNames) {
        BeeColumn column = dataInfo.getColumn(colName);
        if (column.isWritable()) {
          result.add(dataInfo.getColumn(colName));
        }
      }
    }

    for (BeeColumn column : dataInfo.getColumns()) {
      if (column.isWritable() && (colNames.isEmpty() || !colNames.contains(column.getId())
          && !column.isNullable() && !column.hasDefaults())) {
        result.add(column);
      }
    }
    return result;
  }

  private static void getForm(String formName, final String caption, FormCallback formCallback,
      final DataInfo dataInfo, final BeeRow row, final UIObject target,
      final NewRowCallback newRowCallback) {

    FormCallback fcb =
        (formCallback == null) ? FormFactory.getFormCallback(formName) : formCallback;

    FormFactory.createFormView(formName, dataInfo.getViewName(), dataInfo.getColumns(), false, fcb,
        new FormFactory.FormViewCallback() {
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              result.setEditing(true);
              result.start(null);
              result.updateRow(row, false);

              openForm(result, caption, dataInfo, target, newRowCallback);
            }
          }
        });
  }

  private static void insert(final DataInfo dataInfo, IsRow row, final NewRowCallback callback) {
    Queries.insert(dataInfo.getViewName(), dataInfo.getColumns(), row, new Queries.RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        BeeKeeper.getBus().fireEvent(new RowInsertEvent(dataInfo.getViewName(), result));
        if (callback != null) {
          callback.onSuccess(result);
        }
      }
    });
  }

  private static void openForm(final FormView formView, String caption, final DataInfo dataInfo,
      UIObject target, final NewRowCallback callback) {

    String cap = BeeUtils.notEmpty(caption, formView.getCaption(), DEFAULT_CAPTION);
    Global.inputWidget(cap, formView.asWidget(), new InputWidgetCallback() {
      @Override
      public String getErrorMessage() {
        if (!formView.validate()) {
          return InputBoxes.SILENT_ERROR;
        } else if (countValues(formView.getActiveRow(), dataInfo.getColumns()) <= 0) {
          return "All columns cannot be empty";
        } else {
          return null;
        }
      }

      @Override
      public void onCancel() {
        if (callback == null) {
          super.onCancel();
        } else {
          callback.onCancel();
        }
      }

      @Override
      public void onSuccess() {
        insert(dataInfo, formView.getActiveRow(), callback);
      }

      @Override
      public void onTimeout() {
        if (callback == null) {
          super.onTimeout();
        } else {
          callback.onTimeout();
        }
      }
    }, OK, CANCEL, true, DIALOG_STYLE, target);
  }

  private RowFactory() {
  }
}
