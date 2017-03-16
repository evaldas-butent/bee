package com.butent.bee.client.modules.classifiers;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;

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
    clearAll.addClickHandler(clickEvent -> Queries.getRowSet(TBL_DISCOUNTS, null,
        presenter.getDataProvider().getFilter(), new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            if (!DataUtils.isEmpty(result)) {
              Global.confirm(Localized.dictionary().askDelete(result.getNumberOfRows()),
                  () -> Queries.delete(TBL_DISCOUNTS, Filter.idIn(result.getRowIds()),
                      new IntCallback() {
                        @Override
                        public void onSuccess(Integer result) {
                          getGridPresenter().handleAction(Action.REFRESH);
                        }
                      }));
            }
          }
        }));
    header.addCommandItem(clearAll);
  }
}