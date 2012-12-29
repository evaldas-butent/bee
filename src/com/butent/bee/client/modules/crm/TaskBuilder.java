package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.InputTime;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

class TaskBuilder extends AbstractFormInterceptor {

  private static final String NAME_START_DATE = "Start_Date";
  private static final String NAME_START_TIME = "Start_Time";
  private static final String NAME_END_DATE = "End_Date";
  private static final String NAME_END_TIME = "End_Time";

  private static final String NAME_FILES = "Files";

  TaskBuilder() {
    super();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    if (widget instanceof FileCollector) {
      ((FileCollector) widget).bindDnd(getFormView(), getFormView().asWidget().getElement());
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new TaskBuilder();
  }

  @Override
  public void onReadyForInsert(final ReadyForInsertEvent event) {
    event.consume();
    
    IsRow activeRow = getFormView().getActiveRow();

    DateTime start = getStart();
    DateTime end = getEnd(start, Data.getString(VIEW_TASKS, activeRow, COL_EXPECTED_DURATION));

    if (end == null) {
      event.getCallback().onFailure("Įveskite pabaigos laiką arba",
          "pradžios laiką ir numatomą trukmę");
      return;
    }
    if (start != null && TimeUtils.isLeq(end, start)) {
      event.getCallback().onFailure("Pabaigos laikas turi būti didesnis už pradžios laiką");
      return;
    }

    if (Data.isNull(VIEW_TASKS, activeRow, COL_SUMMARY)) {
      event.getCallback().onFailure("Įveskite temą");
      return;
    }

    if (BeeUtils.isEmpty(activeRow.getProperty(PROP_EXECUTORS))) {
      event.getCallback().onFailure("Pasirinkite vykdytoją");
      return;
    }

    BeeRow newRow = DataUtils.cloneRow(activeRow);

    if (start != null) {
      Data.setValue(VIEW_TASKS, newRow, COL_START_TIME, start);
    }
    Data.setValue(VIEW_TASKS, newRow, COL_FINISH_TIME, end);

    BeeRowSet rowSet = Queries.createRowSetForInsert(VIEW_TASKS, getFormView().getDataColumns(),
        newRow, Sets.newHashSet(COL_EXECUTOR, COL_STATUS, COL_START_TIME), true);

    ParameterList args = CrmKeeper.createTaskRequestParameters(TaskEvent.CREATE);
    args.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rowSet));

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasErrors()) {
          event.getCallback().onFailure(response.getErrors());

        } else if (response.hasResponse(String.class)) {
          List<Long> tasks = DataUtils.parseIdList((String) response.getResponse());
          if (tasks.isEmpty()) {
            event.getCallback().onFailure("No tasks created");
            return;
          }

          clearValue(NAME_START_DATE);
          clearValue(NAME_START_TIME);
          clearValue(NAME_END_DATE);
          clearValue(NAME_END_TIME);

          createFiles(tasks);

          event.getCallback().onSuccess(null);
          
          String message = BeeUtils.joinWords("Sukurta naujų užduočių:", tasks.size());
          GridView gridView = getGridView();
          if (gridView != null) {
            gridView.notifyInfo(message);
            gridView.getViewPresenter().handleAction(Action.REFRESH);
          } else {
            BeeKeeper.getScreen().notifyInfo(message);
          }

        } else {
          event.getCallback().onFailure("Unknown response");
        }
      }
    });
  }

  private void clearValue(String widgetName) {
    Widget widget = getFormView().getWidgetByName(widgetName);
    if (widget instanceof Editor) {
      ((Editor) widget).clearValue();
    }
  }

  private void createFiles(final List<Long> tasks) {
    Widget widget = getFormView().getWidgetByName(NAME_FILES);

    if (widget instanceof FileCollector && !((FileCollector) widget).isEmpty()) {
      List<NewFileInfo> files = Lists.newArrayList(((FileCollector) widget).getFiles());

      final List<BeeColumn> columns = Data.getColumns(VIEW_TASK_FILES,
          Lists.newArrayList(COL_TASK, COL_FILE, COL_CAPTION));

      for (final NewFileInfo fileInfo : files) {
        FileUtils.upload(fileInfo, new Callback<Long>() {
          @Override
          public void onSuccess(Long result) {
            for (long taskId : tasks) {
              List<String> values = Lists.newArrayList(BeeUtils.toString(taskId),
                  BeeUtils.toString(result), fileInfo.getCaption());
              Queries.insert(VIEW_TASK_FILES, columns, values, null);
            }
          }
        });
      }

      ((FileCollector) widget).clear();
    }
  }

  private HasDateValue getDate(String widgetName) {
    Widget widget = getFormView().getWidgetByName(widgetName);
    if (widget instanceof InputDate) {
      return ((InputDate) widget).getDate();
    } else {
      return null;
    }
  }

  private DateTime getEnd(DateTime start, String duration) {
    HasDateValue datePart = getDate(NAME_END_DATE);
    if (datePart != null) {
      return TimeUtils.combine(datePart, getMillis(NAME_END_TIME));
    }

    if (start != null) {
      Long millis = TimeUtils.parseTime(duration);
      if (BeeUtils.isPositive(millis)) {
        return TimeUtils.combine(start, millis);
      }
    }
    return null;
  }

  private Long getMillis(String widgetName) {
    Widget widget = getFormView().getWidgetByName(widgetName);
    if (widget instanceof InputTime) {
      return ((InputTime) widget).getMillis();
    } else {
      return null;
    }
  }

  private DateTime getStart() {
    HasDateValue datePart = getDate(NAME_START_DATE);
    if (datePart == null) {
      return null;
    } else {
      return TimeUtils.combine(datePart, getMillis(NAME_START_TIME));
    }
  }
}
