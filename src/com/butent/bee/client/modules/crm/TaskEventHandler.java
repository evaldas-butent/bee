package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.FlexTable.FlexCellFormatter;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.form.FormView;
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
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.Priority;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.List;
import java.util.Map;

public class TaskEventHandler {

  private static class TaskCreateHandler extends AbstractFormCallback {

    @Override
    public void afterCreateWidget(final String name, final Widget widget) {
      if (BeeUtils.same(name, "Priority") && widget instanceof BeeListBox) {
        for (Priority priority : Priority.values()) {
          ((BeeListBox) widget).addItem(priority.name());
        }
      }
    }

    @Override
    public boolean onPrepareForInsert(FormView form, final DataView dataView, IsRow row) {
      Assert.notNull(dataView);
      Assert.notNull(row);

      List<BeeColumn> columns = Lists.newArrayList();
      List<String> values = Lists.newArrayList();

      for (BeeColumn column : form.getDataColumns()) {
        String colName = column.getId();
        String value = row.getString(form.getDataIndex(colName));

        if (BeeUtils.isEmpty(value)) {
          if (BeeUtils.inListSame(colName, "StartTime", "FinishTime", "Task", "Executor")) {
            dataView.notifySevere(colName + ": value required");
            return false;
          }
        } else {
          columns.add(column);
          values.add(value);
        }
      }
      BeeRowSet rs = new BeeRowSet(columns);
      rs.setViewName(VIEW_NAME);
      rs.addRow(0, values.toArray(new String[0]));

      ParameterList args = createParams(TaskEvent.ACTIVATED.name());
      args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          Assert.notNull(response);

          if (response.hasErrors()) {
            dataView.notifySevere(response.getErrors());
          } else if (response.hasResponse(BeeRow.class)) {
            dataView.finishNewRow(BeeRow.restore((String) response.getResponse()));
          } else {
            Global.showError("Unknown response");
          }
        }
      });

      return false;
    }

    @Override
    public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
      newRow.setValue(form.getDataIndex("Owner"), BeeKeeper.getUser().getUserId());
      newRow.setValue(form.getDataIndex("StartTime"), System.currentTimeMillis());
      newRow.setValue(form.getDataIndex("Event"), TaskEvent.ACTIVATED.ordinal());
      newRow.setValue(form.getDataIndex("Priority"), Priority.MEDIUM.ordinal());
    }
  }

  private static class TaskDialog extends DialogBox {
    private int row = -1;
    private FlexTable container = null;
    private InputDate date = null;
    private InputArea comment = null;
    private DataSelector selector = null;
    private BeeCheckBox question = null;
    private InputSpinner minutes = null;

    public TaskDialog(String caption) {
      super(caption);
      Absolute panel = new Absolute(Position.RELATIVE);
      setWidget(panel);
      container = new FlexTable();
      panel.add(container);
      container.setCellSpacing(5);
    }

    public void addAction(String caption, ClickHandler clickHandler) {
      row++;
      BeeButton button = new BeeButton(caption);
      container.setWidget(row, 0, button);
      FlexCellFormatter formater = container.getFlexCellFormatter();
      formater.setColSpan(row, 0, 2);
      formater.setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);

      button.addClickHandler(clickHandler);
    }

    public void addComment(String caption, boolean durationRequired) {
      row++;
      container.setWidget(row, 0, new BeeLabel(caption));
      comment = new InputArea();
      comment.setVisibleLines(5);
      comment.setCharacterWidth(40);
      container.setWidget(row, 1, comment);

      if (durationRequired) {
        row++;
        container.setWidget(row, 0, new BeeLabel("Sugaišta minučių"));
        minutes = new InputSpinner(0, 0, 1440, 5);
        minutes.setWidth("4em");
        container.setWidget(row, 1, minutes);
        addSelector("Darbo tipas", "DurationTypes", "Name");
        addDate("Atlikimo data", ValueType.DATE);
        date.setValue(TimeUtils.today(0).serialize());
      }
    }

    public void addDate(String caption, ValueType dateType) {
      row++;
      container.setWidget(row, 0, new BeeLabel(caption));
      date = new InputDate(dateType);
      container.setWidget(row, 1, date);
    }

    public void addQuestion(String caption, boolean def) {
      row++;
      question = new BeeCheckBox(caption);
      question.setValue(def);
      container.setWidget(row, 1, question);
    }

    public void addSelector(String caption, String relView, String relColumn) {
      row++;
      container.setWidget(row, 0, new BeeLabel(caption));
      BeeColumn col = new BeeColumn(ValueType.LONG, "Dummy");
      selector = new DataSelector(
          RelationInfo.create(Lists.newArrayList(col), null, col.getId(), relView, relColumn),
          true);
      container.setWidget(row, 1, selector);
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
      if (question != null) {
        return question.getValue();
      }
      return false;
    }

    public String getComment() {
      if (comment != null) {
        return comment.getValue();
      }
      return null;
    }

    public Long getDate() {
      if (date != null) {
        return BeeUtils.toLongOrNull(date.getNormalizedValue());
      }
      return null;
    }

    public int getMinutes() {
      if (minutes != null) {
        return minutes.getIntValue();
      }
      return 0;
    }

    public Long getSelector() {
      if (selector != null) {
        return BeeUtils.toLongOrNull(selector.getNormalizedValue());
      }
      return null;
    }
  }

  private static class TaskEditHandler extends AbstractFormCallback {
    private Map<String, Widget> formWidgets = Maps.newHashMap();

    @Override
    public void afterCreateWidget(final String name, final Widget widget) {
      if (BeeUtils.same(name, "Priority")) {
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
    public boolean onSaveChanges(FormView form, IsRow row) {
      return false;
    }

    @Override
    public void onStartEdit(FormView form, IsRow row) {
      if (row == null) {
        return;
      }
      if (getWidget("Priority") != null) {
        Integer idx = row.getInteger(form.getDataIndex("Priority"));
        String text;

        if (BeeUtils.isOrdinal(Priority.class, idx)) {
          text = Priority.values()[idx].name();
        } else {
          text = BeeConst.STRING_EMPTY;
        }
        getWidget("Priority").getElement().setInnerText(text);
      }
      Integer status = row.getInteger(form.getDataIndex("Event"));

      if (BeeUtils.isOrdinal(TaskEvent.class, status)) {
        Long owner = row.getLong(form.getDataIndex("Owner"));
        Long executor = row.getLong(form.getDataIndex("Executor"));

        for (TaskEvent ev : TaskEvent.values()) {
          if (ev == TaskEvent.VISITED) {
            doEvent(TaskEvent.VISITED, form);

          } else if (ev == TaskEvent.ACTIVATED) {
            continue;

          } else {
            Widget widget = getWidget(ev.name());

            if (availableEvent(ev, status, owner, executor)) {
              StyleUtils.unhideDisplay(widget);
            } else {
              StyleUtils.hideDisplay(widget);
            }
          }
        }
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

  public static void register() {
    FormFactory.registerFormCallback("NewTask", new TaskCreateHandler());
    FormFactory.registerFormCallback("Tasks", new TaskEditHandler());
  }

  private static boolean availableEvent(TaskEvent ev, int status, FormView form) {
    IsRow row = form.getRow();
    return availableEvent(ev, status, row.getLong(form.getDataIndex("Owner")),
        row.getLong(form.getDataIndex("Executor")));
  }

  private static boolean availableEvent(TaskEvent ev, Integer status, Long owner, Long executor) {
    long user = BeeKeeper.getUser().getUserId();

    if (user != owner && user != executor) {
      return ev == TaskEvent.COMMENTED || ev == TaskEvent.VISITED;
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
        return status == TaskEvent.ACTIVATED.ordinal();

      case CANCELED:
        return BeeUtils.inList(status,
            TaskEvent.ACTIVATED.ordinal(), TaskEvent.SUSPENDED.ordinal());

      case APPROVED:
        return status == TaskEvent.COMPLETED.ordinal() && user == owner && user != executor;
    }
    return true;
  }

  private static ParameterList createParams(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CrmConstants.CRM_MODULE);
    args.addQueryItem(CrmConstants.CRM_METHOD, name);
    return args;
  }

  private static void createRequest(ParameterList args, final TaskDialog dialog, final FormView form) {
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasErrors()) {
          Global.showError((Object[]) response.getErrors());
        } else if (response.hasResponse(BeeRow.class)) {
          if (dialog != null) {
            dialog.hide();
          }
          form.updateRow(BeeRow.restore((String) response.getResponse()));
        } else {
          Global.showError("Unknown response");
        }
      }
    });
  }

  private static void doApprove(final FormView form) {
    final IsRow data = form.getRow();
    Assert.notEmpty(data.getId());
    final int evOld = data.getInteger(form.getDataIndex("Event"));
    final TaskEvent event = TaskEvent.APPROVED;

    if (!availableEvent(event, evOld, form)) {
      Global.showError("Veiksmas neleidžiamas");
      return;
    }
    final TaskDialog dialog = new TaskDialog("Užduoties patvirtinimas");
    dialog.addComment("Komentaras", false);
    dialog.addAction("Patvirtinti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, "Event"));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, "Event", BeeUtils.toString(event.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private static void doCancel(final FormView form) {
    final IsRow data = form.getRow();
    Assert.notEmpty(data.getId());
    final int evOld = data.getInteger(form.getDataIndex("Event"));
    final TaskEvent event = TaskEvent.CANCELED;

    if (!availableEvent(event, evOld, form)) {
      Global.showError("Veiksmas neleidžiamas");
      return;
    }
    final TaskDialog dialog = new TaskDialog("Užduoties nutraukimas");
    dialog.addComment("Komentaras", false);
    dialog.addAction("Nutraukti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, "Event"));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, "Event", BeeUtils.toString(event.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private static void doComment(final FormView form) {
    final IsRow data = form.getRow();
    Assert.notEmpty(data.getId());

    final TaskDialog dialog = new TaskDialog("Užduoties komentaras, laiko registracija");
    dialog.addComment("Komentaras", true);
    dialog.addAction("Išsaugoti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createParams(TaskEvent.COMMENTED.name());
        args.addDataItem(CrmConstants.VAR_TASK_ID, data.getId());
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
        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            Assert.notNull(response);

            if (response.hasErrors()) {
              Global.showError((Object[]) response.getErrors());
            } else {
              dialog.hide();
              form.updateRow(data);
            }
          }
        });
      }
    });
    dialog.display();
  }

  private static void doComplete(final FormView form) {
    final IsRow data = form.getRow();
    Assert.notEmpty(data.getId());
    final int evOld = data.getInteger(form.getDataIndex("Event"));
    final TaskEvent event = TaskEvent.COMPLETED;

    if (!availableEvent(event, evOld, form)) {
      Global.showError("Veiksmas neleidžiamas");
      return;
    }
    final TaskDialog dialog = new TaskDialog("Užduoties užbaigimas");
    dialog.addComment("Komentaras", true);
    dialog.addAction("Užbaigti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        int minutes = dialog.getMinutes();
        if (!BeeUtils.isPositive(minutes)) {
          Global.showError("Įveskite sugaištą laiką");
          return;
        }
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
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, "Event"));
        rs.setViewName(VIEW_NAME);

        TaskEvent ev;

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});

        if (BeeUtils.equals(data.getLong(form.getDataIndex("Owner")),
            BeeKeeper.getUser().getUserId())) {
          ev = TaskEvent.APPROVED;
        } else {
          ev = TaskEvent.COMPLETED;
        }
        rs.preliminaryUpdate(0, "Event", BeeUtils.toString(ev.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);
        args.addDataItem(CrmConstants.VAR_TASK_DURATION_DATE, BeeUtils.transform(date));
        args.addDataItem(CrmConstants.VAR_TASK_DURATION_TIME, BeeUtils.transform(minutes));
        args.addDataItem(CrmConstants.VAR_TASK_DURATION_TYPE, BeeUtils.transform(type));

        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private static void doEvent(final TaskEvent ev, final FormView form) {
    switch (ev) {
      case VISITED:
        doVisit(form);
        break;

      case COMMENTED:
        doComment(form);
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
    }
  }

  private static void doExtend(final FormView form) {
    final IsRow data = form.getRow();
    Assert.notEmpty(data.getId());
    final int evOld = data.getInteger(form.getDataIndex("Event"));
    final TaskEvent event = TaskEvent.EXTENDED;

    if (!availableEvent(event, evOld, form)) {
      Global.showError("Veiksmas neleidžiamas");
      return;
    }
    final TaskDialog dialog = new TaskDialog("Užduoties termino pratęsimas");
    dialog.addDate("Pabaigos data", ValueType.DATETIME);
    dialog.addComment("Komentaras", false);
    dialog.addAction("Pratęsti terminą", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        Long newTerm = dialog.getDate();
        if (BeeUtils.isEmpty(newTerm)) {
          Global.showError("Įveskite terminą");
          return;
        }
        Long oldTerm = data.getLong(form.getDataIndex("FinishTime"));
        if (newTerm < oldTerm) {
          Global.showError("Neteisingas terminas");
          return;
        }
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.DATETIME, "FinishTime"));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(oldTerm)});
        rs.preliminaryUpdate(0, "FinishTime", BeeUtils.toString(newTerm));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private static void doForward(final FormView form) {
    final IsRow data = form.getRow();
    Assert.notEmpty(data.getId());
    final int evOld = data.getInteger(form.getDataIndex("Event"));
    final TaskEvent event = TaskEvent.FORWARDED;

    if (!availableEvent(event, evOld, form)) {
      Global.showError("Veiksmas neleidžiamas");
      return;
    }
    final TaskDialog dialog = new TaskDialog("Užduoties persiuntimas");
    dialog.addSelector("Vykdytojas", "Users", "FirstName");
    dialog.addQuestion("Palikti siuntėją prie stebėtojų", true);
    dialog.addComment("Komentaras", false);
    dialog.addAction("Persiųsti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        Long newUser = dialog.getSelector();
        if (BeeUtils.isEmpty(newUser)) {
          Global.showError("Įveskite vykdytoją");
          return;
        }
        Long oldUser = data.getLong(form.getDataIndex("Executor"));
        if (BeeUtils.equals(newUser, oldUser)) {
          Global.showError("Nurodėte tą patį vykdytoją");
          return;
        }
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.LONG, "Executor"));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(oldUser)});
        rs.preliminaryUpdate(0, "Executor", BeeUtils.toString(newUser));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        if (dialog.getAnswer()) {
          args.addDataItem(CrmConstants.VAR_TASK_OBSERVE, true);
        }
        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private static void doRenew(final FormView form) {
    final IsRow data = form.getRow();
    Assert.notEmpty(data.getId());
    final int evOld = data.getInteger(form.getDataIndex("Event"));
    final TaskEvent event = TaskEvent.RENEWED;

    if (!availableEvent(event, evOld, form)) {
      Global.showError("Veiksmas neleidžiamas");
      return;
    }
    final TaskDialog dialog = new TaskDialog("Užduoties grąžinimas vykdymui");
    dialog.addComment("Komentaras", false);
    dialog.addAction("Grąžinti vykdymui", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, "Event"));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, "Event", BeeUtils.toString(TaskEvent.ACTIVATED.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private static void doSuspend(final FormView form) {
    final IsRow data = form.getRow();
    Assert.notEmpty(data.getId());
    final int evOld = data.getInteger(form.getDataIndex("Event"));
    final TaskEvent event = TaskEvent.SUSPENDED;

    if (!availableEvent(event, evOld, form)) {
      Global.showError("Veiksmas neleidžiamas");
      return;
    }
    final TaskDialog dialog = new TaskDialog("Užduoties sustabdymas");
    dialog.addComment("Komentaras", false);
    dialog.addAction("Sustabdyti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, "Event"));
        rs.setViewName(VIEW_NAME);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, "Event", BeeUtils.toString(event.ordinal()));

        ParameterList args = createParams(event.name());
        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private static void doVisit(final FormView form) {
    IsRow data = form.getRow();
    Assert.notEmpty(data.getId());

    ParameterList args = createParams(TaskEvent.VISITED.name());
    args.addDataItem(CrmConstants.VAR_TASK_ID, data.getId());

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasErrors()) {
          Global.showError((Object[]) response.getErrors());
        }
      }
    });
  }

  private TaskEventHandler() {
  }
}
