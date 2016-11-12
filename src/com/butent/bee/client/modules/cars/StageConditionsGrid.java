package com.butent.bee.client.modules.cars;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.cars.CarsConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class StageConditionsGrid extends AbstractGridInterceptor {

  private String dataView;

  @Override
  public GridInterceptor getInstance() {
    return new StageConditionsGrid();
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription, CellSource cellSource) {

    if (BeeUtils.same(columnName, COL_STAGE_FIELD)) {
      return new AbstractCellRenderer(cellSource) {
        private int fieldIdx = DataUtils.getColumnIndex(COL_STAGE_FIELD, dataColumns);
        private int opIdx = DataUtils.getColumnIndex(COL_STAGE_OPERATOR, dataColumns);
        private int valueIdx = DataUtils.getColumnIndex(COL_STAGE_VALUE, dataColumns);

        @Override
        public String render(IsRow row) {
          return BeeUtils.joinWords(Data.getColumnLabel(dataView, row.getString(fieldIdx)),
              "<strong>" + EnumUtils.getEnumByIndex(Operator.class,
                  row.getInteger(opIdx)).getCaption() + "</strong>", row.getString(valueIdx));
        }
      };
    }
    return super.getRenderer(columnName, dataColumns, columnDescription, cellSource);
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    dataView = null;
    IsRow parentRow = event.getRow();

    if (Objects.nonNull(parentRow)) {
      dataView = Data.getString(event.getViewName(), parentRow, COL_STAGE_VIEW);
    }
    super.onParentRow(event);
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    HtmlTable tbl = new HtmlTable();

    ListBox field = new ListBox();
    Data.getColumns(dataView).stream().filter(column -> ValueType.isString(column.getType()))
        .forEach(column -> field.addItem(Localized.getLabel(column), column.getId()));
    tbl.setWidget(0, 0, field);

    ListBox op = new ListBox();
    EnumSet.of(Operator.EQ, Operator.NE, Operator.IS_NULL, Operator.NOT_NULL).forEach(operator ->
        op.addItem(operator.getCaption(), BeeUtils.toString(operator.ordinal())));
    tbl.setWidget(1, 0, op);

    InputText value = new InputText();
    tbl.setWidget(2, 0, value);

    Global.inputWidget(Localized.dictionary().specifyCondition(), tbl, new InputCallback() {
      @Override
      public String getErrorMessage() {
        Widget w = null;

        if (BeeUtils.isEmpty(field.getValue())) {
          w = field;
        } else if (BeeUtils.isEmpty(op.getValue())) {
          w = op;
        } else if (BeeUtils.in(BeeUtils.toInt(op.getValue()), Operator.EQ.ordinal(),
            Operator.NE.ordinal()) && BeeUtils.isEmpty(value.getValue())) {
          w = value;
        }
        if (Objects.nonNull(w)) {
          UiHelper.focus(w);
          return Localized.dictionary().valueRequired();
        }
        return null;
      }

      @Override
      public void onSuccess() {
        getGridView().ensureRelId(stage -> Queries.insert(getViewName(),
            Data.getColumns(getViewName(), Arrays.asList(COL_STAGE, COL_STAGE_FIELD,
                COL_STAGE_OPERATOR, COL_STAGE_VALUE)), Arrays.asList(BeeUtils.toString(stage),
                field.getValue(), op.getValue(), value.getValue()), null,
            new RowInsertCallback(getViewName()) {
              @Override
              public void onSuccess(BeeRow result) {
                getGridPresenter().refresh(false, false);
                super.onSuccess(result);
              }
            }));
      }
    });
    return false;
  }
}
