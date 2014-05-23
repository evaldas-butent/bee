package com.butent.bee.client.modules.ec;

import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;

import static com.butent.bee.shared.modules.ec.EcConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.modules.ec.view.SearchByCar;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.ec.EcCarType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ArticleCarsGridInterceptor extends AbstractGridInterceptor implements
    BeforeSelectionHandler<EcCarType> {

  private static final String STYLE_DIALOG = EcStyles.name("add-article-cars");

  ArticleCarsGridInterceptor() {
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    EcKeeper.ensureClientStyleSheet();

    Set<Long> exclusions = new HashSet<>();

    List<? extends IsRow> data = presenter.getGridView().getRowData();
    if (!BeeUtils.isEmpty(data)) {
      int index = getDataIndex(COL_TCD_TYPE);
      for (IsRow row : data) {
        exclusions.add(row.getLong(index));
      }
    }

    SearchByCar widget = new SearchByCar(exclusions);
    widget.addBeforeSelectionHandler(this);

    Global.inputWidget(getDialogCaption(presenter.getGridView()), widget, new InputCallback() {
      @Override
      public void onSuccess() {
      }
    }, STYLE_DIALOG);

    return false;
  }

  @Override
  public GridInterceptor getInstance() {
    return new ArticleCarsGridInterceptor();
  }

  @Override
  public void onBeforeSelection(BeforeSelectionEvent<EcCarType> event) {
    if (event.getItem() != null) {
      LogUtils.getRootLogger().debug(event.getItem().getManufacturer(),
          event.getItem().getModelName(), event.getItem().getTypeId());
    }
    event.cancel();
  }

  private static String getDialogCaption(GridView grid) {
    FormView form = UiHelper.getForm(grid.asWidget());
    IsRow itemRow = (form == null) ? null : form.getActiveRow();

    if (itemRow == null) {
      return Localized.getConstants().ecItemDetailsCarTypes();
    } else {
      return BeeUtils.joinWords(form.getStringValue(COL_TCD_ARTICLE_NAME),
          form.getStringValue(COL_TCD_ARTICLE_NR),
          Localized.getConstants().ecItemDetailsCarTypes());
    }
  }
}
