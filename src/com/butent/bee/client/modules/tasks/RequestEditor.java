package com.butent.bee.client.modules.tasks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HasHandlers;
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
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.Modality;
import com.butent.bee.client.eventsboard.EventsBoard.EventFilesFilter;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.ViewColumn;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskEvent;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskPriority;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskStatus;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RequestEditor extends ProductSupportInterceptor {

  private static final String WIDGET_MANGAER_NAME = "Manager";
  private static final String WIDGET_FILES_NAME = "Files";
  private static final String WIDGET_REQUEST_COMMENTS = "RequestComments";

  private static final String NAME_REQUEST_TREE = "RequestTree";
  private static final String NAME_ORDER = "RequestEventsOrder";

  private static final BeeLogger logger = LogUtils.getLogger(RequestEditor.class);

  private final UserInfo currentUser = BeeKeeper.getUser();

  private RequestEventsHandler eventsHandler;
  private Flow requestComments;

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

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof Flow && BeeUtils.same(name, WIDGET_REQUEST_COMMENTS)) {
      requestComments = (Flow) widget;
      requestComments.clear();
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void afterRefresh(final FormView form, final IsRow row) {
    Widget area = form.getWidgetBySource(COL_REQUEST_CONTENT);

    if (area != null && area instanceof InputArea) {
      StyleUtils.setHeight(area, 60);
      int scroll = area.getElement().getScrollHeight();

      if (scroll > 60) {
        StyleUtils.setHeight(area, scroll + 2);
      }
    }

    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    eventsHandler = new RequestEventsHandler(header, !readStorage(NAME_ORDER));

    boolean finished =
        row.getDateTime(form.getDataIndex(TaskConstants.COL_REQUEST_FINISHED)) != null;

    if (!finished) {
      FaLabel btnFinish = new FaLabel(FontAwesome.CHECK_CIRCLE_O);
      btnFinish.setTitle(Localized.dictionary().requestFinish());
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
      btnFinishToTask.setTitle(Localized.dictionary().requestFinishToTask());
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

    drawComments(row);

    super.afterRefresh(form, row);
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.SAVE && maybeNotifyEmptyProduct(msg -> getFormView().notifySevere(msg))) {
      return false;
    }
    return true;
  }

  @Override
  public FormInterceptor getInstance() {
    return new RequestEditor();
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {

    IsRow oldData = event.getOldRow();
    IsRow newData = event.getNewRow();

    if (oldData == null) {
      return;
    }

    DataInfo data = Data.getDataInfo(VIEW_REQUESTS);

    Map<String, String> oldDataMap = Maps.newHashMap();
    Map<String, String> newDataMap = Maps.newHashMap();

    BeeRowSet rs =
        DataUtils.getUpdated(VIEW_REQUESTS, getFormView().getDataColumns(), oldData, newData, null);

    if (rs == null) {
      return;
    }

    for (BeeColumn column : rs.getColumns()) {

      String oldValue = BeeConst.STRING_EMPTY;
      String newValue = BeeConst.STRING_EMPTY;

      if (data.hasRelation(column.getId())) {
        for (ViewColumn vCol : data.getDescendants(column.getId(), false)) {
          oldValue =
              BeeUtils.join(BeeConst.STRING_COMMA, oldValue, oldData.getString(data
                  .getColumnIndex(vCol.getName())));
          newValue =
              BeeUtils.join(BeeConst.STRING_COMMA, newValue, newData.getString(data
                  .getColumnIndex(vCol.getName())));
        }

      } else {
        oldValue = oldData.getString(data.getColumnIndex(column.getId()));
        newValue = newData.getString(data.getColumnIndex(column.getId()));
      }

      if (BeeUtils.same(column.getId(), COL_PRIORITY)) {
        oldValue = EnumUtils.getCaption(TaskPriority.class, BeeUtils.toInt(oldValue));
        newValue = EnumUtils.getCaption(TaskPriority.class, BeeUtils.toInt(newValue));
      }

      if (column.getType() == ValueType.DATE_TIME) {
        long oldMillis = BeeUtils.toLong(oldValue);
        oldValue = new DateTime(oldMillis).toString();
        long newMillis = BeeUtils.toLong(newValue);
        newValue = new DateTime(newMillis).toString();
      }

      oldDataMap.put(column.getId(), oldValue);
      newDataMap.put(column.getId(), newValue);
    }

    if (oldDataMap.isEmpty() && newDataMap.isEmpty()) {
      return;
    }

    Map<String, Map<String, String>> oldDataSent = Maps.newHashMap();
    Map<String, Map<String, String>> newDataSent = Maps.newHashMap();

    oldDataSent.put(VIEW_REQUESTS, oldDataMap);
    newDataSent.put(VIEW_REQUESTS, newDataMap);

    final FormView form = getFormView();

    if (form == null) {
      return;
    }

    IsRow row = form.getActiveRow();

    if (row == null) {
      return;
    }

    String comment = null;

    if (!BeeUtils.isNegative(form.getDataIndex(COL_COMMENT))) {
      comment = row.getString(form.getDataIndex(COL_COMMENT));
    }

    String newDataProp = Codec.beeSerialize(newDataSent);
    String oldDataProp = Codec.beeSerialize(oldDataSent);

    String prop = Codec.beeSerialize(Lists.newArrayList(oldDataProp, newDataProp));

    insertEventNote(event.getRowId(), comment, prop, TaskEvent.EDIT, NewRequestCommentForm
        .getEventRowCallback(null, event.getRowId(), null, null, null));

    super.onSaveChanges(listener, event);
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

  private static void createUpdateButton(final FormView form, final IsRow row, HeaderView header) {
    FaLabel updateRequestBtn = new FaLabel(FontAwesome.RETWEET);
    updateRequestBtn.setTitle(Localized.dictionary().actionUpdate());
    updateRequestBtn.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        updateRequest(form, row);
      }
    });

    header.addCommandItem(updateRequestBtn);
  }

  private static String getStorageKey(String name) {

    switch (name) {
      case NAME_REQUEST_TREE:
        return BeeUtils.join(BeeConst.STRING_MINUS, name, BeeKeeper.getUser().getUserId(),
            UiConstants.ATTR_SIZE);

      case NAME_ORDER:
        return BeeUtils.join(BeeConst.STRING_MINUS, name, BeeKeeper.getUser().getUserId());
    }
    return name;
  }

  private static void insertEventNote(long id, String comment, String prop, TaskEvent taskEvent,
      RowCallback callback) {
    Long currentUserId = BeeKeeper.getUser().getUserId();
    DateTime time = new DateTime();

    List<BeeColumn> columns =
        Data.getColumns(VIEW_REQUEST_EVENTS, Lists.newArrayList(COL_REQUEST, COL_PUBLISHER,
            COL_PUBLISH_TIME, COL_COMMENT, COL_EVENT, COL_EVENT_PROPERTIES));

    List<String> values =
        Lists.newArrayList(BeeUtils.toString(id),
            BeeUtils.toString(currentUserId), BeeUtils.toString(time.getTime()), comment, BeeUtils
                .toString(taskEvent.ordinal()), prop);

    Queries.insert(VIEW_REQUEST_EVENTS, columns, values, null, callback);
  }

  private static boolean readStorage(String name) {
    String key = getStorageKey(name);
    return BeeKeeper.getStorage().hasItem(key);
  }

  private void finishRequest(final FormView form, final IsRow row) {

    final TaskDialog dialog = new TaskDialog(Localized.dictionary().requestFinishing());

    final String cid = dialog.addComment(true);

    String durId = dialog.addTime(Localized.dictionary().crmSpentTime());
    String durTypeId = dialog.addSelector(Localized.dictionary().crmDurationType(),
        VIEW_REQUEST_DURATION_TYPES, Lists.newArrayList(ALS_DURATION_TYPE_NAME), false, null, null,
        COL_DURATION_TYPE);

    Filter filter = Filter.equals(COL_TASK_TYPE,
        row.getLong(Data.getColumnIndex(VIEW_REQUESTS, COL_REQUEST_TYPE)));

    dialog.getSelector(durTypeId).getOracle()
        .setAdditionalFilter(filter, true);

    final String did = dialog.addDateTime(Localized.dictionary().crmTaskFinishDate(), false,
        TimeUtils.nowMinutes());

    dialog.addAction(Localized.dictionary().actionSave(), new ScheduledCommand() {
      @Override
      public void execute() {

        final String comment = dialog.getComment(cid);
        final String time = dialog.getTime(durId);

        if (BeeUtils.isEmpty(comment)) {
          Global.showError(Localized.dictionary().error(), Collections.singletonList(Localized
              .dictionary().crmEnterComment()));
          return;
        }

        if (BeeUtils.isEmpty(time)) {
          Global.showError(Localized.dictionary().crmSpentTime() + " "
              + Localized.dictionary().valueRequired());
          return;
        }

        BeeRow reqDurTypes = dialog.getSelector(durTypeId).getRelatedRow();
        if (reqDurTypes == null) {
          Global.showError(Localized.dictionary().crmEnterDurationType());
          return;
        }
        final Long type = reqDurTypes
            .getLong(Data.getColumnIndex(VIEW_REQUEST_DURATION_TYPES, COL_DURATION_TYPE));

        final DateTime date = dialog.getDateTime(did);
        if (date == null) {
          Global.showError(Localized.dictionary().crmEnterDueDate());
          return;
        }

        List<BeeColumn> columns =
            Lists.newArrayList(DataUtils.getColumn(TaskConstants.COL_REQUEST_FINISHED, form
                .getDataColumns()));

        List<String> oldValues = Lists.newArrayList(row
            .getString(form.getDataIndex(TaskConstants.COL_REQUEST_FINISHED)));
        List<String> newValues =
            Lists.newArrayList(BeeUtils.toString(date.getTime()));

        columns.add(DataUtils.getColumn(TaskConstants.COL_REQUEST_RESULT, form.getDataColumns()));

        oldValues.add(row.getString(form.getDataIndex(TaskConstants.COL_REQUEST_RESULT)));
        newValues.add(comment);

        Queries.update(form.getViewName(), row.getId(), row.getVersion(), columns, oldValues,
            newValues, form.getChildrenForUpdate(), new RowCallback() {

              @Override
              public void onSuccess(BeeRow result) {
                finishRequestWithTask(result, time, type, comment, date);
                new FinishSaveCallback(form).onSuccess(result);
              }
            });
        dialog.close();
      }
    });

    dialog.display();
  }

  private void finishRequestWithTask(IsRow reqRow, String time, Long type, String comment,
      DateTime date) {
    FormView form = getFormView();
    boolean edited = (reqRow != null) && form.isEditing();

    if (!edited) {
      Global.showError(Localized.dictionary().actionCanNotBeExecuted());
      return;
    }

    final DataInfo taskDataInfo = Data.getDataInfo(TaskConstants.VIEW_TASKS);
    final BeeRow taskRow = RowFactory.createEmptyRow(taskDataInfo, true);

    Long user = BeeKeeper.getUser().getUserId();

    taskRow.setValue(taskDataInfo.getColumnIndex(COL_STATUS), TaskStatus.APPROVED.ordinal());

    String taskType = reqRow.getString(form.getDataIndex("TaskType"));
    taskRow.setValue(taskDataInfo.getColumnIndex(COL_TASK_TYPE), taskType);

    taskRow.setValue(taskDataInfo.getColumnIndex(ClassifierConstants.COL_COMPANY),
        reqRow.getLong(form.getDataIndex(COL_REQUEST_CUSTOMER)));

    taskRow.setValue(taskDataInfo.getColumnIndex(ClassifierConstants.COL_CONTACT), reqRow
        .getLong(form.getDataIndex(COL_REQUEST_CUSTOMER_PERSON)));

    taskRow.setValue(taskDataInfo.getColumnIndex(COL_DESCRIPTION), reqRow
        .getString(form.getDataIndex(COL_REQUEST_CONTENT)));

    taskRow.setValue(taskDataInfo.getColumnIndex(COL_SUMMARY), Localized
        .dictionary().crmRequest() + " " + reqRow.getId());

    taskRow.setValue(taskDataInfo.getColumnIndex(COL_EXECUTOR), user);

    taskRow.setValue(taskDataInfo.getColumnIndex(COL_OWNER), user);

    taskRow.setValue(taskDataInfo.getColumnIndex(COL_START_TIME), TimeUtils.nowMinutes());
    taskRow.setValue(taskDataInfo.getColumnIndex(COL_FINISH_TIME), TimeUtils.nowMinutes());

    if (!BeeUtils.isEmpty(reqRow.getString(form.getDataIndex(COL_PRODUCT)))) {
      taskRow.setValue(taskDataInfo.getColumnIndex(COL_PRODUCT), reqRow
          .getString(form.getDataIndex(COL_PRODUCT)));
    }

    taskRow.setProperty(PROP_EXECUTORS, user);
    taskRow.setProperty(PROP_REQUESTS, reqRow.getId());

    BeeRowSet rowSet = DataUtils.createRowSetForInsert(VIEW_TASKS, taskDataInfo.getColumns(),
        taskRow, Sets.newHashSet(COL_EXECUTOR, COL_STATUS), true);

    ParameterList params = TasksKeeper.createArgs(SVC_FINISH_REQUEST_WITH_TASK);
    params.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rowSet));
    params.addDataItem(VAR_TASK_DURATION_TYPE, type);
    params.addDataItem(VAR_TASK_DURATION_TIME, time);
    params.addDataItem(VAR_TASK_COMMENT, comment);
    params.addDataItem(VAR_TASK_DURATION_DATE, date.serialize());

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        if (!response.hasErrors()) {
          Map<String, String> data = Maps.newLinkedHashMap();
          data.put(BeeUtils.toString(TaskEvent.CREATE.ordinal()), BeeUtils.toString(BeeRow.restore(
              response.getResponseAsString()).getId()));

          insertEventNote(reqRow.getId(), comment, Codec.beeSerialize(data), TaskEvent.CREATE,
              null);
          form.refresh();
        }
      }
    });
  }

  private void drawComments(IsRow row) {
    final Flow rqstComments = getRequestComments();
    if (rqstComments == null) {
      logger.warning("Widget of request comments not found");
      return;
    }

    if (eventsHandler == null) {
      logger.warning("Events handler not initialized");
      return;
    }

    rqstComments.clear();

    if (!DataUtils.isId(row.getId())) {
      return;
    }

    EventFilesFilter filter = new EventFilesFilter(VIEW_REQUEST_FILES,
        COL_REQUEST_EVENT, AdministrationConstants.COL_FILE, AdministrationConstants.ALS_FILE_NAME,
        AdministrationConstants.ALS_FILE_SIZE, AdministrationConstants.ALS_FILE_TYPE, COL_CAPTION);

    eventsHandler.create(rqstComments, row.getId(), filter);
  }

  private Flow getRequestComments() {
    return requestComments;
  }

  private static void toTaskAndFinish(final FormView form, final IsRow reqRow) {
    boolean edited = (reqRow != null) && form.isEditing();

    if (!edited) {
      Global.showError(Localized.dictionary().actionCanNotBeExecuted());
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

    taskRow.setProperty(PROP_REQUESTS, reqRow.getId());
    RowFactory.createRow(taskDataInfo.getNewRowForm(), null, taskDataInfo, taskRow,
        Modality.ENABLED, null,
        new TaskBuilder(files, BeeUtils.toLongOrNull(managerSel.getValue()), true), null,
        new RowCallback() {

          @Override
          public void onSuccess(BeeRow result) {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put(BeeUtils.toString(TaskEvent.CREATE.ordinal()), result.getString(0));

            insertEventNote(reqRow.getId(), null, Codec.beeSerialize(data), TaskEvent.CREATE, null);

            int idxFinished = form.getDataIndex(TaskConstants.COL_REQUEST_FINISHED);

            List<BeeColumn> columns =
                Lists.newArrayList(Data.getColumn(VIEW_REQUESTS, COL_REQUEST_FINISHED));
            List<String> oldValues = Arrays.asList(reqRow.getString(idxFinished));
            List<String> newValues = Arrays.asList(BeeUtils.toString(new DateTime().getTime()));

            Queries.update(form.getViewName(), reqRow.getId(), reqRow.getVersion(),
                columns, oldValues, newValues, form.getChildrenForUpdate(),
                new FinishSaveCallback(form));
          }
        });
  }

  private static void updateRequest(final FormView form, final IsRow row) {
    Global.confirm(Localized.dictionary().requestUpdatingQuestion(), new ConfirmationCallback() {

      @Override
      public void onConfirm() {
        List<BeeColumn> columns = Lists.newArrayList(DataUtils
            .getColumn(TaskConstants.COL_REQUEST_FINISHED, form.getDataColumns()));
        List<String> oldValues = Lists.newArrayList(row
            .getString(form.getDataIndex(TaskConstants.COL_REQUEST_FINISHED)));

        List<String> newValues = new ArrayList<>();
        newValues.add(null);

        Queries.update(form.getViewName(), row.getId(), row.getVersion(), columns, oldValues,
            newValues, form.getChildrenForUpdate(), new FinishSaveCallback(form));

        String comment =
            Localized.dictionary().discussStatus() + ": "
                + Localized.dictionary().crmTaskEventRenewed();

        insertEventNote(row.getId(), comment, null, TaskEvent.RENEW, null);
      }
    });

  }
}
