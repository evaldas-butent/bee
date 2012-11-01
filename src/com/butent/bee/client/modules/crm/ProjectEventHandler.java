package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.logical.ActionEvent;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.Priority;
import com.butent.bee.shared.modules.crm.CrmConstants.ProjectEvent;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectEventHandler {

  private static class ProjectCreateHandler extends AbstractFormCallback {

    @Override
    public FormCallback getInstance() {
      return new ProjectCreateHandler();
    }

    @Override
    public boolean onReadyForInsert(final ReadyForInsertEvent event) {
      List<String> missing = Lists.newArrayList();

      for (String colName : new String[] {"ProjectType", "StartDate", "FinishDate",
          CrmConstants.COL_NAME}) {
        if (!DataUtils.contains(event.getColumns(), colName)) {
          missing.add(colName);
        }
      }

      if (!missing.isEmpty()) {
        event.getCallback().onFailure(missing.toString(), "value required");
        return false;
      }
      
      BeeRowSet rs = new BeeRowSet(VIEW_PROJECTS, event.getColumns());
      rs.addRow(0, event.getValues().toArray(new String[0]));

      ParameterList args = createArgs(ProjectEvent.CREATED.name());
      args.addDataItem(CrmConstants.VAR_PROJECT_DATA, Codec.beeSerialize(rs));

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          Assert.notNull(response);

          if (response.hasErrors()) {
            event.getCallback().onFailure(response.getErrors());

          } else if (response.hasResponse(BeeRow.class)) {
            event.getCallback().onSuccess(BeeRow.restore((String) response.getResponse()));

          } else {
            event.getCallback().onFailure("Unknown response");
          }
        }
      });
      return false;
    }

    @Override
    public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
      newRow.setValue(form.getDataIndex(CrmConstants.COL_OWNER), BeeKeeper.getUser().getUserId());
      newRow.setValue(form.getDataIndex("StartDate"), TimeUtils.today(0).getDays());
      newRow.setValue(form.getDataIndex(CrmConstants.COL_EVENT), ProjectEvent.CREATED.ordinal());
      newRow.setValue(form.getDataIndex(CrmConstants.COL_PRIORITY), Priority.MEDIUM.ordinal());
    }
  }

  private static class ProjectDialog extends DialogBox {
    private static final String COMMENT = "comment";
    private Map<String, Widget> dialogWidgets = Maps.newHashMap();
    private HtmlTable container = null;

    public ProjectDialog(String caption) {
      super(caption);
      addDefaultCloseBox();

      Absolute panel = new Absolute(Position.RELATIVE);
      setWidget(panel);
      container = new HtmlTable();
      panel.add(container);
      container.setBorderSpacing(5);
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

    public void addComment(HtmlTable parent, String caption, String value, boolean required) {
      int row = parent.getRowCount();

      BeeLabel lbl = new BeeLabel(caption);
      if (required) {
        lbl.setStyleName(StyleUtils.NAME_REQUIRED);
      }
      parent.setWidget(row, 0, lbl);
      InputArea comment = new InputArea();
      comment.setVisibleLines(5);
      comment.setCharacterWidth(40);
      comment.setValue(value);
      parent.setWidget(row, 1, comment);
      dialogWidgets.put(COMMENT, comment);
    }

    public void addComment(String caption, String value, boolean required) {
      addComment(container, caption, value, required);
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

  private static class ProjectEditHandler extends AbstractFormCallback
      implements SelectionHandler<IsRow> {

    private final Map<String, Widget> formWidgets = Maps.newHashMap();
    private TreePresenter stageTree = null;

    @Override
    public void afterCreateWidget(String name, final Widget widget,
        WidgetDescriptionCallback callback) {

      if (BeeUtils.same(name, "Stages") && widget instanceof TreeView) {
        ((TreeView) widget).addSelectionHandler(this);
        stageTree = ((TreeView) widget).getTreePresenter();

      } else if (BeeUtils.same(name, "StageDescription")) {
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

      Integer idx = row.getInteger(form.getDataIndex(CrmConstants.COL_EVENT));
      Long owner = row.getLong(form.getDataIndex(CrmConstants.COL_OWNER));

      if (BeeUtils.isOrdinal(ProjectEvent.class, idx)) {
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
      }
      Long user = BeeKeeper.getUser().getUserId();

      for (String name : new String[] {CrmConstants.SVC_ADD_OBSERVERS,
          CrmConstants.SVC_REMOVE_OBSERVERS}) {

        Widget widget = getWidget(name);
        if (widget != null) {
          if (user == owner) {
            StyleUtils.unhideDisplay(widget);
          } else {
            StyleUtils.hideDisplay(widget);
          }
        }
      }
    }

    @Override
    public FormCallback getInstance() {
      return new ProjectEditHandler();
    }

    @Override
    public void onSelection(SelectionEvent<IsRow> event) {
      String text = "";

      if (BeeUtils.allNotNull(event.getSelectedItem(), stageTree, stageTree.getDataColumns())) {
        text = event.getSelectedItem()
            .getString(DataUtils.getColumnIndex("Description", stageTree.getDataColumns()));
      }
      getWidget("StageDescription").getElement().setInnerText(text);
    }

    @Override
    public boolean onStartEdit(FormView form, IsRow row, Scheduler.ScheduledCommand focusCommand) {
      doEvent(ProjectEvent.VISITED, form);
      getWidget("StageDescription").getElement().setInnerText("");
      return true;
    }

    private Widget getWidget(String name) {
      return formWidgets.get(BeeUtils.normalize(name));
    }

    private void setWidget(String name, Widget widget) {
      formWidgets.put(BeeUtils.normalize(name), widget);
    }
  }

  private static final String VIEW_PROJECTS = "UserProjects";

  public static boolean availableEvent(ProjectEvent ev, Integer status, Long owner) {
    Long user = BeeKeeper.getUser().getUserId();

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

  private static ParameterList addEventArgs(FormView form, ParameterList args, ProjectEvent event) {
    IsRow data = form.getActiveRow();
    int evOld = data.getInteger(form.getDataIndex(CrmConstants.COL_EVENT));
    BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.INTEGER, CrmConstants.COL_EVENT));
    rs.setViewName(VIEW_PROJECTS);

    rs.addRow(data.getId(), data.getVersion(), new String[] {BeeUtils.toString(evOld)});
    rs.preliminaryUpdate(0, CrmConstants.COL_EVENT, BeeUtils.toString(event.ordinal()));

    args.addDataItem(CrmConstants.VAR_PROJECT_DATA, Codec.beeSerialize(rs));
    return args;
  }

  private static boolean availableEvent(ProjectEvent ev, int status, FormView form) {
    IsRow row = form.getActiveRow();
    return availableEvent(ev, status, row.getLong(form.getDataIndex(CrmConstants.COL_OWNER)));
  }

  private static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(CrmConstants.CRM_MODULE);
    args.addQueryItem(CrmConstants.CRM_METHOD, CrmConstants.CRM_PROJECT_PREFIX + name);
    return args;
  }

  private static void createRequest(ParameterList args, final ProjectDialog dialog,
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
            BeeKeeper.getBus().fireEvent(new RowUpdateEvent(VIEW_PROJECTS, row));

            if (BeeUtils.contains(actions, Action.CLOSE)) {
              form.fireEvent(new ActionEvent(actions));
            } else if (BeeUtils.contains(actions, Action.REFRESH)) {
              form.updateRow(row, refreshChildren);
            }

          } else if (response.hasResponse(Long.class)) {
            int dataIndex = form.getDataIndex(CrmConstants.COL_LAST_ACCESS);
            String newValue = (String) response.getResponse();

            CellUpdateEvent cellUpdateEvent =  new CellUpdateEvent(VIEW_PROJECTS,
                form.getActiveRow().getId(), form.getActiveRow().getVersion(),
                CrmConstants.COL_LAST_ACCESS, dataIndex, newValue);
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

  private static void doEvent(final ProjectEvent ev, final FormView form) {
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

      case ACTIVATED:
        doActivate(form);
        break;

      case SUSPENDED:
        changeStatus(form, "Projekto sustabdymas", "Sustabdyti", ev, createArgs(ev.name()));
        break;

      case CANCELED:
        changeStatus(form, "Projekto nutraukimas", "Nutraukti", ev, createArgs(ev.name()));
        break;

      case COMPLETED:
        changeStatus(form, "Projekto užbaigimas", "Užbaigti", ev, createArgs(ev.name()));
        break;

      case RENEWED:
        changeStatus(form, "Projekto grąžinimas vykdymui", "Grąžinti vykdymui",
            ProjectEvent.ACTIVATED, createArgs(ev.name()));
        break;

      case CREATED:
      case DELETED:
      case UPDATED:
        Assert.untouchable();
        break;

      case EXTENDED:
        // TODO
        break;
    }
  }

  private static void changeStatus(final FormView form, String caption, String buttonCaption,
      final ProjectEvent event, final ParameterList args) {
    final ProjectDialog dialog = new ProjectDialog(caption);
    dialog.addComment("Komentaras", "", true);
    dialog.addAction(buttonCaption, new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();

        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        addEventArgs(form, args, event);
        args.addDataItem(CrmConstants.VAR_PROJECT_COMMENT, comment);
        createRequest(args, dialog, form, EnumSet.of(Action.CLOSE, Action.REFRESH), true);
      }
    });
    dialog.display();
  }

  private static void doActivate(final FormView form) {
    Global.confirm("Perduoti projektą vykdymui?", new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        ParameterList args = createArgs(ProjectEvent.ACTIVATED.name());
        addEventArgs(form, args, ProjectEvent.ACTIVATED);
        createRequest(args, null, form, EnumSet.of(Action.CLOSE, Action.REFRESH), true);
      }
    });
  }

  private static void doComment(final FormView form) {
    final ProjectDialog dialog = new ProjectDialog("Projekto komentaras");
    dialog.addComment("Komentaras", "", true);
    dialog.addAction("Išsaugoti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        String comment = dialog.getComment();

        if (BeeUtils.isEmpty(comment)) {
          Global.showError("Įveskite komentarą");
          return;
        }
        ParameterList args = createArgs(ProjectEvent.COMMENTED.name());
        args.addDataItem(CrmConstants.VAR_PROJECT_ID, form.getActiveRow().getId());
        args.addDataItem(CrmConstants.VAR_PROJECT_COMMENT, comment);
        createRequest(args, dialog, form, EnumSet.of(Action.REFRESH), true);
      }
    });
    dialog.display();
  }

  private static void doVisit(FormView form) {
    ParameterList args = createArgs(ProjectEvent.VISITED.name());
    args.addDataItem(CrmConstants.VAR_PROJECT_ID, form.getActiveRow().getId());
    createRequest(args, null, form, null, false);
  }

  private ProjectEventHandler() {
  }
}
