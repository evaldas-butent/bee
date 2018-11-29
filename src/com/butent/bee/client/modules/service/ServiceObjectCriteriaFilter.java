package com.butent.bee.client.modules.service;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.FilterValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.service.ServiceConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceObjectCriteriaFilter extends AbstractFilterSupplier {

  private final Map<Long, Pair<String, String>> values = new HashMap<>();

  public ServiceObjectCriteriaFilter(String viewName, BeeColumn column,
      String label, String options) {
    super(viewName, column, label, options);
  }

  @Override
  public FilterValue getFilterValue() {
    if (values.isEmpty()) {
      return null;
    }
    return FilterValue.of(Codec.beeSerialize(values));
  }

  @Override
  public String getLabel() {
    if (values.isEmpty()) {
      return null;
    }
    return values.values().stream()
        .map(pair -> pair.getA() + ": " + pair.getB())
        .collect(Collectors.joining(", "));
  }

  @Override
  public void onRequest(Element target, Scheduler.ScheduledCommand onChange) {
    Queries.getRowSet(ServiceConstants.VIEW_SERVICE_MAIN_CRITERIA, null, result -> {
      if (result.getNumberOfRows() <= 0) {
        notifyInfo("Nerasta pagrindinių kriterijų");
        return;
      }
      Map<Long, Pair<String, InputText>> current = new LinkedHashMap<>();
      HtmlTable table = new HtmlTable();
      int nameIdx = result.getColumnIndex(ServiceConstants.COL_SERVICE_MAIN_CRITERIA);

      for (BeeRow beeRow : result) {
        Long id = beeRow.getId();
        String name = beeRow.getString(nameIdx);
        InputText input = new InputText();

        if (values.containsKey(id)) {
          input.setValue(values.get(id).getB());
        }
        current.put(id, Pair.of(name, input));

        int r = table.getRowCount();
        table.setText(r, 0, name);
        table.setWidget(r, 1, current.get(id).getB());
      }
      Button filter = new Button(Localized.dictionary().doFilter());
      filter.addClickHandler(arg0 -> {
        values.clear();

        current.forEach((id, pair) -> {
          if (!pair.getB().isEmpty()) {
            values.put(id, Pair.of(pair.getA(), pair.getB().getValue()));
          }
        });
        update(true);
      });
      Button cancel = new Button(Localized.dictionary().cancel());
      cancel.addClickHandler(event -> closeDialog());

      int r = table.getRowCount();
      table.setWidget(r, 0, cancel);
      table.setWidget(r, 1, filter);
      table.getCellFormatter().setHorizontalAlignment(r, 1, TextAlign.RIGHT);

      openDialog(target, table, null, onChange);
    });
  }

  @Override
  public Filter parse(FilterValue filterValue) {
    if (filterValue == null) {
      return null;
    }
    return Filter.custom(ServiceConstants.COL_SERVICE_MAIN_CRITERIA,
        Codec.beeSerialize(Codec.deserializeHashMap(filterValue.getValue()).entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> Pair.restore(e.getValue()).getB()))));
  }

  @Override
  public void setFilterValue(FilterValue filterValue) {
    values.clear();

    if (filterValue != null) {
      Codec.deserializeHashMap(filterValue.getValue())
          .forEach((k, v) -> values.put(BeeUtils.toLong(k), Pair.restore(v)));
    }
  }
}
