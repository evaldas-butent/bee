package com.butent.bee.client.modules.tasks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.images.star.Stars;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.validation.ValidationHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.edit.EditorAssistant;
import com.butent.bee.client.view.grid.GridSettings;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.Colors;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.FilterValue;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.export.XCell;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XStyle;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.tasks.TaskConstants.TaskStatus;
import com.butent.bee.shared.modules.tasks.TaskType;
import com.butent.bee.shared.modules.tasks.TaskUtils;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class TaskList {

  private static final class GridHandler extends AbstractGridInterceptor implements ClickHandler {

    private static final String NAME_MODE = "Mode";
    private static final String NAME_SLACK = "Slack";
    private static final String NAME_STAR = "Star";

    private final TaskType type;
    private final String caption;

    private final Long userId;

    private GridHandler(TaskType type, String caption) {
      this.type = type;
      this.caption = caption;

      this.userId = BeeKeeper.getUser().getUserId();
    }

    @Override
    public boolean afterCreateColumn(String columnId, List<? extends IsColumn> dataColumns,
        AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
        EditableColumn editableColumn) {

      if (BeeUtils.same(columnId, NAME_MODE) && column instanceof HasCellRenderer) {
        ((HasCellRenderer) column).setRenderer(new ModeRenderer());

      } else if (BeeUtils.same(columnId, NAME_SLACK) && column instanceof HasCellRenderer) {
        ((HasCellRenderer) column).setRenderer(new SlackRenderer(dataColumns));

      } else if (BeeUtils.inListSame(columnId, COL_FINISH_TIME, COL_EXECUTOR)) {
        editableColumn.addCellValidationHandler(ValidationHelper.DO_NOT_VALIDATE);
      }

      return true;
    }

    @Override
    public void afterCreatePresenter(GridPresenter presenter) {
      presenter.getHeader().clearCommandPanel();

      if (type.equals(TaskType.ALL) || type.equals(TaskType.DELEGATED)) {
        FaLabel confirmTask = new FaLabel(FontAwesome.CHECK_SQUARE_O);
        confirmTask.setTitle(Localized.getConstants().crmTaskConfirm());
        confirmTask.addClickHandler(this);

        presenter.getHeader().addCommandItem(confirmTask);
      }
    }

    @Override
    public boolean beforeAction(Action action, final GridPresenter presenter) {
      if (action == Action.COPY) {
        if (presenter.getMainView().isEnabled() && presenter.getActiveRow() != null) {
          String title = presenter.getActiveRow().getString(getDataIndex(COL_SUMMARY));
          List<String> msg = Lists.newArrayList(Localized.getConstants().crmTaskCopyQuestion());

          List<String> options = Lists.newArrayList(Localized.getConstants().crmNewTask(),
              Localized.getConstants().crmNewRecurringTask(), Localized.getConstants().cancel());
          int defValue = options.size() - 1;

          Global.messageBox(title, Icon.QUESTION, msg, options, defValue, new ChoiceCallback() {
            @Override
            public void onSuccess(final int value) {
              if (value < 0 || value > 1 || presenter.getActiveRow() == null) {
                return;
              }

              ParameterList params = TasksKeeper.createArgs(SVC_GET_TASK_DATA);
              params.addQueryItem(VAR_TASK_ID, presenter.getActiveRow().getId());
              params.addQueryItem(VAR_TASK_PROPERTIES, PROP_OBSERVERS);
              params.addQueryItem(VAR_TASK_RELATIONS, 1);

              BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
                @Override
                public void onResponse(ResponseObject response) {
                  if (Queries.checkRowResponse(SVC_GET_TASK_DATA, VIEW_TASKS, response)) {
                    BeeRow original = BeeRow.restore(response.getResponseAsString());

                    switch (value) {
                      case 0:
                        copyTask(original);
                        break;
                      case 1:
                        copyAsRecurringTask(original);
                        break;
                    }
                  }
                }
              });
            }
          });
        }

        return false;

      } else {
        return super.beforeAction(action, presenter);
      }
    }

    @Override
    public ColumnDescription beforeCreateColumn(GridView gridView,
        ColumnDescription columnDescription) {

      if (type == TaskType.ASSIGNED && columnDescription.is(COL_EXECUTOR)
          || type == TaskType.DELEGATED && columnDescription.is(COL_OWNER)) {

        if (columnDescription.getVisible() == null
            && !GridSettings.hasVisibleColumns(gridView.getGridKey())) {
          ColumnDescription copy = columnDescription.copy();
          copy.setVisible(false);

          return copy;
        }
      }

      return columnDescription;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    @Override
    public String getColumnCaption(String columnName) {
      if (PROP_STAR.equals(columnName)) {
        return Stars.getDefaultHeader();
      } else {
        return super.getColumnCaption(columnName);
      }
    }

    @Override
    public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
        Collection<RowInfo> selectedRows, DeleteMode defMode) {
      Provider provider = presenter.getDataProvider();
      Long owner = activeRow.getLong(provider.getColumnIndex(COL_OWNER));

      if (Objects.equals(owner, userId)) {
        return GridInterceptor.DeleteMode.SINGLE;
      } else {
        presenter.getGridView().notifyWarning(
            BeeUtils.joinWords(Localized.getConstants().crmTask(), activeRow.getId()),
            Localized.getConstants().crmTaskDeleteCanManager());
        return GridInterceptor.DeleteMode.CANCEL;
      }
    }

    @Override
    public List<String> getDeleteRowMessage(IsRow row) {
      String m1 = BeeUtils.joinWords(Localized.getConstants().crmTask(), row.getId());
      String m2 = Localized.getConstants().crmTaskDeleteQuestion();

      return Lists.newArrayList(m1, m2);
    }

    @Override
    public AbstractFilterSupplier getFilterSupplier(String columnName,
        ColumnDescription columnDescription) {
      if (BeeUtils.same(columnName, NAME_SLACK)) {
        return new SlackFilterSupplier(columnDescription.getFilterOptions());

      } else if (BeeUtils.same(columnName, NAME_STAR)) {
        return new StarFilterSupplier(columnDescription.getFilterOptions());

      } else if (BeeUtils.same(columnName, NAME_MODE)) {
        return new ModeFilterSupplier(columnDescription.getFilterOptions());

      } else {
        return super.getFilterSupplier(columnName, columnDescription);
      }
    }

    @Override
    public GridInterceptor getInstance() {
      return new GridHandler(type, caption);
    }

    @Override
    public String getSupplierKey() {
      return BeeUtils.normalize(BeeUtils.join(BeeConst.STRING_UNDER, "grid", GRID_TASKS, type));
    }

    @Override
    public void onClick(ClickEvent event) {
      final GridView gridView = getGridPresenter().getGridView();
      CompoundFilter filter = CompoundFilter.or();

      for (RowInfo row : gridView.getSelectedRows(SelectedRows.ALL)) {
        filter.add(Filter.compareId(row.getId()));
      }

      if (filter.isEmpty()) {

        IsRow selectedRow = gridView.getActiveRow();
        if (selectedRow == null) {
          gridView.notifyWarning(Localized.getConstants().selectAtLeastOneRow());
          return;
        } else {
          confirmTask(gridView, selectedRow);
        }
      } else {
        confirmTasks(gridView, filter);
      }
    }

    @Override
    public void onEditStart(final EditStartEvent event) {
      if (PROP_STAR.equals(event.getColumnId())) {
        IsRow row = event.getRowValue();
        if (row == null) {
          return;
        }

        if (row.getProperty(PROP_USER) == null) {
          return;
        }

        final CellSource source = CellSource.forProperty(PROP_STAR, ValueType.INTEGER);
        EditorAssistant.editStarCell(DEFAULT_STAR_COUNT, event, source, new Consumer<Integer>() {
          @Override
          public void accept(Integer parameter) {
            updateStar(event, source, parameter);
          }
        });
      }
    }

    @Override
    public boolean onLoad(GridDescription gridDescription) {
      gridDescription.setFilter(type.getFilter(new LongValue(userId)));
      return true;
    }

    private void confirmTask(final GridView gridView, final IsRow row) {
      Assert.notNull(row);

      final List<BeeRow> taskRow = Lists.newArrayList(DataUtils.cloneRow(row));
      final DataInfo info = Data.getDataInfo(gridView.getViewName());
      final ResponseObject messages = ResponseObject.emptyResponse();
      long user = BeeUtils.unbox(userId);

      if (!TaskUtils.canConfirmTasks(info, taskRow, user, messages)) {
        if (messages.hasWarnings()) {
          gridView.notifyWarning(messages.getWarnings());
        }
        return;
      }

      final TaskDialog dialog = new TaskDialog(Localized.getConstants().crmTaskConfirmation());

      final String did =
          dialog.addDateTime(Localized.getConstants().crmTaskConfirmDate(), true, TimeUtils
              .nowMinutes());
      final String cid = dialog.addComment(false);

      dialog.addAction(Localized.getConstants().crmTaskConfirm(), new ScheduledCommand() {
        @Override
        public void execute() {
          DateTime approved = dialog.getDateTime(did);
          if (approved == null) {
            gridView.notifySevere(Localized.getConstants().crmEnterConfirmDate());
            return;
          }

          List<String> notes = Lists.newArrayList();

          BeeRow newRow = DataUtils.cloneRow(row);
          newRow.setValue(info.getColumnIndex(COL_STATUS), TaskStatus.APPROVED.ordinal());
          newRow.setValue(info.getColumnIndex(COL_APPROVED), approved);

          notes.add(TaskUtils.getUpdateNote(Localized.getLabel(info.getColumn(COL_STATUS)),
              DataUtils.render(info, row, info.getColumn(COL_STATUS), info
                  .getColumnIndex(COL_STATUS)), DataUtils.render(
                  info, newRow, info.getColumn(COL_STATUS), info.getColumnIndex(COL_STATUS))));

          notes.add(TaskUtils.getInsertNote(Localized.getLabel(info.getColumn(COL_APPROVED)),
              DataUtils.render(info, newRow, info.getColumn(COL_APPROVED), info
                  .getColumnIndex(COL_APPROVED))));

          ParameterList params = TasksKeeper.createArgs(SVC_CONFIRM_TASKS);
          params.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(Lists.newArrayList(row.getId())));
          params.addDataItem(VAR_TASK_APPROVED_TIME, approved.serialize());

          String comment = dialog.getComment(cid);
          if (!BeeUtils.isEmpty(comment)) {
            params.addDataItem(VAR_TASK_COMMENT, comment);
          }

          if (!notes.isEmpty()) {
            params.addDataItem(VAR_TASK_NOTES, Codec.beeSerialize(notes));
          }

          sendRequest(params, gridView);
          dialog.close();
        }
      });

      dialog.display();
    }

    private void confirmTasks(final GridView gridView, final CompoundFilter filter) {

      Queries.getRowSet(gridView.getViewName(), null, filter, new RowSetCallback() {

        @Override
        public void onSuccess(final BeeRowSet result) {
          final DataInfo info = Data.getDataInfo(gridView.getViewName());
          final ResponseObject messages = ResponseObject.emptyResponse();
          long user = BeeUtils.unbox(userId);

          if (!TaskUtils.canConfirmTasks(info, result.getRows(), user, messages)) {
            if (messages.hasWarnings()) {
              gridView.notifyWarning(messages.getWarnings());
            }
            return;
          }

          Global.confirm(Localized.getConstants().crmTasksConfirmQuestion(),
              new ConfirmationCallback() {

                @Override
                public void onConfirm() {
                  DateTime approved = new DateTime();

                  ParameterList params = TasksKeeper.createArgs(SVC_CONFIRM_TASKS);
                  params.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(result.getRowIds()));
                  params.addDataItem(VAR_TASK_APPROVED_TIME, approved.serialize());

                  sendRequest(params, gridView);
                }
              });

        }
      });

    }

    private void copyAsRecurringTask(BeeRow oldRow) {
      DataInfo sourceInfo = Data.getDataInfo(VIEW_TASKS);
      if (sourceInfo == null) {
        return;
      }
      DataInfo targetInfo = Data.getDataInfo(VIEW_RECURRING_TASKS);
      if (targetInfo == null) {
        return;
      }

      BeeRow newRow = RowFactory.createEmptyRow(targetInfo, true);

      Set<String> colNames = Sets.newHashSet(COL_PRIORITY, COL_SUMMARY, COL_DESCRIPTION);
      for (String colName : colNames) {
        String value = oldRow.getString(getDataIndex(colName));
        if (!BeeUtils.isEmpty(value)) {
          newRow.setValue(targetInfo.getColumnIndex(colName), value);
        }
      }

      DateTime startTime = oldRow.getDateTime(getDataIndex(COL_START_TIME));
      DateTime finishTime = oldRow.getDateTime(getDataIndex(COL_FINISH_TIME));

      if (startTime != null) {
        int minutes = TimeUtils.minutesSinceDayStarted(startTime);
        if (minutes > 0) {
          newRow.setValue(targetInfo.getColumnIndex(COL_RT_START_AT),
              TimeUtils.renderMinutes(minutes, true));
        }
      }

      String duration = oldRow.getString(getDataIndex(COL_EXPECTED_DURATION));
      if (!BeeUtils.isEmpty(duration)) {
        newRow.clearCell(targetInfo.getColumnIndex(COL_RT_DURATION_DAYS));
        newRow.setValue(targetInfo.getColumnIndex(COL_RT_DURATION_TIME), duration);

      } else if (startTime != null && finishTime != null) {
        long durationMillis = finishTime.getTime() - startTime.getTime();
        long days = durationMillis / TimeUtils.MILLIS_PER_DAY;
        long time = durationMillis % TimeUtils.MILLIS_PER_DAY;

        if (durationMillis > 0 && (days > 0 || time > TimeUtils.MILLIS_PER_MINUTE)) {
          if (days > 0) {
            newRow.setValue(targetInfo.getColumnIndex(COL_RT_DURATION_DAYS), days);
          } else {
            newRow.clearCell(targetInfo.getColumnIndex(COL_RT_DURATION_DAYS));
          }

          if (time > TimeUtils.MILLIS_PER_MINUTE) {
            newRow.setValue(targetInfo.getColumnIndex(COL_RT_DURATION_TIME),
                TimeUtils.renderMinutes(BeeUtils.toInt(time / TimeUtils.MILLIS_PER_MINUTE), true));
          }
        }
      }

      colNames = Sets.newHashSet(COL_COMPANY, COL_CONTACT, COL_REMINDER);
      for (String colName : colNames) {
        RelationUtils.copyWithDescendants(sourceInfo, colName, oldRow, targetInfo, colName, newRow);
      }

      Long owner = BeeKeeper.getUser().getUserId();
      Long executor = oldRow.getLong(getDataIndex(COL_EXECUTOR));

      if (executor != null) {
        newRow.setProperty(PROP_EXECUTORS, executor.toString());
      }

      String observers = oldRow.getProperty(PROP_OBSERVERS);
      if (!BeeUtils.isEmpty(observers)) {
        List<Long> users = DataUtils.parseIdList(observers);

        if (users.contains(owner)) {
          users.remove(owner);
        }
        if (users.contains(executor)) {
          users.remove(executor);
        }

        if (!users.isEmpty()) {
          newRow.setProperty(PROP_OBSERVERS, DataUtils.buildIdList(users));
        }
      }

      for (String propName : TaskUtils.getRelationPropertyNames()) {
        String propValue = oldRow.getProperty(propName);
        if (!BeeUtils.isEmpty(propValue)) {
          newRow.setProperty(propName, propValue);
        }
      }

      RowFactory.createRow(targetInfo, newRow);
    }

    private void copyTask(BeeRow oldRow) {
      DataInfo dataInfo = Data.getDataInfo(VIEW_TASKS);
      if (dataInfo == null) {
        return;
      }

      BeeRow newRow = RowFactory.createEmptyRow(dataInfo, true);

      Set<String> colNames = Sets.newHashSet(COL_PRIORITY, COL_SUMMARY, COL_DESCRIPTION,
          COL_EXPECTED_DURATION);
      for (String colName : colNames) {
        int index = getDataIndex(colName);
        String value = oldRow.getString(index);

        if (!BeeUtils.isEmpty(value)) {
          newRow.setValue(index, value);
        }
      }

      colNames = Sets.newHashSet(COL_COMPANY, COL_CONTACT, COL_REMINDER);
      for (String colName : colNames) {
        RelationUtils.copyWithDescendants(dataInfo, colName, oldRow, dataInfo, colName, newRow);
      }

      Long owner = BeeKeeper.getUser().getUserId();
      Long executor = oldRow.getLong(getDataIndex(COL_EXECUTOR));

      if (executor != null) {
        newRow.setProperty(PROP_EXECUTORS, executor.toString());
      }

      String observers = oldRow.getProperty(PROP_OBSERVERS);
      if (!BeeUtils.isEmpty(observers)) {
        List<Long> users = DataUtils.parseIdList(observers);

        if (users.contains(owner)) {
          users.remove(owner);
        }
        if (users.contains(executor)) {
          users.remove(executor);
        }

        if (!users.isEmpty()) {
          newRow.setProperty(PROP_OBSERVERS, DataUtils.buildIdList(users));
        }
      }

      for (String propName : TaskUtils.getRelationPropertyNames()) {
        String propValue = oldRow.getProperty(propName);
        if (!BeeUtils.isEmpty(propValue)) {
          newRow.setProperty(propName, propValue);
        }
      }

      RowFactory.createRow(dataInfo, newRow);
    }

    private void sendRequest(final ParameterList params, final GridView gridView) {
      BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {

        @Override
        public void onResponse(ResponseObject response) {
          if (response.hasMessages()) {
            gridView.notifySevere(BeeUtils.joinItems(response.getMessages()));
            return;
          }

          gridView.notifyInfo(Localized.getConstants().crmTaskStatusApproved());
          getGridPresenter().refresh(true);
        }

      });
    }

    private void updateStar(final EditStartEvent event, final CellSource source,
        final Integer value) {
      final long rowId = event.getRowValue().getId();

      Filter filter = Filter.and(Filter.equals(COL_TASK, rowId), Filter.equals(COL_USER, userId));

      Queries.update(VIEW_TASK_USERS, filter, COL_STAR, new IntegerValue(value),
          new Queries.IntCallback() {
            @Override
            public void onSuccess(Integer result) {
              CellUpdateEvent.fire(BeeKeeper.getBus(), VIEW_TASKS, rowId,
                  event.getRowValue().getVersion(), source,
                  (value == null) ? null : BeeUtils.toString(value));
            }
          });
    }
  }

  private static final class ModeFilterSupplier extends AbstractFilterSupplier {

    private enum Mode {
      NEW {
        @Override
        String getLabel() {
          return Localized.getConstants().taskFilterNew();
        }

        @Override
        String getValue() {
          return "0";
        }
      },

      UPDATED {
        @Override
        String getLabel() {
          return Localized.getConstants().taskFilterUpdated();
        }

        @Override
        String getValue() {
          return "1";
        }
      },

      NEW_OR_UPDATED {
        @Override
        String getLabel() {
          return Localized.getConstants().taskFilterNewOrUpdated();
        }

        @Override
        String getValue() {
          return "2";
        }
      };

      private static Mode parseValue(String value) {
        for (Mode mode : Mode.values()) {
          if (BeeUtils.same(value, mode.getValue())) {
            return mode;
          }
        }
        return null;
      }

      abstract String getLabel();

      abstract String getValue();
    }

    private static Widget createMode(String styleName) {
      return new CustomDiv(styleName);
    }

    private static Filter getNewFilter() {
      return Filter.custom(FILTER_TASKS_NEW);
    }

    private static Filter getUpdFilter() {
      return Filter.custom(FILTER_TASKS_UPDATED);
    }

    private Mode mode;

    private ModeFilterSupplier(String options) {
      super(VIEW_TASKS, null, null, options);
    }

    @Override
    public String getComponentLabel(String ownerLabel) {
      return getLabel();
    }

    @Override
    public FilterValue getFilterValue() {
      return (getMode() == null) ? null : FilterValue.of(getMode().getValue());
    }

    @Override
    public String getLabel() {
      return (getMode() == null) ? null : getMode().getLabel();
    }

    @Override
    public void onRequest(Element target, Scheduler.ScheduledCommand onChange) {
      openDialog(target, createWidget(), onChange);
    }

    @Override
    public Filter parse(FilterValue input) {
      if (input != null && BeeUtils.isDigit(input.getValue())) {
        switch (BeeUtils.toInt(input.getValue())) {
          case 0:
            return getNewFilter();
          case 1:
            return getUpdFilter();
          case 2:
            return Filter.or(getNewFilter(), getUpdFilter());
        }
      }

      return null;
    }

    @Override
    public void setFilterValue(FilterValue filterValue) {
      setMode((filterValue == null) ? null : Mode.parseValue(filterValue.getValue()));
    }

    @Override
    protected String getStylePrefix() {
      return "bee-crm-FilterSupplier-Mode-";
    }

    private Widget createWidget() {
      HtmlTable container = new HtmlTable();
      container.addStyleName(getStylePrefix() + "container");

      int row = 0;

      Button bNew = new Button(Localized.getConstants().taskFilterNew());
      bNew.addStyleName(getStylePrefix() + "new");

      bNew.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean changed = !Mode.NEW.equals(getMode());
          setMode(Mode.NEW);
          update(changed);
        }
      });

      container.setWidget(row, 0, bNew);

      Widget modeNew = createMode(TaskList.STYLE_MODE_NEW);
      container.setWidget(row, 1, modeNew);

      row++;

      Button bUpd = new Button(Localized.getConstants().taskFilterUpdated());
      bUpd.addStyleName(getStylePrefix() + "upd");

      bUpd.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean changed = !Mode.UPDATED.equals(getMode());
          setMode(Mode.UPDATED);
          update(changed);
        }
      });

      container.setWidget(row, 0, bUpd);

      Widget modeUpd = createMode(TaskList.STYLE_MODE_UPD);
      container.setWidget(row, 1, modeUpd);

      row++;

      Button both = new Button(Localized.getConstants().taskFilterNewOrUpdated());
      both.addStyleName(getStylePrefix() + "both");

      both.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean changed = !Mode.NEW_OR_UPDATED.equals(getMode());
          setMode(Mode.NEW_OR_UPDATED);
          update(changed);
        }
      });

      container.setWidget(row, 0, both);

      Flow modeBoth = new Flow();
      modeBoth.add(createMode(TaskList.STYLE_MODE_NEW));
      modeBoth.add(createMode(TaskList.STYLE_MODE_UPD));

      container.setWidget(row, 1, modeBoth);

      row++;

      Button all = new Button(Localized.getConstants().taskFilterAll());
      all.addStyleName(getStylePrefix() + "all");

      all.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean changed = getMode() != null;
          setMode(null);
          update(changed);
        }
      });

      container.setWidget(row, 0, all);

      Button cancel = new Button(Localized.getConstants().cancel());
      cancel.addStyleName(getStylePrefix() + "cancel");

      cancel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          closeDialog();
        }
      });

      container.setWidget(row, 1, cancel);

      return container;
    }

    private Mode getMode() {
      return mode;
    }

    private void setMode(Mode mode) {
      this.mode = mode;
    }
  }

  private static final class ModeRenderer extends AbstractCellRenderer {

    private enum Mode {
      NEW, UPD
    }

    private static Mode getMode(IsRow row) {
      if (row == null) {
        return null;
      }
      if (row.getProperty(PROP_USER) == null) {
        return null;
      }

      Long access = BeeUtils.toLongOrNull(row.getProperty(PROP_LAST_ACCESS));
      if (access == null) {
        return Mode.NEW;
      }

      Long publish = BeeUtils.toLongOrNull(row.getProperty(PROP_LAST_PUBLISH));
      if (publish != null && access < publish) {
        return Mode.UPD;
      } else {
        return null;
      }
    }

    private static String renderMode(String styleName) {
      return "<div class=\"" + styleName + "\"></div>";
    }

    private ModeRenderer() {
      super(null);
    }

    @Override
    public XCell export(IsRow row, int cellIndex, Integer styleRef, XSheet sheet) {
      Mode mode = getMode(row);
      if (mode == null) {
        return null;
      }

      XStyle style = new XStyle();

      switch (mode) {
        case NEW:
          style.setColor(Colors.LIGHTGREEN);
          break;
        case UPD:
          style.setColor(Colors.YELLOW);
          break;
      }

      return XCell.forStyle(cellIndex, sheet.registerStyle(style));
    }

    @Override
    public String render(IsRow row) {
      Mode mode = getMode(row);

      if (mode == null) {
        return BeeConst.STRING_EMPTY;
      }

      switch (mode) {
        case NEW:
          return renderMode(TaskList.STYLE_MODE_NEW);
        case UPD:
          return renderMode(TaskList.STYLE_MODE_UPD);
      }

      Assert.untouchable();
      return null;
    }
  }

  private static final class SlackFilterSupplier extends AbstractFilterSupplier {

    private enum Slack {
      LATE {
        @Override
        Filter getFilter() {
          return Filter.and(Filter.isLess(COL_STATUS, IntegerValue.of(TaskStatus.COMPLETED)),
              Filter.isLess(COL_FINISH_TIME, new DateTimeValue(TimeUtils.nowMinutes())));
        }

        @Override
        String getLabel() {
          return Localized.getConstants().taskLabelLate();
        }

        @Override
        String getValue() {
          return "late";
        }
      },

      SCHEDULED {
        @Override
        Filter getFilter() {
          return Filter.and(Filter.isLess(COL_STATUS, IntegerValue.of(TaskStatus.COMPLETED)),
              Filter.isMoreEqual(COL_START_TIME,
                  new DateTimeValue(TimeUtils.today(1).getDateTime())));
        }

        @Override
        String getLabel() {
          return Localized.getConstants().taskLabelScheduled();
        }

        @Override
        String getValue() {
          return "scheduled";
        }
      };

      private static Slack parseValue(String value) {
        for (Slack slack : Slack.values()) {
          if (BeeUtils.same(value, slack.getValue())) {
            return slack;
          }
        }
        return null;
      }

      abstract Filter getFilter();

      abstract String getLabel();

      abstract String getValue();
    }

    private Slack slack;

    private SlackFilterSupplier(String options) {
      super(VIEW_TASKS, null, null, options);
    }

    @Override
    public String getComponentLabel(String ownerLabel) {
      return getLabel();
    }

    @Override
    public FilterValue getFilterValue() {
      return (getSlack() == null) ? null : FilterValue.of(getSlack().getValue());
    }

    @Override
    public String getLabel() {
      return (getSlack() == null) ? null : getSlack().getLabel();
    }

    @Override
    public void onRequest(Element target, Scheduler.ScheduledCommand onChange) {
      openDialog(target, createWidget(), onChange);
    }

    @Override
    public Filter parse(FilterValue input) {
      Slack s = (input == null) ? null : Slack.parseValue(input.getValue());
      return (s == null) ? null : s.getFilter();
    }

    @Override
    public void setFilterValue(FilterValue filterValue) {
      setSlack((filterValue == null) ? null : Slack.parseValue(filterValue.getValue()));
    }

    @Override
    protected String getStylePrefix() {
      return "bee-crm-FilterSupplier-Slack-";
    }

    private Widget createWidget() {
      Flow container = new Flow();
      container.addStyleName(getStylePrefix() + "container");

      Button late = new Button(Localized.getConstants().taskFilterLate());
      late.addStyleName(getStylePrefix() + "late");

      late.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean changed = !Slack.LATE.equals(getSlack());
          setSlack(Slack.LATE);
          update(changed);
        }
      });

      container.add(late);

      Button scheduled = new Button(Localized.getConstants().taskFilterScheduled());
      scheduled.addStyleName(getStylePrefix() + "scheduled");

      scheduled.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean changed = !Slack.SCHEDULED.equals(getSlack());
          setSlack(Slack.SCHEDULED);
          update(changed);
        }
      });

      container.add(scheduled);

      Button all = new Button(Localized.getConstants().taskFilterAll());
      all.addStyleName(getStylePrefix() + "all");

      all.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean changed = getSlack() != null;
          setSlack(null);
          update(changed);
        }
      });

      container.add(all);

      Button cancel = new Button(Localized.getConstants().cancel());
      cancel.addStyleName(getStylePrefix() + "cancel");

      cancel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          closeDialog();
        }
      });

      container.add(cancel);

      return container;
    }

    private Slack getSlack() {
      return slack;
    }

    private void setSlack(Slack slack) {
      this.slack = slack;
    }
  }

  private static final class StarFilterSupplier extends AbstractFilterSupplier {

    private boolean starred;

    private StarFilterSupplier(String options) {
      super(VIEW_TASKS, null, null, options);
    }

    @Override
    public String getComponentLabel(String ownerLabel) {
      return getLabel();
    }

    @Override
    public FilterValue getFilterValue() {
      return isStarred() ? FilterValue.of(null, false) : null;
    }

    @Override
    public String getLabel() {
      return isStarred() ? Localized.getConstants().taskFilterStarred() : null;
    }

    @Override
    public void onRequest(Element target, Scheduler.ScheduledCommand onChange) {
      openDialog(target, createWidget(), onChange);
    }

    @Override
    public Filter parse(FilterValue input) {
      if (input != null && BeeUtils.isFalse(input.getEmptyValues())) {
        return Filter.in(Data.getIdColumn(VIEW_TASKS), VIEW_TASK_USERS, COL_TASK,
            Filter.and(BeeKeeper.getUser().getFilter(COL_USER), Filter.notNull(COL_STAR)));
      } else {
        return null;
      }
    }

    @Override
    public void setFilterValue(FilterValue filterValue) {
      setStarred(filterValue != null && BeeUtils.isFalse(filterValue.getEmptyValues()));
    }

    @Override
    protected String getStylePrefix() {
      return "bee-crm-FilterSupplier-Star-";
    }

    private Widget createWidget() {
      Flow container = new Flow();
      container.addStyleName(getStylePrefix() + "container");

      Button star = new Button(Localized.getConstants().taskFilterStarred());
      star.addStyleName(getStylePrefix() + "starred");

      star.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean changed = !isStarred();
          setStarred(true);
          update(changed);
        }
      });

      container.add(star);

      Button all = new Button(Localized.getConstants().taskFilterAll());
      all.addStyleName(getStylePrefix() + "all");

      all.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          boolean changed = isStarred();
          setStarred(false);
          update(changed);
        }
      });

      container.add(all);

      Button cancel = new Button(Localized.getConstants().cancel());
      cancel.addStyleName(getStylePrefix() + "cancel");

      cancel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          closeDialog();
        }
      });

      container.add(cancel);

      return container;
    }

    private boolean isStarred() {
      return starred;
    }

    private void setStarred(boolean starred) {
      this.starred = starred;
    }
  }

  private static final int DEFAULT_STAR_COUNT = 3;

  private static final String STYLE_MODE_NEW = "bee-crm-Mode-new";
  private static final String STYLE_MODE_UPD = "bee-crm-Mode-upd";

  static Consumer<GridOptions> getFeedFilterHandler(Feed feed) {
    final TaskType type = TaskType.getByFeed(feed);
    Assert.notNull(type);

    Consumer<GridOptions> consumer = new Consumer<GridFactory.GridOptions>() {
      @Override
      public void accept(GridOptions input) {
        String caption = BeeUtils.notEmpty(input.getCaption(), type.getCaption());
        GridFactory.openGrid(GRID_TASKS, new GridHandler(type, caption), input);
      }
    };

    return consumer;
  }

  static void open(String args) {
    TaskType type = TaskType.getByPrefix(args);

    if (type == null) {
      Global.showError(Lists.newArrayList(GRID_TASKS, "Type not recognized:", args));
    } else {
      GridFactory.openGrid(GRID_TASKS, new GridHandler(type, type.getCaption()));
    }
  }

  private TaskList() {
    super();
  }
}
