package com.butent.bee.client.modules.crm;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.Disclosure;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.ActionEvent;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.FlexTable.FlexCellFormatter;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.RendererFactory;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.Priority;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaskEventHandler {

  private static class ObserverHandler extends AbstractGridCallback {

    private Long owner = null;
    private Long executor = null;

    private ObserverHandler() {
    }

    @Override
    public boolean beforeAddRow(final GridPresenter presenter) {
      if (!Objects.equal(BeeKeeper.getUser().getUserId(), getOwner())) {
        presenter.getGridView().notifyWarning("Not an owner");
        return false;
      }

      List<Filter> filters = Lists.newArrayList();
      if (getOwner() != null) {
        filters.add(excludeUser(getOwner()));
      }
      if (getExecutor() != null && !getExecutor().equals(getOwner())) {
        filters.add(excludeUser(getExecutor()));
      }

      int index = presenter.getDataProvider().getColumnIndex(CrmConstants.COL_USER);
      for (IsRow row : presenter.getGridView().getGrid().getRowData()) {
        filters.add(excludeUser(row.getLong(index)));
      }

      final long task = presenter.getGridView().getRelId();

      Queries.getRowSet("Users", null, Filter.and(filters), null, new RowSetCallback() {
        @Override
        public void onSuccess(final BeeRowSet result) {
          if (result.isEmpty()) {
            presenter.getGridView().notifyWarning("Everybody is watching you");
            return;
          }

          MultiSelector selector = new MultiSelector("Stebėtojai", result,
              Lists.newArrayList(CrmConstants.COL_FIRST_NAME, CrmConstants.COL_LAST_NAME),
              new MultiSelector.SelectionCallback() {
                @Override
                public void onSelection(List<IsRow> rows) {
                  addObservers(presenter, task, rows);
                }
              });

          Widget target = null;
          if (result.getNumberOfRows() < 20) {
            HeaderView header = presenter.getView().getHeader();
            if (header != null) {
              for (int i = header.getWidgetCount() - 1; i >= 0; i--) {
                if (header.getWidget(i).isVisible()) {
                  target = header.getWidget(i);
                  break;
                }
              }
            }
          }

          if (target == null) {
            selector.center();
          } else {
            selector.showRelativeTo(target);
          }
        }
      });

      return false;
    }

    @Override
    public int beforeDeleteRow(GridPresenter presenter, IsRow row) {
      int result;
      if (row == null) {
        result = GridCallback.DELETE_CANCEL;
      } else {
        Long usr = BeeKeeper.getUser().getUserId();
        Long obs = row.getLong(presenter.getDataProvider().getColumnIndex(CrmConstants.COL_USER));

        if (usr == null || obs == null || obs.equals(getOwner()) || obs.equals(getExecutor())) {
          result = GridCallback.DELETE_CANCEL;
        } else if (usr.equals(getOwner())) {
          result = GridCallback.DELETE_DEFAULT;
        } else if (usr.equals(obs)) {
          result = GridCallback.DELETE_DEFAULT;
        } else {
          presenter.getGridView().notifyWarning("the only limit is yourself");
          result = GridCallback.DELETE_CANCEL;
        }
      }
      return result;
    }

    @Override
    public int beforeDeleteRows(GridPresenter presenter, IsRow activeRow,
        Collection<RowInfo> selectedRows) {
      if (activeRow != null) {
        presenter.deleteRow(activeRow, false);
      }
      return GridCallback.DELETE_CANCEL;
    }

    @Override
    public void beforeRefresh(GridPresenter presenter) {
      for (Map.Entry<String, Filter> entry : getFilters().entrySet()) {
        presenter.getDataProvider().setParentFilter(entry.getKey(), entry.getValue());
      }
    }

    @Override
    public Map<String, Filter> getInitialFilters() {
      return getFilters();
    }

    private void addObservers(final GridPresenter presenter, long task, List<IsRow> users) {
      if (BeeUtils.isEmpty(users)) {
        return;
      }

      List<BeeColumn> columns =
          Lists.newArrayList(new BeeColumn(ValueType.LONG, CrmConstants.COL_TASK),
              new BeeColumn(ValueType.LONG, CrmConstants.COL_USER));
      BeeRowSet rowSet = new BeeRowSet("TaskObservers", columns);

      for (IsRow row : users) {
        rowSet.addRow(new BeeRow(DataUtils.NEW_ROW_ID,
            new String[] {BeeUtils.toString(task), BeeUtils.toString(row.getId())}));
      }

      Queries.insertRowSet(rowSet, new RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet result) {
          for (BeeRow row : result.getRows()) {
            BeeKeeper.getBus().fireEvent(new RowInsertEvent(result.getViewName(), row));
            presenter.getGridView().getGrid().insertRow(row, false);
          }
        }
      });
    }

    private Long getExecutor() {
      return executor;
    }

    private Map<String, Filter> getFilters() {
      Map<String, Filter> filters = Maps.newHashMap();

      Filter filter;
      if (getExecutor() == null) {
        filter = null;
      } else {
        filter = ComparisonFilter.isNotEqual(CrmConstants.COL_USER, new LongValue(getExecutor()));
      }
      filters.put(CrmConstants.COL_EXECUTOR, filter);

      if (getOwner() == null || getOwner().equals(getExecutor())) {
        filter = null;
      } else {
        filter = ComparisonFilter.isNotEqual(CrmConstants.COL_USER, new LongValue(getOwner()));
      }
      filters.put(CrmConstants.COL_OWNER, filter);

      return filters;
    }

    private Long getOwner() {
      return owner;
    }

    private void setExecutor(Long executor) {
      this.executor = executor;
    }

    private void setOwner(Long owner) {
      this.owner = owner;
    }
  }

  private static class TaskCreateHandler extends AbstractFormCallback {

    private class UserCollector implements MultiSelector.SelectionCallback {
      private final List<IsRow> users;
      private final BeeListBox widget;

      private final int firstNameIndex;
      private final int lastNameIndex;

      private UserCollector(List<IsRow> users, BeeListBox widget, List<BeeColumn> columns) {
        this.users = users;
        this.widget = widget;

        this.firstNameIndex = DataUtils.getColumnIndex(CrmConstants.COL_FIRST_NAME, columns);
        this.lastNameIndex = DataUtils.getColumnIndex(CrmConstants.COL_LAST_NAME, columns);
      }

      @Override
      public void onSelection(List<IsRow> rows) {
        if (!BeeUtils.isEmpty(rows)) {
          for (IsRow row : rows) {
            users.add(row);
            widget.addItem(BeeUtils.joinWords(row.getString(firstNameIndex),
                row.getString(lastNameIndex)));
          }
        }
      }
    }

    private final List<IsRow> executors = Lists.newArrayList();
    private final List<IsRow> observers = Lists.newArrayList();

    private BeeListBox executorWidget = null;
    private BeeListBox observerWidget = null;

    @Override
    public void afterCreateWidget(final String name, final Widget widget,
        WidgetDescriptionCallback callback) {

      if (BeeUtils.same(name, "ExecutorList") && widget instanceof BeeListBox) {
        executorWidget = (BeeListBox) widget;
        executorWidget.addKeyDownHandler(new KeyDownHandler() {
          @Override
          public void onKeyDown(KeyDownEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_DELETE) {
              removeUsers(executors, executorWidget,
                  EventUtils.hasModifierKey(event.getNativeEvent()));
            }
          }
        });

      } else if (BeeUtils.same(name, "ObserverList") && widget instanceof BeeListBox) {
        observerWidget = (BeeListBox) widget;
        observerWidget.addKeyDownHandler(new KeyDownHandler() {
          @Override
          public void onKeyDown(KeyDownEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_DELETE) {
              removeUsers(observers, observerWidget,
                  EventUtils.hasModifierKey(event.getNativeEvent()));
            }
          }
        });

      } else if (BeeUtils.same(name, "ExecutorAdd") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            selectUsers(true);
          }
        });

      } else if (BeeUtils.same(name, "ObserverAdd") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            selectUsers(false);
          }
        });
      }
    }

    @Override
    public FormCallback getInstance() {
      return new TaskCreateHandler();
    }

    @Override
    public boolean onReadyForInsert(final ReadyForInsertEvent event) {
      if (executors.isEmpty()) {
        event.getCallback().onFailure("Pasirinkite vykdytoją");
        return false;
      }

      List<String> missing = Lists.newArrayList();

      for (String colName : new String[] {"StartTime", "FinishTime", "Description"}) {
        if (!DataUtils.contains(event.getColumns(), colName)) {
          missing.add(colName);
        }
      }

      if (!missing.isEmpty()) {
        event.getCallback().onFailure(missing.toString(), "value required");
        return false;
      }
      
      BeeRowSet rs = new BeeRowSet(VIEW_TASKS, event.getColumns());
      rs.addRow(0, event.getValues().toArray(new String[0]));

      ParameterList args = createParams(TaskEvent.ACTIVATED.name());
      args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
      args.addDataItem(CrmConstants.VAR_TASK_EXECUTORS, joinUsers(executors));

      if (!observers.isEmpty()) {
        args.addDataItem(CrmConstants.VAR_TASK_OBSERVERS, joinUsers(observers));
      }
      
      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          Assert.notNull(response);

          if (response.hasErrors()) {
            event.getCallback().onFailure(response.getErrors());

          } else if (response.hasResponse(Integer.class)) {
            event.getCallback().onSuccess(null);
            resetUsers();
            
            GridView gridView = getGridView();
            if (gridView != null) {
              gridView.notifyInfo("New tasks created", (String) response.getResponse());
              gridView.getViewPresenter().handleAction(Action.REFRESH);
            }

          } else {
            event.getCallback().onFailure("Unknown response");
          }
        }
      });
      return false;
    }
    
    @Override
    public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
      resetUsers();
      Long userId = BeeKeeper.getUser().getUserId();
      newRow.setValue(form.getDataIndex(CrmConstants.COL_OWNER), userId);
      newRow.setValue(form.getDataIndex(CrmConstants.COL_EXECUTOR), userId);

      newRow.setValue(form.getDataIndex("StartTime"), System.currentTimeMillis());
      newRow.setValue(form.getDataIndex(CrmConstants.COL_EVENT), TaskEvent.ACTIVATED.ordinal());
      newRow.setValue(form.getDataIndex(CrmConstants.COL_PRIORITY), Priority.MEDIUM.ordinal());
    }

    private String joinUsers(List<IsRow> users) {
      StringBuilder sb = new StringBuilder();

      for (IsRow row : users) {
        if (sb.length() > 0) {
          sb.append(BeeConst.CHAR_COMMA);
        }
        sb.append(row.getId());
      }
      return sb.toString();
    }

    private void removeUsers(List<IsRow> users, BeeListBox widget, boolean all) {
      if (all) {
        users.clear();
        widget.clear();
      } else {
        int index = widget.getSelectedIndex();

        if (BeeUtils.betweenExclusive(index, 0, widget.getItemCount())) {
          users.remove(index);
          widget.removeItem(index);
        }
      }
    }

    private void resetUsers() {
      removeUsers(executors, executorWidget, true);
      removeUsers(observers, observerWidget, true);
    }

    private void selectUsers(final boolean ex) {
      List<Filter> filters = Lists.newArrayList();
      if (!ex) {
        filters.add(excludeUser(BeeKeeper.getUser().getUserId()));
      }

      for (IsRow row : executors) {
        filters.add(excludeUser(row.getId()));
      }
      for (IsRow row : observers) {
        filters.add(excludeUser(row.getId()));
      }

      Queries.getRowSet("Users", null, Filter.and(filters), null, new RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet result) {
          if (result.isEmpty()) {
            Global.showError("No more heroes any more");
            return;
          }

          MultiSelector selector = new MultiSelector(ex ? "Vykdytojai" : "Stebėtojai", result,
              Lists.newArrayList(CrmConstants.COL_FIRST_NAME, CrmConstants.COL_LAST_NAME),
              new UserCollector(ex ? executors : observers, ex ? executorWidget : observerWidget,
                  result.getColumns()));
          selector.center();
        }
      });
    }
  }

  private static class TaskDialog extends DialogBox {
    private static final String DATE = "date";
    private static final String QUESTION = "question";
    private static final String COMMENT = "comment";
    private static final String MINUTES = "minutes";
    private static final String SELECTOR = "selector";
    private static final String PRIORITY = "priority";
    private final Map<String, Widget> dialogWidgets = Maps.newHashMap();
    private FlexTable container = null;

    public TaskDialog(String caption) {
      super(caption);
      addDefaultCloseBox();

      Absolute panel = new Absolute(Position.RELATIVE);
      setWidget(panel);
      container = new FlexTable();
      panel.add(container);
      container.setCellSpacing(5);
    }

    public void addAction(FlexTable parent, String caption, ClickHandler clickHandler) {
      int row = parent.getRowCount();

      BeeButton button = new BeeButton(caption);
      parent.setWidget(row, 0, button);
      FlexCellFormatter formater = parent.getFlexCellFormatter();
      formater.setColSpan(row, 0, 2);
      formater.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);

      button.addClickHandler(clickHandler);
    }

    public void addAction(String caption, ClickHandler clickHandler) {
      addAction(container, caption, clickHandler);
    }

    public void addComment(FlexTable parent, String caption, boolean required, boolean showDuration) {
      int row = parent.getRowCount();

      BeeLabel lbl = new BeeLabel(caption);
      if (required) {
        lbl.setStyleName(StyleUtils.NAME_REQUIRED);
      }
      parent.setWidget(row, 0, lbl);
      InputArea comment = new InputArea();
      comment.setVisibleLines(5);
      comment.setCharacterWidth(40);
      parent.setWidget(row, 1, comment);
      dialogWidgets.put(COMMENT, comment);

      if (showDuration) {
        row++;
        Disclosure panel = new Disclosure(new BeeLabel("Laiko registracija"));
        FlexTable flex = new FlexTable();
        flex.setCellSpacing(5);
        panel.setContentWidget(flex);
        parent.getFlexCellFormatter().setColSpan(row, 0, 2);
        parent.setWidget(row, 0, panel);

        addMinutes(flex, "Sugaišta minučių", 0, 0, 1440, 5);
        addSelector(SELECTOR, flex, "Darbo tipas", "DurationTypes",
            Lists.newArrayList(CrmConstants.COL_NAME), false);
        addDate(flex, "Atlikimo data", ValueType.DATE, false,
            new Long(TimeUtils.today(0).getDays()));
      }
    }

    public void addComment(String caption, boolean required, boolean showDuration) {
      addComment(container, caption, required, showDuration);
    }

    public void addDate(FlexTable parent, String caption, ValueType dateType, boolean required,
        Long def) {
      int row = parent.getRowCount();

      BeeLabel lbl = new BeeLabel(caption);
      if (required) {
        lbl.setStyleName(StyleUtils.NAME_REQUIRED);
      }
      parent.setWidget(row, 0, lbl);
      InputDate date = new InputDate(dateType);
      parent.setWidget(row, 1, date);

      if (def != null) {
        date.setValue(BeeUtils.toString(def));
      }
      dialogWidgets.put(DATE, date);
    }

    public void addDate(String caption, ValueType dateType, boolean required, Long def) {
      addDate(container, caption, dateType, required, def);
    }

    public void addMinutes(FlexTable parent, String caption, int def, int min, int max, int step) {
      int row = parent.getRowCount();

      parent.setWidget(row, 0, new BeeLabel(caption));
      InputSpinner minutes = new InputSpinner(def, min, max, step);
      minutes.setWidth("4em");
      parent.setWidget(row, 1, minutes);
      dialogWidgets.put(MINUTES, minutes);
    }

    public void addMinutes(String caption, int def, int min, int max, int step) {
      addMinutes(container, caption, def, min, max, step);
    }

    public void addPriority(FlexTable parent, String caption, int def) {
      int row = parent.getRowCount();

      parent.setWidget(row, 0, new BeeLabel(caption));
      BeeListBox list = new BeeListBox();
      list.addCaptions(Priority.class);

      list.setValueNumeric(true);
      list.setValue(BeeUtils.toString(def));

      parent.setWidget(row, 1, list);
      dialogWidgets.put(PRIORITY, list);
    }

    public void addPriority(String caption, int def) {
      addPriority(container, caption, def);
    }

    public void addQuestion(FlexTable parent, String caption, boolean def) {
      int row = parent.getRowCount();

      BeeCheckBox question = new BeeCheckBox(caption);
      question.setValue(def);
      parent.setWidget(row, 1, question);
      dialogWidgets.put(QUESTION, question);
    }

    public void addQuestion(String caption, boolean def) {
      addQuestion(container, caption, def);
    }

    public void addSelector(String id, FlexTable parent, String caption, String relView,
        List<String> relColumns, boolean required) {
      int row = parent.getRowCount();

      BeeLabel lbl = new BeeLabel(caption);
      if (required) {
        lbl.setStyleName(StyleUtils.NAME_REQUIRED);
      }
      parent.setWidget(row, 0, lbl);

      DataSelector selector = new DataSelector(Relation.create(relView, relColumns), true);
      selector.addSimpleHandler(RendererFactory.createRenderer(relView, relColumns));

      parent.setWidget(row, 1, selector);
      dialogWidgets.put(id, selector);
    }

    public void addSelector(String caption, String relView, List<String> relColumns,
        boolean required) {
      addSelector(SELECTOR, caption, relView, relColumns, required);
    }

    public void addSelector(String id, String caption, String relView, List<String> relColumns,
        boolean required) {
      addSelector(id, container, caption, relView, relColumns, required);
    }

    public void display() {
      center();

      for (Widget widget : container) {
        if (widget instanceof Focusable) {
          ((Focusable) widget).setFocus(true);
          break;
        }
      }
    }

    public boolean getAnswer() {
      if (dialogWidgets.containsKey(QUESTION)) {
        return ((BeeCheckBox) dialogWidgets.get(QUESTION)).getValue();
      }
      return false;
    }

    public String getComment() {
      if (dialogWidgets.containsKey(COMMENT)) {
        return ((InputArea) dialogWidgets.get(COMMENT)).getValue();
      }
      return null;
    }

    public Long getDate() {
      if (dialogWidgets.containsKey(DATE)) {
        return BeeUtils.toLongOrNull(((InputDate) dialogWidgets.get(DATE)).getNormalizedValue());
      }
      return null;
    }

    public int getMinutes() {
      if (dialogWidgets.containsKey(MINUTES)) {
        return ((InputSpinner) dialogWidgets.get(MINUTES)).getIntValue();
      }
      return 0;
    }

    public int getPriority() {
      if (dialogWidgets.containsKey(PRIORITY)) {
        return BeeUtils.toInt(((BeeListBox) dialogWidgets.get(PRIORITY)).getValue());
      }
      return 0;
    }

    public Long getSelector() {
      return getSelector(SELECTOR);
    }

    public Long getSelector(String id) {
      if (dialogWidgets.containsKey(id)) {
        return BeeUtils.toLongOrNull(((DataSelector) dialogWidgets.get(id)).getNormalizedValue());
      }
      return null;
    }
  }

  private static class TaskEditHandler extends AbstractFormCallback {

    private final Map<String, Widget> formWidgets = Maps.newHashMap();
    private ObserverHandler observerHandler = null;

    @Override
    public void afterCreateWidget(String name, final Widget widget,
        WidgetDescriptionCallback callback) {

      if (BeeUtils.same(name, "TaskObservers") && widget instanceof ChildGrid) {
        setObserverHandler(new ObserverHandler());
        ((ChildGrid) widget).setGridCallback(getObserverHandler());

      } else if (widget instanceof HasClickHandlers) {
        setWidget(name, widget);
        TaskEvent ev;

        try {
          ev = TaskEvent.valueOf(name);
        } catch (Exception e) {
          ev = null;
        }
        if (ev != null) {
          final TaskEvent event = ev;
          ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
              doEvent(event, UiHelper.getForm(widget));
            }
          });
        }
      }
    }

    @Override
    public void afterRefresh(FormView form, IsRow row) {
      if (row == null) {
        return;
      }

      Integer idx = row.getInteger(form.getDataIndex(CrmConstants.COL_EVENT));
      if (BeeUtils.isOrdinal(TaskEvent.class, idx)) {
        Long owner = row.getLong(form.getDataIndex(CrmConstants.COL_OWNER));
        Long executor = row.getLong(form.getDataIndex(CrmConstants.COL_EXECUTOR));

        for (TaskEvent ev : TaskEvent.values()) {
          Widget widget = getWidget(ev.name());

          if (widget != null) {
            if (availableEvent(ev, idx, owner, executor)) {
              StyleUtils.unhideDisplay(widget);
            } else {
              StyleUtils.hideDisplay(widget);
            }
          }
        }
      }
    }

    @Override
    public void beforeRefresh(FormView form, IsRow row) {
      Long owner = (row == null) ? null : row.getLong(form.getDataIndex(CrmConstants.COL_OWNER));
      Long exec = (row == null) ? null : row.getLong(form.getDataIndex(CrmConstants.COL_EXECUTOR));

      if (getObserverHandler() != null) {
        getObserverHandler().setOwner(owner);
        getObserverHandler().setExecutor(exec);
      }
    }

    @Override
    public FormCallback getInstance() {
      return new TaskEditHandler();
    }

    @Override
    public void onStartEdit(FormView form, IsRow row) {
      doEvent(TaskEvent.VISITED, form);
    }

    private ObserverHandler getObserverHandler() {
      return observerHandler;
    }

    private Widget getWidget(String name) {
      return formWidgets.get(BeeUtils.normalize(name));
    }

    private void setObserverHandler(ObserverHandler observerHandler) {
      this.observerHandler = observerHandler;
    }

    private void setWidget(String name, Widget widget) {
      formWidgets.put(BeeUtils.normalize(name), widget);
    }
  }

  private static final String VIEW_TASKS = "UserTasks";

  public static boolean availableEvent(TaskEvent ev, Integer status, Long owner, Long executor) {
    long user = BeeKeeper.getUser().getUserId();

    if (user != owner) {
      boolean ok = (ev == TaskEvent.COMMENTED || ev == TaskEvent.VISITED);

      if (!ok && user == executor) {
        ok = ((ev == TaskEvent.FORWARDED || ev == TaskEvent.COMPLETED)
            && status == TaskEvent.ACTIVATED.ordinal());
      }
      return ok;
    }
    switch (ev) {
      case COMMENTED:
      case VISITED:
      case ACTIVATED:
      case DELETED:
        return true;

      case RENEWED:
        return status != TaskEvent.ACTIVATED.ordinal();

      case FORWARDED:
      case EXTENDED:
      case SUSPENDED:
      case COMPLETED:
      case UPDATED:
        return status == TaskEvent.ACTIVATED.ordinal();

      case CANCELED:
        return BeeUtils.inList(status,
            TaskEvent.ACTIVATED.ordinal(), TaskEvent.SUSPENDED.ordinal());

      case APPROVED:
        return status == TaskEvent.COMPLETED.ordinal() && user != executor;
    }
    return true;
  }

  public static void register() {
    FormFactory.registerFormCallback("NewTask", new TaskCreateHandler());
    FormFactory.registerFormCallback("Task", new TaskEditHandler());
  }

  private static boolean availableEvent(TaskEvent ev, int status, FormView form) {
    IsRow row = form.getActiveRow();
    return availableEvent(ev, status, row.getLong(form.getDataIndex(CrmConstants.COL_OWNER)),
        row.getLong(form.getDataIndex(CrmConstants.COL_EXECUTOR)));
  }

  private static ParameterList createParams(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CrmConstants.CRM_MODULE);
    args.addQueryItem(CrmConstants.CRM_METHOD, CrmConstants.CRM_TASK_PREFIX + name);
    return args;
  }

  private static void createRequest(ParameterList args, final TaskDialog dialog,
      final FormView form, final Set<Action> actions, final boolean refreshChildren) {
    if (dialog != null) {
      DomUtils.enableChildren(dialog, false);
    }
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (dialog != null) {
          dialog.hide();
        }
        if (response.hasErrors()) {
          Global.showError(response.getErrors());
        } else {
          if (response.hasResponse(BeeRow.class)) {
            BeeRow row = BeeRow.restore((String) response.getResponse());
            BeeKeeper.getBus().fireEvent(new RowUpdateEvent(VIEW_TASKS, row));

            if (BeeUtils.contains(actions, Action.CLOSE)) {
              form.fireEvent(new ActionEvent(actions));
            } else if (BeeUtils.contains(actions, Action.REFRESH)) {
              form.updateRow(row, refreshChildren);
            }

          } else if (response.hasResponse(Long.class)) {
            int dataIndex = form.getDataIndex(CrmConstants.COL_LAST_ACCESS);
            String newValue = (String) response.getResponse();

            CellUpdateEvent cellUpdateEvent =
                new CellUpdateEvent(VIEW_TASKS, form.getActiveRow().getId(),
                    form.getActiveRow().getVersion(), CrmConstants.COL_LAST_ACCESS, dataIndex,
                    newValue);
            BeeKeeper.getBus().fireEvent(cellUpdateEvent);

            if (BeeUtils.contains(actions, Action.CLOSE)) {
              form.fireEvent(new ActionEvent(actions));

            } else {
              form.getActiveRow().setValue(dataIndex, newValue);
              if (BeeUtils.contains(actions, Action.REFRESH)) {
                form.refresh(refreshChildren);
              }
            }
          } else {
            Global.showError("Unknown response");
          }
        }
      }
    });
  }

  private static void doApprove(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties patvirtinimas");
    dialog.addComment("Komentaras", false, false);
    dialog.addAction("Patvirtinti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        IsRow data = form.getActiveRow();
        int evOld = data.getInteger(form.getDataIndex(CrmConstants.COL_EVENT));
        TaskEvent event = TaskEvent.APPROVED;
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, CrmConstants.COL_EVENT));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, CrmConstants.COL_EVENT, BeeUtils.toString(event.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));

        if (!BeeUtils.isEmpty(comment)) {
          args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);
        }
        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REFRESH), true);
      }
    });
    dialog.display();
  }

  private static void doCancel(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties nutraukimas");
    dialog.addComment("Komentaras", true, false);
    dialog.addAction("Nutraukti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        IsRow data = form.getActiveRow();
        int evOld = data.getInteger(form.getDataIndex(CrmConstants.COL_EVENT));
        TaskEvent event = TaskEvent.CANCELED;
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, CrmConstants.COL_EVENT));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, CrmConstants.COL_EVENT, BeeUtils.toString(event.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REFRESH), true);
      }
    });
    dialog.display();
  }

  private static void doComment(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties komentaras, laiko registracija");
    dialog.addComment("Komentaras", true, true);
    dialog.addAction("Išsaugoti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createParams(TaskEvent.COMMENTED.name());
        args.addDataItem(CrmConstants.VAR_TASK_ID, form.getActiveRow().getId());
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        int minutes = dialog.getMinutes();

        if (BeeUtils.isPositive(minutes)) {
          Long type = dialog.getSelector();
          if (!DataUtils.isId(type)) {
            Global.showError("Įveskite darbo tipą");
            return;
          }
          Long date = dialog.getDate();
          if (date == null) {
            Global.showError("Įveskite atlikimo datą");
            return;
          }
          args.addDataItem(CrmConstants.VAR_TASK_DURATION_DATE, date);
          args.addDataItem(CrmConstants.VAR_TASK_DURATION_TIME, minutes);
          args.addDataItem(CrmConstants.VAR_TASK_DURATION_TYPE, type);
        }
        createRequest(args, dialog, form, EnumSet.of(Action.REFRESH), true);
      }
    });
    dialog.display();
  }

  private static void doComplete(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties užbaigimas");
    dialog.addComment("Komentaras", true, true);
    dialog.addAction("Užbaigti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        TaskEvent event = TaskEvent.COMPLETED;
        TaskEvent ev = event;
        IsRow data = form.getActiveRow();
        int evOld = data.getInteger(form.getDataIndex(CrmConstants.COL_EVENT));
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, CrmConstants.COL_EVENT));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});

        if (Objects.equal(data.getLong(form.getDataIndex(CrmConstants.COL_OWNER)),
            BeeKeeper.getUser().getUserId())) {
          ev = TaskEvent.APPROVED;
        }
        rs.preliminaryUpdate(0, CrmConstants.COL_EVENT, BeeUtils.toString(ev.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        int minutes = dialog.getMinutes();

        if (BeeUtils.isPositive(minutes)) {
          Long type = dialog.getSelector();
          if (!DataUtils.isId(type)) {
            Global.showError("Įveskite darbo tipą");
            return;
          }
          Long date = dialog.getDate();
          if (date == null) {
            Global.showError("Įveskite atlikimo datą");
            return;
          }
          args.addDataItem(CrmConstants.VAR_TASK_DURATION_DATE, date);
          args.addDataItem(CrmConstants.VAR_TASK_DURATION_TIME, minutes);
          args.addDataItem(CrmConstants.VAR_TASK_DURATION_TYPE, type);
        }

        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REFRESH), true);
      }
    });
    dialog.display();
  }

  private static void doEvent(final TaskEvent ev, final FormView form) {
    IsRow row = form.getActiveRow();
    Assert.state(DataUtils.isId(row.getId()));

    if (!availableEvent(ev, row.getInteger(form.getDataIndex(CrmConstants.COL_EVENT)), form)) {
      Global.showError("Veiksmas neleidžiamas");
      return;
    }
    switch (ev) {
      case VISITED:
        doVisit(form);
        break;

      case COMMENTED:
        doComment(form);
        break;

      case UPDATED:
        doUpdate(form);
        break;

      case FORWARDED:
        doForward(form);
        break;

      case EXTENDED:
        doExtend(form);
        break;

      case SUSPENDED:
        doSuspend(form);
        break;

      case CANCELED:
        doCancel(form);
        break;

      case COMPLETED:
        doComplete(form);
        break;

      case APPROVED:
        doApprove(form);
        break;

      case RENEWED:
        doRenew(form);
        break;

      case ACTIVATED:
      case DELETED:
        Assert.untouchable();
    }
  }

  private static void doExtend(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties termino keitimas");
    dialog.addDate("Naujas terminas", ValueType.DATETIME, true, null);
    dialog.addComment("Komentaras", false, false);
    dialog.addAction("Keisti terminą", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        Long newTerm = dialog.getDate();
        if (newTerm == null) {
          Global.showError("Įveskite terminą");
          return;
        }
        IsRow data = form.getActiveRow();
        Long oldTerm = data.getLong(form.getDataIndex("FinishTime"));
        if (Objects.equal(newTerm, oldTerm)
            || newTerm < Math.max(data.getLong(form.getDataIndex("StartTime")),
                System.currentTimeMillis())) {
          Global.showError("Neteisingas terminas");
          return;
        }
        String comment = dialog.getComment();
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.DATETIME, "FinishTime"));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(oldTerm)});
        rs.preliminaryUpdate(0, "FinishTime", BeeUtils.toString(newTerm));

        ParameterList args = createParams(TaskEvent.EXTENDED.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));

        if (!BeeUtils.isEmpty(comment)) {
          args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);
        }
        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REFRESH), true);
      }
    });
    dialog.display();
  }

  private static void doForward(final FormView form) {
    final IsRow data = form.getActiveRow();
    final Long owner = data.getLong(form.getDataIndex(CrmConstants.COL_OWNER));
    final Long oldUser = data.getLong(form.getDataIndex(CrmConstants.COL_EXECUTOR));

    final TaskDialog dialog = new TaskDialog("Užduoties persiuntimas");
    dialog.addSelector("Vykdytojas", "Users",
        Lists.newArrayList(CrmConstants.COL_FIRST_NAME, CrmConstants.COL_LAST_NAME), true);

    if (!Objects.equal(owner, oldUser)) {
      dialog.addQuestion("Pašalinti siuntėją iš stebėtojų", false);
    }
    dialog.addComment("Komentaras", true, false);
    dialog.addAction("Persiųsti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        Long newUser = dialog.getSelector();
        if (newUser == null) {
          Global.showError("Įveskite vykdytoją");
          return;
        }
        if (Objects.equal(newUser, oldUser)) {
          Global.showError("Nurodėte tą patį vykdytoją");
          return;
        }
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.LONG, CrmConstants.COL_EXECUTOR));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(oldUser)});
        rs.preliminaryUpdate(0, CrmConstants.COL_EXECUTOR, BeeUtils.toString(newUser));

        ParameterList args = createParams(TaskEvent.FORWARDED.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        if (!Objects.equal(owner, oldUser) && dialog.getAnswer()) {
          args.addDataItem(CrmConstants.VAR_TASK_OBSERVE, BeeUtils.toString(true));
        }
        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REFRESH), true);
      }
    });
    dialog.display();
  }

  private static void doRenew(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties grąžinimas vykdymui");
    dialog.addComment("Komentaras", false, false);
    dialog.addAction("Grąžinti vykdymui", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        IsRow data = form.getActiveRow();
        int evOld = data.getInteger(form.getDataIndex(CrmConstants.COL_EVENT));
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, CrmConstants.COL_EVENT));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, CrmConstants.COL_EVENT,
            BeeUtils.toString(TaskEvent.ACTIVATED.ordinal()));

        ParameterList args = createParams(TaskEvent.RENEWED.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));

        if (!BeeUtils.isEmpty(comment)) {
          args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);
        }
        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REFRESH), true);
      }
    });
    dialog.display();
  }

  private static void doSuspend(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties sustabdymas");
    dialog.addComment("Komentaras", true, false);
    dialog.addAction("Sustabdyti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        IsRow data = form.getActiveRow();
        int evOld = data.getInteger(form.getDataIndex(CrmConstants.COL_EVENT));
        TaskEvent event = TaskEvent.SUSPENDED;
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, CrmConstants.COL_EVENT));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, CrmConstants.COL_EVENT, BeeUtils.toString(event.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REFRESH), true);
      }
    });
    dialog.display();
  }

  private static void doUpdate(final FormView form) {
    final IsRow data = form.getActiveRow();
    final int oldPriority =
        BeeUtils.unbox(data.getInteger(form.getDataIndex(CrmConstants.COL_PRIORITY)));
    final int oldTerm = BeeUtils.unbox(data.getInteger(form.getDataIndex("ExpectedDuration")));
    final long oldCompany = BeeUtils.unbox(data.getLong(form.getDataIndex("Company")));
    final long oldPerson = BeeUtils.unbox(data.getLong(form.getDataIndex("CompanyPerson")));

    final TaskDialog dialog = new TaskDialog("Užduoties koregavimas");
    dialog.addPriority("Prioritetas", oldPriority);
    dialog.addMinutes("Numatoma trukmė min.", oldTerm, 0, 43200, 30);
    dialog.addSelector("Įmonė", "Companies", Lists.newArrayList(CrmConstants.COL_NAME), false);
    dialog.addSelector("PERSON", "Asmuo", "CompanyPersons",
        Lists.newArrayList(CrmConstants.COL_FIRST_NAME, CrmConstants.COL_LAST_NAME), false);
    dialog.addAction("Išsaugoti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        List<BeeColumn> cols = Lists.newArrayList();
        List<String> old = Lists.newArrayList();
        List<String> vals = Lists.newArrayList();

        int priority = dialog.getPriority();
        if (!Objects.equal(oldPriority, priority)) {
          cols.add(new BeeColumn(ValueType.INTEGER, CrmConstants.COL_PRIORITY));
          old.add(BeeUtils.toString(oldPriority));
          vals.add(BeeUtils.toString(priority));
        }
        int term = dialog.getMinutes();
        if (!Objects.equal(oldTerm, term)) {
          cols.add(new BeeColumn(ValueType.DATETIME, "ExpectedDuration"));
          old.add(BeeUtils.toString(oldTerm));
          vals.add(BeeUtils.toString(term));
        }
        Long company = dialog.getSelector();
        if (DataUtils.isId(company) && !Objects.equal(oldCompany, company)) {
          cols.add(new BeeColumn(ValueType.LONG, "Company"));
          old.add(BeeUtils.toString(oldCompany));
          vals.add(BeeUtils.toString(company));
        }
        Long person = dialog.getSelector("PERSON");
        if (DataUtils.isId(person) && !Objects.equal(oldPerson, person)) {
          cols.add(new BeeColumn(ValueType.LONG, "CompanyPerson"));
          old.add(BeeUtils.toString(oldPerson));
          vals.add(BeeUtils.toString(person));
        }
        if (cols.isEmpty()) {
          Global.showError("Nėra pakeitimų");
        } else {
          BeeRowSet rs = new BeeRowSet(VIEW_TASKS, cols);
          BeeRow row = new BeeRow(data.getId(), data.getVersion(), old.toArray(new String[0]));
          rs.addRow(row);

          for (int i = 0; i < vals.size(); i++) {
            row.preliminaryUpdate(i, vals.get(i));
          }
          ParameterList args = createParams(TaskEvent.UPDATED.name());
          args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));

          createRequest(args, dialog, form, EnumSet.of(Action.REFRESH), true);
        }
      }
    });
    dialog.display();
  }

  private static void doVisit(FormView form) {
    ParameterList args = createParams(TaskEvent.VISITED.name());
    args.addDataItem(CrmConstants.VAR_TASK_ID, form.getActiveRow().getId());

    createRequest(args, null, form, null, false);
  }

  private static Filter excludeUser(long userId) {
    return ComparisonFilter.compareId(Operator.NE, userId);
  }

  private TaskEventHandler() {
  }
}
