package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.calendar.CalendarWidget;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.AbstractColumn;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Map;

public class CalendarKeeper {

  private static class CalendarGridHandler extends AbstractGridCallback {

    @Override
    public boolean afterCreateColumn(String columnId, List<? extends IsColumn> dataColumns,
        AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
        EditableColumn editableColumn) {
      if (BeeUtils.same(columnId, CalendarConstants.COL_NAME) && editableColumn != null) {
        editableColumn.addCellValidationHandler(new CellValidateEvent.Handler() {
          public boolean validateCell(CellValidateEvent event) {
            if (event.isPostValidation() && !event.sameValue() && !event.isNewRow()) {
              updateCalendarName(event.getRowId(), event.getNewValue());
            }
            return true;
          }
        });
      }
      return true;
    }

    @Override
    public void afterDeleteRow(long rowId) {
      removeUserCalendar(rowId);
    }

    @Override
    public boolean beforeAction(Action action, GridPresenter presenter) {
      if (Action.BOOKMARK.equals(action)) {
        IsRow row = presenter.getActiveRow();
        if (row != null && !hasUserCalendar(row.getId())) {
          createUserCalendar(row);
        }
        return false;
      } else {
        return true;
      }
    }
  }

  private static BeeRowSet userCalendars = null;

  private static final Map<Long, String> FAVORITE_CALENDARS = Maps.newHashMap();

  private static FormView settingsForm = null;

  public static void loadUserCalendars() {
    ParameterList params = createRequestParameters(CalendarConstants.SVC_GET_USER_CALENDARS);

    BeeKeeper.getRpc().makeGetRequest(params,
        new ResponseCallback() {
          public void onResponse(ResponseObject response) {
            if (response.hasResponse(BeeRowSet.class)) {
              userCalendars = BeeRowSet.restore((String) response.getResponse());
              if (userCalendars != null && !userCalendars.isEmpty()) {
                for (IsRow row : userCalendars.getRows()) {
                  createFavoriteWidget(row);
                }
              }
            }
          }
        });
  }

  public static void register() {
    Global.registerCaptions(CalendarConstants.AppointmentStatus.class);
    Global.registerCaptions(CalendarConstants.ReminderMethod.class);
    Global.registerCaptions(CalendarConstants.ResponseStatus.class);
    Global.registerCaptions(CalendarConstants.Transparency.class);
    Global.registerCaptions("Calendar_Visibility", CalendarConstants.Visibility.class);

    Global.registerCaptions(CalendarConstants.TimeBlockClick.class);

    GridFactory.registerGridCallback(CalendarConstants.GRID_CALENDARS, new CalendarGridHandler());

    loadUserCalendars();
  }

  static ParameterList createRequestParameters(String service) {
    ParameterList params = BeeKeeper.getRpc().createParameters(CalendarConstants.CALENDAR_MODULE);
    params.addQueryItem(CalendarConstants.CALENDAR_METHOD, service);
    return params;
  }

  static void editSettings(long calendarId, CalendarWidget calendarWidget) {
    if (settingsForm == null) {
      createSettingsForm(calendarId, calendarWidget);
    } else {
      openSettingsForm(calendarId, calendarWidget);
    }
  }

  static CalendarSettings getSettings(long calendarId) {
    return new CalendarSettings(getCalendarRow(calendarId), userCalendars.getColumns());
  }

  private static void createFavoriteWidget(IsRow row) {
    final long calId = DataUtils.getLong(userCalendars, row, CalendarConstants.COL_CALENDAR);
    String calName = getCalendarName(row);

    Html widget = new Html(calName, new Command() {
      public void execute() {
        openCalendar(calId);
      }
    });

    FAVORITE_CALENDARS.put(calId, widget.getId());
    BeeKeeper.getScreen().addCommandItem(widget);
  }

  private static void createSettingsForm(final long calendarId,
      final CalendarWidget calendarWidget) {
    FormFactory.createFormView(CalendarConstants.FORM_CALENDAR_SETTINGS,
        userCalendars.getColumns(),
        new FormFactory.FormViewCallback() {
          public void onFailure(String[] reason) {
            BeeKeeper.getScreen().notifyWarning(reason);
          }

          public void onSuccess(FormView result) {
            if (result != null) {
              settingsForm = result;
              settingsForm.setEditing(true);
              settingsForm.start(null);

              openSettingsForm(calendarId, calendarWidget);
            }
          }
        }, false);
  }

