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
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.FlexTable.FlexCellFormatter;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.edit.EditFormEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.Priority;
import com.butent.bee.shared.modules.crm.CrmConstants.ProjectEvent;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectEventHandler {

  private static class ProjectCreateHandler extends AbstractFormCallback {
    private static int counter = 0;

    @Override
    public void afterCreateWidget(final String name, final Widget widget) {
      if (BeeUtils.same(name, CrmConstants.COL_PRIORITY) && widget instanceof BeeListBox) {
        for (Priority priority : Priority.values()) {
          ((BeeListBox) widget).addItem(priority.name());
        }
      }
    }

    @Override
    public FormCallback getInstance() {
      if (counter++ == 0) {
        return this;
      } else {
        return new ProjectCreateHandler();
      }
    }

    @Override
    public boolean onPrepareForInsert(FormView form, final DataView dataView, IsRow row) {
      Assert.noNulls(dataView, row);

      List<BeeColumn> columns = Lists.newArrayList();
      List<String> values = Lists.newArrayList();

      for (BeeColumn column : form.getDataColumns()) {
        String colName = column.getId();
        String value = row.getString(form.getDataIndex(colName));

        if (!BeeUtils.isEmpty(value)) {
          columns.add(column);
          values.add(value);

        } else if (BeeUtils.inListSame(colName, "ProjectType", "StartDate", "FinishDate", "Name")) {
          dataView.notifySevere(colName + ": value required");
          return false;
        }
      }
      BeeRowSet rs = new BeeRowSet(VIEW_NAME, columns);
      rs.addRow(0, values.toArray(new String[0]));

      ParameterList args = createArgs(ProjectEvent.CREATED.name());
      args.addDataItem(CrmConstants.VAR_PROJECT_DATA, Codec.beeSerialize(rs));

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          Assert.notNull(response);

          if (response.hasErrors()) {
            dataView.notifySevere(response.getErrors());

          } else if (response.hasResponse(BeeRow.class)) {
            dataView.finishNewRow(BeeRow.restore((String) response.getResponse()));

          } else {
            dataView.notifySevere("Unknown response");
          }
        }
      });
      return false;
    }

    @Override
    public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
      newRow.setValue(form.getDataIndex(CrmConstants.COL_OWNER), BeeKeeper.getUser().getUserId());
      newRow.setValue(form.getDataIndex("StartDate"), TimeUtils.today(0).getDay());
      newRow.setValue(form.getDataIndex(CrmConstants.COL_EVENT), ProjectEvent.CREATED.ordinal());
      newRow.setValue(form.getDataIndex(CrmConstants.COL_PRIORITY), Priority.MEDIUM.ordinal());
    }
  }

  private static class ProjectDialog extends DialogBox {
    private static final String COMMENT = "comment";
    private Map<String, Widget> dialogWidgets = Maps.newHashMap();
    private FlexTable container = null;

    public ProjectDialog(String caption) {
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

    public void addComment(FlexTable parent, String caption, boolean required) {
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
    }

    public void addComment(String caption, boolean required) {
      addComment(container, caption, required);
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

    public String getComment() {
      if (dialogWidgets.containsKey(COMMENT)) {
        return ((InputArea) dialogWidgets.get(COMMENT)).getValue();
      }
      return null;
    }
  }

  private static class ProjectEditHandler extends AbstractFormCallback {
    private static int counter = 0;
    private Map<String, Widget> formWidgets = Maps.newHashMap();

    @Override
    public void afterCreateWidget(String name, final Widget widget) {
      if (!BeeUtils.isEmpty(name)
          && BeeUtils.inListSame(name, CrmConstants.COL_PRIORITY, CrmConstants.COL_EVENT)) {
        setWidget(name, widget);

      } else if (widget instanceof HasClickHandlers) {
        setWidget(name, widget);
        ProjectEvent ev;

        try {
          ev = ProjectEvent.valueOf(name);
        } catch (Exception e) {
          ev = null;
        }
        if (ev != null) {
          final ProjectEvent event = ev;
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
      Integer idx = row.getInteger(form.getDataIndex(CrmConstants.COL_PRIORITY));

      if (BeeUtils.isOrdinal(Priority.class, idx)) {
        text = Priority.values()[idx].name();
      }
      getWidget(CrmConstants.COL_PRIORITY).getElement().setInnerText(text);

      text = BeeConst.STRING_EMPTY;
      idx = row.getInteger(form.getDataIndex(CrmConstants.COL_EVENT));

      if (BeeUtils.isOrdinal(ProjectEvent.class, idx)) {
        Long owner = row.getLong(form.getDataIndex(CrmConstants.COL_OWNER));

        for (ProjectEvent ev : ProjectEvent.values()) {
          Widget widget = getWidget(ev.name());

          if (widget != null) {
            if (availableEvent(ev, idx, owner)) {
              StyleUtils.unhideDisplay(widget);
            } else {
              StyleUtils.hideDisplay(widget);
            }
          }
        }
        text = ProjectEvent.values()[idx].name();
      }
      getWidget(CrmConstants.COL_EVENT).getElement().setInnerText(text);
    }

    @Override
    public ProjectEditHandler getInstance() {
      if (counter++ == 0) {
        return this;
      } else {
        return new ProjectEditHandler();
      }
    }

    @Override
    public void onStartEdit(FormView form, IsRow row) {
      doEvent(ProjectEvent.VISITED, form);
    }

    private Widget getWidget(String name) {
      return formWidgets.get(BeeUtils.normalize(name));
    }

    private void setWidget(String name, Widget widget) {
      formWidgets.put(BeeUtils.normalize(name), widget);
    }
  }

  private static final String VIEW_NAME = "UserProjects";

  public static boolean availableEvent(ProjectEvent ev, Integer status, Long owner) {
    long user = BeeKeeper.getUser().getUserId();

    if (user != owner) {
      return (ev == ProjectEvent.COMMENTED || ev == ProjectEvent.VISITED);
    }
    switch (ev) {
      case COMMENTED:
      case VISITED:
      case CREATED:
      case DELETED:
        return true;

      case ACTIVATED:
        return status == ProjectEvent.CREATED.ordinal();

      case RENEWED:
        return status != ProjectEvent.CREATED.ordinal()
            && status != ProjectEvent.ACTIVATED.ordinal();

      case EXTENDED:
      case SUSPENDED:
      case COMPLETED:
      case UPDATED:
        return status == ProjectEvent.ACTIVATED.ordinal();

      case CANCELED:
        return BeeUtils.inList(status,
            ProjectEvent.ACTIVATED.ordinal(), ProjectEvent.SUSPENDED.ordinal());
    }
    return true;
  }

  public static void register() {
    FormFactory.registerFormCallback("NewProject", new ProjectCreateHandler());
    FormFactory.registerFormCallback("Project", new ProjectEditHandler());
  }

  private static boolean availableEvent(ProjectEvent ev, int status, FormView form) {
    IsRow row = form.getRow();
    return availableEvent(ev, status, row.getLong(form.getDataIndex(CrmConstants.COL_OWNER)));
  }

  private static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CrmConstants.CRM_MODULE);
    args.addQueryItem(CrmConstants.CRM_METHOD, CrmConstants.CRM_PROJECT_PREFIX + name);
    return args;
  }

  private static ParameterList createEventArgs(FormView form, ProjectEvent event) {
    IsRow data = form.getRow();
    int evOld = data.getInteger(form.getDataIndex(CrmConstants.COL_EVENT));
    BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, CrmConstants.COL_EVENT));
    rs.setViewName(VIEW_NAME);

    rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
    rs.preliminaryUpdate(0, CrmConstants.COL_EVENT, BeeUtils.toString(event.ordinal()));

    ParameterList args = createArgs(event.name());
    args.addDataItem(CrmConstants.VAR_PROJECT_DATA, Codec.beeSerialize(rs));
    return args;
  }

  private static void createRequest(ParameterList args, final ProjectDialog dialog,
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
            int dataIndex = form.getDataIndex(CrmConstants.COL_LAST_ACCESS);
            String newValue = (String) response.getResponse();

            CellUpdateEvent cellUpdateEvent =
                new CellUpdateEvent(VIEW_NAME, form.getRow().getId(), form.getRow().getVersion(),
                    CrmConstants.COL_LAST_ACCESS, dataIndex, newValue);
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

  private static void doEvent(final ProjectEvent ev, final FormView form) {
    IsRow row = form.getRow();
    Assert.notEmpty(row.getId());

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

      case ACTIVATED:
        doActivate(form);
        break;

      case SUSPENDED:
        changeStatus(form, "Projekto sustabdymas", "Sustabdyti", ev);
        break;

      case CANCELED:
        changeStatus(form, "Projekto nutraukimas", "Nutraukti", ev);
        break;

      case COMPLETED:
        changeStatus(form, "Projekto užbaigimas", "Užbaigti", ev);
        break;

      case RENEWED:
        changeStatus(form, "Projekto grąžinimas vykdymui", "Grąžinti vykdymui", ev);
        break;

      case CREATED:
      case DELETED:
        Assert.untouchable();
    }
  }

  private static void changeStatus(final FormView form, String caption, String buttonCaption,
      final ProjectEvent event) {
    final ProjectDialog dialog = new ProjectDialog(caption);
    dialog.addComment("Komentaras", true);
    dialog.addAction(buttonCaption, new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();

        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createEventArgs(form, event);
        args.addDataItem(CrmConstants.VAR_PROJECT_COMMENT, comment);
        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REQUERY));
      }
    });
    dialog.display();
  }

  private static void doActivate(final FormView form) {
    Global.confirm("Perduoti projektą vykdymui?", new BeeCommand() {
      @Override
      public void execute() {
        ParameterList args = createEventArgs(form, ProjectEvent.ACTIVATED);
        createRequest(args, null, form, EnumSet.of(Action.CLOSE, Action.REQUERY));
      }
    });
  }

  private static void doComment(final FormView form) {
    final ProjectDialog dialog = new ProjectDialog("Užduoties komentaras");
    dialog.addComment("Komentaras", true);
    dialog.addAction("Išsaugoti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();

        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createArgs(ProjectEvent.COMMENTED.name());
        args.addDataItem(CrmConstants.VAR_PROJECT_ID, form.getRow().getId());
        args.addDataItem(CrmConstants.VAR_PROJECT_COMMENT, comment);
        createRequest(args, dialog, form, EnumSet.of(Action.REQUERY));
      }
    });
    dialog.display();
  }

  private static void doVisit(FormView form) {
    ParameterList args = createArgs(ProjectEvent.VISITED.name());
    args.addDataItem(CrmConstants.VAR_PROJECT_ID, form.getRow().getId());
    createRequest(args, null, form, null);
  }

  private ProjectEventHandler() {
  }
}
