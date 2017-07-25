package com.butent.bee.client.data;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.ModalForm;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.presenter.NewRowPresenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.HasDimensions;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.discussions.DiscussionsConstants;
import com.butent.bee.shared.modules.documents.DocumentConstants;
import com.butent.bee.shared.modules.service.ServiceConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HandlesActions;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class RowFactory {

  public static final String DIALOG_STYLE = BeeConst.CSS_CLASS_PREFIX + "NewRow";

  public static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "NewRow-";

  public static final String STYLE_NEW_ROW_TABLE = STYLE_PREFIX + "table";
  public static final String STYLE_NEW_ROW_LABEL_CELL = STYLE_PREFIX + "labelCell";
  public static final String STYLE_NEW_ROW_LABEL = STYLE_PREFIX + "label";
  public static final String STYLE_NEW_ROW_INPUT_CELL = STYLE_PREFIX + "inputCell";
  public static final String STYLE_NEW_ROW_INPUT = STYLE_PREFIX + "input";

  public static final int GENERATED_FORM_WIDTH = 500;
  public static final int GENERATED_HEADER_HEIGHT = 40;
  public static final int GENERATED_ROW_HEIGHT = 40;
  public static final int GENERATED_HEIGHT_MARGIN = 20;

  private static final String STYLE_MENU_POPUP = STYLE_PREFIX + "menu-popup";
  private static final String STYLE_MENU_PANEL = STYLE_PREFIX + "menu-panel";
  private static final String STYLE_MENU_ITEM = STYLE_PREFIX + "menu-item";

  private static final int GENERATED_AREA_HEIGHT = 60;

  private static final String DEFAULT_CAPTION = Localized.dictionary().actionNew();

  private static final BeeLogger logger = LogUtils.getLogger(RowFactory.class);

  public static BeeRow createEmptyRow(DataInfo dataInfo) {
    return createEmptyRow(dataInfo, true);
  }

  public static BeeRow createEmptyRow(DataInfo dataInfo, boolean defaults) {
    BeeRow row = DataUtils.createEmptyRow(dataInfo.getColumnCount());
    if (defaults) {
      setDefaults(row, dataInfo);
    }
    return row;
  }

  public static void createRelatedRow(DataSelector selector, String defValue) {
    Assert.notNull(selector);

    DataInfo dataInfo = selector.getOracle().getDataInfo();

    String formName = selector.getNewRowForm();

    if (BeeUtils.isEmpty(formName)) {
      List<BeeColumn> columns = getColumns(dataInfo, selector.getNewRowColumns(),
          selector.getChoiceColumns());
      if (columns.isEmpty()) {
        logger.warning(dataInfo.getViewName(), "columns not available for create row");
        return;
      }

      formName = generateFormName(dataInfo, columns);

      FormDescription formDescription = FormFactory.getFormDescription(formName);
      if (formDescription == null) {
        formDescription = createFormDescription(formName, dataInfo, columns);
        FormFactory.putFormDescription(formName, formDescription);
      }
    }

    BeeRow row = createEmptyRow(dataInfo, true);

    SelectorEvent event = SelectorEvent.fireNewRow(selector, row, formName, defValue);

    if (!event.isConsumed()) {
      if (!BeeUtils.isEmpty(event.getDefValue())) {
        for (String colName : selector.getChoiceColumns()) {
          BeeColumn column = dataInfo.getColumn(colName);

          if (column != null && column.isEditable() && ValueType.isString(column.getType())) {
            Data.squeezeValue(dataInfo.getViewName(), row, column.getId(),
                event.getDefValue().trim());
            break;
          }
        }
      }

      Opener opener = Opener.in(UiHelper.getRelationNewRowWindowType(), selector.getElement(),
          event.getOnOpenNewRow());

      createRelatedRow(formName, row, selector, opener);
    }
  }

  public static void createRelatedRow(String formName, BeeRow row, DataSelector selector) {
    Assert.notNull(selector);

    createRelatedRow(formName, row, selector,
        Opener.in(UiHelper.getRelationNewRowWindowType(), selector.getElement(), null));
  }

  private static void createRelatedRow(String formName, BeeRow row, final DataSelector selector,
      Opener opener) {

    Assert.notEmpty(formName);
    Assert.notNull(row);
    Assert.notNull(selector);

    selector.setAdding(true);

    createRow(formName, selector.getNewRowCaption(), selector.getOracle().getDataInfo(), row,
        opener, null, new RowCallback() {
          @Override
          public void onCancel() {
            if (selector.isAttached() && selector.isAdding()) {
              selector.setAdding(false);

              if (UiHelper.isInteractive(selector)) {
                selector.setFocus(true);
              }
            }
          }

          @Override
          public void onSuccess(BeeRow result) {
            if (selector.isAttached() && selector.isAdding()) {
              SelectorEvent.fireRowCreated(selector, result);
              selector.setAdding(false);

              if (UiHelper.isInteractive(selector)) {
                selector.setSelection(result, null, true);
              }
            }
          }
        });
  }

  public static void createRow(String viewName) {
    createRow(viewName, getDefaultOpener());
  }

  public static void createRow(String viewName, Opener opener) {
    createRow(viewName, opener, null);
  }

  public static void createRow(String viewName, RowCallback rowCallback) {
    createRow(viewName, getDefaultOpener(), rowCallback);
  }

  public static void createRow(String viewName, Opener opener, RowCallback rowCallback) {
    Assert.notEmpty(viewName);

    DataInfo dataInfo = Data.getDataInfo(viewName);
    if (dataInfo == null) {
      return;
    }

    BeeRow row = createEmptyRow(dataInfo, true);
    createRow(dataInfo.getNewRowForm(), dataInfo.getNewRowCaption(),
        dataInfo, row, opener, null, rowCallback);
  }

  public static void createRow(DataInfo dataInfo, BeeRow row) {
    createRow(dataInfo, row, getDefaultOpener());
  }

  public static void createRow(DataInfo dataInfo, BeeRow row, Opener opener) {
    createRow(dataInfo, row, opener, null);
  }

  public static void createRow(DataInfo dataInfo, BeeRow row, RowCallback rowCallback) {
    createRow(dataInfo, row, getDefaultOpener(), rowCallback);
  }

  public static void createRow(DataInfo dataInfo, BeeRow row, Opener opener,
      RowCallback rowCallback) {

    Assert.notNull(dataInfo);
    createRow(dataInfo.getNewRowForm(), dataInfo.getNewRowCaption(), dataInfo, row, opener, null,
        rowCallback);
  }

  public static void createRow(String formName, String caption, DataInfo dataInfo, BeeRow row,
      RowCallback rowCallback) {

    createRow(formName, caption, dataInfo, row, getDefaultOpener(), null, rowCallback);
  }

  public static void createRow(String formName, String caption, DataInfo dataInfo, BeeRow row,
      Opener opener, RowCallback rowCallback) {

    createRow(formName, caption, dataInfo, row, opener, null, rowCallback);
  }

  public static void createRow(String formName, String caption, DataInfo dataInfo, BeeRow row,
      FormInterceptor formInterceptor, RowCallback rowCallback) {

    createRow(formName, caption, dataInfo, row, getDefaultOpener(), formInterceptor, rowCallback);
  }

  public static void createRow(String formName, String caption, DataInfo dataInfo, BeeRow row,
      Opener opener, FormInterceptor formInterceptor, RowCallback rowCallback) {

    Assert.notEmpty(formName);

    Assert.notNull(dataInfo);
    Assert.notNull(row);

    getForm(formName, caption, formInterceptor, dataInfo, row, opener, rowCallback);
  }

  public static void createRowUsingForm(String viewName, String formName, RowCallback rowCallback) {
    Assert.notEmpty(viewName);
    Assert.notEmpty(formName);

    DataInfo dataInfo = Data.getDataInfo(viewName);

    if (dataInfo != null) {
      BeeRow row = createEmptyRow(dataInfo, true);
      createRow(formName, dataInfo.getNewRowCaption(), dataInfo, row, getDefaultOpener(), null,
          rowCallback);
    }
  }

  public static void showMenu(Widget target) {
    Vertical panel = new Vertical();
    panel.addStyleName(STYLE_MENU_PANEL);

    addMenuItem(panel, Module.TASKS, TaskConstants.VIEW_TASKS,
        Localized.dictionary().crmNewTask());

    addMenuItem(panel, Module.CLASSIFIERS, ClassifierConstants.VIEW_COMPANIES,
        Localized.dictionary().newClient());

    addMenuItem(panel, Module.DOCUMENTS, DocumentConstants.VIEW_DOCUMENTS,
        Localized.dictionary().documentNew());

    addMenuItem(panel, Module.TASKS, TaskConstants.VIEW_TODO_LIST,
        Localized.dictionary().crmNewTodoItem());

    addMenuItem(panel, Module.TASKS, TaskConstants.TBL_REQUESTS,
        Localized.dictionary().crmNewRequest());

    addMenuItem(panel, Module.DISCUSSIONS, DiscussionsConstants.VIEW_DISCUSSIONS,
        Localized.dictionary().announcementNew(), () -> {
          DataInfo dataInfo = Data.getDataInfo(DiscussionsConstants.VIEW_DISCUSSIONS);
          BeeRow row = createEmptyRow(dataInfo, true);

          createRow(DiscussionsConstants.FORM_NEW_ANNOUNCEMENT,
              Localized.dictionary().announcementNew(), dataInfo, row, getDefaultOpener(), null);
        });

    addMenuItem(panel, Module.SERVICE, ServiceConstants.TBL_SERVICE_MAINTENANCE,
        Localized.dictionary().svcNewMaintenance());

    if (!panel.isEmpty()) {
      Popup popup = new Popup(OutsideClick.CLOSE, STYLE_MENU_POPUP);

      popup.setWidget(panel);
      popup.setHideOnEscape(true);

      if (target == null) {
        popup.center();
      } else {
        popup.showRelativeTo(target.getElement());
      }
    }
  }

  private static void addMenuItem(HasWidgets panel, Module module, String viewName, String text) {
    addMenuItem(panel, module, viewName, text, () -> createRow(viewName, getDefaultOpener()));
  }

  private static void addMenuItem(HasWidgets panel, Module module, String viewName, String text,
      final Runnable command) {

    if (BeeKeeper.getUser().isModuleVisible(ModuleAndSub.of(module))
        && BeeKeeper.getUser().canCreateData(viewName)) {

      Label label = new Label(text);
      label.addStyleName(STYLE_MENU_ITEM);

      label.addClickHandler(event -> {
        UiHelper.closeDialog((Widget) event.getSource());
        command.run();
      });

      panel.add(label);
    }
  }

  public static int setDefaults(BeeRow row, DataInfo dataInfo) {
    if (row == null || dataInfo == null) {
      return BeeConst.UNDEF;
    }

    List<String> colNames = new ArrayList<>();
    for (BeeColumn column : dataInfo.getColumns()) {
      if (column.hasDefaults()) {
        colNames.add(column.getId());
      }
    }
    if (colNames.isEmpty()) {
      return 0;
    }

    return DataUtils.setDefaults(row, colNames, dataInfo.getColumns(), Global.getDefaults())
        + RelationUtils.setDefaults(dataInfo, row, colNames, dataInfo.getColumns(),
        BeeKeeper.getUser().getUserData());
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
          BeeUtils.notEmpty(Localized.getLabel(column), column.getId()));

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

      if (widgetType == FormWidget.CHECK_BOX) {
        input.setAttribute(UiConstants.ATTR_HTML, BeeConst.STRING_MINUS);
      }

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
    List<BeeColumn> result = new ArrayList<>();

    List<String> colNames = new ArrayList<>();
    if (!BeeUtils.isEmpty(specified)) {
      List<String> list = DataUtils.parseColumns(specified, dataInfo.getColumns());
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
        if (column.isEditable()) {
          result.add(dataInfo.getColumn(colName));
        }
      }
    }

    for (BeeColumn column : dataInfo.getColumns()) {
      if (column.isEditable() && (colNames.isEmpty() || !colNames.contains(column.getId())
          && !column.isNullable() && !column.hasDefaults())) {
        result.add(column);
      }
    }
    return result;
  }

  private static Opener getDefaultOpener() {
    return Opener.in(UiHelper.getOtherNewRowWindowType(), null);
  }

  private static void getForm(String formName, final String caption,
      FormInterceptor formInterceptor, final DataInfo dataInfo, final BeeRow row,
      final Opener opener, final RowCallback rowCallback) {

    FormInterceptor fcb =
        (formInterceptor == null) ? FormFactory.getFormInterceptor(formName) : formInterceptor;

    FormFactory.createFormView(formName, dataInfo.getViewName(), dataInfo.getColumns(), true, fcb,
        (formDescription, result) -> {
          if (result != null) {
            result.setAdding(true);
            result.setEditing(true);
            result.start(null);

            Opener formOpener;
            if (opener == null) {
              formOpener = getDefaultOpener();
            } else {
              formOpener = opener.normalize();
            }

            openForm(result, caption, dataInfo, row, formOpener, rowCallback);
          }
        });
  }

  public static void openForm(final FormView formView, String caption, final DataInfo dataInfo,
      final BeeRow row, Opener opener, final RowCallback callback) {

    final String cap = BeeUtils.notEmpty(caption, formView.getCaption(), DEFAULT_CAPTION);

    final FormInterceptor interceptor = formView.getFormInterceptor();

    Set<Action> enabledActions = EnumSet.noneOf(Action.class);
    if (interceptor != null && interceptor.saveOnPrintNewRow()) {
      enabledActions.add(Action.PRINT);
    }

    final NewRowPresenter presenter = new NewRowPresenter(formView, dataInfo, cap, enabledActions);
    if (interceptor != null) {
      interceptor.afterCreatePresenter(presenter);
    }

    final ModalForm dialog = opener.isPopup() ? new ModalForm(presenter, formView, false) : null;

    final RowCallback closer = new RowCallback() {
      @Override
      public void onCancel() {
        closeForm();
        if (callback != null) {
          callback.onCancel();
        }
      }

      @Override
      public void onSuccess(BeeRow result) {
        closeForm();
        if (callback != null) {
          callback.onSuccess(result);
        }
      }

      private void closeForm() {
        if (opener.isPopup()) {
          dialog.close();
        } else {
          BeeKeeper.getScreen().closeWidget(presenter.getMainView());
        }
      }
    };

    presenter.setActionDelegate(new HandlesActions() {
      @Override
      public void handleAction(Action action) {
        if (interceptor != null && !interceptor.beforeAction(action, presenter)) {
          return;
        }

        switch (action) {
          case CANCEL:
            closer.onCancel();
            break;

          case CLOSE:
            formView.onClose(new CloseCallback() {
              @Override
              public void onClose() {
                closer.onCancel();
              }

              @Override
              public void onSave() {
                handleAction(Action.SAVE);
              }
            });
            break;

          case PRINT:
            Global.confirm(cap, Icon.QUESTION,
                Collections.singletonList(Localized.dictionary().saveAndPrintQuestion()),
                Localized.dictionary().saveAndPrintAction(), Localized.dictionary().cancel(),
                () -> {
                  if (interceptor != null && !interceptor.beforeAction(Action.SAVE, presenter)) {
                    return;
                  }

                  presenter.save(new RowCallback() {
                    @Override
                    public void onCancel() {
                      closer.onCancel();
                    }

                    @Override
                    public void onSuccess(BeeRow result) {
                      closer.onSuccess(result);

                      RowEditor.open(dataInfo.getViewName(), result,
                          Opener.in(opener.getWindowType(),
                              fv -> fv.getViewPresenter().handleAction(Action.PRINT)));
                    }
                  });
                });
            break;

          case SAVE:
            presenter.save(closer);
            break;

          default:
            logger.warning(NameUtils.getName(this), action, "not implemented");
        }
      }
    });

    Runnable onOpen = () -> {
      if (interceptor != null) {
        interceptor.onStartNewRow(formView, row);
      }

      formView.updateRow(row, true);

      formView.focus();

      if (opener.getOnOpen() != null) {
        opener.getOnOpen().accept(formView);
      }
    };

    if (opener.isPopup()) {
      dialog.setOnSave(input -> {
        if (formView.checkOnSave(input)) {
          presenter.handleAction(Action.SAVE);
        }
      });

      dialog.setOnEscape(input -> {
        if (formView.checkOnClose(input)) {
          presenter.handleAction(Action.CLOSE);
        }
      });

      dialog.addOpenHandler(event -> onOpen.run());

      dialog.setPreviewEnabled(opener.isModal());

      if (opener.getTarget() == null) {
        dialog.centerOrCascade(formView.getContainerStyleName());
      } else {
        dialog.showRelativeTo(opener.getTarget());
      }

    } else {
      opener.onCreate(presenter);
      onOpen.run();
    }
  }

  private RowFactory() {
  }
}
