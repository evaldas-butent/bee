package com.butent.bee.client.modules.crm;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllDragAndDropHandlers;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.composite.FileGroup;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.composite.InputTime;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.render.RendererFactory;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

class TaskEditor extends AbstractFormInterceptor {

  private static class TaskDialog extends DialogBox {

    private static final String STYLE_DIALOG = CRM_STYLE_PREFIX + "taskDialog";
    private static final String STYLE_CELL = "Cell";

    private TaskDialog(String caption) {
      super(caption, STYLE_DIALOG);
      addDefaultCloseBox();

      HtmlTable container = new HtmlTable();
      container.addStyleName(STYLE_DIALOG + "-container");

      setWidget(container);
    }

    private void addAction(String caption, ScheduledCommand command) {
      String styleName = STYLE_DIALOG + "-action";

      BeeButton button = new BeeButton(caption, command);
      button.addStyleName(styleName);

      HtmlTable table = getContainer();
      int row = table.getRowCount();
      int col = 0;

      table.setWidget(row, col, button);

      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);
      table.getCellFormatter().setHorizontalAlignment(row, col,
          HasHorizontalAlignment.ALIGN_CENTER);

      table.getCellFormatter().setColSpan(row, col, 2);
    }

    private String addComment(boolean required) {
      String styleName = STYLE_DIALOG + "-commentLabel";
      BeeLabel label = new BeeLabel("Komentaras:");
      label.addStyleName(styleName);
      if (required) {
        label.addStyleName(StyleUtils.NAME_REQUIRED);
      }

      HtmlTable table = getContainer();
      int row = table.getRowCount();
      int col = 0;

      table.setWidget(row, col, label);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);
      col++;

      InputArea input = new InputArea();
      styleName = STYLE_DIALOG + "-commentArea";
      input.addStyleName(styleName);

