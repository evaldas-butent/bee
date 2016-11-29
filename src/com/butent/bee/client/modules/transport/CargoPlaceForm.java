package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.data.ParentRowCreator;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public class CargoPlaceForm extends AbstractFormInterceptor {

  @Override
  public String getCaption() {
    if (Objects.equals(getViewName(), TBL_CARGO_LOADING)) {
      return Localized.dictionary().cargoLoadingPlace();
    } else if (Objects.equals(getViewName(), TBL_CARGO_UNLOADING)) {
      return Localized.dictionary().cargoUnloadingPlace();
    } else {
      return super.getCaption();
    }
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    super.onStartNewRow(form, oldRow, newRow);

    if (getGridView().isEmpty()) {
      IsRow parentRow = ViewHelper.getFormRow(getGridView().asWidget());

      if (DataUtils.isNewRow(parentRow)) {
        FormView parentForm = ViewHelper.getForm(getGridView().asWidget());

        if (parentForm.getViewPresenter() instanceof ParentRowCreator) {
          ((ParentRowCreator) parentForm.getViewPresenter()).createParentRow(parentForm,
              result -> fillValuesByParameters(newRow, result));
        }

      } else  {
        fillValuesByParameters(newRow, parentRow);
      }
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoPlaceForm();
  }

  private void fillValuesByParameters(IsRow newRow, IsRow parentRow) {
    if (parentRow != null) {
      String prefix = BeeUtils.removePrefix(getViewName(), COL_CARGO);

      parentRow.getProperties().forEach((name, value) -> {
        int colIndex = getDataIndex(BeeUtils.removePrefix(name, prefix));

        if (colIndex >= 0) {
          newRow.setValue(colIndex, value);
        }
      });
    }
  }
}
