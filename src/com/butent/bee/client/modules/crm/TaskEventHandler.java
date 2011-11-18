package com.butent.bee.client.modules.crm;

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
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.FlexTable.FlexCellFormatter;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.presenter.Action;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.edit.EditFormEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.Priority;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaskEventHandler {

  private static class TaskCreateHandler extends AbstractFormCallback {

    private class UserCollector implements MultiSelector.SelectionCallback {
      private final List<IsRow> users;
      private final BeeListBox widget;

      private final int firstNameIndex;
      private final int lastNameIndex;

      private UserCollector(List<IsRow> users, BeeListBox widget, List<BeeColumn> columns) {
        this.users = users;
        this.widget = widget;
        
        this.firstNameIndex = DataUtils.getColumnIndex(CrmConstants.COLUMN_FIRST_NAME, columns);
        this.lastNameIndex = DataUtils.getColumnIndex(CrmConstants.COLUMN_LAST_NAME, columns);
      }

      public void onSelection(List<IsRow> rows) {
        if (!BeeUtils.isEmpty(rows)) {
          for (IsRow row : rows) {
            users.add(row);
            widget.addItem(BeeUtils.concat(1, row.getString(firstNameIndex),
                row.getString(lastNameIndex)));
          }
          widget.setAllVisible();
        }
      }
    }

    private static int counter = 0;

    private final List<IsRow> executors = Lists.newArrayList();
    private final List<IsRow> observers = Lists.newArrayList();

    private BeeListBox executorWidget = null;
    private BeeListBox observerWidget = null;

    @Override
    public void afterCreateWidget(final String name, final Widget widget) {
      if (BeeUtils.same(name, "Priority") && widget instanceof BeeListBox) {
        for (Priority priority : Priority.values()) {
          ((BeeListBox) widget).addItem(priority.name());
        }

      } else if (BeeUtils.same(name, "ExecutorList") && widget instanceof BeeListBox) {
        executorWidget = (BeeListBox) widget;
        executorWidget.addKeyDownHandler(new KeyDownHandler() {
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
          public void onKeyDown(KeyDownEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_DELETE) {
              removeUsers(observers, observerWidget,
                  EventUtils.hasModifierKey(event.getNativeEvent()));
            }
          }
        });

      } else if (BeeUtils.same(name, "ExecutorAdd") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            selectUsers(true);
          }
        });

      } else if (BeeUtils.same(name, "ObserverAdd") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent event) {
            selectUsers(false);
          }
        });
      }
    }

    @Override
    public TaskCreateHandler getInstance() {
      if (counter++ == 0) {
        return this;
      } else {
        return new TaskCreateHandler();
      }
    }

    @Override
    public boolean onPrepareForInsert(FormView form, final DataView dataView, IsRow row) {
      Assert.noNulls(dataView, row);
      
      if (executors.isEmpty()) {
        dataView.notifySevere("Pasirinkite vykdytoją");
        return false;
      }

      List<BeeColumn> columns = Lists.newArrayList();
      List<String> values = Lists.newArrayList();

      for (BeeColumn column : form.getDataColumns()) {
        String colName = column.getId();
        String value = row.getString(form.getDataIndex(colName));
        
        if (!BeeUtils.isEmpty(value) || BeeUtils.same(colName, CrmConstants.COLUMN_EXECUTOR)) {
          columns.add(column);
          values.add(value);
        } else if (BeeUtils.inListSame(colName, "StartTime", "FinishTime", "Description")) {
          dataView.notifySevere(colName + ": value required");
          return false;
        }
      }

      BeeRowSet rs = new BeeRowSet(columns);
      rs.setViewName(VIEW_NAME);
      rs.addRow(0, values.toArray(new String[0]));

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
            dataView.notifySevere(response.getErrors());
          } else if (response.hasResponse(BeeRowSet.class)) {
            BeeRowSet rowSet = BeeRowSet.restore((String) response.getResponse());
            if (rowSet.isEmpty()) {
              dataView.notifySevere("Response empty");
            } else if (rowSet.getNumberOfRows() == 1) {
              dataView.finishNewRow(rowSet.getRow(0));
            } else {
              dataView.getViewPresenter().handleAction(Action.REQUERY);
              dataView.finishNewRow(null);
              resetUsers();
            }
          } else {
            dataView.notifySevere("Unknown response");
          }
        }
      });
      return false;
    }

    @Override
    public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
      newRow.setValue(form.getDataIndex("Owner"), BeeKeeper.getUser().getUserId());
      newRow.setValue(form.getDataIndex("StartTime"), System.currentTimeMillis());
      newRow.setValue(form.getDataIndex(EVENT_NAME), TaskEvent.ACTIVATED.ordinal());
      newRow.setValue(form.getDataIndex("Priority"), Priority.MEDIUM.ordinal());
    }
    
    private Filter excludeUser(long userId) {
      return ComparisonFilter.compareId(CrmConstants.COLUMN_USER_ID, Operator.NE, userId);
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
        int index = executorWidget.getSelectedIndex();
        if (BeeUtils.betweenExclusive(index, 0, widget.getItemCount())) {
          users.remove(index);
          widget.removeItem(index);
        }
      }
      widget.setVisibleItemCount(Math.max(widget.getItemCount(), 1));
    }
    
    private void resetUsers() {
      removeUsers(executors, executorWidget, true);
      removeUsers(observers, observerWidget, true);
    }
    
    private void selectUsers(final boolean ex) {
      List<Filter> filters = Lists.newArrayList(excludeUser(BeeKeeper.getUser().getUserId()));
      for (IsRow row : executors) {
        filters.add(excludeUser(row.getId()));
      }
      for (IsRow row : observers) {
        filters.add(excludeUser(row.getId()));
      }
      
      Queries.getRowSet("Users", null, CompoundFilter.and(filters), null, new RowSetCallback() {
        public void onFailure(String[] reason) {
        }

        public void onSuccess(BeeRowSet result) {
          if (result.isEmpty()) {
            Global.showError("No more heroes any more");
            return;
          }

          MultiSelector selector = new MultiSelector(ex ? "Vykdytojai" : "Stebėtojai", result,
              Lists.newArrayList(CrmConstants.COLUMN_FIRST_NAME, CrmConstants.COLUMN_LAST_NAME),
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
    private Map<String, Widget> dialogWidgets = Maps.newHashMap();
    private FlexTable container = null;

    public TaskDialog(String caption) {
      super(caption);
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
        addSelector(SELECTOR, flex, "Darbo tipas", "DurationTypes", "Name", false);
        addDate(flex, "Atlikimo data", ValueType.DATE, false, new Long(TimeUtils.today(0).getDay()));
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

      for (Priority value : Priority.values()) {
        list.addItem(value.name());
      }
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
        String relColumn, boolean required) {
      int row = parent.getRowCount();

      BeeLabel lbl = new BeeLabel(caption);
      if (required) {
        lbl.setStyleName(StyleUtils.NAME_REQUIRED);
      }
      parent.setWidget(row, 0, lbl);
      BeeColumn col = new BeeColumn(ValueType.LONG, "Dummy");
      DataSelector selector = new DataSelector(
          RelationInfo.create(Lists.newArrayList(col), null, col.getId(), relView, relColumn),
          true);
      parent.setWidget(row, 1, selector);
      dialogWidgets.put(id, selector);
    }

    public void addSelector(String caption, String relView, String relColumn,
        boolean required) {
      addSelector(SELECTOR, caption, relView, relColumn, required);
    }

    public void addSelector(String id, String caption, String relView, String relColumn,
        boolean required) {
      addSelector(id, container, caption, relView, relColumn, required);
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
    private static int counter = 0;
    private Map<String, Widget> formWidgets = Maps.newHashMap();

    @Override
    public void afterCreateWidget(String name, final Widget widget) {
      if (!BeeUtils.isEmpty(name) && BeeUtils.inListSame(name, "Priority", EVENT_NAME)) {
        setWidget(name, widget);

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
      String text = BeeConst.STRING_EMPTY;
      Integer idx = row.getInteger(form.getDataIndex("Priority"));

      if (BeeUtils.isOrdinal(Priority.class, idx)) {
        text = Priority.values()[idx].name();
      }
      getWidget("Priority").getElement().setInnerText(text);

      text = BeeConst.STRING_EMPTY;
      idx = row.getInteger(form.getDataIndex(EVENT_NAME));

      if (BeeUtils.isOrdinal(TaskEvent.class, idx)) {
        Long owner = row.getLong(form.getDataIndex("Owner"));
        Long executor = row.getLong(form.getDataIndex(CrmConstants.COLUMN_EXECUTOR));

        for (TaskEvent ev : TaskEvent.values()) {
          if (ev == TaskEvent.VISITED) {
            doEvent(TaskEvent.VISITED, form);

          } else if (ev == TaskEvent.ACTIVATED) {
            continue;

          } else {
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
        text = TaskEvent.values()[idx].name();
      }
      getWidget(EVENT_NAME).getElement().setInnerText(text);
    }

    @Override
    public TaskEditHandler getInstance() {
      if (counter++ == 0) {
        return this;
      } else {
        return new TaskEditHandler();
      }
    }

    private Widget getWidget(String name) {
      return formWidgets.get(BeeUtils.normalize(name));
    }

    private void setWidget(String name, Widget widget) {
      formWidgets.put(BeeUtils.normalize(name), widget);
    }
  }

  private static final String VIEW_NAME = "TaskUsers";
  private static final String EVENT_NAME = "Event";

  public static void register() {
    FormFactory.registerFormCallback("NewTask", new TaskCreateHandler());
    FormFactory.registerFormCallback("Tasks", new TaskEditHandler());
  }

  private static boolean availableEvent(TaskEvent ev, int status, FormView form) {
    IsRow row = form.getRow();
    return availableEvent(ev, status, row.getLong(form.getDataIndex("Owner")),
        row.getLong(form.getDataIndex(CrmConstants.COLUMN_EXECUTOR)));
  }

  private static boolean availableEvent(TaskEvent ev, Integer status, Long owner, Long executor) {
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

  private static ParameterList createParams(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CrmConstants.CRM_MODULE);
    args.addQueryItem(CrmConstants.CRM_METHOD, name);
    return args;
  }

  private static void createRequest(ParameterList args, final TaskDialog dialog,
      final FormView form, final Set<Action> actions) {
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
          Global.showError((Object[]) response.getErrors());
        } else {
          if (response.hasResponse(BeeRow.class)) {
            BeeRow row = BeeRow.restore((String) response.getResponse());
            BeeKeeper.getBus().fireEvent(new RowUpdateEvent(VIEW_NAME, row));

            if (BeeUtils.contains(actions, Action.CLOSE)) {
              form.fireEvent(new EditFormEvent(actions.contains(Action.REQUERY)
                  ? State.PENDING : State.CHANGED));
            } else if (BeeUtils.contains(actions, Action.REQUERY)) {
              form.updateRow(row, true);
            } else if (BeeUtils.contains(actions, Action.REFRESH)) {
              form.updateRow(row, false);
            } else {
              form.setRow(row);
            }

          } else if (response.hasResponse(Long.class)) {
            int dataIndex = form.getDataIndex(CrmConstants.COLUMN_LAST_ACCESS);
            String newValue = (String) response.getResponse();

            CellUpdateEvent cellUpdateEvent =
                new CellUpdateEvent(VIEW_NAME, form.getRow().getId(), form.getRow().getVersion(),
                    CrmConstants.COLUMN_LAST_ACCESS, dataIndex, newValue);
            BeeKeeper.getBus().fireEvent(cellUpdateEvent);

            if (BeeUtils.contains(actions, Action.CLOSE)) {
              form.fireEvent(new EditFormEvent(actions.contains(Action.REQUERY)
                  ? State.PENDING : State.CHANGED));
            } else {
              form.getRow().setValue(dataIndex, newValue);
              if (BeeUtils.contains(actions, Action.REQUERY)) {
                form.refresh(true);
              } else if (BeeUtils.contains(actions, Action.REFRESH)) {
                form.refresh(false);
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
        IsRow data = form.getRow();
        int evOld = data.getInteger(form.getDataIndex(EVENT_NAME));
        TaskEvent event = TaskEvent.APPROVED;
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, EVENT_NAME));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, EVENT_NAME, BeeUtils.toString(event.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));

        if (!BeeUtils.isEmpty(comment)) {
          args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);
        }
        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REQUERY));
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
        IsRow data = form.getRow();
        int evOld = data.getInteger(form.getDataIndex(EVENT_NAME));
        TaskEvent event = TaskEvent.CANCELED;
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, EVENT_NAME));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, EVENT_NAME, BeeUtils.toString(event.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REQUERY));
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
        args.addDataItem(CrmConstants.VAR_TASK_ID, form.getRow().getId());
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        int minutes = dialog.getMinutes();

        if (BeeUtils.isPositive(minutes)) {
          Long type = dialog.getSelector();
          if (BeeUtils.isEmpty(type)) {
            Global.showError("Įveskite darbo tipą");
            return;
          }
          Long date = dialog.getDate();
          if (BeeUtils.isEmpty(date)) {
            Global.showError("Įveskite atlikimo datą");
            return;
          }
          args.addDataItem(CrmConstants.VAR_TASK_DURATION_DATE, BeeUtils.transform(date));
          args.addDataItem(CrmConstants.VAR_TASK_DURATION_TIME, BeeUtils.transform(minutes));
          args.addDataItem(CrmConstants.VAR_TASK_DURATION_TYPE, BeeUtils.transform(type));
        }
        createRequest(args, dialog, form, EnumSet.of(Action.REQUERY));
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
        IsRow data = form.getRow();
        int evOld = data.getInteger(form.getDataIndex(EVENT_NAME));
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, EVENT_NAME));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});

        if (BeeUtils.equals(data.getLong(form.getDataIndex("Owner")),
            BeeKeeper.getUser().getUserId())) {
          ev = TaskEvent.APPROVED;
        }
        rs.preliminaryUpdate(0, EVENT_NAME, BeeUtils.toString(ev.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        int minutes = dialog.getMinutes();

        if (BeeUtils.isPositive(minutes)) {
          Long type = dialog.getSelector();
          if (BeeUtils.isEmpty(type)) {
            Global.showError("Įveskite darbo tipą");
            return;
          }
          Long date = dialog.getDate();
          if (BeeUtils.isEmpty(date)) {
            Global.showError("Įveskite atlikimo datą");
            return;
          }
          args.addDataItem(CrmConstants.VAR_TASK_DURATION_DATE, BeeUtils.transform(date));
          args.addDataItem(CrmConstants.VAR_TASK_DURATION_TIME, BeeUtils.transform(minutes));
          args.addDataItem(CrmConstants.VAR_TASK_DURATION_TYPE, BeeUtils.transform(type));
        }

        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REQUERY));
      }
    });
    dialog.display();
  }

  private static void doEvent(final TaskEvent ev, final FormView form) {
    Assert.notEmpty(form.getRow().getId());

    if (!availableEvent(ev, form.getRow().getInteger(form.getDataIndex(EVENT_NAME)), form)) {
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
        if (BeeUtils.isEmpty(newTerm)) {
          Global.showError("Įveskite terminą");
          return;
        }
        IsRow data = form.getRow();
        Long oldTerm = data.getLong(form.getDataIndex("FinishTime"));
        if (BeeUtils.equals(newTerm, oldTerm)
            || newTerm < BeeUtils.max(data.getLong(form.getDataIndex("StartTime")),
                System.currentTimeMillis())) {
          Global.showError("Neteisingas terminas");
          return;
        }
        String comment = dialog.getComment();
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.DATETIME, "FinishTime"));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(oldTerm)});
        rs.preliminaryUpdate(0, "FinishTime", BeeUtils.toString(newTerm));

        ParameterList args = createParams(TaskEvent.EXTENDED.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));

        if (!BeeUtils.isEmpty(comment)) {
          args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);
        }
        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE));
      }
    });
    dialog.display();
  }

  private static void doForward(final FormView form) {
    final IsRow data = form.getRow();
    final Long owner = data.getLong(form.getDataIndex("Owner"));
    final Long oldUser = data.getLong(form.getDataIndex(CrmConstants.COLUMN_EXECUTOR));

    final TaskDialog dialog = new TaskDialog("Užduoties persiuntimas");
    dialog.addSelector("Vykdytojas", "Users", CrmConstants.COLUMN_FIRST_NAME, true);

    if (!BeeUtils.equals(owner, oldUser)) {
      dialog.addQuestion("Pašalinti siuntėją iš stebėtojų", false);
    }
    dialog.addComment("Komentaras", true, false);
    dialog.addAction("Persiųsti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        Long newUser = dialog.getSelector();
        if (BeeUtils.isEmpty(newUser)) {
          Global.showError("Įveskite vykdytoją");
          return;
        }
        if (BeeUtils.equals(newUser, oldUser)) {
          Global.showError("Nurodėte tą patį vykdytoją");
          return;
        }
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.LONG, CrmConstants.COLUMN_EXECUTOR));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(oldUser)});
        rs.preliminaryUpdate(0, CrmConstants.COLUMN_EXECUTOR, BeeUtils.toString(newUser));

        ParameterList args = createParams(TaskEvent.FORWARDED.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        if (!BeeUtils.equals(owner, oldUser) && dialog.getAnswer()) {
          args.addDataItem(CrmConstants.VAR_TASK_OBSERVE, true);
        }
        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REQUERY));
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
        IsRow data = form.getRow();
        int evOld = data.getInteger(form.getDataIndex(EVENT_NAME));
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, EVENT_NAME));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, EVENT_NAME, BeeUtils.toString(TaskEvent.ACTIVATED.ordinal()));

        ParameterList args = createParams(TaskEvent.RENEWED.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));

        if (!BeeUtils.isEmpty(comment)) {
          args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);
        }
        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REQUERY));
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
        IsRow data = form.getRow();
        int evOld = data.getInteger(form.getDataIndex(EVENT_NAME));
        TaskEvent event = TaskEvent.SUSPENDED;
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, EVENT_NAME));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, EVENT_NAME, BeeUtils.toString(event.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REQUERY));
      }
    });
    dialog.display();
  }

  private static void doUpdate(final FormView form) {
    final IsRow data = form.getRow();
    final int oldPriority = BeeUtils.unbox(data.getInteger(form.getDataIndex("Priority")));
    final int oldTerm = BeeUtils.unbox(data.getInteger(form.getDataIndex("ExpectedDuration")));
    final long oldCompany = BeeUtils.unbox(data.getLong(form.getDataIndex("Company")));
    final long oldPerson = BeeUtils.unbox(data.getLong(form.getDataIndex("CompanyPerson")));

    final TaskDialog dialog = new TaskDialog("Užduoties koregavimas");
    dialog.addPriority("Prioritetas", oldPriority);
    dialog.addMinutes("Numatoma trukmė", oldTerm, 0, 43200, 30);
    dialog.addSelector("Įmonė", "Companies", "Name", false);
    dialog.addSelector("PERSON", "Asmuo", "CompanyPersons", CrmConstants.COLUMN_FIRST_NAME, false);
    dialog.addAction("Išsaugoti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        List<BeeColumn> cols = Lists.newArrayList();
        List<String> old = Lists.newArrayList();
        List<String> vals = Lists.newArrayList();

        int priority = dialog.getPriority();
        if (!BeeUtils.equals(oldPriority, priority)) {
          cols.add(new BeeColumn(ValueType.INTEGER, "Priority"));
          old.add(BeeUtils.toString(oldPriority));
          vals.add(BeeUtils.toString(priority));
        }
        int term = dialog.getMinutes();
        if (!BeeUtils.equals(oldTerm, term)) {
          cols.add(new BeeColumn(ValueType.DATETIME, "ExpectedDuration"));
          old.add(BeeUtils.toString(oldTerm));
          vals.add(BeeUtils.toString(term));
        }
        Long company = dialog.getSelector();
        if (!BeeUtils.isEmpty(company) && !BeeUtils.equals(oldCompany, company)) {
          cols.add(new BeeColumn(ValueType.LONG, "Company"));
          old.add(BeeUtils.toString(oldCompany));
          vals.add(BeeUtils.toString(company));
        }
        Long person = dialog.getSelector("PERSON");
        if (!BeeUtils.isEmpty(person) && !BeeUtils.equals(oldPerson, person)) {
          cols.add(new BeeColumn(ValueType.LONG, "CompanyPerson"));
          old.add(BeeUtils.toString(oldPerson));
          vals.add(BeeUtils.toString(person));
        }
        if (cols.isEmpty()) {
          Global.showError("Nėra pakeitimų");
        } else {
          BeeRowSet rs = new BeeRowSet(VIEW_NAME, cols);
          BeeRow row = new BeeRow(data.getId(), data.getVersion(), old.toArray(new String[0]));
          rs.addRow(row);

          for (int i = 0; i < vals.size(); i++) {
            row.preliminaryUpdate(i, vals.get(i));
          }
          ParameterList args = createParams(TaskEvent.UPDATED.name());
          args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));

          createRequest(args, dialog, form, EnumSet.of(Action.REQUERY));
        }
      }
    });
    dialog.display();
  }

  private static void doVisit(FormView form) {
    ParameterList args = createParams(TaskEvent.VISITED.name());
    args.addDataItem(CrmConstants.VAR_TASK_ID, form.getRow().getId());

    createRequest(args, null, form, null);
  }

  private TaskEventHandler() {
  }
}
