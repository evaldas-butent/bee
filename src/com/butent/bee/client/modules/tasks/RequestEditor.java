package com.butent.bee.client.modules.tasks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.UserInfo;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.FileGroup;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.event.logical.SelectorEvent.Handler;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.ViewFactory.SupplierKind;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskStatus;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RequestEditor extends AbstractFormInterceptor {

  private static final String WIDGET_MANGAER_NAME = "Manager";
  private static final String WIDGET_FILES_NAME = "Files";
  private static final String WIDGET_RESULT_PROPERTIES = "ResultProperties";
  private static final String STYLE_PREFIX = TaskConstants.CRM_STYLE_PREFIX + "request-";

  private static final String STYLE_PROPERTY_CAPTION = STYLE_PREFIX + "prop-caption";
  private static final String STYLE_PROPERTY_DATA = STYLE_PREFIX + "prop-data";

  private static final BeeLogger logger = LogUtils.getLogger(RequestEditor.class);

  private final UserInfo currentUser = BeeKeeper.getUser();

  private static final class FinishSaveCallback extends RowUpdateCallback {

    private final FormView formView;

    private FinishSaveCallback(FormView formView) {
      super(formView.getViewName());
      this.formView = formView;
    }

    @Override
    public void onSuccess(BeeRow result) {
      super.onSuccess(result);
      formView.updateRow(result, true);
      formView.refresh();
    }
  }

  static FlowPanel resultProperties;
  private Label productLabel;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(WIDGET_RESULT_PROPERTIES, name) && widget instanceof FlowPanel) {
      resultProperties = (FlowPanel) widget;
    } else if (BeeUtils.same(COL_PRODUCT, name) && (widget instanceof Label)) {
      productLabel = (Label) widget;
      productLabel.setStyleName(StyleUtils.NAME_REQUIRED, false);
    } else if (BeeUtils.same(COL_REQUEST_TYPE, name) && (widget instanceof DataSelector)) {
      ((DataSelector) widget).addSelectorHandler(new Handler() {

        @Override
        public void onDataSelector(SelectorEvent event) {
          TasksKeeper.getProductRequired(getActiveRow(), productLabel, getViewName());
          getFormView().refresh();
        }
      });
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void afterRefresh(final FormView form, final IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    boolean finished =
        row.getDateTime(form.getDataIndex(TaskConstants.COL_REQUEST_FINISHED)) != null;

    if (!finished) {
      FaLabel btnFinish = new FaLabel(FontAwesome.CHECK_CIRCLE_O);
      btnFinish.setTitle(Localized.getConstants().requestFinish());
      btnFinish.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          finishRequest(form, row);
        }
      });

      header.addCommandItem(btnFinish);
      form.setEnabled(true);
    }

    if (currentUser.canCreateData(TaskConstants.VIEW_TASKS) && !finished) {
      FaLabel btnFinishToTask = new FaLabel(FontAwesome.LIST);
      btnFinishToTask.setTitle(Localized.getConstants().requestFinishToTask());
      btnFinishToTask.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
          toTaskAndFinish(form, row);
        }
      });

      header.addCommandItem(btnFinishToTask);
    }

    if (finished) {
      form.setEnabled(false);
      createUpdateButton(form, row, header);
    }

    showResultProperties(form, row);

  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.SAVE) {
      if (TasksKeeper.getProductRequired(getActiveRow(), productLabel, getViewName())) {
        if (Data.isNull(VIEW_REQUESTS, getActiveRow(), COL_PRODUCT)) {
          getFormView().notifySevere(Localized.getConstants().crmTaskProduct() + " "
              + Localized.getConstants().valueRequired());
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    TasksKeeper.getProductRequired(row, productLabel, getViewName());
  }

  @Override
  public FormInterceptor getInstance() {
    return new RequestEditor();
  }

  @Override
  public boolean onStartEdit(final FormView form, final IsRow row, ScheduledCommand focusCommand) {
    final Widget fileWidget = form.getWidgetByName(PROP_FILES);

    if (fileWidget instanceof FileGroup) {
      ((FileGroup) fileWidget).clear();

      ParameterList params = TasksKeeper.createArgs(SVC_GET_REQUEST_FILES);
      params.addDataItem(COL_REQUEST, row.getId());

      BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(form);

          if (response.hasErrors()) {
            return;
          }
          List<FileInfo> files = FileInfo.restoreCollection((String) response.getResponse());

          if (!files.isEmpty()) {
            for (FileInfo file : files) {
              ((FileGroup) fileWidget).addFile(file);
            }
          }
        }
      });
    }
    return true;
  }

  private static String appendIdsData(String ids, String oldIds) {
    String result = ids;
    if (!BeeUtils.isEmpty(oldIds)) {
      List<Long> oldIdsList = DataUtils.parseIdList(oldIds);
      List<Long> newIdsList = DataUtils.parseIdList(ids);

      oldIdsList.addAll(newIdsList);
      result = DataUtils.buildIdList(oldIdsList);
    }
    return result;
  }

  private static void createUpdateButton(final FormView form, final IsRow row, HeaderView header) {
    FaLabel updateRequestBtn = new FaLabel(FontAwesome.RETWEET);
    updateRequestBtn.setTitle(Localized.getConstants().actionUpdate());
    updateRequestBtn.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        updateRequest(form, row);
      }
    });

    header.addCommandItem(updateRequestBtn);
  }

  private static void finishRequest(final FormView form, final IsRow row) {

    final TaskDialog dialog =
        new TaskDialog(Localized.getConstants().requestFinishing());

    final String cid = dialog.addComment(true);

    final Map<String, String> durIds = dialog.addDuration();

    Filter filter =
        Filter.in(Data.getIdColumn(VIEW_DURATION_TYPES), "RequestDurationTypes", COL_DURATION_TYPE,
            Filter.equals(COL_TASK_TYPE, row.getLong(
                Data.getColumnIndex(VIEW_REQUESTS, COL_REQUEST_TYPE))));

    dialog.getSelector(durIds.get(COL_DURATION_TYPE)).getOracle()
        .setAdditionalFilter(filter, true);

    dialog.addAction(Localized.getConstants().actionSave(), new ScheduledCommand() {
      @Override
      public void execute() {

        final String comment = dialog.getComment(cid);
        final String time = dialog.getTime(durIds.get(COL_DURATION));

        if (BeeUtils.isEmpty(comment)) {
          Global.showError(Localized.getConstants().error(), Collections.singletonList(Localized
              .getConstants().crmEnterComment()));
          return;
        }

        if (!BeeUtils.isEmpty(time)) {
          final Long type = dialog.getSelector(durIds.get(COL_DURATION_TYPE)).getRelatedId();
          if (!DataUtils.isId(type)) {
            Global.showError(Localized.getConstants().crmEnterDurationType());
            return;
          }

          final DateTime date = dialog.getDateTime(durIds.get(COL_DURATION_DATE));
          if (date == null) {
            Global.showError(Localized.getConstants().crmEnterDueDate());
            return;
          }

          List<BeeColumn> columns =
              Lists.newArrayList(DataUtils.getColumn(TaskConstants.COL_REQUEST_FINISHED, form
                  .getDataColumns()));

          List<String> oldValues = Lists.newArrayList(row
              .getString(form.getDataIndex(TaskConstants.COL_REQUEST_FINISHED)));
          List<String> newValues =
              Lists.newArrayList(BeeUtils.toString(new DateTime().getTime()));

          columns.add(DataUtils.getColumn(TaskConstants.COL_REQUEST_RESULT, form.getDataColumns()));

          oldValues.add(row.getString(form.getDataIndex(TaskConstants.COL_REQUEST_RESULT)));
          newValues.add(comment);

          Queries.update(form.getViewName(), row.getId(), row.getVersion(), columns, oldValues,
              newValues, form.getChildrenForUpdate(), new RowCallback() {

                @Override
                public void onSuccess(BeeRow result) {
                  finishRequestWithTask(form, result, time, type, comment);
                }
              });

          dialog.close();
        } else {
          Global.showError(Localized.getConstants().crmSpentTime() + " "
              + Localized.getConstants().valueRequired());
          return;
        }
      }
    });

    dialog.display();
  }

  private static void toTaskAndFinish(final FormView form, final IsRow reqRow) {
    boolean edited = (reqRow != null) && form.isEditing();

    if (!edited) {
      Global.showError(Localized.getConstants().actionCanNotBeExecuted());
      return;
    }

    DataInfo taskDataInfo = Data.getDataInfo(TaskConstants.VIEW_TASKS);
    BeeRow taskRow = RowFactory.createEmptyRow(taskDataInfo, true);

    taskRow.setValue(taskDataInfo.getColumnIndex(ClassifierConstants.COL_COMPANY),
        reqRow.getLong(form.getDataIndex(COL_REQUEST_CUSTOMER)));

    taskRow.setValue(taskDataInfo.getColumnIndex(ClassifierConstants.ALS_COMPANY_NAME),
        reqRow.getString(form.getDataIndex(COL_REQUEST_CUSTOMER_NAME)));

    taskRow.setValue(taskDataInfo.getColumnIndex(ClassifierConstants.COL_CONTACT), reqRow
        .getLong(form.getDataIndex(COL_REQUEST_CUSTOMER_PERSON)));

    taskRow.setValue(taskDataInfo.getColumnIndex(ClassifierConstants.ALS_CONTACT_FIRST_NAME),
        reqRow
            .getString(form.getDataIndex(ALS_PERSON_FIRST_NAME)));

    taskRow.setValue(taskDataInfo.getColumnIndex(ClassifierConstants.ALS_CONTACT_LAST_NAME), reqRow
        .getString(form.getDataIndex(ALS_PERSON_LAST_NAME)));

    taskRow.setValue(taskDataInfo.getColumnIndex(TaskConstants.ALS_OWNER_FIRST_NAME),
        reqRow.getString(form.getDataIndex(ClassifierConstants.COL_FIRST_NAME)));

    taskRow.setValue(taskDataInfo.getColumnIndex(TaskConstants.ALS_OWNER_LAST_NAME), reqRow
        .getString(form.getDataIndex(ClassifierConstants.COL_LAST_NAME)));

    taskRow.setValue(taskDataInfo.getColumnIndex(TaskConstants.COL_DESCRIPTION), reqRow
        .getString(form.getDataIndex(COL_REQUEST_CONTENT)));

    DataSelector managerSel = (DataSelector) form.getWidgetByName(WIDGET_MANGAER_NAME);
    Map<Long, FileInfo> files = Maps.newHashMap();
    FileGroup filesList = (FileGroup) form.getWidgetByName(WIDGET_FILES_NAME);

    for (FileInfo f : filesList.getFiles()) {
      files.put(f.getId(), f);
    }

    RowFactory.createRow(taskDataInfo.getNewRowForm(), null, taskDataInfo, taskRow, null,
        new TaskBuilder(files, BeeUtils.toLongOrNull(managerSel.getValue()), true),
        new RowCallback() {

          @Override
          public void onSuccess(BeeRow result) {
            if (result != null && BeeUtils.isPositive(result.getNumberOfCells())) {
              createResulProperties(reqRow, result.getString(0), form);
            }
          }
        });
  }

  private static void updateRequest(final FormView form, final IsRow row) {
    Global.confirm(Localized.getConstants().requestUpdatingQuestion(), new ConfirmationCallback() {

      @Override
      public void onConfirm() {
        List<BeeColumn> columns = Lists.newArrayList(DataUtils
            .getColumn(TaskConstants.COL_REQUEST_FINISHED, form.getDataColumns()));
        List<String> oldValues = Lists.newArrayList(row
            .getString(form.getDataIndex(TaskConstants.COL_REQUEST_FINISHED)));

        List<String> newValues = Lists.newArrayList(BeeUtils.toString(null));

        Queries.update(form.getViewName(), row.getId(), row.getVersion(), columns, oldValues,
            newValues, form.getChildrenForUpdate(), new FinishSaveCallback(form));
      }
    });

  }

  private ClickHandler getTaskLinkClickHandler(final Long id) {
    return new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        final DataInfo dataInfo = Data.getDataInfo(VIEW_TASKS);

        Queries.getRow(VIEW_TASKS, id, new RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            RowEditor.openForm(FORM_TASK, dataInfo, result, Opener.NEW_TAB, null, null);
          }

          @Override
          public void onFailure(String... reason) {
            getFormView().notifySevere(Localized.getConstants().crmTaskNotFound());
            logger.warning("Error open task:", reason);
          }

        });
      }
    };
  }

  private void showResultProperties(final FormView form, final IsRow row) {
    if (resultProperties == null) {
      return;
    }
    int idxProp = form.getDataIndex(TaskConstants.COL_REQUEST_RESULT_PROPERTIES);

    resultProperties.clear();

    if (idxProp < 0) {
      return;
    }

    String data = row.getString(idxProp);

    if (BeeUtils.isEmpty(data)) {
      return;
    }

    Map<String, String> resultData = Codec.deserializeMap(data);

    for (String key : resultData.keySet()) {
      List<Long> rowIds = DataUtils.parseIdList(resultData.get(key));

      if (BeeUtils.isEmpty(rowIds)) {
        continue;
      }

      if (BeeUtils.isSuffix(key, TaskConstants.FORM_TASK)) {
        CustomDiv div = new CustomDiv(STYLE_PROPERTY_CAPTION);
        div.setText(Localized.getMessages().crmCreatedNewTasks(rowIds.size()));
        resultProperties.add(div);
      }

      for (Long id : rowIds) {
        if (!DataUtils.isId(id)) {
          continue;
        }

        InternalLink link = new InternalLink(BeeUtils.toString(id)
            + BeeConst.DEFAULT_ROW_SEPARATOR);
        link.addStyleName(STYLE_PROPERTY_DATA);
        link.addClickHandler(getTaskLinkClickHandler(id));

        resultProperties.add(link);
      }
    }
  }

  private static void finishRequestWithTask(final FormView form, final IsRow reqRow, String time,
      Long type, String comment) {
    boolean edited = (reqRow != null) && form.isEditing();

    if (!edited) {
      Global.showError(Localized.getConstants().actionCanNotBeExecuted());
      return;
    }

    final DataInfo taskDataInfo = Data.getDataInfo(TaskConstants.VIEW_TASKS);
    final BeeRow taskRow = RowFactory.createEmptyRow(taskDataInfo, true);
    String taskType = reqRow.getString(form.getDataIndex("TaskType"));

    if (!BeeUtils.isEmpty(taskType)) {
      taskRow.setValue(taskDataInfo.getColumnIndex(COL_TASK_TYPE), taskType);
    }

    taskRow.setValue(taskDataInfo.getColumnIndex(ClassifierConstants.COL_COMPANY),
        reqRow.getLong(form.getDataIndex(COL_REQUEST_CUSTOMER)));

    taskRow.setValue(taskDataInfo.getColumnIndex(ClassifierConstants.COL_CONTACT), reqRow
        .getLong(form.getDataIndex(COL_REQUEST_CUSTOMER_PERSON)));

    taskRow.setValue(taskDataInfo.getColumnIndex(COL_DESCRIPTION), reqRow
        .getString(form.getDataIndex(COL_REQUEST_CONTENT)));

    taskRow.setValue(taskDataInfo.getColumnIndex(COL_SUMMARY), Localized
        .getConstants().trAssessmentRequest()
        + " " + reqRow.getId());

    taskRow.setValue(taskDataInfo.getColumnIndex(COL_EXECUTOR), BeeKeeper.getUser()
        .getUserId());

    taskRow.setValue(taskDataInfo.getColumnIndex(COL_STATUS), TaskStatus.APPROVED.ordinal());

    if (!BeeUtils.isEmpty(reqRow.getString(form.getDataIndex(COL_PRODUCT)))) {
      taskRow.setValue(taskDataInfo.getColumnIndex(COL_PRODUCT), reqRow
          .getString(form.getDataIndex(COL_PRODUCT)));
    }

    ParameterList params = TasksKeeper.createArgs(SVC_FINISH_REQUEST_WITH_TASK);
    params.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(taskRow));
    params.addDataItem(COL_DURATION_TYPE, type);
    params.addDataItem(COL_DURATION, time);
    params.addDataItem(COL_COMMENT, comment);

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        if (!response.hasErrors()) {
          createResulProperties(reqRow, response.getResponseAsString(), form);
        }
      }
    });
  }

  public static void createResulProperties(IsRow reqRow, String ids, FormView form) {
    int idxFinished = form.getDataIndex(TaskConstants.COL_REQUEST_FINISHED);
    int idxProp = form.getDataIndex(TaskConstants.COL_REQUEST_RESULT_PROPERTIES);
    List<BeeColumn> columns = Lists.newArrayList();
    List<String> oldValues = Lists.newArrayList();
    List<String> newValues = Lists.newArrayList();
    Map<String, String> propData = Maps.newHashMap();

    if (idxProp > -1) {
      if (!BeeUtils.isEmpty(reqRow.getString(idxProp))) {
        propData = Codec.deserializeMap(reqRow.getString(idxProp));
      }
    }

    columns.add(DataUtils
        .getColumn(TaskConstants.COL_REQUEST_FINISHED, form.getDataColumns()));

    oldValues.add(reqRow.getString(idxFinished));

    newValues.add(BeeUtils.toString(new DateTime().getTime()));

    if (DataUtils.isId(ids)) {

      Queries.insert(AdministrationConstants.VIEW_RELATIONS, Data.getColumns(
          AdministrationConstants.VIEW_RELATIONS, Lists.newArrayList(COL_REQUEST, COL_TASK)), Lists
          .newArrayList(String.valueOf(reqRow.getId()), ids));

      columns.add(DataUtils
          .getColumn(TaskConstants.COL_REQUEST_RESULT_PROPERTIES, form.getDataColumns()));
      oldValues.add(reqRow.getString(idxProp));

      String key = SupplierKind.FORM.getKey(TaskConstants.FORM_TASK);
      String oldIds = propData.get(key);

      ids = appendIdsData(ids, oldIds);
      propData.put(key, ids);
      newValues.add(Codec.beeSerialize(propData));
    }

    Queries.update(form.getViewName(), reqRow.getId(), reqRow.getVersion(),
        columns, oldValues, newValues, form.getChildrenForUpdate(),
        new FinishSaveCallback(form));
  }
}
