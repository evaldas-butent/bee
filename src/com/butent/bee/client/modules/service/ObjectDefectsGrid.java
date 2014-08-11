package com.butent.bee.client.modules.service;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.RenderingEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.css.values.Display;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

public class ObjectDefectsGrid extends AbstractGridInterceptor {

  private static final LocalizableConstants localizableConstants = Localized.getConstants();
  private boolean checked;
  private CheckBox showAllCheckBox;

  ObjectDefectsGrid() {
    this.checked = false;
  }

  @Override
  public void afterCreatePresenter(final GridPresenter presenter) {
    HeaderView header = presenter.getHeader();
    header.clearCommandPanel();

    showAllCheckBox = new CheckBox(localizableConstants.svcActionShowFromProjects());

    header.addCommandItem(showAllCheckBox);
    showAllCheckBox.setChecked(isChecked());
    showAllCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        Assert.notNull(event.getValue());

        if (event.getValue().booleanValue()) {
          setChecked(true);
        } else {
          setChecked(false);
        }

        presenter.handleAction(Action.REFRESH);
      }
    });
  }

  @Override
  public void afterRender(GridView gridView, RenderingEvent event) {
    super.afterRender(gridView, event);
    displayElements(getGridPresenter());
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    displayElements(presenter);
    if (action == Action.ADD) {
      DefectBuilder.start(getGridView());
      return false;
    } else {
      return super.beforeAction(action, presenter);
    }
  }

  @Override
  public void beforeRefresh(GridPresenter presenter) {
    FormView form = UiHelper.getForm(presenter.getMainView().asWidget());
    if (form != null && !BeeUtils.isEmpty(form.getViewName()) && form.getActiveRow() != null) {
      DataInfo dataInfo = Data.getDataInfo(form.getViewName());

      if (dataInfo != null) {
        Integer objStatus =
            form.getActiveRow().getInteger(dataInfo.getColumnIndex(COL_OBJECT_STATUS));

        Filter currStatusFilter = Filter.isEqual(COL_OBJECT_STATUS, Value.getValue(objStatus));
        Filter otherStatusFilter = Filter.isNull(COL_OBJECT_STATUS);

        if (objStatus != null && objStatus.intValue() > ObjectStatus.SERVICE_OBJECT.ordinal()
            || isChecked()) {
          otherStatusFilter =
              Filter.isMore(COL_OBJECT_STATUS, Value
                  .getValue(ObjectStatus.SERVICE_OBJECT.ordinal()));
        }

        presenter.getDataProvider().setParentFilter(COL_OBJECT_STATUS, Filter.or(currStatusFilter,
            otherStatusFilter));
      }
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new ObjectDefectsGrid();
  }

  private void displayElements(GridPresenter presenter) {

    FormView form = UiHelper.getForm(presenter.getMainView().asWidget());
    if (form != null && !BeeUtils.isEmpty(form.getViewName()) && form.getActiveRow() != null) {
      DataInfo dataInfo = Data.getDataInfo(form.getViewName());

      if (dataInfo != null) {
        Integer objStatus =
            form.getActiveRow().getInteger(dataInfo.getColumnIndex(COL_OBJECT_STATUS));

        if (BeeUtils.unbox(objStatus) != ObjectStatus.SERVICE_OBJECT.ordinal()
            && showAllCheckBox != null) {
          StyleUtils.setDisplay(showAllCheckBox, Display.NONE);
        } else if (showAllCheckBox != null) {
          StyleUtils.setDisplay(showAllCheckBox, Display.INLINE);
        }
      }
    }
  }

  private boolean isChecked() {
    return checked;
  }

  private void setChecked(boolean checked) {
    this.checked = checked;
  }
}
