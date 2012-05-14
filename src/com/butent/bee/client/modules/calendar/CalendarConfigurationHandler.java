package com.butent.bee.client.modules.calendar;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowCallback;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class CalendarConfigurationHandler extends AbstractFormCallback {

  private Long company = null;
  private Long appointmentType = null;

  private Long theme = null;
  private Long timeZone = null;
  
  private int companyIndex = BeeConst.UNDEF;
  private int appointmentTypeIndex = BeeConst.UNDEF;

  private int themeIndex = BeeConst.UNDEF;
  private int timeZoneIndex = BeeConst.UNDEF;
  
  private IsRow oldRow = null;

  CalendarConfigurationHandler() {
    super();
  }

  @Override
  public boolean beforeAction(Action action, final FormPresenter presenter) {
    if (Action.SAVE.equals(action)) {
      final IsRow row = presenter.getActiveRow();
      if (row == null) {
        return false;
      }

      String co = row.getString(getCompanyIndex());
      if (BeeUtils.isEmpty(co)) {
        presenter.getNotificationListener().notifySevere("Company is required");
        presenter.getView().getContent().focus(CalendarConstants.COL_COMPANY);
        return false;
      }

      String at = row.getString(getAppointmentTypeIndex());
      if (BeeUtils.isEmpty(at)) {
        presenter.getNotificationListener().notifySevere("Appointment type is required");
        presenter.getView().getContent().focus(CalendarConstants.COL_APPOINTMENT_TYPE);
        return false;
      }
      
      final boolean insert = DataUtils.isNewRow(row);
      
      final String viewName = presenter.getViewName();
      List<BeeColumn> columns = presenter.getDataProvider().getColumns();

      RowCallback callback = new Queries.RowCallback() {
        @Override
        public void onSuccess(BeeRow result) {
          updateFields(result);
          presenter.getView().getContent().updateRow(result, false);

          if (insert) {
            BeeKeeper.getBus().fireEvent(new RowInsertEvent(viewName, result));
          } else {
            BeeKeeper.getBus().fireEvent(new RowUpdateEvent(viewName, result));
          }
        }
      };

      if (insert) {
        Queries.insert(viewName, columns, row, callback);
      } else {
        int cnt = Queries.update(viewName, columns, getOldRow(), row, callback);
        if (cnt <= 0) {
          presenter.getNotificationListener().notifyInfo("No changes found");
        }
      }
      return false;
    }
    return true;
  }

  @Override
  public boolean hasFooter(int rowCount) {
    return false;
  }

  @Override
  public void onSetActiveRow(IsRow row) {
    if (row == null) {
      setOldRow(null);
    } else if (getOldRow() == null) {
      setOldRow(row.clone());
    } else if (!DataUtils.equals(getOldRow(), row)) {
      DataUtils.updateRow(getOldRow(), row);
    }
  }

  @Override
  public void onShow(FormPresenter presenter) {
    presenter.getView().getContent().setEditing(true);
    
    Provider provider = presenter.getDataProvider();
    
    setCompanyIndex(provider.getColumnIndex(CalendarConstants.COL_COMPANY));
    setAppointmentTypeIndex(provider.getColumnIndex(CalendarConstants.COL_APPOINTMENT_TYPE));
    setThemeIndex(provider.getColumnIndex(CalendarConstants.COL_THEME));
    setTimeZoneIndex(provider.getColumnIndex(CalendarConstants.COL_TIME_ZONE));
  }

  Long getAppointmentType() {
    return appointmentType;
  }

  Long getCompany() {
    return company;
  }

  Long getTheme() {
    return theme;
  }

  Long getTimeZone() {
    return timeZone;
  }

  private int getAppointmentTypeIndex() {
    return appointmentTypeIndex;
  }

  private int getCompanyIndex() {
    return companyIndex;
  }

  private IsRow getOldRow() {
    return oldRow;
  }

  private int getThemeIndex() {
    return themeIndex;
  }

  private int getTimeZoneIndex() {
    return timeZoneIndex;
  }

  private void setAppointmentType(Long appointmentType) {
    this.appointmentType = appointmentType;
  }

  private void setAppointmentTypeIndex(int appointmentTypeIndex) {
    this.appointmentTypeIndex = appointmentTypeIndex;
  }

  private void setCompany(Long company) {
    this.company = company;
  }

  private void setCompanyIndex(int companyIndex) {
    this.companyIndex = companyIndex;
  }

  private void setOldRow(IsRow oldRow) {
    this.oldRow = oldRow;
  }

  private void setTheme(Long theme) {
    this.theme = theme;
  }

  private void setThemeIndex(int themeIndex) {
    this.themeIndex = themeIndex;
  }

  private void setTimeZone(Long timeZone) {
    this.timeZone = timeZone;
  }

  private void setTimeZoneIndex(int timeZoneIndex) {
    this.timeZoneIndex = timeZoneIndex;
  }

  private void updateFields(BeeRow row) {
    if (row == null) {
      return;
    }

    setCompany(row.getLong(getCompanyIndex()));
    setAppointmentType(row.getLong(getAppointmentTypeIndex()));
    
    setTheme(row.getLong(getThemeIndex()));
    setTimeZone(row.getLong(getTimeZoneIndex()));
  }
}
