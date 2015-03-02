package com.butent.bee.client.modules.classifiers;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.BeeUtils;

public class CompanyPersonForm extends AbstractFormInterceptor {

  private class ContactsHandler implements SelectorEvent.Handler {

    @Override
    public void onDataSelector(SelectorEvent event) {
      if (event.isNewRow()) {
        final BeeRow row = event.getNewRow();

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.COL_PHONE,
            BeeUtils.trim(getStringValue(ClassifierConstants.COL_PHONE)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.COL_MOBILE,
            BeeUtils.trim(getStringValue(ClassifierConstants.COL_MOBILE)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.COL_FAX,
            BeeUtils.trim(getStringValue(ClassifierConstants.COL_FAX)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.ALS_EMAIL_ID,
            BeeUtils.trim(getStringValue(ClassifierConstants.ALS_EMAIL_ID)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.COL_EMAIL,
            BeeUtils.trim(getStringValue(ClassifierConstants.COL_EMAIL)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.COL_WEBSITE,
            BeeUtils.trim(getStringValue(ClassifierConstants.COL_WEBSITE)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.COL_ADDRESS,
            BeeUtils.trim(getStringValue(ClassifierConstants.COL_ADDRESS)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.COL_CITY,
            BeeUtils.trim(getStringValue(ClassifierConstants.COL_CITY)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.ALS_CITY_NAME,
            BeeUtils.trim(getStringValue(ClassifierConstants.ALS_CITY_NAME)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.COL_POST_INDEX,
            BeeUtils.trim(getStringValue(ClassifierConstants.COL_POST_INDEX)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row,
            ClassifierConstants.COL_SOCIAL_CONTACTS,
            BeeUtils.trim(getStringValue(ClassifierConstants.COL_SOCIAL_CONTACTS)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.COL_COUNTRY,
            BeeUtils.trim(getStringValue(ClassifierConstants.COL_COUNTRY)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.ALS_COUNTRY_NAME,
            BeeUtils.trim(getStringValue(ClassifierConstants.ALS_COUNTRY_NAME)));

        Data.setValue(ClassifierConstants.VIEW_PERSONS, row, ClassifierConstants.ALS_COUNTRY_CODE,
            BeeUtils.trim(getStringValue(ClassifierConstants.ALS_COUNTRY_CODE)));

      }
    }
  }

  private DataSelector ds;

  @Override
  public FormInterceptor getInstance() {
    return new CompanyPersonForm();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {
    super.afterCreateWidget(name, widget, callback);

    if (widget instanceof DataSelector && "Person".equals(name)) {
      ds = (DataSelector) widget;
      ds.addSelectorHandler(new ContactsHandler());
    }
  }
}
