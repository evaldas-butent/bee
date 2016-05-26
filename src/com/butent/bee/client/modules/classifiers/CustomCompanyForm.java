package com.butent.bee.client.modules.classifiers;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.COL_SERVICE;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.Set;

public abstract class CustomCompanyForm extends AbstractFormInterceptor {

  private Long company;

  @Override
  public void afterCreate(FormView form) {
    Global.getParameter(AdministrationConstants.PRM_COMPANY,
        input -> company = BeeUtils.toLongOrNull(input));

    super.afterCreate(form);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, "CompanyPayAccounts") && widget instanceof ChildGrid) {
      ((ChildGrid) widget).setGridInterceptor(new AbstractGridInterceptor() {
        @Override
        public void afterCreateEditor(String source, Editor editor, boolean embedded) {
          if ((BeeUtils.same(source, "Account") || BeeUtils.same(source, COL_SERVICE))
              && editor instanceof DataSelector) {

            ((DataSelector) editor).addSelectorHandler(event -> {
              if (BeeUtils.same(event.getRelatedViewName(), TBL_COMPANY_BANK_ACCOUNTS)) {
                if (event.isOpened()) {
                  int idx = getDataIndex("Account");
                  Set<Long> ids = new HashSet<>();

                  for (IsRow row : getGridView().getRowData()) {
                    ids.add(row.getLong(idx));
                  }
                  event.getSelector()
                      .setAdditionalFilter(Filter.and(Filter.equals(COL_COMPANY, company),
                          Filter.idNotIn(ids)));
                }
              }
            });
          }
          super.afterCreateEditor(source, editor, embedded);
        }

        @Override
        public GridInterceptor getInstance() {
          return null;
        }
      });
    }
    super.afterCreateWidget(name, widget, callback);
  }
}
