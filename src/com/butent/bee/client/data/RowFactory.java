package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.dialog.ModalForm;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.presenter.NewRowPresenter;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HandlesActions;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class RowFactory {

  public static final String DIALOG_STYLE = "bee-NewRow";

  public static final String STYLE_NEW_ROW_TABLE = "bee-NewRow-table";
  public static final String STYLE_NEW_ROW_LABEL_CELL = "bee-NewRow-labelCell";
  public static final String STYLE_NEW_ROW_LABEL = "bee-NewRow-label";
  public static final String STYLE_NEW_ROW_INPUT_CELL = "bee-NewRow-inputCell";
  public static final String STYLE_NEW_ROW_INPUT = "bee-NewRow-input";

  public static final int GENERATED_FORM_WIDTH = 360;
  public static final int GENERATED_HEADER_HEIGHT = 30;
  public static final int GENERATED_ROW_HEIGHT = 32;
  public static final int GENERATED_HEIGHT_MARGIN = 20;

  private static final int GENERATED_AREA_HEIGHT = 60;

  private static final String DEFAULT_CAPTION = "Naujas";

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

    SelectorEvent event = SelectorEvent.fireNewRow(selector, row);
    String value = selector.getDisplayValue();

    if (!event.isConsumed() && !BeeUtils.isEmpty(value)) {
      for (String colName : selector.getChoiceColumns()) {
        BeeColumn column = dataInfo.getColumn(colName);
        if (column != null && column.isWritable() && ValueType.isString(column.getType())) {
          Data.setValue(dataInfo.getViewName(), row, column.getId(), value.trim());
          break;
        }
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

    createRow(formName, caption, dataInfo, row, selector, new RowCallback() {
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
    });
  }

  public static void createRow(String formName, String caption, DataInfo dataInfo, BeeRow row,
      UIObject target, FormCallback formCallback, RowCallback rowCallback) {
    Assert.notEmpty(formName);

    Assert.notNull(dataInfo);
    Assert.notNull(row);

    getForm(formName, caption, formCallback, dataInfo, row, target, rowCallback);
  }

  public static void createRow(String formName, String caption, DataInfo dataInfo, BeeRow row,
      RowCallback rowCallback) {
    createRow(formName, caption, dataInfo, row, null, null, rowCallback);
  }

  public static void createRow(String formName, String caption, DataInfo dataInfo, BeeRow row,
      UIObject target, RowCallback rowCallback) {
    createRow(formName, caption, dataInfo, row, target, null, rowCallback);
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

  private static FormDescription createFormDescription(String formName, DataInfo dataInfo,
      List<BeeColumn> columns) {
    Document doc = XMLParser.createDocument();
    Element form = doc.createElement(FormFactory.TAG_FORM);

    form.setAttribute(UiConstants.ATTR_NAME, formName);
    form.setAttribute(UiConstants.ATTR_VIEW_NAME, dataInfo.getViewName());

    form.setAttribute(HasDimensions.ATTR_WIDTH, BeeUtils.toString(GENERATED_FORM_WIDTH));

    Element table = doc.createElement(FormWidget.TABLE.getTagName());
    table.setAttribute(UiConstants.ATTR_CLASS, STYLE_NEW_ROW_TABLE);

    int height = GENERATED_HEADER_HEIGHT + GENERATED_HEIGHT_MARGIN;
    for (BeeColumn column : columns) {
      Element row = doc.createElement(UiConstants.TAG_ROW);

      Element labelCell = doc.createElement(UiConstants.TAG_CELL);
      labelCell.setAttribute(UiConstants.ATTR_CLASS, STYLE_NEW_ROW_LABEL_CELL);

      Element label = doc.createElement(FormWidget.LABEL.getTagName());
      label.setAttribute(UiConstants.ATTR_HTML,
          BeeUtils.notEmpty(column.getLabel(), column.getId()));

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

      if (column.isText()) {
        height += GENERATED_AREA_HEIGHT;
      } else {
        height += GENERATED_ROW_HEIGHT;
      }
    }

    form.setAttribute(HasDimensions.ATTR_HEIGHT, BeeUtils.toString(height));

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
      List<String> list = DataUtils.parseColumns(preferred, dataInfo.getColumns());
      if (!list.isEmpty()) {
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
      final RowCallback rowCallback) {

    FormCallback fcb =
        (formCallback == null) ? FormFactory.getFormCallback(formName) : formCallback;

    FormFactory.createFormView(formName, dataInfo.getViewName(), dataInfo.getColumns(), true, fcb,
        new FormFactory.FormViewCallback() {
          @Override
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              result.setEditing(true);
              result.start(null);

              openForm(result, caption, dataInfo, row, target, rowCallback);
            }
          }
        });
  }

  private static void openForm(final FormView formView, String caption, final DataInfo dataInfo,
      final BeeRow row, UIObject target, final RowCallback callback) {

    String cap = BeeUtils.notEmpty(caption, formView.getCaption(), DEFAULT_CAPTION);

    final NewRowPresenter presenter = new NewRowPresenter(formView, dataInfo, cap);
    final ModalForm dialog = new ModalForm(presenter.getWidget(), formView, false, true);

    final CloseCallback close = new CloseCallback() {
      @Override
      public void onClose() {
        dialog.hide();
        if (callback != null) {
          callback.onCancel();
        }
      }

      @Override
      public void onSave() {
        presenter.save(new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            dialog.hide();
            if (callback != null) {
              callback.onSuccess(result);
            }
          }
        });
      }
    };

    presenter.setActionDelegate(new HandlesActions() {
      @Override
      public void handleAction(Action action) {
        if (Action.CLOSE.equals(action)) {
          formView.onClose(close);
        } else if (Action.SAVE.equals(action)) {
          close.onSave();
        }
      }
    });

    dialog.setOnSave(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        if (formView.checkOnSave()) {
          presenter.handleAction(Action.SAVE);
        }
      }
    });

    dialog.setOnEscape(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        if (formView.checkOnClose()) {
          presenter.handleAction(Action.CLOSE);
        }
      }
    });

    dialog.addAttachHandler(new AttachEvent.Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent event) {
        if (event.isAttached()) {
          formView.updateRow(row, true);
        }
      }
    });

    if (target == null) {
      dialog.center();
    } else {
      dialog.showRelativeTo(target);
    }

    UiHelper.focus(formView.getRootWidget());
  }

  private RowFactory() {
  }
}
