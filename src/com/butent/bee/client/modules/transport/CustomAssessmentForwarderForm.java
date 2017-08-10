package com.butent.bee.client.modules.transport;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

public class CustomAssessmentForwarderForm extends PrintFormInterceptor {

  private static final String NAME_NOTE_LABEL = "NoteLabel";

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (BeeUtils.same(editableWidget.getColumnId(), COL_CONSOLIDATED_TRANSPORTATION)) {
      editableWidget.addCellValidationHandler(event -> {
        styleRequiredField(NAME_NOTE_LABEL, event.getNewValue() == null);
        return true;
      });
    }
    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == Action.SAVE) {
      IsRow row = getActiveRow();
      Dictionary dic = Localized.dictionary();

      if (!BeeUtils.unbox(row.getBoolean(getDataIndex(COL_CONSOLIDATED_TRANSPORTATION)))
          && BeeKeeper.getUser().isColumnVisible(Data.getDataInfo(TBL_ASSESSMENT_FORWARDERS),
          COL_NOTE)) {

        if (BeeUtils.isEmpty(row.getString(getDataIndex(COL_NOTE)))) {
          getFormView().notifySevere(dic.fieldRequired(dic.transportConditions()));
          getFormView().focus(COL_NOTE);
          return false;
        }
      }
    }

    return super.beforeAction(action, presenter);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    styleRequiredField(NAME_NOTE_LABEL,
        row.getString(getDataIndex(COL_CONSOLIDATED_TRANSPORTATION)) == null);

    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return null;
  }

  @Override
  public void onStartNewRow(FormView form, IsRow row) {
    Global.getParameterRelation(PRM_FORWARDER_EXPEDITION_TYPE, (aLong, s) -> {
      if (DataUtils.isId(aLong)) {
        row.setValue(getDataIndex(COL_EXPEDITION), aLong);
        row.setValue(Data.getColumnIndex(TBL_ASSESSMENT_FORWARDERS, ALS_EXPEDITION_TYPE), s);
        getFormView().refreshBySource(COL_EXPEDITION);
      }
    });

    super.onStartNewRow(form, row);
  }

  private void styleRequiredField(String name, boolean value) {
    Widget label = getFormView().getWidgetByName(name);

    if (label != null) {
      label.setStyleName(StyleUtils.NAME_REQUIRED, value);
    }
  }
}
