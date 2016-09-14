package com.butent.bee.client.modules.classifiers;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

public class DiscountsGrid extends AbstractGridInterceptor {

  @Override
  public GridInterceptor getInstance() {
    return new DiscountsGrid();
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    HeaderView header = presenter.getHeader();

    if (header == null) {
      return;
    }

    header.clearCommandPanel();

    Button clearAll = new Button();
    clearAll.setText(Localized.dictionary().clear());
    clearAll.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if (!getGridView().isEmpty()) {
          Global.confirm(Localized.dictionary().askDeleteAll(), new ConfirmationCallback() {

            @Override
            public void onConfirm() {

              Filter filter = Filter.or(Filter.notNull(COL_COMPANY), Filter.notNull(COL_ITEM),
                  Filter.notNull(COL_CATEGORY));
              Queries.delete(TBL_DISCOUNTS, filter, new IntCallback() {

                @Override
                public void onSuccess(Integer result) {
                  if (BeeUtils.isPositive(result)) {
                    getGridPresenter().handleAction(Action.REFRESH);
                  }
                }
              });
            }
          });
        }
      }
    });

    header.addCommandItem(clearAll);
  }
}