      table.setWidget(row, col, input);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);

      return input.getId();
    }

    private String addDate(String caption, boolean required, DateTime def) {
      HtmlTable table = getContainer();
      int row = table.getRowCount();
      int col = 0;

      String styleName = STYLE_DIALOG + "-dateLabel";
      BeeLabel label = new BeeLabel(caption);
      label.addStyleName(styleName);
      if (required) {
        label.setStyleName(StyleUtils.NAME_REQUIRED);
      }

      table.setWidget(row, col, label);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);
      col++;

      styleName = STYLE_DIALOG + "-dateInput";
      InputDate input = new InputDate(ValueType.DATETIME);
      input.setDateTimeFormat(Format.getDefaultDateTimeFormat());
      input.addStyleName(styleName);

      if (def != null) {
        input.setDate(def);
      }

      table.setWidget(row, col, input);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);

      return input.getId();
    }

    private Map<String, String> addDuration() {
      Map<String, String> result = Maps.newHashMap();

      result.put(COL_DURATION, addTime("Sugaištas laikas:"));
      result.put(COL_DURATION_TYPE, addSelector("Darbo tipas:", VIEW_DURATION_TYPES,
          Lists.newArrayList(COL_NAME), false));
      result.put(COL_DURATION_DATE, addDate("Atlikimo data:", false, TimeUtils.nowMinutes()));

      return result;
    }

    private String addFileCollector() {
      HtmlTable table = getContainer();
      int row = table.getRowCount();
      int col = 0;

      String styleName = STYLE_DIALOG + "-filesLabel";
      BeeLabel label = new BeeLabel("Bylos:");
      label.addStyleName(styleName);

      table.setWidget(row, col, label);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);
      col++;

      styleName = STYLE_DIALOG + "-fileCollector";
      FileCollector collector = new FileCollector(new BeeImage(Global.getImages().attachment()));
      collector.addStyleName(styleName);

      table.setWidget(row, col, collector);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);

      Widget panel = getWidget();
      if (panel instanceof HasAllDragAndDropHandlers) {
        collector.bindDnd((HasAllDragAndDropHandlers) panel, panel.getElement());
      }

      return collector.getId();
    }

    private String addSelector(String caption, String relView, List<String> relColumns,
        boolean required) {
      HtmlTable table = getContainer();
      int row = table.getRowCount();
      int col = 0;

      String styleName = STYLE_DIALOG + "-selectorLabel";
      BeeLabel label = new BeeLabel(caption);
      label.addStyleName(styleName);
      if (required) {
        label.setStyleName(StyleUtils.NAME_REQUIRED);
      }

      table.setWidget(row, col, label);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);
      col++;

      styleName = STYLE_DIALOG + "-selectorInput";
      DataSelector selector = new DataSelector(Relation.create(relView, relColumns), true);
      selector.addSimpleHandler(RendererFactory.createRenderer(relView, relColumns));
      selector.addStyleName(styleName);

      table.setWidget(row, col, selector);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);

      return selector.getId();
    }

    private String addTime(String caption) {
      HtmlTable table = getContainer();
      int row = table.getRowCount();
      int col = 0;

      String styleName = STYLE_DIALOG + "-timeLabel";
      BeeLabel label = new BeeLabel(caption);
      label.addStyleName(styleName);

      table.setWidget(row, col, label);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);
      col++;

      styleName = STYLE_DIALOG + "-timeInput";
      InputTime input = new InputTime(ValueType.TEXT);
      input.addStyleName(styleName);

      table.setWidget(row, col, input);
      table.getCellFormatter().addStyleName(row, col, styleName + STYLE_CELL);

      return input.getId();
    }

    private void display() {
      center();
      UiHelper.focus(getContent());
    }

    private Widget getChild(String id) {
      return DomUtils.getChildQuietly(getContent(), id);
    }

    private String getComment(String id) {
      Widget child = getChild(id);
      if (child instanceof InputArea) {
        return ((InputArea) child).getValue();
      } else {
        return null;
      }
    }

    private HtmlTable getContainer() {
      return (HtmlTable) getContent();
    }

    private HasDateValue getDate(String id) {
      Widget child = getChild(id);
      if (child instanceof InputDate) {
        return ((InputDate) child).getDate();
      } else {
        return null;
      }
    }

    private List<NewFileInfo> getFiles(String id) {
      Widget child = getChild(id);
      if (child instanceof FileCollector) {
        return ((FileCollector) child).getFiles();
      } else {
        return Lists.newArrayList();
      }
    }

    private Long getSelector(String id) {
      Widget child = getChild(id);
      if (child instanceof DataSelector) {
        return ((DataSelector) child).getRelatedId();
      } else {
        return null;
      }
    }

    private String getTime(String id) {
      Widget child = getChild(id);
      if (child instanceof InputTime) {
        return ((InputTime) child).getValue();
      } else {
        return null;
      }
    }
  }

  private static final String STYLE_EVENT = CRM_STYLE_PREFIX + "taskEvent-";
  private static final String STYLE_EVENT_ROW = STYLE_EVENT + "row";
  private static final String STYLE_EVENT_COL = STYLE_EVENT + "col-";
  private static final String STYLE_EVENT_FILES = STYLE_EVENT + "files";

  private static final String STYLE_DURATION = CRM_STYLE_PREFIX + "taskDuration-";
  private static final String STYLE_DURATION_CELL = "Cell";

  private final List<String> relations = Lists.newArrayList(PROP_COMPANIES, PROP_PERSONS,
      PROP_APPOINTMENTS, PROP_TASKS);

  private final long userId;

  TaskEditor() {
    super();
    this.userId = BeeKeeper.getUser().getUserId();
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    if (row == null) {
      return;
    }

    Integer status = row.getInteger(form.getDataIndex(COL_STATUS));
    Long owner = row.getLong(form.getDataIndex(COL_OWNER));
    Long executor = row.getLong(form.getDataIndex(COL_EXECUTOR));

    for (final TaskEvent event : TaskEvent.values()) {
      String label = event.getCommandLabel();

      if (!BeeUtils.isEmpty(label) && isEventEnabled(event, status, owner, executor)) {
        header.addCommandItem(new BeeButton(label, new ClickHandler() {
          @Override
          public void onClick(ClickEvent e) {
            doEvent(event);
          }
        }));
      }
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new TaskEditor();
  }

  @Override
  public void onClose(List<String> messages, IsRow oldRow, IsRow newRow) {
    List<String> captions = Lists.newArrayList();

    if (!DataUtils.sameIdSet(oldRow.getProperty(PROP_OBSERVERS),
        newRow.getProperty(PROP_OBSERVERS))) {
      captions.add("Stebėtojai");
    }
    
    List<String> updatedRelations = getUpdatedRelations(oldRow, newRow);
    for (String relation : updatedRelations) {
      DataInfo dataInfo = Data.getDataInfo(relation, false);
      
      String caption = (dataInfo == null) ? relation 
          : BeeUtils.notEmpty(LocaleUtils.maybeLocalize(dataInfo.getCaption()), relation);
      captions.add(caption);
    }
    
    if (!captions.isEmpty()) {
      String join = BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR, captions);
      if (messages.isEmpty()) {
        messages.add(BeeUtils.joinWords(Global.CONSTANTS.changedValues(), join));
      } else {
        messages.add(join);
      }
    }
  }

  @Override
  public void onSaveChanges(SaveChangesEvent event) {
    IsRow oldRow = event.getOldRow();
    IsRow newRow = event.getNewRow();
    
    if (oldRow == null || newRow == null) {
      return;
    }

    if (DataUtils.sameIdSet(oldRow.getProperty(PROP_OBSERVERS), newRow.getProperty(PROP_OBSERVERS))
        && getUpdatedRelations(oldRow, newRow).isEmpty()) {
      return;
    }
    
    event.setConsumed(true);
    
    ParameterList params = createRequestParams(TaskEvent.EDIT, null);
    sendRequest(params, new Callback<ResponseObject>() {
      @Override
      public void onSuccess(ResponseObject result) {
        if (!Queries.checkResponse(TaskEvent.EDIT.getCaption(), VIEW_TASKS, result,
            BeeRow.class, this)) {
          return;
        }

        BeeRow data = BeeRow.restore((String) result.getResponse());
        if (data == null) {
          onFailure(TaskEvent.EDIT.name(), VIEW_TASKS, "cannot restore row");
          return;
        }

        BeeKeeper.getBus().fireEvent(new RowUpdateEvent(VIEW_TASKS, data));
      }
    });
  }

  @Override
  public boolean onStartEdit(final FormView form, final IsRow row, ScheduledCommand focusCommand) {

    Long owner = row.getLong(form.getDataIndex(COL_OWNER));
    Long executor = row.getLong(form.getDataIndex(COL_EXECUTOR));

    Integer oldStatus = row.getInteger(form.getDataIndex(COL_STATUS));

    form.setEnabled(Objects.equal(owner, userId));

    BeeRowSet rowSet = new BeeRowSet(form.getViewName(), form.getDataColumns());
    rowSet.addRow(DataUtils.cloneRow(row));

    ParameterList params = CrmKeeper.createTaskRequestParameters(TaskEvent.VISIT);
    params.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rowSet));

    if (Objects.equal(executor, userId)
        && Objects.equal(oldStatus, TaskStatus.NOT_VISITED.ordinal())) {
      rowSet.getRow(0).preliminaryUpdate(rowSet.getColumnIndex(COL_STATUS),
          BeeUtils.toString(TaskStatus.ACTIVE.ordinal()));
    }

    sendRequest(params, new Callback<ResponseObject>() {
      @Override
      public void onFailure(String... reason) {
        form.updateRow(row, true);
        form.notifySevere(reason);
      }

      @Override
      public void onSuccess(ResponseObject result) {
        if (!Queries.checkResponse(TaskEvent.VISIT.getCaption(), VIEW_TASKS, result,
            BeeRow.class, this)) {
          return;
        }

        BeeRow data = BeeRow.restore((String) result.getResponse());
        if (data == null) {
          onFailure(TaskEvent.VISIT.name(), VIEW_TASKS, "cannot restore row");
          return;
        }

        BeeKeeper.getBus().fireEvent(new RowUpdateEvent(VIEW_TASKS, data));

        Widget fileWidget = form.getWidgetByName(PROP_FILES);
        if (fileWidget instanceof FileGroup) {
          ((FileGroup) fileWidget).clear();
        }

        List<StoredFile> files = StoredFile.restoreCollection(data.getProperty(PROP_FILES));
        if (!files.isEmpty()) {
          if (fileWidget instanceof FileGroup) {
            for (StoredFile file : files) {
              if (file.getRelatedId() == null) {
                ((FileGroup) fileWidget).addFile(file);
              }
            }
          }
        }

        String events = data.getProperty(PROP_EVENTS);
        if (!BeeUtils.isEmpty(events)) {
          showEventsAndDuration(form, BeeRowSet.restore(events), files);
        }

        form.updateRow(data, true);
      }
    });
    return false;
  }

  private void addDurationCell(HtmlTable display, int row, int col, String value, String style) {
    Widget widget = new CustomDiv(STYLE_DURATION + style);
    if (!BeeUtils.isEmpty(value)) {
      widget.getElement().setInnerText(value);
    }

    display.setWidget(row, col, widget, STYLE_DURATION + style + STYLE_DURATION_CELL);
  }

  private Widget createEventCell(String colName, String value) {
    Widget widget = new CustomDiv(STYLE_EVENT + colName);
    if (!BeeUtils.isEmpty(value)) {
      widget.getElement().setInnerText(value);
    }
    return widget;
  }

  private ParameterList createRequestParams(TaskEvent event, List<String> colNames,
      List<String> newValues, String comment) {

    FormView form = getFormView();
    String viewName = form.getViewName();

    BeeRowSet rowSet = new BeeRowSet(viewName, form.getDataColumns());
    BeeRow row = DataUtils.cloneRow(form.getActiveRow());

    if (form.isEnabled()) {
      BeeRowSet updated = DataUtils.getUpdated(viewName, form.getDataColumns(), form.getOldRow(),
          form.getActiveRow());
      if (!DataUtils.isEmpty(updated)) {
        for (int i = 0; i < updated.getNumberOfColumns(); i++) {
          int index = rowSet.getColumnIndex(updated.getColumnId(i));

          row.setValue(index, form.getOldRow().getString(index));
          row.preliminaryUpdate(index, form.getActiveRow().getString(index));
        }
      }
    }

    for (int i = 0; i < colNames.size(); i++) {
      row.preliminaryUpdate(rowSet.getColumnIndex(colNames.get(i)), newValues.get(i));
    }

    rowSet.addRow(row);

    ParameterList params = CrmKeeper.createTaskRequestParameters(event);
    params.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rowSet));

    if (!BeeUtils.isEmpty(comment)) {
      params.addDataItem(VAR_TASK_COMMENT, comment);
    }

    if (form.isEnabled()) {
      List<String> updatedRelations = getUpdatedRelations(form.getOldRow(), form.getActiveRow());
      if (!updatedRelations.isEmpty()) {
        params.addDataItem(VAR_TASK_RELATIONS, NameUtils.join(updatedRelations));
      }
    }

    return params;
  }

  private ParameterList createRequestParams(TaskEvent event, String comment) {
    List<String> list = Lists.newArrayList();
    return createRequestParams(event, list, list, comment);
  }

  private ParameterList createRequestParams(TaskEvent event, String colName, String newValue,
      String comment) {
    return createRequestParams(event, Lists.newArrayList(colName), Lists.newArrayList(newValue),
        comment);
  }

  private void doActivate() {
    final TaskDialog dialog = new TaskDialog("Užduoties perdavimas vykdymui");

    final String cid = dialog.addComment(false);

    dialog.addAction("Perduoti vykdymui", new ScheduledCommand() {
      @Override
      public void execute() {

        String value = BeeUtils.toString(TaskStatus.ACTIVE.ordinal());
        ParameterList params = createRequestParams(TaskEvent.ACTIVATE, COL_STATUS, value,
            dialog.getComment(cid));

        sendRequest(params, TaskEvent.ACTIVATE);
        dialog.hide();
      }
    });

    dialog.display();
  }

  private void doApprove() {
    final TaskDialog dialog = new TaskDialog("Užduoties patvirtinimas");

    final String did = dialog.addDate("Patvirtinimo data:", true, TimeUtils.nowMinutes());
    final String cid = dialog.addComment(false);

    dialog.addAction("Patvirtinti", new ScheduledCommand() {
      @Override
      public void execute() {

        HasDateValue approved = dialog.getDate(did);
        if (approved == null) {
          showError("Įveskite patvirtinimo datą");
          return;
        }

        String value = approved.serialize();
        ParameterList params = createRequestParams(TaskEvent.APPROVE, COL_APPROVED, value,
            dialog.getComment(cid));

        sendRequest(params, TaskEvent.APPROVE);
        dialog.hide();
      }
    });

    dialog.display();
  }

  private void doCancel() {
    final TaskDialog dialog = new TaskDialog("Užduoties nutraukimas");

    final String cid = dialog.addComment(true);

    dialog.addAction("Nutraukti", new ScheduledCommand() {
      @Override
      public void execute() {

        String comment = dialog.getComment(cid);
        if (BeeUtils.isEmpty(comment)) {
          showError("Įveskite komentarą");
          return;
        }

        String value = BeeUtils.toString(TaskStatus.CANCELED.ordinal());
        ParameterList params = createRequestParams(TaskEvent.CANCEL, COL_STATUS, value, comment);

        sendRequest(params, TaskEvent.CANCEL);
        dialog.hide();
      }
    });

    dialog.display();
  }

  private void doComment() {
    final TaskDialog dialog = new TaskDialog("Užduoties komentaras, laiko registracija");

    final String cid = dialog.addComment(true);
    final String fid = dialog.addFileCollector();

    final Map<String, String> durIds = dialog.addDuration();

    dialog.addAction("Išsaugoti", new ScheduledCommand() {
      @Override
      public void execute() {

        String comment = dialog.getComment(cid);
        if (BeeUtils.isEmpty(comment)) {
          showError("Įveskite komentarą");
          return;
        }

        final long taskId = getTaskId();

        ParameterList params = createRequestParams(TaskEvent.COMMENT, comment);

        if (setDurationParams(dialog, durIds, params)) {
          final List<NewFileInfo> files = dialog.getFiles(fid);

          dialog.hide();

          sendRequest(params, new Callback<ResponseObject>() {
            @Override
            public void onFailure(String... reason) {
              getFormView().notifySevere(reason);
            }

            @Override
            public void onSuccess(ResponseObject result) {
              if (!Queries.checkResponse(TaskEvent.COMMENT.getCaption(), VIEW_TASKS, result,
                  BeeRow.class, this)) {
                return;
              }

              BeeRow row = BeeRow.restore((String) result.getResponse());
              if (row == null) {
                onFailure(TaskEvent.COMMENT.getCaption(), VIEW_TASKS, "cannot restore row");
                return;
              }

              onResponse(row);

              Long teId = BeeUtils.toLongOrNull(row.getProperty(PROP_LAST_EVENT_ID));
              if (DataUtils.isId(teId) && !files.isEmpty()) {
                sendFiles(files, taskId, teId);
              }
            }
          });
        }
      }
    });

    dialog.display();
  }

  private void doComplete() {
    final TaskDialog dialog = new TaskDialog("Užduoties užbaigimas");

    final String did = dialog.addDate("Įvykdymo data:", true, TimeUtils.nowMinutes());
    final String cid = dialog.addComment(true);

    final Map<String, String> durIds = dialog.addDuration();

    dialog.addAction("Užbaigti", new ScheduledCommand() {
      @Override
      public void execute() {

        HasDateValue completed = dialog.getDate(did);
        if (completed == null) {
          showError("Įveskite įvykdymo datą");
          return;
        }

        String comment = dialog.getComment(cid);
        if (BeeUtils.isEmpty(comment)) {
          showError("Įveskite komentarą");
          return;
        }

        List<String> colNames = Lists.newArrayList(COL_STATUS, COL_COMPLETED);
        List<String> values = Lists.newArrayList(BeeUtils.toString(TaskStatus.COMPLETED.ordinal()),
            completed.serialize());

        ParameterList params = createRequestParams(TaskEvent.COMPLETE, colNames, values, comment);

        if (setDurationParams(dialog, durIds, params)) {
          sendRequest(params, TaskEvent.COMPLETE);
          dialog.hide();
        }
      }
    });

    dialog.display();
  }

  private void doEvent(TaskEvent event) {
    if (!isEventEnabled(event, getStatus(), getOwner(), getExecutor())) {
      showError("Veiksmas neleidžiamas");
    }

    switch (event) {
      case COMMENT:
        doComment();
        break;

      case FORWARD:
        doForward();
        break;

      case EXTEND:
        doExtend();
        break;

      case SUSPEND:
        doSuspend();
        break;

      case CANCEL:
        doCancel();
        break;

      case COMPLETE:
        doComplete();
        break;

      case APPROVE:
        doApprove();
        break;

      case RENEW:
        doRenew();
        break;

      case ACTIVATE:
        doActivate();
        break;

      case CREATE:
      case VISIT:
      case EDIT:
        Assert.untouchable();
    }
  }

  private void doExtend() {
    final TaskDialog dialog = new TaskDialog("Užduoties termino keitimas");

    final String did = dialog.addDate("Naujas terminas:", true, null);
    final String cid = dialog.addComment(false);

    dialog.addAction("Keisti terminą", new ScheduledCommand() {
      @Override
      public void execute() {

        HasDateValue newTerm = dialog.getDate(did);
        if (newTerm == null) {
          showError("Įveskite terminą");
          return;
        }

        DateTime oldTerm = getDateTime(COL_FINISH_TIME);
        if (Objects.equal(newTerm, oldTerm)) {
          showError("Terminas nepakeistas");
          return;
        }

        DateTime start = getDateTime(COL_START_TIME);
        if (start != null && TimeUtils.isLeq(newTerm, start)) {
          showError("Terminas turi būti didesnis už pradžios datą");
          return;
        }

        if (TimeUtils.isLess(newTerm, TimeUtils.nowMinutes())) {
          showError("Time travel not supported");
          return;
        }

        String value = newTerm.serialize();
        ParameterList params = createRequestParams(TaskEvent.EXTEND, COL_FINISH_TIME, value,
            dialog.getComment(cid));

        sendRequest(params, TaskEvent.EXTEND);
        dialog.hide();
      }
    });

    dialog.display();
  }

  private void doForward() {
    final Long oldUser = getExecutor();

    final TaskDialog dialog = new TaskDialog("Užduoties persiuntimas");

    final String sid = dialog.addSelector("Vykdytojas:", CommonsConstants.VIEW_USERS,
        Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME), true);

    final String cid = dialog.addComment(true);

    dialog.addAction("Persiųsti", new ScheduledCommand() {
      @Override
      public void execute() {

        Long newUser = dialog.getSelector(sid);
        if (newUser == null) {
          showError("Įveskite vykdytoją");
          return;
        }
        if (Objects.equal(newUser, oldUser)) {
          showError("Nurodėte tą patį vykdytoją");
          return;
        }

        String comment = dialog.getComment(cid);
        if (BeeUtils.isEmpty(comment)) {
          showError("Įveskite komentarą");
          return;
        }

        String value = BeeUtils.toString(newUser);
        ParameterList params = createRequestParams(TaskEvent.FORWARD, COL_EXECUTOR, value, comment);

        sendRequest(params, TaskEvent.FORWARD);
        dialog.hide();
      }
    });

    dialog.display();
  }

  private void doRenew() {
    final TaskDialog dialog = new TaskDialog("Užduoties grąžinimas vykdymui");

    final String cid = dialog.addComment(false);

    dialog.addAction("Grąžinti vykdymui", new ScheduledCommand() {
      @Override
      public void execute() {

        TaskStatus newStatus = isExecutor() ? TaskStatus.ACTIVE : TaskStatus.NOT_VISITED;

        List<String> colNames = Lists.newArrayList(COL_STATUS);
        List<String> values = Lists.newArrayList(BeeUtils.toString(newStatus.ordinal()));

        if (getDateTime(COL_COMPLETED) == null) {
          colNames.add(COL_COMPLETED);
          values.add(null);
        }
        if (getDateTime(COL_APPROVED) == null) {
          colNames.add(COL_APPROVED);
          values.add(null);
        }

        ParameterList params = createRequestParams(TaskEvent.RENEW, colNames, values,
            dialog.getComment(cid));

        sendRequest(params, TaskEvent.RENEW);
        dialog.hide();
      }
    });

    dialog.display();
  }

  private void doSuspend() {
    final TaskDialog dialog = new TaskDialog("Užduoties sustabdymas");

    final String cid = dialog.addComment(true);

    dialog.addAction("Sustabdyti", new ScheduledCommand() {
      @Override
      public void execute() {

        String comment = dialog.getComment(cid);
        if (BeeUtils.isEmpty(comment)) {
          showError("Įveskite komentarą");
          return;
        }

        String value = BeeUtils.toString(TaskStatus.SUSPENDED.ordinal());
        ParameterList params = createRequestParams(TaskEvent.SUSPEND, COL_STATUS, value, comment);

        sendRequest(params, TaskEvent.SUSPEND);
        dialog.hide();
      }
    });

    dialog.display();
  }

  private List<StoredFile> filterEventFiles(List<StoredFile> input, long teId) {
    if (input.isEmpty()) {
      return input;
    }
    List<StoredFile> result = Lists.newArrayList();

    for (StoredFile file : input) {
      Long id = file.getRelatedId();
      if (id != null && id == teId) {
        result.add(file);
      }
    }
    return result;
  }

  private DateTime getDateTime(String colName) {
    return getFormView().getActiveRow().getDateTime(getFormView().getDataIndex(colName));
  }

  private Long getExecutor() {
    return getLong(COL_EXECUTOR);
  }

  private Long getLong(String colName) {
    return getFormView().getActiveRow().getLong(getFormView().getDataIndex(colName));
  }

  private Long getOwner() {
    return getLong(COL_OWNER);
  }

  private Integer getStatus() {
    return getFormView().getActiveRow().getInteger(getFormView().getDataIndex(COL_STATUS));
  }

  private long getTaskId() {
    return getFormView().getActiveRow().getId();
  }

  private List<String> getUpdatedRelations(IsRow oldRow, IsRow newRow) {
    List<String> updatedRelations = Lists.newArrayList();
    if (oldRow == null || newRow == null) {
      return updatedRelations;
    }

    for (String relation : relations) {
      if (!DataUtils.sameIdSet(oldRow.getProperty(relation), newRow.getProperty(relation))) {
        updatedRelations.add(relation);
      }
    }
    return updatedRelations;
  }

  private boolean isEventEnabled(TaskEvent event, Integer status, Long owner, Long executor) {
    if (event == null || status == null || owner == null || executor == null) {
      return false;
    }

    if (userId != owner) {
      if (event == TaskEvent.COMMENT) {
        return true;
      } else if (userId == executor && status == TaskStatus.ACTIVE.ordinal()) {
        return (event == TaskEvent.FORWARD || event == TaskEvent.COMPLETE);
      } else {
        return false;
      }
    }

    switch (event) {
      case COMMENT:
        return true;

      case RENEW:
        return TaskStatus.in(status, TaskStatus.SUSPENDED, TaskStatus.CANCELED,
            TaskStatus.COMPLETED);

      case FORWARD:
        return TaskStatus.in(status, TaskStatus.NOT_VISITED, TaskStatus.ACTIVE,
            TaskStatus.SCHEDULED);

      case EXTEND:
      case SUSPEND:
      case COMPLETE:
        return TaskStatus.in(status, TaskStatus.NOT_VISITED, TaskStatus.ACTIVE);

      case CANCEL:
        return TaskStatus.in(status, TaskStatus.NOT_VISITED, TaskStatus.ACTIVE,
            TaskStatus.SUSPENDED, TaskStatus.SCHEDULED);

      case APPROVE:
        return status == TaskStatus.COMPLETED.ordinal() && userId != executor;

      case ACTIVATE:
        return TaskStatus.in(status, TaskStatus.NOT_VISITED, TaskStatus.SCHEDULED);

      case CREATE:
      case EDIT:
      case VISIT:
        return false;
    }

    return false;
  }

  private boolean isExecutor() {
    return Objects.equal(userId, getExecutor());
  }

  private void onResponse(BeeRow data) {
    BeeKeeper.getBus().fireEvent(new RowUpdateEvent(VIEW_TASKS, data));

    FormView form = getFormView();

    String events = data.getProperty(PROP_EVENTS);
    if (!BeeUtils.isEmpty(events)) {
      List<StoredFile> files = StoredFile.restoreCollection(data.getProperty(PROP_FILES));
      showEventsAndDuration(form, BeeRowSet.restore(events), files);
    }

    form.updateRow(data, true);
  }

  private void requeryEvents(final long taskId) {
    ParameterList params = CrmKeeper.createTaskRequestParameters(SVC_GET_TASK_DATA);
    params.addDataItem(VAR_TASK_ID, taskId);

    Callback<ResponseObject> callback = new Callback<ResponseObject>() {
      @Override
      public void onFailure(String... reason) {
        getFormView().notifySevere(reason);
      }

      @Override
      public void onSuccess(ResponseObject result) {
        if (getFormView().getActiveRow().getId() != taskId) {
          return;
        }

        if (Queries.checkResponse(SVC_GET_TASK_DATA, VIEW_TASKS, result, BeeRow.class, this)) {
          BeeRow row = BeeRow.restore((String) result.getResponse());
          if (row == null) {
            onFailure(SVC_GET_TASK_DATA, "cannot restore row");
          } else {
            onResponse(row);
          }
        }
      }
    };

    sendRequest(params, callback);
  }

  private void sendFiles(final List<NewFileInfo> files, final long taskId, final long teId) {

    final Holder<Integer> counter = Holder.of(0);

    final List<BeeColumn> columns = Data.getColumns(VIEW_TASK_FILES,
        Lists.newArrayList(COL_TASK, COL_TASK_EVENT, COL_FILE, COL_CAPTION));

    for (final NewFileInfo fileInfo : files) {
      FileUtils.upload(fileInfo, new Callback<Long>() {
        @Override
        public void onSuccess(Long result) {
          List<String> values = Lists.newArrayList(BeeUtils.toString(taskId),
              BeeUtils.toString(teId), BeeUtils.toString(result), fileInfo.getCaption());

          Queries.insert(VIEW_TASK_FILES, columns, values, new RowCallback() {
            @Override
            public void onSuccess(BeeRow row) {
              counter.set(counter.get() + 1);
              if (counter.get() == files.size()) {
                requeryEvents(taskId);
              }
            }
          });
        }
      });
    }
  }

  private void sendRequest(ParameterList params, final Callback<ResponseObject> callback) {
    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasErrors()) {
          if (callback != null) {
            callback.onFailure(response.getErrors());
          }
        } else {
          if (callback != null) {
            callback.onSuccess(response);
          }
        }
      }
    });
  }

  private void sendRequest(ParameterList params, final TaskEvent event) {
    Callback<ResponseObject> callback = new Callback<ResponseObject>() {
      @Override
      public void onFailure(String... reason) {
        getFormView().notifySevere(reason);
      }

      @Override
      public void onSuccess(ResponseObject result) {
        if (Queries.checkResponse(event.getCaption(), VIEW_TASKS, result, BeeRow.class, this)) {
          BeeRow row = BeeRow.restore((String) result.getResponse());
          if (row == null) {
            onFailure(event.name(), VIEW_TASKS, "cannot restore row");
          } else {
            onResponse(row);
          }
        }
      }
    };

    sendRequest(params, callback);
  }

  private boolean setDurationParams(TaskDialog dialog, Map<String, String> ids,
      ParameterList params) {
    String time = dialog.getTime(ids.get(COL_DURATION));

    if (!BeeUtils.isEmpty(time)) {
      Long type = dialog.getSelector(ids.get(COL_DURATION_TYPE));
      if (!DataUtils.isId(type)) {
        showError("Įveskite darbo tipą");
        return false;
      }

      HasDateValue date = dialog.getDate(ids.get(COL_DURATION_DATE));
      if (date == null) {
        showError("Įveskite atlikimo datą");
        return false;
      }

      params.addDataItem(VAR_TASK_DURATION_DATE, date.serialize());
      params.addDataItem(VAR_TASK_DURATION_TIME, time);
      params.addDataItem(VAR_TASK_DURATION_TYPE, type);
    }
    return true;
  }

  private void showDurations(FormView form, Table<String, String, Integer> durations) {
    Widget widget = form.getWidgetByName(VIEW_TASK_DURATIONS);
    if (!(widget instanceof Flow)) {
      return;
    }

    Flow panel = (Flow) widget;
    panel.clear();

    if (durations.isEmpty()) {
      return;
    }

    Set<String> rows = durations.rowKeySet();
    Set<String> columns = durations.columnKeySet();

    HtmlTable display = new HtmlTable();
    display.addStyleName(STYLE_DURATION + "display");

    int r = 0;
    int c = 0;

    addDurationCell(display, r, c++, "Sugaištas laikas:", "caption");
    for (String column : columns) {
      addDurationCell(display, r, c++, column, "colLabel");
    }
    r++;

    int totMillis = 0;
    for (String row : rows) {
      c = 0;
      addDurationCell(display, r, c++, row, "rowLabel");

      int rowMillis = 0;
      for (String column : columns) {
        int millis = BeeUtils.unbox(durations.get(row, column));

        if (millis > 0) {
          addDurationCell(display, r, c, TimeUtils.renderTime(millis), "value");

          rowMillis += millis;
          totMillis += millis;
        }
        c++;
      }

      if (columns.size() > 1) {
        addDurationCell(display, r, c, TimeUtils.renderTime(rowMillis), "rowTotal");
      }
      r++;
    }

    if (rows.size() > 1) {
      c = 1;

      for (String column : columns) {
        Collection<Integer> values = durations.column(column).values();

        int colMillis = 0;
        for (Integer value : values) {
          colMillis += BeeUtils.unbox(value);
        }
        addDurationCell(display, r, c++, TimeUtils.renderTime(colMillis), "rowTotal");
      }

      if (columns.size() > 1) {
        addDurationCell(display, r, c, TimeUtils.renderTime(totMillis), "colTotal");
      }
    }

    panel.add(display);
  }

  private void showError(String message) {
    Global.showError("Klaida", Lists.newArrayList(message));
  }

  private void showEvent(Flow panel, BeeRow row, List<BeeColumn> columns, List<StoredFile> files,
      Table<String, String, Integer> durations) {
    Flow container = new Flow();
    container.addStyleName(STYLE_EVENT_ROW);

    int c = 0;
    Flow col0 = new Flow();
    col0.addStyleName(STYLE_EVENT_COL + BeeUtils.toString(c));

    Integer ev = row.getInteger(DataUtils.getColumnIndex(COL_EVENT, columns));
    TaskEvent event = NameUtils.getEnumByIndex(TaskEvent.class, ev);
    if (event != null) {
      col0.add(createEventCell(COL_EVENT, event.getCaption()));
    }

    DateTime publishTime = row.getDateTime(DataUtils.getColumnIndex(COL_PUBLISH_TIME, columns));
    if (publishTime != null) {
      col0.add(createEventCell(COL_PUBLISH_TIME,
          Format.getDefaultDateTimeFormat().format(publishTime)));
    }

    String publisher = BeeUtils.joinWords(
        row.getString(DataUtils.getColumnIndex(COL_PUBLISHER_FIRST_NAME, columns)),
        row.getString(DataUtils.getColumnIndex(COL_PUBLISHER_LAST_NAME, columns)));
    if (!BeeUtils.isEmpty(publisher)) {
      col0.add(createEventCell(COL_PUBLISHER, publisher));
    }

    container.add(col0);

    c++;
    Flow col1 = new Flow();
    col1.addStyleName(STYLE_EVENT_COL + BeeUtils.toString(c));

    String note = row.getString(DataUtils.getColumnIndex(COL_EVENT_NOTE, columns));
    if (!BeeUtils.isEmpty(note)) {
      col1.add(createEventCell(COL_EVENT_NOTE, note));
    }

    String comment = row.getString(DataUtils.getColumnIndex(COL_COMMENT, columns));
    if (!BeeUtils.isEmpty(comment)) {
      col1.add(createEventCell(COL_COMMENT, comment));
    }

    container.add(col1);

    String duration = row.getString(DataUtils.getColumnIndex(COL_DURATION, columns));
    if (!BeeUtils.isEmpty(duration)) {
      c++;
      Flow col2 = new Flow();
      col2.addStyleName(STYLE_EVENT_COL + BeeUtils.toString(c));

      col2.add(createEventCell(COL_DURATION, "Sugaištas laikas: " + duration));

      String durType = row.getString(DataUtils.getColumnIndex(COL_DURATION_TYPE, columns));
      if (!BeeUtils.isEmpty(durType)) {
        col2.add(createEventCell(COL_DURATION_TYPE, durType));
      }

      DateTime durDate = row.getDateTime(DataUtils.getColumnIndex(COL_DURATION_DATE, columns));
      if (durDate != null) {
        col2.add(createEventCell(COL_DURATION_DATE, durDate.toCompactString()));
      }

      container.add(col2);

      int millis = TimeUtils.parseTime(duration);
      if (millis > 0 && !BeeUtils.isEmpty(publisher) && !BeeUtils.isEmpty(durType)) {
        Integer value = durations.get(publisher, durType);
        durations.put(publisher, durType, millis + BeeUtils.unbox(value));
      }
    }

    panel.add(container);

    if (!files.isEmpty()) {
      Simple fileContainer = new Simple();
      fileContainer.addStyleName(STYLE_EVENT_FILES);

      FileGroup fileGroup = new FileGroup();
      fileGroup.addFiles(files);

      fileContainer.setWidget(fileGroup);
      panel.add(fileContainer);
    }
  }

  private void showEventsAndDuration(FormView form, BeeRowSet rowSet, List<StoredFile> files) {
    Widget widget = form.getWidgetByName(VIEW_TASK_EVENTS);
    if (!(widget instanceof Flow) || DataUtils.isEmpty(rowSet)) {
      return;
    }

    Flow panel = (Flow) widget;
    panel.clear();

    Table<String, String, Integer> durations = TreeBasedTable.create();

    for (BeeRow row : rowSet.getRows()) {
      showEvent(panel, row, rowSet.getColumns(), filterEventFiles(files, row.getId()), durations);
    }

    showDurations(form, durations);

    if (panel.getWidgetCount() > 1 && form.asWidget().isVisible()) {
      final Widget last = panel.getWidget(panel.getWidgetCount() - 1);
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          last.getElement().scrollIntoView();
        }
      });
    }
  }
}
