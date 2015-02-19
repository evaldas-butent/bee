package com.butent.bee.client.modules.classifiers;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.BeeUtils;

public class PersonsCompaniesGrid extends AbstractGridInterceptor {

  private String gridType;
  private String viewType;

  public PersonsCompaniesGrid(String gridType) {
    this.gridType = gridType;

    if (BeeUtils.same(ClassifierConstants.GRID_PERSON_COMPANIES, gridType)) {
      this.viewType = ClassifierConstants.VIEW_PERSONS;
    } else if (BeeUtils.same(ClassifierConstants.GRID_COMPANY_PERSONS, gridType)) {
      this.viewType = ClassifierConstants.VIEW_COMPANIES;
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new PersonsCompaniesGrid(gridType);
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {

    if (viewType != null) {
      GridPresenter presenter = getGridPresenter();
      FormView parentForm = ViewHelper.getForm(presenter.getMainView().asWidget());

      if (parentForm == null) {
        return false;
      }

      if (BeeUtils.isEmpty(parentForm.getViewName()) && parentForm.getActiveRow() == null) {
        return false;
      }

      if (BeeUtils.same(parentForm.getViewName(), viewType)) {
        DataInfo info = Data.getDataInfo(parentForm.getViewName());

        int phoneIdx = info.getColumnIndex(ClassifierConstants.COL_PHONE);
        int mobileIdx = info.getColumnIndex(ClassifierConstants.COL_MOBILE);
        int faxIdx = info.getColumnIndex(ClassifierConstants.COL_FAX);
        int emailIdIdx = info.getColumnIndex(ClassifierConstants.ALS_EMAIL_ID);
        int emailIdx = info.getColumnIndex(ClassifierConstants.COL_EMAIL);
        int websiteIdx = info.getColumnIndex(ClassifierConstants.COL_WEBSITE);
        int addressIdx = info.getColumnIndex(ClassifierConstants.COL_ADDRESS);
        int cityIdx = info.getColumnIndex(ClassifierConstants.COL_CITY);
        int cityNameIdx = info.getColumnIndex(ClassifierConstants.ALS_CITY_NAME);
        int countryNameIdx = info.getColumnIndex(ClassifierConstants.ALS_COUNTRY_NAME);
        int countryCodeIdx = info.getColumnIndex(ClassifierConstants.ALS_COUNTRY_CODE);
        int countryIdx = info.getColumnIndex(ClassifierConstants.COL_COUNTRY);
        int postIdx = info.getColumnIndex(ClassifierConstants.COL_POST_INDEX);
        int socialIdx = info.getColumnIndex(ClassifierConstants.COL_SOCIAL_CONTACTS);

        newRow
            .setValue(gridView.getDataIndex(ClassifierConstants.COL_PHONE), parentForm
                .getActiveRow()
                .getString(phoneIdx));
        newRow.setValue(gridView.getDataIndex(ClassifierConstants.COL_MOBILE), parentForm
            .getActiveRow().getString(
                mobileIdx));
        newRow.setValue(gridView.getDataIndex(ClassifierConstants.COL_FAX), parentForm
            .getActiveRow()
            .getString(faxIdx));
        newRow
            .setValue(gridView.getDataIndex(ClassifierConstants.ALS_EMAIL_ID), parentForm
                .getActiveRow().getString(
                    emailIdIdx));
        newRow
            .setValue(gridView.getDataIndex(ClassifierConstants.COL_EMAIL), parentForm
                .getActiveRow()
                .getString(emailIdx));
        newRow.setValue(gridView.getDataIndex(ClassifierConstants.COL_WEBSITE), parentForm
            .getActiveRow().getString(
                websiteIdx));
        newRow.setValue(gridView.getDataIndex(ClassifierConstants.COL_ADDRESS), parentForm
            .getActiveRow().getString(
                addressIdx));
        newRow
            .setValue(gridView.getDataIndex(ClassifierConstants.COL_CITY), parentForm
                .getActiveRow()
                .getString(cityIdx));
        newRow.setValue(gridView.getDataIndex(ClassifierConstants.ALS_CITY_NAME), parentForm
            .getActiveRow().getString(
                cityNameIdx));
        newRow.setValue(gridView.getDataIndex(ClassifierConstants.COL_POST_INDEX), parentForm
            .getActiveRow().getString(
                postIdx));
        newRow.setValue(gridView.getDataIndex(ClassifierConstants.COL_SOCIAL_CONTACTS), parentForm
            .getActiveRow().getString(
                socialIdx));
        newRow.setValue(gridView.getDataIndex(ClassifierConstants.COL_COUNTRY), parentForm
            .getActiveRow().getString(
                countryIdx));
        newRow.setValue(gridView.getDataIndex(ClassifierConstants.ALS_COUNTRY_NAME), parentForm
            .getActiveRow().getString(
                countryNameIdx));
        newRow.setValue(gridView.getDataIndex(ClassifierConstants.ALS_COUNTRY_CODE), parentForm
            .getActiveRow().getString(
                countryCodeIdx));
      }
      return true;
    } else {
      return false;
    }
  }
}
