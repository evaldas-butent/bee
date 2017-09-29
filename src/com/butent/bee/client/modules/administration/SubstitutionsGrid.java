package com.butent.bee.client.modules.administration;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.CustomAction;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Arrays;
import java.util.Objects;

public class SubstitutionsGrid extends AbstractGridInterceptor {

  private CustomAction substitutionAction = new CustomAction(FontAwesome.GEARS, ev -> substitute());

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    substitutionAction.setTitle(Localized.dictionary().crmTaskDoExecute());
    presenter.getHeader().addCommandItem(substitutionAction);

    super.afterCreatePresenter(presenter);
  }

  @Override
  public GridInterceptor getInstance() {
    return new SubstitutionsGrid();
  }

  private void substitute() {
    if (Objects.isNull(getActiveRow())) {
      getGridView().notifyWarning(Localized.dictionary().selectActiveRow());
      return;
    }
    Global.confirm(Localized.dictionary().substitution(), Icon.QUESTION,
        Arrays.asList(BeeUtils.joinWords(getStringValue(COL_FIRST_NAME),
            getStringValue(COL_LAST_NAME)), "->",
            BeeUtils.joinWords(getStringValue(COL_SUBSTITUTE + COL_FIRST_NAME),
                getStringValue(COL_SUBSTITUTE + COL_LAST_NAME))), () -> {

          substitutionAction.running();
          ParameterList args = AdministrationKeeper.createArgs(SVC_DO_SUBSTITUTION);
          args.addDataItem(COL_SUBSTITUTION, getActiveRowId());

          BeeKeeper.getRpc().makeRequest(args, response -> {
            substitutionAction.idle();
            response.notify(getGridView());
            getGridPresenter().refresh(true, false);
          });
        });
  }
}
