package com.butent.bee.client.modules.classifiers;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Collator;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CompanyTypeReport extends ReportInterceptor {
  
  private static final class Type implements Comparable<Type> {
    private final Long id;
    private final String name;

    private Type(Long id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public int compareTo(Type o) {
      if (this == o || Objects.equals(id, o.id)) {
        return BeeConst.COMPARE_EQUAL;
      } else if (id == null) {
        return BeeConst.COMPARE_MORE;
      } else {
        return Collator.DEFAULT.compare(name, o.name);
      }
    }

    @Override
    public int hashCode() {
      return id == null ? 0 : id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Type && Objects.equals(id, ((Type) obj).id);
    }
  }

  private static final String NAME_START_DATE = "StartDate";
  private static final String NAME_END_DATE = "EndDate";

  private static final String NAME_TYPES = "Types";

  private static final String STYLE_PREFIX = StyleUtils.CLASS_NAME_PREFIX + "co-ctr-";

  private static final String STYLE_TABLE = STYLE_PREFIX + "table";
  private static final String STYLE_HEADER = STYLE_PREFIX + "header";

  private static final String STYLE_YEAR = STYLE_PREFIX + "year";
  private static final String STYLE_MONTH = STYLE_PREFIX + "month";

  private static final String STYLE_VALUE = STYLE_PREFIX + "value";
  private static final String STYLE_ROW_TOTAL = STYLE_PREFIX + "row-total";
  private static final String STYLE_COL_TOTAL = STYLE_PREFIX + "col-total";

  private static final String STYLE_DETAILS = STYLE_PREFIX + "details";
  private static final String STYLE_SUMMARY = STYLE_PREFIX + "summary";

  CompanyTypeReport() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new CompanyTypeReport();
  }

  @Override
  public void onLoad(FormView form) {
    Long user = BeeKeeper.getUser().getUserId();
    if (!DataUtils.isId(user)) {
      return;
    }

    Widget widget = form.getWidgetByName(NAME_START_DATE);
    DateTime dateTime = BeeKeeper.getStorage().getDateTime(storageKey(NAME_START_DATE, user));
    if (widget instanceof InputDateTime && dateTime != null) {
      ((InputDateTime) widget).setDateTime(dateTime);
    }

    widget = form.getWidgetByName(NAME_END_DATE);
    dateTime = BeeKeeper.getStorage().getDateTime(storageKey(NAME_END_DATE, user));
    if (widget instanceof InputDateTime && dateTime != null) {
      ((InputDateTime) widget).setDateTime(dateTime);
    }

    widget = form.getWidgetByName(NAME_TYPES);
    String idList = BeeKeeper.getStorage().get(storageKey(NAME_TYPES, user));
    if (widget instanceof MultiSelector && !BeeUtils.isEmpty(idList)) {
      ((MultiSelector) widget).render(idList);
    }
  }

  @Override
  public void onUnload(FormView form) {
    Long user = BeeKeeper.getUser().getUserId();
    if (!DataUtils.isId(user)) {
      return;
    }

    BeeKeeper.getStorage().set(storageKey(NAME_START_DATE, user), getDateTime(NAME_START_DATE));
    BeeKeeper.getStorage().set(storageKey(NAME_END_DATE, user), getDateTime(NAME_END_DATE));

    BeeKeeper.getStorage().set(storageKey(NAME_TYPES, user), getEditorValue(NAME_TYPES));
  }

  @Override
  protected void clearFilter() {
    clearEditor(NAME_START_DATE);
    clearEditor(NAME_END_DATE);

    clearEditor(NAME_TYPES);
  }

  @Override
  protected void doReport() {
    DateTime start = getDateTime(NAME_START_DATE);
    DateTime end = getDateTime(NAME_END_DATE);

    if (!checkRange(start, end)) {
      return;
    }

    ParameterList params = ClassifierKeeper.createArgs(SVC_GET_COMPANY_TYPE_REPORT);

    if (start != null) {
      params.addDataItem(Service.VAR_FROM, start.getTime());
    }
    if (end != null) {
      params.addDataItem(Service.VAR_TO, end.getTime());
    }

    String types = getEditorValue(NAME_TYPES);
    if (!BeeUtils.isEmpty(types)) {
      params.addDataItem(COL_RELATION_TYPE, types);
    }

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasMessages()) {
          response.notify(getFormView());
        }

        if (response.hasResponse(SimpleRowSet.class)) {
          SimpleRowSet data = SimpleRowSet.restore(response.getResponseAsString());
          renderData(transformData(data));
        } else {
          getFormView().notifyWarning(Localized.getConstants().nothingFound());
        }
      }
    });
  }

  @Override
  protected String getStorageKeyPrefix() {
    return "CompanyTypeReport_";
  }

  private void renderData(Table<YearMonth, Type, Integer> data) {
    HasIndexedWidgets container = getDataContainer();
    if (container == null) {
      return;
    }

    if (!container.isEmpty()) {
      container.clear();
    }
    
    List<YearMonth> yms = Lists.newArrayList(data.rowKeySet());
    if (yms.size() > 1) {
      Collections.sort(yms);
    }
    
    List<Type> types = Lists.newArrayList(data.columnKeySet());
    if (types.size() > 1) {
      Collections.sort(types);
    }
    
    int[] colTotals = new int[types.size()];

    final HtmlTable table = new HtmlTable(STYLE_TABLE);

    int row = 0;
    
    int yearCol = 0;
    int monthCol = 1;
    
    int valueStartCol = 2;

    table.setText(row, yearCol, Localized.getConstants().year(), STYLE_HEADER);
    table.setText(row, monthCol, Localized.getConstants().month(), STYLE_HEADER);

    for (int j = 0; j < types.size(); j++) {
      table.setText(row, valueStartCol + j, types.get(j).name, STYLE_HEADER);
    }
    
    if (types.size() > 1) {
      table.setText(row, valueStartCol + types.size(), Localized.getConstants().total(),
          STYLE_HEADER);
    }

    row++;

    for (YearMonth ym : yms) {
      table.setValue(row, yearCol, ym.getYear(), STYLE_YEAR);
      table.setText(row, monthCol, Format.renderMonthFullStandalone(ym.getMonth()), STYLE_MONTH);
      
      int rowTotal = 0;

      for (int j = 0; j < types.size(); j++) {
        Type type = types.get(j);

        if (data.contains(ym, type)) {
          Integer value = data.get(ym, type);
          table.setText(row, valueStartCol + j, renderQuantity(value), STYLE_VALUE);
          
          colTotals[j] += value;
          rowTotal += value;

        } else {
          table.setText(row, valueStartCol + j, BeeConst.STRING_EMPTY, STYLE_VALUE);
        }
      }

      if (types.size() > 1) {
        table.setText(row, valueStartCol + types.size(), renderQuantity(rowTotal), STYLE_ROW_TOTAL);
      }
      
      table.getRowFormatter().addStyleName(row, STYLE_DETAILS);
      row++;
    }

    if (yms.size() > 1) {
      for (int j = 0; j < types.size(); j++) {
        table.setText(row, valueStartCol + j, renderQuantity(colTotals[j]), STYLE_COL_TOTAL);
      }

      if (types.size() > 1) {
        table.setText(row, valueStartCol + types.size(), renderQuantity(ArrayUtils.sum(colTotals)),
            STYLE_ROW_TOTAL, STYLE_COL_TOTAL);
      }
      
      table.getRowFormatter().addStyleName(row, STYLE_SUMMARY);
    }

    table.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
      }
    });

    container.add(table);
  }

  private static Table<YearMonth, Type, Integer> transformData(SimpleRowSet data) {
    Table<YearMonth, Type, Integer> table = HashBasedTable.create();
    
    int valueIndex = data.getNumberOfColumns() - 1;
    
    for (SimpleRow row : data) {
      YearMonth ym = new YearMonth(row.getInt(BeeConst.YEAR), row.getInt(BeeConst.MONTH));
      Type type = new Type(row.getLong(COL_RELATION_TYPE), row.getValue(COL_RELATION_TYPE_NAME));
      Integer value = row.getInt(valueIndex);
      
      if (BeeUtils.isPositive(value)) {
        table.put(ym, type, value);
      }
    }
    
    return table;
  }
}