  private static void createUserCalendar(IsRow calendarRow) {
    List<BeeColumn> columns = Lists.newArrayList(
        new BeeColumn(ValueType.LONG, CalendarConstants.COL_USER),
        new BeeColumn(ValueType.LONG, CalendarConstants.COL_CALENDAR));
    List<String> values = Lists.newArrayList(
        BeeUtils.toString(BeeKeeper.getUser().getUserId()),
        BeeUtils.toString(calendarRow.getId()));

    Queries.insert(CalendarConstants.VIEW_USER_CALENDARS, columns, values,
        new Queries.RowCallback() {
          public void onFailure(String[] reason) {
            BeeKeeper.getScreen().notifySevere(reason);
          }

          public void onSuccess(BeeRow result) {
            if (result != null) {
              if (userCalendars == null) {
                loadUserCalendars();
              } else {
                userCalendars.addRow(result);
                createFavoriteWidget(result);
              }
            }
          }
        });
  }

  private static String getCalendarName(IsRow row) {
    return DataUtils.getString(userCalendars, row, CalendarConstants.COL_CALENDAR_NAME);
  }

  private static BeeRow getCalendarRow(long calendarId) {
    for (BeeRow row : userCalendars.getRows()) {
      if (DataUtils.getLong(userCalendars, row, CalendarConstants.COL_CALENDAR) == calendarId) {
        return row;
      }
    }
    return null;
  }

  private static HasWidgets getFavoritesContainer() {
    return BeeKeeper.getScreen().getCommandPanel();
  }

  private static boolean hasUserCalendar(long id) {
    if (userCalendars == null) {
      return false;
    } else {
      return userCalendars.getRowById(id) != null;
    }
  }

  private static void openCalendar(long id) {
    BeeKeeper.getScreen().updateActivePanel(new CalendarPanel(id, getSettings(id)));
  }

  private static void openSettingsForm(long calendarId, final CalendarWidget calendarWidget) {
    final BeeRow oldRow = getCalendarRow(calendarId);
    final BeeRow newRow = DataUtils.cloneRow(oldRow, userCalendars.getNumberOfColumns());
    settingsForm.updateRow(newRow, false);

    String caption = BeeUtils.concat(1, getCalendarName(oldRow), settingsForm.getCaption());

    Global.inputWidget(caption, settingsForm.asWidget(), new InputCallback() {
      public void onSuccess() {
        String oldValue;
        String newValue;

        List<BeeColumn> columns = Lists.newArrayList();
        List<String> oldValues = Lists.newArrayList();
        List<String> newValues = Lists.newArrayList();

        for (int i = 0; i < userCalendars.getNumberOfColumns(); i++) {
          oldValue = oldRow.getString(i);
          newValue = newRow.getString(i);

          if (!BeeUtils.equalsTrimRight(oldValue, newValue)) {
            columns.add(userCalendars.getColumn(i));
            oldValues.add(oldValue);
            newValues.add(newValue);

            oldRow.setValue(i, newValue);
          }
        }

        if (!columns.isEmpty()) {
          calendarWidget.getSettings().update(newRow, userCalendars.getColumns());
          calendarWidget.refresh();

          Queries.update(CalendarConstants.VIEW_USER_CALENDARS, oldRow.getId(),
              oldRow.getVersion(), columns, oldValues, newValues, new Queries.RowCallback() {
                public void onFailure(String[] reason) {
                  BeeKeeper.getScreen().notifySevere(reason);
                }

                public void onSuccess(BeeRow result) {
                  userCalendars.updateRow(result);
                }
              });
        }
      }
    });
  }

  private static void removeUserCalendar(long id) {
    if (userCalendars != null) {
      int index = userCalendars.getRowIndex(id);
      if (!BeeConst.isUndef(index)) {
        userCalendars.removeRow(index);
      }
    }

    String widgetId = FAVORITE_CALENDARS.get(id);
    if (!BeeUtils.isEmpty(widgetId)) {
      FAVORITE_CALENDARS.remove(id);
      HasWidgets container = getFavoritesContainer();
      if (container instanceof Widget) {
        Widget widget = DomUtils.getChildQuietly((Widget) container, widgetId);
        if (widget != null) {
          container.remove(widget);
        }
      }
    }
  }

  private static void updateCalendarName(long id, String value) {
    if (BeeUtils.isEmpty(value)) {
      return;
    }

    if (userCalendars != null) {
      IsRow row = userCalendars.getRowById(id);
      if (row != null) {
        DataUtils.setValue(userCalendars, row, CalendarConstants.COL_CALENDAR_NAME, value);
      }
    }

    String widgetId = FAVORITE_CALENDARS.get(id);
    if (!BeeUtils.isEmpty(widgetId)) {
      Element element = DomUtils.getElementQuietly(widgetId);
      if (element != null) {
        element.setInnerText(value);
      }
    }
  }

  private CalendarKeeper() {
  }
}
