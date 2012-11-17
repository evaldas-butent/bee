package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.images.Images;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.modules.crm.CrmConstants.ProjectEvent;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ProjectList {

  private static class GridHandler extends AbstractGridCallback {

    private final Type type;
    private final Long userId;

    private final Map<String, Editor> filterWidgets = Maps.newHashMap();

    private GridHandler(Type type) {
      this.type = type;
      this.userId = BeeKeeper.getUser().getUserId();
    }

    @Override
    public boolean afterCreateColumn(String columnId, final List<? extends IsColumn> dataColumns,
        final AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
        EditableColumn editableColumn) {

      if (BeeUtils.same(columnId, "Mode") && column instanceof HasCellRenderer) {
        ((HasCellRenderer) column).setRenderer(new AbstractCellRenderer() {

          private Widget modeNew = null;
          private Widget modeUpd = null;

          private int laIndex;
          private int lpIndex;

          {
            setOptions(column.getOptions());
            laIndex = DataUtils.getColumnIndex(CrmConstants.COL_LAST_ACCESS, dataColumns);
            lpIndex = DataUtils.getColumnIndex(CrmConstants.COL_LAST_PUBLISH, dataColumns);
          }

          @Override
          public String render(IsRow row) {
            if (row == null) {
              return null;
            }
            Long access = row.getLong(laIndex);
            if (access == null) {
              return getHtml(modeNew);
            }
            Long publish = row.getLong(lpIndex);
            if (access < publish) {
              return getHtml(modeUpd);
            }
            return BeeConst.STRING_EMPTY;
          }

          public void setOptions(String options) {
            if (!BeeUtils.isEmpty(options)) {
              int idx = 0;
              for (String mode : NameUtils.NAME_SPLITTER.split(options)) {
                ImageResource resource = Images.get(mode);
                Widget widget = (resource == null) ? new BeeLabel(mode) : new BeeImage(resource);
                switch (idx++) {
                  case 0:
                    modeNew = widget;
                    break;
                  case 1:
                    modeUpd = widget;
                    break;
                }
              }
            }
          }

          private String getHtml(Widget widget) {
            if (widget == null) {
              return null;
            } else if (widget instanceof BeeLabel) {
              return widget.getElement().getInnerHTML();
            } else {
              DomUtils.createId(widget, "mode");
              return widget.getElement().getString();
            }
          }
        });
      }
      return true;
    }

    @Override
    public void afterCreateWidget(String name, IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {
      if (widget instanceof HasClickHandlers && BeeUtils.same(name, "Filter")) {
        ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            if (getGridPresenter() != null) {
              updateFilter(getGridPresenter());
              getGridPresenter().refresh(true);
            }
          }
        });
      } else if (widget instanceof Editor) {
        this.filterWidgets.put(BeeUtils.normalize(name), (Editor) widget);
      }
    }

    @Override
    public boolean beforeCreateColumn(String columnId, List<? extends IsColumn> dataColumns,
        ColumnDescription columnDescription) {
      return getType().equals(Type.OBSERVED) || !BeeUtils.same(columnId, CrmConstants.COL_OWNER);
    }

    @Override
    public int beforeDeleteRow(GridPresenter presenter, IsRow row) {
      Provider provider = presenter.getDataProvider();

      if (!ProjectEventHandler.availableEvent(ProjectEvent.DELETED,
          row.getInteger(provider.getColumnIndex(CrmConstants.COL_EVENT)),
          row.getLong(provider.getColumnIndex(CrmConstants.COL_OWNER)))) {

        presenter.getGridView().notifyWarning("Verboten");
        return GridCallback.DELETE_CANCEL;
      }
      return GridCallback.DELETE_DEFAULT;
    }

    @Override
    public int beforeDeleteRows(GridPresenter presenter, IsRow activeRow,
        Collection<RowInfo> selectedRows) {
      presenter.deleteRow(activeRow, false);
      return GridCallback.DELETE_CANCEL;
    }

    @Override
    public void beforeRefresh(GridPresenter presenter) {
      updateFilter(presenter);
    }

    @Override
    public String getCaption() {
      if (getType() == null) {
        return null;
      } else {
        return getType().getCaption();
      }
    }

    @Override
    public Map<String, Filter> getInitialFilters() {
      Filter filter = Filter.or(Lists.newArrayList(getEventFilter(ProjectEvent.CREATED),
          getEventFilter(ProjectEvent.ACTIVATED), getEventFilter(ProjectEvent.SUSPENDED)));
      Map<String, Filter> filters = Maps.newHashMap();
      filters.put(FILTER_KEY, filter);
      return filters;
    }

    @Override
    public boolean onLoad(GridDescription gridDescription) {
      gridDescription.setCaption(null);

      if (getUserId() != null && getType() != null) {
        Value user = new LongValue(getUserId());
        Filter filter = null;

        switch (getType()) {
          case CONTROLLED:
            filter = ComparisonFilter.isEqual(CrmConstants.COL_OWNER, user);
            break;
          case OBSERVED:
            filter = Filter.and(ComparisonFilter.isEqual(CrmConstants.COL_USER, user),
                ComparisonFilter.isNotEqual(CrmConstants.COL_OWNER, user));
            break;
        }
        gridDescription.setFilter(filter);
      }
      return true;
    }

    private Filter getEventFilter(ProjectEvent pe) {
      if (pe == null) {
        return null;
      } else {
        return ComparisonFilter.isEqual(CrmConstants.COL_EVENT, new IntegerValue(pe.ordinal()));
      }
    }

    private Filter getFilter() {
      CompoundFilter andFilter = Filter.and();
      Value now = new IntegerValue(new JustDate().getDays());

      if (isChecked("Changed")) {
        andFilter.add(Filter.or(Filter.isEmpty(CrmConstants.COL_LAST_ACCESS),
            ComparisonFilter.compareWithColumn(CrmConstants.COL_LAST_ACCESS, Operator.LT,
                CrmConstants.COL_LAST_PUBLISH)));
      }
      if (isChecked("Overdue")) {
        andFilter.add(ComparisonFilter.isLess("FinishDate", now),
            getEventFilter(ProjectEvent.ACTIVATED));
      } else {
        CompoundFilter orFilter = Filter.or();

        for (ProjectEvent pe : ProjectEvent.values()) {
          if (isChecked(pe.name())) {
            orFilter.add(getEventFilter(pe));
          }
        }
        if (!orFilter.isEmpty()) {
          andFilter.add(orFilter);
        }
      }
      return andFilter.isEmpty() ? null : andFilter;
    }

    private Type getType() {
      return type;
    }

    private Long getUserId() {
      return userId;
    }

    private boolean isChecked(String filter) {
      String name = BeeUtils.normalize(filter);

      if (filterWidgets.containsKey(name)) {
        return BeeUtils.toBoolean(filterWidgets.get(name).getNormalizedValue());
      }
      return false;
    }

    private void updateFilter(GridPresenter presenter) {
      if (presenter != null) {
        presenter.getDataProvider().setParentFilter(FILTER_KEY, getFilter());
      }
    }
  }

  private enum Type {
    CONTROLLED("Valdomi projektai"),
    OBSERVED("Stebimi projektai");

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
      GridFactory.openGrid("UserProjects", new GridHandler(type));
    }
  }

  private ProjectList() {
  }
}
