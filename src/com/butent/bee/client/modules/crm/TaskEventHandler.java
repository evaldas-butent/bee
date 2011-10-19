package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.FlexTable.FlexCellFormatter;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RelationInfo;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class TaskEventHandler {

  private static class TaskDialog extends DialogBox {
    private int row = -1;
    private FlexTable container = null;
    private InputDate dateWidget = null;
    private InputArea commentWidget = null;
    private DataSelector selector = null;
    private BeeCheckBox question = null;

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

    public void addComment(String caption) {
      row++;
      container.setWidget(row, 0, new BeeLabel(caption));
      commentWidget = new InputArea();
      commentWidget.setVisibleLines(5);
      commentWidget.setCharacterWidth(40);
      container.setWidget(row, 1, commentWidget);
    }

    public void addDateTime(String caption) {
      row++;
      container.setWidget(row, 0, new BeeLabel(caption));
      dateWidget = new InputDate(ValueType.DATETIME);
      container.setWidget(row, 1, dateWidget);
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
      if (commentWidget != null) {
        return commentWidget.getValue();
      }
      return null;
    }

    public Long getDateTime() {
      if (dateWidget != null) {
        return BeeUtils.toLongOrNull(dateWidget.getNormalizedValue());
      }
      return null;
    }

    public Long getSelector() {
      if (selector != null) {
        return BeeUtils.toLongOrNull(selector.getNormalizedValue());
      }
      return null;
    }
  }

  public TaskEventHandler(final TaskEvent ev, final FormView form) {
    if (BeeUtils.isEmpty(form.getRowData().getId())) {
      return;
    }
    switch (ev) {
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

      case ACTIVATED:
        Global.showError("Event usnsupported:", ev.name());
        break;
    }
  }

  private ParameterList createParams(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CrmConstants.CRM_MODULE);
    args.addQueryItem(CrmConstants.CRM_METHOD, name);
    // args.addQueryItem(Service.RPC_VAR_CTP, ContentType.BINARY);
    return args;
  }

  private void createRequest(ParameterList args, final TaskDialog dialog, final FormView form) {
    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        Assert.notNull(response);

        if (response.hasErrors()) {
          Global.showError((Object[]) response.getErrors());
        } else if (response.hasResponse(BeeRow.class)) {
          dialog.hide();
          form.updateRowData(BeeRow.restore((String) response.getResponse()));
        } else {
          Global.showError("Unknown response");
        }
      }
    });
  }

  private void doComment(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties komentaras, laiko registracija");
    dialog.addComment("Komentaras");
    dialog.addAction("Išsaugoti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createParams(TaskEvent.COMMENTED.name());
        args.addDataItem(CrmConstants.VAR_TASK_ID, form.getRowData().getId());
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            Assert.notNull(response);

            if (response.hasErrors()) {
              Global.showError((Object[]) response.getErrors());
            } else {
              dialog.hide();
              form.updateRowData(form.getRowData());
            }
          }
        });
      }
    });
    dialog.display();
  }

  private void doForward(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties persiuntimas");
    dialog.addSelector("Vykdytojas", "Users", "FirstName");
    dialog.addQuestion("Įtraukti siuntėją į stebėtojus", true);
    dialog.addComment("Komentaras");
    dialog.addAction("Persiųsti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Long newUser = dialog.getSelector();
        if (BeeUtils.isEmpty(newUser)) {
          Global.showError("Įveskite vykdytoją");
          return;
        }
        IsRow data = form.getRowData();
        Long oldUser = data.getLong(form.getDataIndex("Executor"));

        if (BeeUtils.equals(newUser, oldUser)) {
          Global.showError("Nurodykite kitą vykdytoją");
          return;
        }
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createParams(TaskEvent.FORWARDED.name());
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.LONG, "Executor"));
        rs.setViewName("Tasks");

        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(oldUser)});
        rs.preliminaryUpdate(0, "Executor", BeeUtils.toString(newUser));

        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        if (dialog.getAnswer()) {
          args.addDataItem(CrmConstants.VAR_TASK_OBSERVER, oldUser);
        }
        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private void doExtend(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties termino pratęsimas");
    dialog.addDateTime("Pabaigos data");
    dialog.addComment("Komentaras");
    dialog.addAction("Pratęsti terminą", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Long newTerm = dialog.getDateTime();
        if (BeeUtils.isEmpty(newTerm)) {
          Global.showError("Įveskite terminą");
          return;
        }
        IsRow data = form.getRowData();
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
        ParameterList args = createParams(TaskEvent.EXTENDED.name());
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.DATETIME, "FinishTime"));
        rs.setViewName("Tasks");
        rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(oldTerm)});
        rs.preliminaryUpdate(0, "FinishTime", BeeUtils.toString(newTerm));

        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private void doRenew(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties grąžinimas vykdymui");
    dialog.addComment("Komentaras");
    dialog.addAction("Grąžinti vykdymui", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createParams(TaskEvent.RENEWED.name());
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, "Event"),
            new BeeColumn(ValueType.DATETIME, "EventTime"));
        rs.setViewName("Tasks");

        IsRow data = form.getRowData();

        rs.addRow(data.getId(), data.getVersion(),
            new String[] {data.getString(form.getDataIndex("Event")),
                data.getString(form.getDataIndex("EventTime"))});
        rs.preliminaryUpdate(0, "Event", BeeUtils.toString(TaskEvent.ACTIVATED.ordinal()));

        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private void doSuspend(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties sustabdymas");
    dialog.addComment("Komentaras");
    dialog.addAction("Sustabdyti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createParams(TaskEvent.SUSPENDED.name());
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, "Event"),
            new BeeColumn(ValueType.DATETIME, "EventTime"));
        rs.setViewName("Tasks");

        IsRow data = form.getRowData();

        rs.addRow(data.getId(), data.getVersion(),
            new String[] {data.getString(form.getDataIndex("Event")),
                data.getString(form.getDataIndex("EventTime"))});
        rs.preliminaryUpdate(0, "Event", BeeUtils.toString(TaskEvent.SUSPENDED.ordinal()));

        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private void doCancel(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties nutraukimas");
    dialog.addComment("Komentaras");
    dialog.addAction("Nutraukti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createParams(TaskEvent.CANCELED.name());
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, "Event"),
            new BeeColumn(ValueType.DATETIME, "EventTime"));
        rs.setViewName("Tasks");

        IsRow data = form.getRowData();

        rs.addRow(data.getId(), data.getVersion(),
            new String[] {data.getString(form.getDataIndex("Event")),
                data.getString(form.getDataIndex("EventTime"))});
        rs.preliminaryUpdate(0, "Event", BeeUtils.toString(TaskEvent.CANCELED.ordinal()));

        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private void doComplete(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties užbaigimas");
    dialog.addComment("Komentaras");
    dialog.addAction("Užbaigti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createParams(TaskEvent.COMPLETED.name());
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, "Event"),
            new BeeColumn(ValueType.DATETIME, "EventTime"));
        rs.setViewName("Tasks");

        IsRow data = form.getRowData();
        TaskEvent ev;

        rs.addRow(data.getId(), data.getVersion(),
            new String[] {data.getString(form.getDataIndex("Event")),
                data.getString(form.getDataIndex("EventTime"))});

        if (BeeUtils.equals(data.getLong(form.getDataIndex("Owner")),
            BeeKeeper.getUser().getUserId())) {
          ev = TaskEvent.APPROVED;
        } else {
          ev = TaskEvent.COMPLETED;
        }
        rs.preliminaryUpdate(0, "Event", BeeUtils.toString(ev.ordinal()));

        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }

  private void doApprove(final FormView form) {
    final TaskDialog dialog = new TaskDialog("Užduoties patvirtinimas");
    dialog.addComment("Komentaras");
    dialog.addAction("Patvirtinti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String comment = dialog.getComment();
        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createParams(TaskEvent.APPROVED.name());
        BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, "Event"),
            new BeeColumn(ValueType.DATETIME, "EventTime"));
        rs.setViewName("Tasks");

        IsRow data = form.getRowData();

        rs.addRow(data.getId(), data.getVersion(),
            new String[] {data.getString(form.getDataIndex("Event")),
                data.getString(form.getDataIndex("EventTime"))});
        rs.preliminaryUpdate(0, "Event", BeeUtils.toString(TaskEvent.APPROVED.ordinal()));

        args.addDataItem(CrmConstants.VAR_TASK_DATA, Codec.beeSerialize(rs));
        args.addDataItem(CrmConstants.VAR_TASK_COMMENT, comment);

        createRequest(args, dialog, form);
      }
    });
    dialog.display();
  }
}
