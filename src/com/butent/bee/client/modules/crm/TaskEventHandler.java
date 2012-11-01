package com.butent.bee.client.modules.crm;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.Disclosure;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.render.RendererFactory;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;
import java.util.Map;

public class TaskEventHandler {

  private static class TaskCreateHandler extends AbstractFormCallback {

    @Override
    public FormCallback getInstance() {
      return new TaskCreateHandler();
    }

    @Override
    public boolean onReadyForInsert(final ReadyForInsertEvent event) {
      IsRow row = getFormView().getActiveRow();
      String executors = row.getProperty(PROP_EXECUTORS);
      String observers = row.getProperty(PROP_OBSERVERS);

      if (BeeUtils.isEmpty(executors)) {
        event.getCallback().onFailure("Pasirinkite vykdytoją");
        return false;
      }

      List<String> missing = Lists.newArrayList();
      for (String colName : new String[] {COL_START_TIME, COL_FINISH_TIME, COL_DESCRIPTION}) {
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

      ParameterList args = createParams(TaskEvent.ACTIVATED);
      args.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rs));
      args.addDataItem(VAR_TASK_EXECUTORS, executors);

      if (!BeeUtils.isEmpty(observers)) {
        args.addDataItem(VAR_TASK_OBSERVERS, observers);
      }

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          Assert.notNull(response);

          if (response.hasErrors()) {
            event.getCallback().onFailure(response.getErrors());

          } else if (response.hasResponse(Integer.class)) {
            event.getCallback().onSuccess(null);

            GridView gridView = getGridView();
            if (gridView != null) {
              gridView.notifyInfo("Sukurta naujų užduočių:", (String) response.getResponse());
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
      Long userId = BeeKeeper.getUser().getUserId();
      newRow.setValue(form.getDataIndex(COL_OWNER), userId);
      newRow.setValue(form.getDataIndex(COL_EXECUTOR), userId);

      newRow.setValue(form.getDataIndex(COL_START_TIME), TimeUtils.nextHour(0));
      newRow.setValue(form.getDataIndex(COL_EVENT), TaskEvent.ACTIVATED.ordinal());
      newRow.setValue(form.getDataIndex(COL_PRIORITY), Priority.MEDIUM.ordinal());
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
    
    private HtmlTable container = null;

    public TaskDialog(String caption) {
      super(caption);
      addDefaultCloseBox();

      Absolute panel = new Absolute(Position.RELATIVE);

      container = new HtmlTable();
      container.setBorderSpacing(5);

      panel.add(container);
      setWidget(panel);
    }

    public void addAction(HtmlTable parent, String caption, ClickHandler clickHandler) {
      int row = parent.getRowCount();

      BeeButton button = new BeeButton(caption);
      parent.setWidget(row, 0, button);
      parent.getCellFormatter().setColSpan(row, 0, 2);
      parent.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);

      button.addClickHandler(clickHandler);
    }

    public void addAction(String caption, ClickHandler clickHandler) {
      addAction(container, caption, clickHandler);
    }

    public void addComment(HtmlTable parent, String caption, boolean required, boolean showDuration) {
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
        HtmlTable table = new HtmlTable();
        table.setBorderSpacing(5);

        panel.setContentWidget(table);
        parent.getCellFormatter().setColSpan(row, 0, 2);
        parent.setWidget(row, 0, panel);

        addMinutes(table, "Sugaišta minučių", 0, 0, 1440, 5);
        addSelector(SELECTOR, table, "Darbo tipas", "DurationTypes",
            Lists.newArrayList(COL_NAME), false);
        addDate(table, "Atlikimo data", ValueType.DATE, false,
            new Long(TimeUtils.today(0).getDays()));
      }
    }

    public void addComment(String caption, boolean required, boolean showDuration) {
      addComment(container, caption, required, showDuration);
    }

