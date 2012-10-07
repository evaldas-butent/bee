package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.dialog.ModalForm;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.presenter.NewRowPresenter;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.view.edit.SelectorEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HandlesActions;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class RowFactory {

  public static final String DIALOG_STYLE = "bee-NewRow";

  private static final String DEFAULT_CAPTION = "Naujas";

  private static final String STYLE_NEW_ROW_TABLE = "bee-NewRow-table";
  private static final String STYLE_NEW_ROW_LABEL_CELL = "bee-NewRow-labelCell";
  private static final String STYLE_NEW_ROW_LABEL = "bee-NewRow-label";
  private static final String STYLE_NEW_ROW_INPUT_CELL = "bee-NewRow-inputCell";
  private static final String STYLE_NEW_ROW_INPUT = "bee-NewRow-input";

  private static final int GENERATED_FORM_WIDTH = 360;
  private static final int GENERATED_HEADER_HEIGHT = 30;
  private static final int GENERATED_ROW_HEIGHT = 32;
  private static final int GENERATED_AREA_HEIGHT = 60;
  private static final int GENERATED_HEIGHT_MARGIN = 20;

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
    if (!BeeUtils.isEmpty(value)) {
      for (String colName : selector.getChoiceColumns()) {
        BeeColumn column = dataInfo.getColumn(colName);
        if (column != null && column.isWritable() && ValueType.isString(column.getType())) {
          Data.setValue(dataInfo.getViewName(), row, column.getId(), value.trim());
          break;
        }
      }
    }

    SelectorEvent.fireNewRow(selector, row);

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
    
    form.setAttribute(HasDimensions.ATTR_WIDTH, BeeUtils.toString(GENERATED_FORM_WIDTH));

    Element table = doc.createElement(FormWidget.FLEX_TABLE.getTagName());
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
      final RowCallback rowCallback) {

    FormCallback fcb =
        (formCallback == null) ? FormFactory.getFormCallback(formName) : formCallback;

    FormFactory.createFormView(formName, dataInfo.getViewName(), dataInfo.getColumns(), false, fcb,
        new FormFactory.FormViewCallback() {
          @Override
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              result.setEditing(true);
              result.start(null);
              result.updateRow(row, false);

              openForm(result, caption, dataInfo, target, rowCallback);
            }
          }
        });
  }

  private static void insert(final DataInfo dataInfo, IsRow row, final RowCallback callback) {
    Queries.insert(dataInfo.getViewName(), dataInfo.getColumns(), row, new RowCallback() {
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
      UIObject target, final RowCallback callback) {

    String cap = BeeUtils.notEmpty(caption, formView.getCaption(), DEFAULT_CAPTION);

    NewRowPresenter presenter = new NewRowPresenter(formView, cap);

    final ModalForm dialog = new ModalForm(presenter.getWidget(), formView, false, true);
    final Holder<State> state = Holder.of(State.OPEN);

    final Command close = new Command() {
      @Override
      public void execute() {
        state.set(State.CANCELED);
        dialog.hide();
      }
    };
    
    presenter.setActionDelegate(new HandlesActions() {
      @Override
      public void handleAction(Action action) {
        if (Action.CLOSE.equals(action)) {
          formView.onCancel(close);

        } else if (Action.SAVE.equals(action)) {
          if (validate(formView, dataInfo)) {
            state.set(State.CONFIRMED);
            dialog.hide();
          }
        }
      }
    });

    Binder.addKeyDownHandler(dialog, new KeyDownHandler() {
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
          event.preventDefault();
          formView.onCancel(close);

        } else if (UiHelper.isSave(event.getNativeEvent())) {
          event.preventDefault();
          if (validate(formView, dataInfo)) {
            state.set(State.CONFIRMED);
            dialog.hide();
          }
        }
      }
    });

    dialog.addCloseHandler(new CloseHandler<Popup>() {
      public void onClose(CloseEvent<Popup> event) {
        if (State.CONFIRMED.equals(state.get())) {
          insert(dataInfo, formView.getActiveRow(), callback);
        } else if (callback != null) {
          callback.onCancel();
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

  private static boolean validate(FormView formView, DataInfo dataInfo) {
    if (!formView.validate()) {
      return false;
    } else if (countValues(formView.getActiveRow(), dataInfo.getColumns()) <= 0) {
      formView.notifySevere("All columns cannot be empty");
      return false;
    } else {
      return true;
    }
  }

  private RowFactory() {
  }
}
