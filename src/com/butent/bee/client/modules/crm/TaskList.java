package com.butent.bee.client.modules.crm;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.images.Images;
import com.butent.bee.client.images.star.Stars;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.validation.ValidationHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.edit.EditorAssistant;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Procedure;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;

class TaskList {

  private static class GridHandler extends AbstractGridInterceptor {

    private final Type type;
    private final Long userId;

    private GridHandler(Type type) {
      this.type = type;
      this.userId = BeeKeeper.getUser().getUserId();
    }

    @Override
    public boolean afterCreateColumn(String columnId, final List<? extends IsColumn> dataColumns,
        final AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
        EditableColumn editableColumn) {

      if (BeeUtils.same(columnId, "Mode") && column instanceof HasCellRenderer) {
        ((HasCellRenderer) column).setRenderer(new ModeRenderer(column.getOptions()));

      } else if (BeeUtils.inListSame(columnId, COL_FINISH_TIME, COL_EXECUTOR)) {
        editableColumn.addCellValidationHandler(ValidationHelper.DO_NOT_VALIDATE);
      }
      
      return true;
    }

    @Override
    public boolean beforeCreateColumn(String columnId, List<? extends IsColumn> dataColumns,
        ColumnDescription columnDescription) {
      switch (type) {
        case ASSIGNED:
          return !BeeUtils.same(columnId, COL_EXECUTOR);
        case DELEGATED:
          return !BeeUtils.same(columnId, COL_OWNER);
        default:
          return true;
      }
    }

    @Override
    public int beforeDeleteRow(GridPresenter presenter, IsRow row) {
      Provider provider = presenter.getDataProvider();
      Long owner = row.getLong(provider.getColumnIndex(COL_OWNER));

      if (owner == null || owner == userId) {
        return GridInterceptor.DELETE_DEFAULT;
      } else {
        presenter.getGridView().notifyWarning("Verboten");
        return GridInterceptor.DELETE_CANCEL;
      }
    }

    @Override
    public int beforeDeleteRows(GridPresenter presenter, IsRow activeRow,
        Collection<RowInfo> selectedRows) {
      presenter.deleteRow(activeRow, false);
      return GridInterceptor.DELETE_CANCEL;
    }

    @Override
    public String getCaption() {
      return type.getCaption();
    }

    @Override
    public String getColumnCaption(String columnName) {
      if (PROP_STAR.equals(columnName)) {
        return Stars.getDefaultHeader();
      } else {
        return super.getColumnCaption(columnName);
      }
    }

    @Override
    public String getSupplierKey() {
      return BeeUtils.normalize(BeeUtils.join(BeeConst.STRING_UNDER, "grid", GRID_TASKS, type));
    }

    @Override
    public void onEditStart(final EditStartEvent event) {
      if (PROP_STAR.equals(event.getColumnId())) {
        IsRow row = event.getRowValue();
        if (row == null) {
          return;
        }
        
        if (row.getProperty(PROP_USER) == null) {
          return;
        }
        
        final CellSource source = CellSource.forProperty(PROP_STAR, ValueType.INTEGER);

        EditorAssistant.editStarCell(event, source, new Procedure<Integer>() {
          @Override
          public void call(Integer parameter) {
            updateStar(event, source, parameter);
          }
        });
      }
    }

    @Override
    public boolean onLoad(GridDescription gridDescription) {
      gridDescription.setFilter(type.getFilter(new LongValue(userId)));
      return true;
    }

    private void updateStar(final EditStartEvent event, final CellSource source,
        final Integer value) {
      final long rowId = event.getRowValue().getId();

      Filter filter = Filter.and(ComparisonFilter.isEqual(COL_TASK, new LongValue(rowId)),
          ComparisonFilter.isEqual(COL_USER, new LongValue(userId)));

      Queries.update(VIEW_TASK_USERS, filter, COL_STAR, new IntegerValue(value),
          new Queries.IntCallback() {
            @Override
            public void onSuccess(Integer result) {
              BeeKeeper.getBus().fireEvent(new CellUpdateEvent(VIEW_TASKS, rowId,
                  event.getRowValue().getVersion(), source,
                  (value == null) ? null : BeeUtils.toString(value)));
            }
          });
    }
  }

  private static class ModeRenderer extends AbstractCellRenderer {
    private String modeNew = null;
    private String modeUpd = null;

    private ModeRenderer(String options) {
      super(null);
      setOptions(options);
    }

    @Override
    public String render(IsRow row) {
      if (row == null) {
        return null;
      }

      if (row.getProperty(PROP_USER) == null) {
        return BeeConst.STRING_EMPTY;
      }
      
      Long access = BeeUtils.toLongOrNull(row.getProperty(PROP_LAST_ACCESS));
      if (access == null) {
        return modeNew;
      }

      Long publish = BeeUtils.toLongOrNull(row.getProperty(PROP_LAST_PUBLISH));
      if (publish != null && access < publish) {
        return modeUpd;
      }
      return BeeConst.STRING_EMPTY;
    }

    public void setOptions(String options) {
      if (!BeeUtils.isEmpty(options)) {
        int idx = 0;

        for (String mode : NameUtils.NAME_SPLITTER.split(options)) {
          String html = BeeUtils.notEmpty(Images.getHtml(mode), mode);

          switch (idx++) {
            case 0:
              modeNew = html;
              break;
            case 1:
              modeUpd = html;
              break;
          }
        }
      }
    }
  }

  private enum Type implements HasCaption {
    ASSIGNED("Gautos užduotys") {
      @Override
      Filter getFilter(LongValue userValue) {
        return ComparisonFilter.isEqual(COL_EXECUTOR, userValue);
      }
    },
    
    DELEGATED("Deleguotos užduotys") {
      @Override
      Filter getFilter(LongValue userValue) {
        return ComparisonFilter.isEqual(COL_OWNER, userValue);
      }
    },
    
    OBSERVED("Stebimos užduotys") {
      @Override
      Filter getFilter(LongValue userValue) {
        return Filter.and(ComparisonFilter.isNotEqual(COL_OWNER, userValue),
            ComparisonFilter.isNotEqual(COL_EXECUTOR, userValue),
            Filter.in(Data.getIdColumn(VIEW_TASKS), VIEW_TASK_USERS, COL_TASK,
                ComparisonFilter.isEqual(COL_USER, userValue)));
      }
    },
    
    GENERAL("Užduočių sąrašas") {
      @Override
      Filter getFilter(LongValue userValue) {
        return null;
      }
    };

    private final String caption;

    private Type(String caption) {
      this.caption = caption;
    }

    @Override
    public String getCaption() {
      return caption;
    }
    
    abstract Filter getFilter(LongValue userValue);
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
      Global.showError(Lists.newArrayList(GRID_TASKS, "Type not recognized:", args));
    } else {
      GridFactory.openGrid(GRID_TASKS, new GridHandler(type));
    }
  }

  private TaskList() {
    super();
  }
}
