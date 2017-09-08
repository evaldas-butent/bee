package com.butent.bee.client.modules.classifiers;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class CustomCompanyForm extends CompanyForm {

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid && (BeeUtils.same(name, GRID_COMPANY_TASK_TEMPLATES))) {
      ((ChildGrid) widget).addReadyHandler(re -> {
        GridView gridView = ViewHelper.getChildGrid(getFormView(), GRID_COMPANY_TASK_TEMPLATES);

        if (gridView != null) {
          gridView.getGrid().addMutationHandler(mu -> getTotalRiskRating(gridView));
        }
      });
    }

    super.afterCreateWidget(name, widget, callback);
  }

  private void getTotalRiskRating(GridView gridView) {
    List<IsRow> data = gridView.getGrid().getRowData();
    int total = 0;

    if (!data.isEmpty()) {
      for (IsRow row : data) {
        total += BeeUtils.unbox(Data.getInteger(gridView.getViewName(), row,
            TaskConstants.COL_RISK_RATING));
      }
    }

    Widget label = getFormView().getWidgetByName(NAME_RISK_RATING_LABEL);
    if (label != null && label instanceof Label) {
      ((Label) label).setText(BeeUtils.joinWords(Localized.dictionary().crmTaskRiskRatingTotal(),
          BeeUtils.toString(total)));
    }
  }

}
