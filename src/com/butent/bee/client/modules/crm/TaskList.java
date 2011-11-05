package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.modules.crm.CrmConstants.TaskEvent;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public class TaskList {

  private static class FormHandler extends AbstractFormCallback implements ClickHandler {

    private final Type type;
    private GridPanel gridPanel = null;
    private Map<String, Editor> filterWidgets = Maps.newHashMap();

    private FormHandler(Type type) {
      super();
      this.type = type;
    }

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (widget instanceof GridPanel) {
        setGridPanel((GridPanel) widget);
        getGridPanel().setGridCallback(new GridHandler(getType(), this));

      } else if (widget instanceof HasClickHandlers && BeeUtils.same(name, "Filter")) {
        ((HasClickHandlers) widget).addClickHandler(this);

      } else if (widget instanceof Editor) {
        this.filterWidgets.put(BeeUtils.normalize(name), (Editor) widget);
      }
    }

    public Filter getFilter() {
      List<Filter> filters = Lists.newArrayList();
      Value now = new LongValue(new DateTime().getTime());

      Value dateFrom = getDateValue("DateFrom");
      if (dateFrom != null) {
        filters.add(ComparisonFilter.isMoreEqual("FinishTime", dateFrom));
      }
      Value dateTo = getDateValue("DateTo");
      if (dateTo != null) {
        filters.add(ComparisonFilter.isLess("FinishTime", dateTo));
      }
      if (isChecked("Overdue")) {
        filters.add(ComparisonFilter.isLess("FinishTime", now));
      }
      if (isChecked("Updated")) {
        filters.add(ComparisonFilter.compareWithColumn("LastAccess", Operator.LT, "LastPublish"));
      }
      List<Filter> orFilters = Lists.newArrayList();

      if (isChecked("Scheduled")) {
        orFilters.add(ComparisonFilter.isMore("StartTime", now));
      }
      if (isChecked("Executing")) {
        Filter flt = ComparisonFilter.isEqual("Event",
            new IntegerValue(TaskEvent.ACTIVATED.ordinal()));

        if (!isChecked("Scheduled")) {
          flt = CompoundFilter.and(flt, ComparisonFilter.isLessEqual("StartTime", now));
        }
        orFilters.add(flt);
      }
      for (TaskEvent flt : TaskEvent.values()) {
        if (isChecked(flt.name())) {
          orFilters.add(ComparisonFilter.isEqual("Event", new IntegerValue(flt.ordinal())));
        }
      }
      if (!orFilters.isEmpty()) {
        filters.add(CompoundFilter.or(orFilters));
      }

      return filters.isEmpty() ? null : CompoundFilter.and(filters);
    }

    public void onClick(ClickEvent event) {
      if (getGridPanel() == null) {
        return;
      }
      getGridPanel().getPresenter().getDataProvider().setParentFilter(FILTER_KEY, getFilter(),
          true);
    }

    @Override
    public boolean onLoad(Element formElement) {
      formElement.setAttribute("caption", getType().getCaption());
      return true;
    }

    @Override
    public void onShow(FormPresenter presenter) {
      Editor widget = filterWidgets.get(BeeUtils.normalize("Executing"));

      if (widget != null) {
        widget.setValue("true");
      }
    }

    private Value getDateValue(String filter) {
      Value date = null;
      String name = BeeUtils.normalize(filter);

      if (filterWidgets.containsKey(name)) {
        String dt = filterWidgets.get(name).getNormalizedValue();

        if (!BeeUtils.isEmpty(dt)) {
          date = new LongValue(BeeUtils.toLong(dt));
        }
      }
      return date;
    }

    private GridPanel getGridPanel() {
      return gridPanel;
    }

    private Type getType() {
      return type;
    }

    private boolean isChecked(String filter) {
      String name = BeeUtils.normalize(filter);

      if (filterWidgets.containsKey(name)) {
        return BeeUtils.toBoolean(filterWidgets.get(name).getNormalizedValue());
      }
      return false;
    }

    private void setGridPanel(GridPanel gridPanel) {
      this.gridPanel = gridPanel;
    }
  }

  private static class GridHandler extends AbstractGridCallback {

    private final Type type;
    private final FormHandler formHandler;
    private final Long userId;

    private GridHandler(Type type, FormHandler formHandler) {
      super();
      this.type = type;
      this.formHandler = formHandler;
      this.userId = BeeKeeper.getUser().getUserId();
    }

    @Override
    public boolean beforeCreateColumn(String columnId, List<BeeColumn> dataColumns,
        ColumnDescription columnDescription) {

      return getType().equals(Type.ASSIGNED) && !BeeUtils.same(columnId, "Executor")
          || getType().equals(Type.DELEGATED) && !BeeUtils.same(columnId, "Owner");
    }

    @Override
    public boolean onLoad(GridDescription gridDescription) {
      gridDescription.setCaption(null);

      if (getUserId() != null && getType() != null) {
        Filter filter = null;
        Value user = new LongValue(getUserId());

        switch (getType()) {
          case ASSIGNED:
            filter = ComparisonFilter.isEqual("Executor", user);
            break;
          case DELEGATED:
            filter = CompoundFilter.and(
                ComparisonFilter.isEqual("Owner", user),
                ComparisonFilter.isNotEqual("Executor", user));
            break;
          case OBSERVED:
            filter = CompoundFilter.and(
                ComparisonFilter.isEqual("User", user),
                ComparisonFilter.isNotEqual("Owner", user),
                ComparisonFilter.isNotEqual("Executor", user));
            break;
        }
        gridDescription.setFilter(filter);
        gridDescription.setParentFilter(FILTER_KEY, formHandler.getFilter());
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

  private static final String FILTER_KEY = "f1";

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
