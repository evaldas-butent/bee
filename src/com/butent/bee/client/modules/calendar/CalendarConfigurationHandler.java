package com.butent.bee.client.modules.calendar;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowCallback;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory.FormCallback;
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

  private Long timeZone = null;
  
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

      final String viewName = presenter.getViewName();

      String co = Data.getString(viewName, row, CalendarConstants.COL_COMPANY);
      if (BeeUtils.isEmpty(co)) {
        presenter.getNotificationListener().notifySevere("Company is required");
        presenter.getView().getContent().focus(CalendarConstants.COL_COMPANY);
        return false;
      }

      String at = Data.getString(viewName, row, CalendarConstants.COL_APPOINTMENT_TYPE);
      if (BeeUtils.isEmpty(at)) {
        presenter.getNotificationListener().notifySevere("Appointment type is required");
        presenter.getView().getContent().focus(CalendarConstants.COL_APPOINTMENT_TYPE);
        return false;
      }
      
      final boolean insert = DataUtils.isNewRow(row);
      
      List<BeeColumn> columns = presenter.getDataProvider().getColumns();

      RowCallback callback = new Queries.RowCallback() {
        @Override
        public void onSuccess(BeeRow result) {
          updateFields(viewName, result);
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
  public FormCallback getInstance() {
    return new CalendarConfigurationHandler();
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
  }

  Long getAppointmentType() {
    return appointmentType;
  }

  Long getCompany() {
    return company;
  }

  Long getTimeZone() {
    return timeZone;
  }

  private IsRow getOldRow() {
    return oldRow;
  }

  private void setAppointmentType(Long appointmentType) {
    this.appointmentType = appointmentType;
  }

  private void setCompany(Long company) {
    this.company = company;
  }

  private void setOldRow(IsRow oldRow) {
    this.oldRow = oldRow;
  }

  private void setTimeZone(Long timeZone) {
    this.timeZone = timeZone;
  }

  private void updateFields(String viewName, BeeRow row) {
    if (row == null) {
      return;
    }

    setCompany(Data.getLong(viewName, row, CalendarConstants.COL_COMPANY));
    setAppointmentType(Data.getLong(viewName, row, CalendarConstants.COL_APPOINTMENT_TYPE));
    
    setTimeZone(Data.getLong(viewName, row, CalendarConstants.COL_TIME_ZONE));
  }
}
