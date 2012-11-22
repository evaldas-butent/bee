package com.butent.bee.client.modules.crm;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
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
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.composite.InputTime;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.render.RendererFactory;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.Editor;
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
import com.butent.bee.shared.data.CustomProperties;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;
import java.util.Map;

class TaskEventHandler {

  private static class TaskBuilder extends AbstractFormInterceptor {

    private static final String NAME_START_DATE = "Start_Date";
    private static final String NAME_START_TIME = "Start_Time";
    private static final String NAME_END_DATE = "End_Date";
    private static final String NAME_END_TIME = "End_Time";

    private static final String NAME_FILES = "Files";
    
    @Override
    public void afterCreateWidget(String name, IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {
      if (widget instanceof FileCollector) {
        ((FileCollector) widget).bindDnd(getFormView());
      }
    }
    
    @Override
    public FormInterceptor getInstance() {
      return new TaskBuilder();
    }

    @Override
    public boolean onReadyForInsert(final ReadyForInsertEvent event) {
      IsRow activeRow = getFormView().getActiveRow();

      DateTime start = getStart();
      DateTime end = getEnd(start, Data.getString(VIEW_TASKS, activeRow, COL_EXPECTED_DURATION));

      if (end == null) {
        event.getCallback().onFailure("Įveskite pabaigos laiką arba",
            "pradžios laiką ir numatomą trukmę");
        return false;
      }
      if (start != null && TimeUtils.isLeq(end, start)) {
        event.getCallback().onFailure("Pabaigos laikas turi būti didesnis už pradžios laiką");
        return false;
      }
      
      if (Data.isNull(VIEW_TASKS, activeRow, COL_SUMMARY)) {
        event.getCallback().onFailure("Įveskite temą");
        return false;
      }

      if (BeeUtils.isEmpty(activeRow.getProperty(PROP_EXECUTORS))) {
        event.getCallback().onFailure("Pasirinkite vykdytoją");
        return false;
      }

      BeeRow newRow = DataUtils.cloneRow(activeRow);
      
      if (start != null) {
        Data.setValue(VIEW_TASKS, newRow, COL_START_TIME, start);
      }
      Data.setValue(VIEW_TASKS, newRow, COL_FINISH_TIME, end);
      
      BeeRowSet rowSet = Queries.createRowSetForInsert(VIEW_TASKS, getFormView().getDataColumns(),
          newRow, Sets.newHashSet(COL_EXECUTOR), true);

      ParameterList args = createParams(TaskEvent.ACTIVATED);
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

            GridView gridView = getGridView();
            if (gridView != null) {
              gridView.notifyInfo("Sukurta naujų užduočių:", String.valueOf(tasks.size()));
              gridView.getViewPresenter().handleAction(Action.REFRESH);
            }
            
          } else {
            event.getCallback().onFailure("Unknown response");
          }
        }
      });
      return false;
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
        int millis = TimeUtils.parseTime(duration);
        if (millis > 0) {
          return TimeUtils.combine(start, millis);
        }
      }
      return null;
    }
    
    private int getMillis(String widgetName) {
      Widget widget = getFormView().getWidgetByName(widgetName);
      if (widget instanceof InputTime) {
        return TimeUtils.parseTime(((InputTime) widget).getValue());
      } else {
        return 0;
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

  private static class TaskDialog extends DialogBox {

    private static final String DATE = "date";
    private static final String QUESTION = "question";
    private static final String COMMENT = "comment";
    private static final String MINUTES = "minutes";
    private static final String SELECTOR = "selector";
    private static final String PRIORITY = "priority";

    private final Map<String, Widget> dialogWidgets = Maps.newHashMap();
    
    private HtmlTable container = null;

    private TaskDialog(String caption) {
      super(caption);
      addDefaultCloseBox();

      Absolute panel = new Absolute(Position.RELATIVE);

      container = new HtmlTable();
      container.setBorderSpacing(5);

      panel.add(container);
      setWidget(panel);
    }

    private void addAction(HtmlTable parent, String caption, ClickHandler clickHandler) {
      int row = parent.getRowCount();

      BeeButton button = new BeeButton(caption);
      parent.setWidget(row, 0, button);
      parent.getCellFormatter().setColSpan(row, 0, 2);
      parent.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);

      button.addClickHandler(clickHandler);
    }

    private void addAction(String caption, ClickHandler clickHandler) {
      addAction(container, caption, clickHandler);
    }

    private void addComment(HtmlTable parent, String caption, boolean required, boolean showDuration) {
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
        addSelector(SELECTOR, table, "Darbo tipas", VIEW_DURATION_TYPES,
            Lists.newArrayList(COL_NAME), false);
        addDate(table, "Atlikimo data", ValueType.DATE, false,
            new Long(TimeUtils.today(0).getDays()));
      }
    }

    private void addComment(String caption, boolean required, boolean showDuration) {
      addComment(container, caption, required, showDuration);
    }

    private void addDate(HtmlTable parent, String caption, ValueType dateType, boolean required,
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

    private void addDate(String caption, ValueType dateType, boolean required, Long def) {
      addDate(container, caption, dateType, required, def);
    }

    private void addMinutes(HtmlTable parent, String caption, int def, int min, int max, int step) {
      int row = parent.getRowCount();

      parent.setWidget(row, 0, new BeeLabel(caption));
      InputSpinner minutes = new InputSpinner(def, min, max, step);
      minutes.setWidth("4em");
      parent.setWidget(row, 1, minutes);
      dialogWidgets.put(MINUTES, minutes);
    }

    private void addMinutes(String caption, int def, int min, int max, int step) {
      addMinutes(container, caption, def, min, max, step);
    }

    private void addPriority(HtmlTable parent, String caption, int def) {
      int row = parent.getRowCount();

      parent.setWidget(row, 0, new BeeLabel(caption));
      BeeListBox list = new BeeListBox();
      list.addCaptions(Priority.class);

      list.setValueNumeric(true);
      list.setValue(BeeUtils.toString(def));

      parent.setWidget(row, 1, list);
      dialogWidgets.put(PRIORITY, list);
    }

    private void addPriority(String caption, int def) {
      addPriority(container, caption, def);
    }

    private void addQuestion(HtmlTable parent, String caption, boolean def) {
      int row = parent.getRowCount();

      BeeCheckBox question = new BeeCheckBox(caption);
      question.setValue(def);
      parent.setWidget(row, 1, question);
      dialogWidgets.put(QUESTION, question);
    }

    private void addQuestion(String caption, boolean def) {
      addQuestion(container, caption, def);
    }

    private void addSelector(String id, HtmlTable parent, String caption, String relView,
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

    private void addSelector(String caption, String relView, List<String> relColumns,
        boolean required) {
      addSelector(SELECTOR, caption, relView, relColumns, required);
    }

    private void addSelector(String id, String caption, String relView, List<String> relColumns,
        boolean required) {
      addSelector(id, container, caption, relView, relColumns, required);
    }

    private void display() {
      center();
      UiHelper.focus(container);
    }

    private boolean getAnswer() {
      if (dialogWidgets.containsKey(QUESTION)) {
        return ((BeeCheckBox) dialogWidgets.get(QUESTION)).getValue();
      }
      return false;
    }

    private String getComment() {
      if (dialogWidgets.containsKey(COMMENT)) {
        return ((InputArea) dialogWidgets.get(COMMENT)).getValue();
      }
      return null;
    }

    private Long getDate() {
      if (dialogWidgets.containsKey(DATE)) {
        return BeeUtils.toLongOrNull(((InputDate) dialogWidgets.get(DATE)).getNormalizedValue());
      }
      return null;
    }

    private int getMinutes() {
      if (dialogWidgets.containsKey(MINUTES)) {
        return ((InputSpinner) dialogWidgets.get(MINUTES)).getIntValue();
      }
      return 0;
    }

    private int getPriority() {
      if (dialogWidgets.containsKey(PRIORITY)) {
        return BeeUtils.toInt(((BeeListBox) dialogWidgets.get(PRIORITY)).getValue());
      }
      return 0;
    }

    private Long getSelector() {
      return getSelector(SELECTOR);
    }

    private Long getSelector(String id) {
      if (dialogWidgets.containsKey(id)) {
        return BeeUtils.toLongOrNull(((DataSelector) dialogWidgets.get(id)).getNormalizedValue());
      }
      return null;
    }
  }

  private static class TaskEditor extends AbstractFormInterceptor {

    private final Map<String, IdentifiableWidget> formWidgets = Maps.newHashMap();

    private TaskEditor() {
      super();
    }

    @Override
    public void afterCreateWidget(String name, final IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {

      if (widget instanceof HasClickHandlers) {
        setWidget(name, widget);
        final TaskEvent event = NameUtils.getEnumByName(TaskEvent.class, name);

        if (event != null) {
          ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
              doEvent(event, UiHelper.getForm(widget.asWidget()));
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

      Integer idx = row.getInteger(form.getDataIndex(COL_STATUS));
      if (BeeUtils.isOrdinal(TaskEvent.class, idx)) {
        Long owner = row.getLong(form.getDataIndex(COL_OWNER));
        Long executor = row.getLong(form.getDataIndex(COL_EXECUTOR));

        for (TaskEvent ev : TaskEvent.values()) {
          IdentifiableWidget widget = getWidget(ev.name());

          if (widget != null) {
            if (availableEvent(ev, idx, owner, executor)) {
              StyleUtils.unhideDisplay(widget.asWidget());
            } else {
              StyleUtils.hideDisplay(widget.asWidget());
            }
          }
        }
      }
    }

    @Override
    public FormInterceptor getInstance() {
      return new TaskEditor();
    }

    @Override
    public boolean onStartEdit(final FormView form, final IsRow row,
        final Scheduler.ScheduledCommand focusCommand) {
      ParameterList args = createParams(TaskEvent.VISITED);
      args.addDataItem(VAR_TASK_ID, row.getId());
      
      String exclude = DataUtils.buildIdList(row.getLong(form.getDataIndex(COL_OWNER)),
          row.getLong(form.getDataIndex(COL_EXECUTOR)));
      if (!BeeUtils.isEmpty(exclude)) {
        args.addDataItem(VAR_TASK_DATA, exclude);
      }

      sendRequest(args, new Callback<ResponseObject>() {
        @Override
        public void onFailure(String... reason) {
          form.updateRow(row, true);
          form.notifySevere(reason);
        }

        @Override
        public void onSuccess(ResponseObject result) {
          row.setProperties(CustomProperties.restore((String) result.getResponse()));

          form.updateRow(row, true);
          if (focusCommand != null) {
            focusCommand.execute();
          }
        }
      });
      return false;
    }

    private IdentifiableWidget getWidget(String name) {
      return formWidgets.get(BeeUtils.normalize(name));
    }

    private void setWidget(String name, IdentifiableWidget widget) {
      formWidgets.put(BeeUtils.normalize(name), widget);
    }
  }

  static boolean availableEvent(TaskEvent ev, Integer status, Long owner, Long executor) {
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
      case EDITED:
        return status == TaskEvent.ACTIVATED.ordinal();

      case CANCELED:
        return BeeUtils.inList(status,
            TaskEvent.ACTIVATED.ordinal(), TaskEvent.SUSPENDED.ordinal());

      case APPROVED:
        return status == TaskEvent.COMPLETED.ordinal() && user != executor;
    }
    return true;
  }

  static void register() {
    FormFactory.registerFormInterceptor(FORM_NEW_TASK, new TaskBuilder());
    FormFactory.registerFormInterceptor(FORM_TASK, new TaskEditor());
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

  private static void doApprove(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties patvirtinimas");
    dialog.addComment("Komentaras", false, false);
    dialog.addAction("Patvirtinti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();
        IsRow data = form.getActiveRow();
        int evOld = data.getInteger(form.getDataIndex(COL_STATUS));
        TaskEvent event = TaskEvent.APPROVED;
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, COL_STATUS));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, COL_STATUS, BeeUtils.toString(event.ordinal()));

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
        int evOld = data.getInteger(form.getDataIndex(COL_STATUS));
        TaskEvent event = TaskEvent.CANCELED;
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, COL_STATUS));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, COL_STATUS, BeeUtils.toString(event.ordinal()));

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
        int evOld = data.getInteger(form.getDataIndex(COL_STATUS));
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, COL_STATUS));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});

        if (Objects.equal(data.getLong(form.getDataIndex(COL_OWNER)),
            BeeKeeper.getUser().getUserId())) {
          ev = TaskEvent.APPROVED;
        }
        rs.preliminaryUpdate(0, COL_STATUS, BeeUtils.toString(ev.ordinal()));

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

    if (!availableEvent(ev, row.getInteger(form.getDataIndex(COL_STATUS)), form)) {
      Global.showError("Veiksmas neleidžiamas");
      return;
    }
    switch (ev) {
      case COMMENTED:
        doComment(form);
        break;

      case EDITED:
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
    dialog.addSelector("Vykdytojas", CommonsConstants.VIEW_USERS,
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
        int evOld = data.getInteger(form.getDataIndex(COL_STATUS));
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, COL_STATUS));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, COL_STATUS,
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
        int evOld = data.getInteger(form.getDataIndex(COL_STATUS));
        TaskEvent event = TaskEvent.SUSPENDED;
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, COL_STATUS));
        rs.setViewName(VIEW_TASKS);

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
        rs.preliminaryUpdate(0, COL_STATUS, BeeUtils.toString(event.ordinal()));

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
    final int oldTerm = BeeUtils.unbox(data.getInteger(form.getDataIndex(COL_EXPECTED_DURATION)));
    final long oldCompany = BeeUtils.unbox(data.getLong(form.getDataIndex(COL_COMPANY)));
    final long oldPerson = BeeUtils.unbox(data.getLong(form.getDataIndex(COL_CONTACT)));

    final TaskDialog dialog = new TaskDialog("Užduoties koregavimas");
    dialog.addPriority("Prioritetas", oldPriority);
    dialog.addMinutes("Numatoma trukmė", oldTerm, 0, 43200, 30);
    dialog.addSelector("Įmonė", CommonsConstants.VIEW_COMPANIES, Lists.newArrayList(COL_NAME), false);
    dialog.addSelector("PERSON", "Asmuo", CommonsConstants.VIEW_COMPANY_PERSONS,
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
          cols.add(new BeeColumn(ValueType.TEXT, COL_EXPECTED_DURATION));
          old.add(BeeUtils.toString(oldTerm));
          vals.add(BeeUtils.toString(term));
        }
        Long company = dialog.getSelector();
        if (DataUtils.isId(company) && !Objects.equal(oldCompany, company)) {
          cols.add(new BeeColumn(ValueType.LONG, COL_COMPANY));
          old.add(BeeUtils.toString(oldCompany));
          vals.add(BeeUtils.toString(company));
        }
        Long person = dialog.getSelector("PERSON");
        if (DataUtils.isId(person) && !Objects.equal(oldPerson, person)) {
          cols.add(new BeeColumn(ValueType.LONG, COL_CONTACT));
          old.add(BeeUtils.toString(oldPerson));
          vals.add(BeeUtils.toString(person));
        }
        if (cols.isEmpty()) {
          Global.showError("Nėra pakeitimų");
        } else {
          BeeRowSet rs = new BeeRowSet(VIEW_TASKS, cols);
          BeeRow row = new BeeRow(data.getId(), data.getVersion(), ArrayUtils.toArray(old));
          rs.addRow(row);

          for (int i = 0; i < vals.size(); i++) {
            row.preliminaryUpdate(i, vals.get(i));
          }
          ParameterList args = createParams(TaskEvent.EDITED);
          args.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rs));

          sendRequest(args, null);
        }
      }
    });
    dialog.display();
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

  private TaskEventHandler() {
  }
}