    public void addDate(HtmlTable parent, String caption, ValueType dateType, boolean required,
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

    public void addMinutes(HtmlTable parent, String caption, int def, int min, int max, int step) {
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

    public void addPriority(HtmlTable parent, String caption, int def) {
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

    public void addQuestion(HtmlTable parent, String caption, boolean def) {
      int row = parent.getRowCount();

      BeeCheckBox question = new BeeCheckBox(caption);
      question.setValue(def);
      parent.setWidget(row, 1, question);
      dialogWidgets.put(QUESTION, question);
    }

    public void addQuestion(String caption, boolean def) {
      addQuestion(container, caption, def);
    }

    public void addSelector(String id, HtmlTable parent, String caption, String relView,
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

    @Override
    public void afterCreateWidget(String name, final Widget widget,
        WidgetDescriptionCallback callback) {

      if (widget instanceof HasClickHandlers) {
        setWidget(name, widget);
        final TaskEvent event = NameUtils.getEnumByName(TaskEvent.class, name);

        if (event != null) {
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

      Integer idx = row.getInteger(form.getDataIndex(COL_EVENT));
      if (BeeUtils.isOrdinal(TaskEvent.class, idx)) {
        Long owner = row.getLong(form.getDataIndex(COL_OWNER));
        Long executor = row.getLong(form.getDataIndex(COL_EXECUTOR));

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
    public FormCallback getInstance() {
      return new TaskEditHandler();
    }

    @Override
    public boolean onStartEdit(final FormView form, final IsRow row,
        final Scheduler.ScheduledCommand focusCommand) {
      ParameterList args = createParams(TaskEvent.VISITED);
      args.addDataItem(VAR_TASK_ID, row.getId());
      
      final Long owner = row.getLong(form.getDataIndex(COL_OWNER));
      final Long executor = row.getLong(form.getDataIndex(COL_EXECUTOR));

      sendRequest(args, new Callback<ResponseObject>() {
        @Override
        public void onFailure(String... reason) {
          row.clearProperty(PROP_OBSERVERS);
          form.updateRow(row, true);

          form.notifySevere(reason);
        }

        @Override
        public void onSuccess(ResponseObject result) {
          if (result.hasResponse(String.class)) {
            List<Long> observers = DataUtils.parseIdList((String) result.getResponse());
            observers.remove(owner);
            observers.remove(executor);
            
            row.setProperty(PROP_OBSERVERS, DataUtils.buildIdList(observers));
          } else {
            row.clearProperty(PROP_OBSERVERS);
          }

          form.updateRow(row, true);
          if (focusCommand != null) {
            focusCommand.execute();
          }
        }
      });
      return false;
    }

    private Widget getWidget(String name) {
      return formWidgets.get(BeeUtils.normalize(name));
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
    return availableEvent(ev, status, row.getLong(form.getDataIndex(COL_OWNER)),
        row.getLong(form.getDataIndex(COL_EXECUTOR)));
  }

  private static ParameterList createParams(TaskEvent event) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CRM_MODULE);
    args.addQueryItem(CRM_METHOD, CRM_TASK_PREFIX + event.name());
    return args;
  }

  private static void sendRequest(ParameterList args, final Callback<ResponseObject> callback) {
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasErrors()) {
          callback.onFailure(response.getErrors());
        } else {
          callback.onSuccess(response);
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
        int evOld = data.getInteger(form.getDataIndex(COL_EVENT));
        TaskEvent event = TaskEvent.APPROVED;
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, COL_EVENT));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, COL_EVENT, BeeUtils.toString(event.ordinal()));

        ParameterList args = createParams(event);
        args.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rs));

        if (!BeeUtils.isEmpty(comment)) {
          args.addDataItem(VAR_TASK_COMMENT, comment);
        }

        sendRequest(args, null);
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
        int evOld = data.getInteger(form.getDataIndex(COL_EVENT));
        TaskEvent event = TaskEvent.CANCELED;
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, COL_EVENT));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, COL_EVENT, BeeUtils.toString(event.ordinal()));

        ParameterList args = createParams(event);
        args.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(VAR_TASK_COMMENT, comment);

