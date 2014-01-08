package com.butent.bee.client.modules.commons;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.HasHandlers;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.presenter.GridFormPresenter;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

class ItemFormHandler extends AbstractFormInterceptor {

  @Override
  public FormInterceptor getInstance() {
    return new ItemFormHandler();
  }

  @Override
  public void onReadyForInsert(HasHandlers listener, final ReadyForInsertEvent event) {
    Assert.notNull(event);

    String price = null;
    String currency = null;

    for (int i = 0; i < event.getColumns().size(); i++) {
      String colName = event.getColumns().get(i).getId();
      String value = event.getValues().get(i);

      if (BeeUtils.same(colName, COL_ITEM_PRICE)) {
        price = value;
      } else if (BeeUtils.same(colName, COL_ITEM_CURRENCY)) {
        currency = value;
      }
    }

    if (!BeeUtils.isEmpty(price) && BeeUtils.isEmpty(currency)) {
      event.getCallback().onFailure(Localized.getConstants().currency(),
          Localized.getConstants().valueRequired());
      event.consume();
      return;
    }

    BeeRowSet rs = new BeeRowSet(VIEW_ITEMS, event.getColumns());
    rs.addRow(0, ArrayUtils.toArray(event.getValues()));

    ParameterList args = CommonsKeeper.createArgs(SVC_ITEM_CREATE);
    args.addDataItem(VAR_ITEM_DATA, Codec.beeSerialize(rs));

    String categories = getFormView().getActiveRow().getProperty(PROP_CATEGORIES);

    if (!BeeUtils.isEmpty(categories)) {
      args.addDataItem(VAR_ITEM_CATEGORIES, categories);
    }
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

    event.consume();
  }

  @Override
  public boolean onStartEdit(final FormView form, final IsRow row,
      final Scheduler.ScheduledCommand focusCommand) {

    Filter flt = ComparisonFilter.isEqual(COL_ITEM, new LongValue(row.getId()));

    Queries.getRowSet(TBL_ITEM_CATEGORIES, null, flt, null, new RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        if (!result.isEmpty()) {
          List<Long> categ = Lists.newArrayList();

          int index = result.getColumnIndex(COL_CATEGORY);
          for (IsRow r : result.getRows()) {
            categ.add(r.getLong(index));
          }
          row.setProperty(PROP_CATEGORIES, DataUtils.buildIdList(categ));
        }

        form.updateRow(row, true);
        focusCommand.execute();
      }
    });

    return false;
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    if (form.getViewPresenter() instanceof GridFormPresenter) {
      GridInterceptor gcb = ((GridFormPresenter) form.getViewPresenter()).getGridInterceptor();

      if (gcb != null && gcb instanceof ItemGridHandler) {
        ItemGridHandler grd = (ItemGridHandler) gcb;

        if (grd.showServices()) {
          newRow.setValue(form.getDataIndex(COL_ITEM_IS_SERVICE), 1);
        }

        IsRow category = grd.getSelectedCategory();
        if (category != null) {
          newRow.setProperty(PROP_CATEGORIES, BeeUtils.toString(category.getId()));
        }
      }
    }
  }
}