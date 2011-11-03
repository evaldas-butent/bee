package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.data.filter.ColumnValueFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class TaskList {

  private static class FormHandler extends AbstractFormCallback implements ClickHandler {

    private final Type type;

    private GridPanel gridPanel = null;

    private Editor dateFromWidget = null;
    private Editor dateToWidget = null;

    private Editor overdueWidget = null;

    private FormHandler(Type type) {
      super();
      this.type = type;
    }

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (widget instanceof GridPanel) {
        setGridPanel((GridPanel) widget);
        getGridPanel().setGridCallback(new GridHandler(getType()));

      } else if (widget instanceof HasClickHandlers && BeeUtils.same(name, "Filter")) {
        ((HasClickHandlers) widget).addClickHandler(this);

      } else if (widget instanceof Editor) {
        if (BeeUtils.same(name, "DateFrom")) {
          setDateFromWidget((Editor) widget);
        } else if (BeeUtils.same(name, "DateTo")) {
          setDateToWidget((Editor) widget);
        } else if (BeeUtils.same(name, "Overdue")) {
          setOverdueWidget((Editor) widget);
        }
      }
    }

    public void onClick(ClickEvent event) {
      if (getGridPanel() == null) {
        return;
      }

      String field = "FinishTime";
      List<Filter> filters = Lists.newArrayList();

      if (getDateFromWidget() != null) {
        String dateFrom = getDateFromWidget().getNormalizedValue();
        if (!BeeUtils.isEmpty(dateFrom)) {
          filters.add(new ColumnValueFilter(field, Operator.GE,
              new LongValue(BeeUtils.toLong(dateFrom))));
        }
      }

      if (getDateToWidget() != null) {
        String dateTo = getDateToWidget().getNormalizedValue();
        if (!BeeUtils.isEmpty(dateTo)) {
          filters.add(new ColumnValueFilter(field, Operator.LT,
              new LongValue(BeeUtils.toLong(dateTo))));
        }
      }

      if (getOverdueWidget() != null && BeeConst.isTrue(getOverdueWidget().getNormalizedValue())) {
        filters.add(new ColumnValueFilter(field, Operator.LT,
            new LongValue(new DateTime().getTime())));
      }

      Filter filter = filters.isEmpty() ? null : CompoundFilter.and(filters);
      getGridPanel().getPresenter().getDataProvider().setParentFilter("f1", filter, true);
    }

    @Override
    public boolean onLoad(Element formElement) {
      formElement.setAttribute("caption", getType().getCaption());
      return true;
    }

    private Editor getDateFromWidget() {
      return dateFromWidget;
    }

    private Editor getDateToWidget() {
      return dateToWidget;
    }

    private GridPanel getGridPanel() {
      return gridPanel;
    }

    private Editor getOverdueWidget() {
      return overdueWidget;
    }

    private Type getType() {
      return type;
    }

    private void setDateFromWidget(Editor dateFromWidget) {
      this.dateFromWidget = dateFromWidget;
    }

    private void setDateToWidget(Editor dateToWidget) {
      this.dateToWidget = dateToWidget;
    }

    private void setGridPanel(GridPanel gridPanel) {
      this.gridPanel = gridPanel;
    }

    private void setOverdueWidget(Editor overdueWidget) {
      this.overdueWidget = overdueWidget;
    }
  }

  private static class GridHandler extends AbstractGridCallback {

    private final Type type;
    private final Long userId;

    private GridHandler(Type type) {
      super();
      this.type = type;
      this.userId = BeeKeeper.getUser().getUserId();
    }

    @Override
    public boolean onLoad(GridDescription gridDescription) {
      gridDescription.setCaption(null);

      if (getUserId() != null && getType() != null) {
        Filter filter = null;
        Value user = new LongValue(getUserId());

        switch (getType()) {
          case ASSIGNED:
            filter = new ColumnValueFilter("Executor", Operator.EQ, user);
            break;
          case DELEGATED:
            filter = CompoundFilter.and(
                new ColumnValueFilter("Owner", Operator.EQ, user),
                new ColumnValueFilter("Executor", Operator.NE, user));
            break;
          case OBSERVED:
            filter = CompoundFilter.and(
                new ColumnValueFilter("User", Operator.EQ, user),
                new ColumnValueFilter("Owner", Operator.NE, user),
                new ColumnValueFilter("Executor", Operator.NE, user));
            break;
        }
        gridDescription.setFilter(filter);
      }
      return true;
    }

    private Type getType() {
      return type;
    }

    private Long getUserId() {
      return userId;
    }
  }

  private enum Type {
    ASSIGNED("Gautos užduotys"),
    DELEGATED("Deleguotos užduotys"),
    OBSERVED("Stebimos užduotys");

    private final String caption;

    private Type(String caption) {
      this.caption = caption;
    }

    private String getCaption() {
      return caption;
    }
  }

  public static void open(String args) {
    Type type = null;

    for (Type z : Type.values()) {
      if (BeeUtils.startsSame(args, z.name())) {
        type = z;
        break;
      }
    }

    if (type == null) {
      Global.showError("Type not recognized:", args);
    } else {
      FormFactory.openForm("TaskList", new FormHandler(type));
    }
  }

  private TaskList() {
    super();
  }
}