        sendRequest(args, null);
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
        ParameterList args = createParams(TaskEvent.COMMENTED);
        args.addDataItem(VAR_TASK_ID, form.getActiveRow().getId());
        args.addDataItem(VAR_TASK_COMMENT, comment);

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
          args.addDataItem(VAR_TASK_DURATION_DATE, date);
          args.addDataItem(VAR_TASK_DURATION_TIME, minutes);
          args.addDataItem(VAR_TASK_DURATION_TYPE, type);
        }

        sendRequest(args, null);
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
        int evOld = data.getInteger(form.getDataIndex(COL_EVENT));
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, COL_EVENT));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});

        if (Objects.equal(data.getLong(form.getDataIndex(COL_OWNER)),
            BeeKeeper.getUser().getUserId())) {
          ev = TaskEvent.APPROVED;
        }
        rs.preliminaryUpdate(0, COL_EVENT, BeeUtils.toString(ev.ordinal()));

        ParameterList args = createParams(event);
        args.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(VAR_TASK_COMMENT, comment);

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
          args.addDataItem(VAR_TASK_DURATION_DATE, date);
          args.addDataItem(VAR_TASK_DURATION_TIME, minutes);
          args.addDataItem(VAR_TASK_DURATION_TYPE, type);
        }

        sendRequest(args, null);
      }
    });
    dialog.display();
  }

  private static void doEvent(final TaskEvent ev, final FormView form) {
    IsRow row = form.getActiveRow();
    Assert.state(DataUtils.isId(row.getId()));

    if (!availableEvent(ev, row.getInteger(form.getDataIndex(COL_EVENT)), form)) {
      Global.showError("Veiksmas neleidžiamas");
      return;
    }
    switch (ev) {
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
      case VISITED:
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
        Long oldTerm = data.getLong(form.getDataIndex(COL_FINISH_TIME));
        if (Objects.equal(newTerm, oldTerm)
            || newTerm < Math.max(data.getLong(form.getDataIndex(COL_START_TIME)),
                System.currentTimeMillis())) {
          Global.showError("Neteisingas terminas");
          return;
        }
        String comment = dialog.getComment();
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.DATETIME, COL_FINISH_TIME));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(oldTerm)});
        rs.preliminaryUpdate(0, COL_FINISH_TIME, BeeUtils.toString(newTerm));

        ParameterList args = createParams(TaskEvent.EXTENDED);
        args.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rs));

        if (!BeeUtils.isEmpty(comment)) {
          args.addDataItem(VAR_TASK_COMMENT, comment);
        }

        sendRequest(args, null);
      }
    });
    dialog.display();
  }

  private static void doForward(final FormView form) {
    final IsRow data = form.getActiveRow();
    final Long owner = data.getLong(form.getDataIndex(COL_OWNER));
    final Long oldUser = data.getLong(form.getDataIndex(COL_EXECUTOR));

    final TaskDialog dialog = new TaskDialog("Užduoties persiuntimas");
    dialog.addSelector("Vykdytojas", "Users",
        Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME), true);

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
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.LONG, COL_EXECUTOR));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(oldUser)});
        rs.preliminaryUpdate(0, COL_EXECUTOR, BeeUtils.toString(newUser));

        ParameterList args = createParams(TaskEvent.FORWARDED);
        args.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(VAR_TASK_COMMENT, comment);

        if (!Objects.equal(owner, oldUser) && dialog.getAnswer()) {
          args.addDataItem(VAR_TASK_OBSERVE, BeeUtils.toString(true));
        }

        sendRequest(args, null);
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
        int evOld = data.getInteger(form.getDataIndex(COL_EVENT));
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, COL_EVENT));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, COL_EVENT,
            BeeUtils.toString(TaskEvent.ACTIVATED.ordinal()));

        ParameterList args = createParams(TaskEvent.RENEWED);
        args.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rs));

        if (!BeeUtils.isEmpty(comment)) {
          args.addDataItem(VAR_TASK_COMMENT, comment);
        }

        sendRequest(args, null);
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
        int evOld = data.getInteger(form.getDataIndex(COL_EVENT));
        TaskEvent event = TaskEvent.SUSPENDED;
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, COL_EVENT));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, COL_EVENT, BeeUtils.toString(event.ordinal()));

        ParameterList args = createParams(event);
        args.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(VAR_TASK_COMMENT, comment);

        sendRequest(args, null);
      }
    });
    dialog.display();
  }

  private static void doUpdate(final FormView form) {
    final IsRow data = form.getActiveRow();
    final int oldPriority =
        BeeUtils.unbox(data.getInteger(form.getDataIndex(COL_PRIORITY)));
    final int oldTerm = BeeUtils.unbox(data.getInteger(form.getDataIndex("ExpectedDuration")));
    final long oldCompany = BeeUtils.unbox(data.getLong(form.getDataIndex("Company")));
    final long oldPerson = BeeUtils.unbox(data.getLong(form.getDataIndex("CompanyPerson")));

    final TaskDialog dialog = new TaskDialog("Užduoties koregavimas");
    dialog.addPriority("Prioritetas", oldPriority);
    dialog.addMinutes("Numatoma trukmė min.", oldTerm, 0, 43200, 30);
    dialog.addSelector("Įmonė", "Companies", Lists.newArrayList(COL_NAME), false);
    dialog.addSelector("PERSON", "Asmuo", "CompanyPersons",
        Lists.newArrayList(COL_FIRST_NAME, COL_LAST_NAME), false);
    dialog.addAction("Išsaugoti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        List<BeeColumn> cols = Lists.newArrayList();
        List<String> old = Lists.newArrayList();
        List<String> vals = Lists.newArrayList();

        int priority = dialog.getPriority();
        if (!Objects.equal(oldPriority, priority)) {
          cols.add(new BeeColumn(ValueType.INTEGER, COL_PRIORITY));
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
          ParameterList args = createParams(TaskEvent.UPDATED);
          args.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rs));

          sendRequest(args, null);
        }
      }
    });
    dialog.display();
  }

  private TaskEventHandler() {
  }
}
