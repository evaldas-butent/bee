package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public final class CalendarFilterHelper {

  private static final String STYLE_INPUT_LABELS = "bee-cal-filter-input-labels";
  private static final String STYLE_FILTER_ACTION = "bee-cal-filter-action";
  private static final String STYLE_FILTER_ACTION_PANEL = STYLE_FILTER_ACTION + "-panel";
  private static final String STYLE_FILTER_ACTION_BUTTON = STYLE_FILTER_ACTION + "-button";

  interface DialogCallback {
    void onClear();

    void onFilter(Map<CalendarConstants.CalendarFilterDataType, String> selectedData);

  }

  static void openDialog(final DialogCallback callback, Map<String, String> filterValues) {

    DialogBox filterDialog = DialogBox.create(Localized.dictionary().filter());
    Flow filterContent = new Flow();

    filterDialog.setHideOnEscape(true);

    filterDialog.add(filterContent);

    Flow filter = new Flow();
    filter.add(new CalendarFilterWidget(
        filterValues.get(CalendarConstants.CalendarFilterDataType.PROJECT.name()),
        CalendarConstants.CalendarFilterDataType.PROJECT));

    filter.add(new CalendarFilterWidget(
        filterValues.get(CalendarConstants.CalendarFilterDataType.COURSE.name()),
        CalendarConstants.CalendarFilterDataType.COURSE));

    filterContent.add(filter);

    Flow actions = new Flow();
    actions.setStyleName(STYLE_FILTER_ACTION_PANEL);
    filterContent.add(actions);

    Button filterButton = new Button(Localized.dictionary().doFilter());
    filterButton.addStyleName(STYLE_FILTER_ACTION_BUTTON);

    filterButton.addClickHandler(getFilterClickHandler(filterDialog, callback, filter));
    actions.add(filterButton);

    Button clearButton = new Button(Localized.dictionary().clear());
    clearButton.addStyleName(STYLE_FILTER_ACTION_BUTTON);
    clearButton.addClickHandler(getClearClickHandler(filter, callback));
    actions.add(clearButton);

    filterDialog.center();
    filterButton.setFocus(true);
  }

  private static ClickHandler getFilterClickHandler(final DialogBox filterDialog,
      final DialogCallback callback, final Flow filterContent) {
    return event -> {
      filterDialog.close();
      Map<CalendarConstants.CalendarFilterDataType, String> filterData = Maps.newHashMap();
      for (Widget filterWidget: filterContent) {
        if (filterWidget instanceof CalendarFilterWidget) {
          CalendarFilterWidget filter = (CalendarFilterWidget) filterWidget;
          if (!BeeUtils.isEmpty(filter.getData())) {
            filterData.put(filter.getDataType(), filter.getData());
          }
        }
      }

      callback.onFilter(filterData);
    };
  }

  private static ClickHandler getClearClickHandler(
      final Flow filterContent, final DialogCallback callback) {
    return event -> {
      for (Widget filterWidget: filterContent) {
        if (filterWidget instanceof CalendarFilterWidget) {
          CalendarFilterWidget filter = (CalendarFilterWidget) filterWidget;
          filter.clearData();
        }
      }

      callback.onClear();
    };
  }

  private CalendarFilterHelper() {
  }


  static class CalendarFilterWidget extends Flow {

    private CalendarConstants.CalendarFilterDataType dataType;
    private InputText filterInput;

    CalendarFilterWidget(String data, CalendarConstants.CalendarFilterDataType dataType) {
      this.dataType = dataType;

      Label filterLabel = new Label(dataType.getCaption());
      filterLabel.setStyleName(STYLE_INPUT_LABELS);

      filterInput = new InputText();

      if (!BeeUtils.isEmpty(data)) {
        filterInput.setValue(data);
      }

      add(filterLabel);
      add(filterInput);

    }

    public CalendarConstants.CalendarFilterDataType getDataType() {
      return dataType;
    }

    public String getData() {
      return filterInput.getValue();
    }

    public void clearData() {
      filterInput.clearValue();
    }

  }
}