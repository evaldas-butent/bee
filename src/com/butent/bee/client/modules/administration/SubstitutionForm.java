package com.butent.bee.client.modules.administration;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class SubstitutionForm extends AbstractFormInterceptor {

  Flow info;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (Objects.equals(name, TBL_SUBSTITUTIONS) && widget instanceof Flow) {
      info = (Flow) widget;
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public FormInterceptor getInstance() {
    return new SubstitutionForm();
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    if (Objects.isNull(info)) {
      return;
    }
    info.clear();

    if (Objects.isNull(getDateTimeValue(COL_SUBSTITUTION_EXECUTED))) {
      return;
    }
    Queries.getRowSet(TBL_SUBSTITUTION_OBJECTS, Arrays.asList(COL_OBJECT, COL_OBJECT_ID),
        Filter.equals(COL_SUBSTITUTION, getActiveRowId()), rs -> {

          Multimap<String, Long> data = HashMultimap.create();
          rs.forEach(beeRow -> data.put(beeRow.getString(0), beeRow.getLong(1)));

          HtmlTable table = new HtmlTable();

          data.keySet().forEach(k -> {
            Collection<Long> ids = data.get(k);

            InternalLink cnt = new InternalLink(BeeUtils.toString(ids.size()));
            cnt.addClickHandler(clickEvent ->
                GridFactory.openGrid(k, null, GridFactory.GridOptions.forFilter(Filter.idIn(ids))));

            int r = table.getRowCount();
            table.setText(r, 0, Data.getViewCaption(k));
            table.setText(r, 1, BeeConst.STRING_NBSP);
            table.setWidget(r, 2, cnt);
          });
          info.clear();
          info.add(table);
        });
    super.beforeRefresh(form, row);
  }
}
