package com.butent.bee.client.modules.calendar.view;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.COL_USER;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.projects.ProjectConstants.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.composite.Relations;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.utils.BeeUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import java.util.Objects;

public class AppointmentForm extends AbstractFormInterceptor implements ClickHandler {

  private DataSelector prjSelector;
  private static final String NEW_ACT_LABEL = "NewActLabel";

  private class RelationsHandler implements SelectorEvent.Handler {

    @Override
    public void onDataSelector(SelectorEvent event) {
      final String viewName = event.getRelatedViewName();

      if (event.isNewRow()) {
        if (Objects.equals(viewName, VIEW_TASKS)) {
          event.consume();

          final String formName = event.getNewRowFormName();
          final BeeRow row = event.getNewRow();
          final DataSelector selector = event.getSelector();

          Long projectId = getLongValue(COL_PROJECT);
          if (DataUtils.isId(projectId)) {
            String project = getStringValue(ALS_PROJECT_NAME);

            Data.setValue(TaskConstants.VIEW_TASKS, row, COL_PROJECT, projectId);
            Data.setValue(TaskConstants.VIEW_TASKS, row, ALS_PROJECT_NAME, project);
          }

          Long companyId = getLongValue(COL_COMPANY);
          if (DataUtils.isId(companyId)) {
            String company = getStringValue(ALS_COMPANY_NAME);

            Data.setValue(VIEW_TASKS, row, COL_COMPANY, companyId);
            Data.setValue(VIEW_TASKS, row, ALS_COMPANY_NAME, company);
          }
          RowFactory.createRelatedRow(formName, row, selector);
        }
      } else {
        Filter filter = null;
        Long user = BeeKeeper.getUser().getUserId();

        if (Objects.equals(viewName, VIEW_TASKS)) {
          filter = Filter.or(Filter.equals(COL_EXECUTOR, user), Filter.equals(COL_OWNER, user),
              Filter.in(COL_TASK_ID, VIEW_TASK_USERS, COL_TASK, Filter.equals(COL_USER, user)));
        } else if (Objects.equals(viewName, VIEW_PROJECTS)) {
          filter = Filter.in(Data.getIdColumn(VIEW_PROJECTS), VIEW_PROJECT_USERS, COL_PROJECT,
              Filter.equals(COL_USER, user));
        }

        event.getSelector().setAdditionalFilter(filter);
      }
    }
  }

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (BeeUtils.same(editableWidget.getColumnId(), COL_PROJECT)) {
      prjSelector = (DataSelector) widget;
      prjSelector.addSelectorHandler(event -> {
        Filter filter = Filter.in(Data.getIdColumn(VIEW_PROJECTS), VIEW_PROJECT_USERS, COL_PROJECT,
            Filter.equals(COL_USER, BeeKeeper.getUser().getUserId()));

        event.getSelector().setAdditionalFilter(filter);
      });
    }

    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof Relations) {
      ((Relations) widget).setSelectorHandler(new RelationsHandler());
    } else if (BeeUtils.same(NEW_ACT_LABEL, name)) {
      ((FaLabel)widget).addClickHandler(this);
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public FormInterceptor getInstance() {
    return new AppointmentForm();
  }


  @Override
  public void onClick(ClickEvent clickEvent) {
    DataSelector tradeAct = (DataSelector) getFormView().getWidgetBySource(TradeActConstants.COL_TRADE_ACT);

    if (tradeAct != null) {
      DataInfo dataInfo = tradeAct.getOracle().getDataInfo();
      BeeRow row = RowFactory.createEmptyRow(dataInfo, true);
      SelectorEvent event = SelectorEvent.fireNewRow(tradeAct, row, tradeAct.getNewRowForm(),
        tradeAct.getDisplayValue());

      Data.setValue(TradeActConstants.VIEW_TRADE_ACTS, row, TradeActConstants.COL_TA_KIND,
        TradeActKind.SALE.ordinal());

      RowFactory.createRelatedRow(tradeAct.getNewRowForm(), row, tradeAct, Opener.MODAL);
    }
  }

  public DataSelector getProjectSelector() {
    return prjSelector;
  }
}