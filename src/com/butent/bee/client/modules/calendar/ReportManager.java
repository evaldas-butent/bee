package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.modules.calendar.CalendarConstants.Report;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

class ReportManager {

  private class AttendeeTypeWidgetHandler implements KeyDownHandler, DoubleClickHandler {
    @Override
    public void onDoubleClick(DoubleClickEvent event) {
      event.preventDefault();
      addAttendeeTypes((BeeListBox) event.getSource());
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
      if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DELETE) {
        event.preventDefault();

        BeeListBox listBox = (BeeListBox) event.getSource();
        int index = listBox.getSelectedIndex();
        if (BeeUtils.isIndex(attendeeTypes, index)) {
          attendeeTypes.remove(index);
          refreshAttendeeTypeWidget(listBox);
        }
      }
    }
  }

  private class AttendeeWidgetHandler implements KeyDownHandler, DoubleClickHandler {
    @Override
    public void onDoubleClick(DoubleClickEvent event) {
      event.preventDefault();
      addAttendees((BeeListBox) event.getSource());
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
      if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DELETE) {
        event.preventDefault();

        BeeListBox listBox = (BeeListBox) event.getSource();
        int index = listBox.getSelectedIndex();
        if (BeeUtils.isIndex(attendees, index)) {
          attendees.remove(index);
          refreshAttendeeWidget(listBox);
        }
      }
    }
  }

  private static final String STYLE_PREFIX = "bee-cal-ReportOptions-";

  private final Map<Report, BeeRow> reportOptions = Maps.newHashMap();

  private final List<Long> attendees = Lists.newArrayList();
  private final List<Long> attendeeTypes = Lists.newArrayList();

  private final AttendeeWidgetHandler attendeeWidgetHandler = new AttendeeWidgetHandler();
  private final AttendeeTypeWidgetHandler attendeeTypeWidgetHandler =
      new AttendeeTypeWidgetHandler();

  ReportManager() {
    super();
  }

  void register() {
    for (final Report report : Report.values()) {
      Global.addReport(report.getCaption(), new Command() {
        @Override
        public void execute() {
          CalendarKeeper.getData(Lists.newArrayList(VIEW_ATTENDEE_TYPES, VIEW_ATTENDEES),
              new Command() {
                @Override
                public void execute() {
                  onSelectReport(report);
                }
              });
        }
      });
    }
  }

  private void addAttendees(final BeeListBox listBox) {
    BeeRowSet rowSet = CalendarKeeper.getAttendees();
    if (rowSet == null || rowSet.isEmpty()) {
      return;
    }

    final List<Long> attIds = Lists.newArrayList();
    final BeeListBox widget = new BeeListBox(true);

    String viewName = rowSet.getViewName();
    for (BeeRow row : rowSet.getRows()) {
      long id = row.getId();
      if (attendees.contains(id)) {
        continue;
      }

      String item = BeeUtils.joinItems(Data.getString(viewName, row, COL_NAME),
          Data.getString(viewName, row, COL_TYPE_NAME));
      widget.addItem(item);
      attIds.add(id);
    }

    if (attIds.isEmpty()) {
      Global.showError("Nerasta resursų, kuriuos galima pridėti");
      return;
    }

    if (BeeUtils.betweenInclusive(attIds.size(), 2, 20)) {
      widget.setAllVisible();
    } else if (attIds.size() > 20) {
      widget.setVisibleItemCount(20);
    }

    widget.addStyleName(CalendarStyleManager.ADD_RESOURCES);

    final InputCallback callback = new InputCallback() {
      @Override
      public void onSuccess() {
        int cnt = 0;
        for (int index = 0; index < widget.getItemCount(); index++) {
          if (widget.isItemSelected(index)) {
            attendees.add(attIds.get(index));
            cnt++;
          }
        }
        if (cnt > 0) {
          refreshAttendeeWidget(listBox);
        }
      }
    };

    widget.addDoubleClickHandler(new DoubleClickHandler() {
      @Override
      public void onDoubleClick(DoubleClickEvent event) {
        Popup popup = DomUtils.getParentPopup(widget);
        if (popup != null) {
          popup.hide();
          callback.onSuccess();
        }
      }
    });

    Global.inputWidget("Pasirinkite resursus", widget, callback, false,
        RowFactory.DIALOG_STYLE, listBox, false);
  }

  private void addAttendeeTypes(final BeeListBox listBox) {
    BeeRowSet rowSet = CalendarKeeper.getAttendeeTypes();
    if (rowSet == null || rowSet.isEmpty()) {
      return;
    }

    final List<Long> atpIds = Lists.newArrayList();
    final BeeListBox widget = new BeeListBox(true);

    String viewName = rowSet.getViewName();
    for (BeeRow row : rowSet.getRows()) {
      long id = row.getId();
      if (attendeeTypes.contains(id)) {
        continue;
      }

      String item = Data.getString(viewName, row, COL_NAME);
      widget.addItem(item);
      atpIds.add(id);
    }

    if (atpIds.isEmpty()) {
      Global.showError("Nerasta resursų tipų, kuriuos galima pridėti");
      return;
    }

    if (BeeUtils.betweenInclusive(atpIds.size(), 2, 20)) {
      widget.setAllVisible();
    } else if (atpIds.size() > 20) {
      widget.setVisibleItemCount(20);
    }

    widget.addStyleName(CalendarStyleManager.ADD_RESOURCES);

    final InputCallback callback = new InputCallback() {
      @Override
      public void onSuccess() {
        int cnt = 0;
        for (int index = 0; index < widget.getItemCount(); index++) {
          if (widget.isItemSelected(index)) {
            attendeeTypes.add(atpIds.get(index));
            cnt++;
          }
        }
        if (cnt > 0) {
          refreshAttendeeTypeWidget(listBox);
        }
      }
    };

    widget.addDoubleClickHandler(new DoubleClickHandler() {
      @Override
      public void onDoubleClick(DoubleClickEvent event) {
        Popup popup = DomUtils.getParentPopup(widget);
        if (popup != null) {
          popup.hide();
          callback.onSuccess();
        }
      }
    });

    Global.inputWidget("Pasirinkite resursų tipus", widget, callback, false,
        RowFactory.DIALOG_STYLE, listBox, false);
  }

  private void addStyle(Widget widget, String styleName) {
    widget.addStyleName(STYLE_PREFIX + styleName);
  }

  private void doReport(final Report report, final BeeRow row) {
    ParameterList params = CalendarKeeper.createRequestParameters(SVC_DO_REPORT);
    params.addQueryItem(PARAM_REPORT, report.ordinal());

    BeeRowSet rowSet = new BeeRowSet(VIEW_REPORT_OPTIONS, Data.getColumns(VIEW_REPORT_OPTIONS));
    rowSet.addRow(row);
    
    BeeKeeper.getRpc().sendText(params, Codec.beeSerialize(rowSet), new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        BeeRowSet rs = null;
        if (response.hasResponse(BeeRowSet.class)) {
          rs = BeeRowSet.restore((String) response.getResponse());
        }

        if (rs == null || rs.isEmpty()) {
          BeeKeeper.getScreen().notifyWarning(report.getCaption(), "nėra duomenų");
        } else {
          showReport(report, getReportCaption(report, row), rs);
        }
      }
    });
  }
  
  private String getReportCaption(Report report, BeeRow row) {
    String caption = Data.getString(VIEW_REPORT_OPTIONS, row, COL_CAPTION);
    if (!BeeUtils.isEmpty(caption)) {
      return caption.trim();
    }
    
    StringBuilder sb = new StringBuilder(report.getCaption());
    String separator = BeeUtils.space(2);
    
    JustDate lower = Data.getDate(VIEW_REPORT_OPTIONS, row, COL_LOWER_DATE);
    if (lower != null) {
      sb.append(separator).append("nuo").append(separator).append(lower.toString());
    }

    JustDate upper = Data.getDate(VIEW_REPORT_OPTIONS, row, COL_UPPER_DATE);
    if (upper != null) {
      sb.append(separator).append("iki").append(separator).append(upper.toString());
    }
    
    return sb.toString();
  }

  private void onSelectReport(final Report report) {
    BeeRow options = reportOptions.get(report);
    if (options != null) {
      openDialog(report, options);
      return;
    }

    ParameterList params = CalendarKeeper.createRequestParameters(SVC_GET_REPORT_OPTIONS);
    params.addQueryItem(PARAM_REPORT, report.ordinal());

    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        BeeRow row = null;
        if (response.hasResponse(BeeRow.class)) {
          row = BeeRow.restore((String) response.getResponse());
        }

        if (row == null) {
          BeeKeeper.getScreen().notifyWarning(report.getCaption(), "nėra duomenų");
        } else {
          reportOptions.put(report, row);
          openDialog(report, row);
        }
      }
    });
  }

  private void openDialog(final Report report, final BeeRow options) {
    final String viewName = VIEW_REPORT_OPTIONS;

    final Flow container = new Flow();
    addStyle(container, "container");

    BeeLabel capLabel = new BeeLabel("Pavadinimas:");
    addStyle(capLabel, "capLabel");
    container.add(capLabel);

    final InputText caption = new InputText();
    caption.setValue(BeeUtils.trim(Data.getString(viewName, options, COL_CAPTION)));
    Integer precision = Data.getColumnPrecision(viewName, COL_CAPTION);
    if (BeeUtils.isPositive(precision)) {
      caption.setMaxLength(precision);
    }
    addStyle(caption, "caption");
    container.add(caption);

    BeeLabel ldLabel = new BeeLabel("Data nuo:");
    addStyle(ldLabel, "ldLabel");
    container.add(ldLabel);

    final InputDate lowerDate = new InputDate(Data.getColumnType(viewName, COL_LOWER_DATE));
    lowerDate.setValue(Data.getString(viewName, options, COL_LOWER_DATE));
    addStyle(lowerDate, "lowerDate");
    container.add(lowerDate);

    BeeLabel udLabel = new BeeLabel("Data iki:");
    addStyle(udLabel, "udLabel");
    container.add(udLabel);

    final InputDate upperDate = new InputDate(Data.getColumnType(viewName, COL_UPPER_DATE));
    upperDate.setValue(Data.getString(viewName, options, COL_UPPER_DATE));
    addStyle(upperDate, "upperDate");
    container.add(upperDate);
    
    final InputSpinner lowerHour;
    final InputSpinner upperHour;
    
    if (EnumSet.of(Report.BUSY_HOURS, Report.CANCEL_HOURS).contains(report)) {
      BeeLabel lhLabel = new BeeLabel("Valanda nuo:");
      addStyle(lhLabel, "lhLabel");
      container.add(lhLabel);

      int value = BeeUtils.unbox(Data.getInteger(viewName, options, COL_LOWER_HOUR));
      lowerHour = new InputSpinner(value, 0, TimeUtils.HOURS_PER_DAY - 1);
      addStyle(lowerHour, "lowerHour");
      container.add(lowerHour);

      BeeLabel uhLabel = new BeeLabel("Valanda iki:");
      addStyle(uhLabel, "uhLabel");
      container.add(uhLabel);

      value = BeeUtils.positive(BeeUtils.unbox(Data.getInteger(viewName, options, COL_UPPER_HOUR)),
          TimeUtils.HOURS_PER_DAY);
      upperHour = new InputSpinner(value, 0, TimeUtils.HOURS_PER_DAY);
      addStyle(upperHour, "upperHour");
      container.add(upperHour);
    } else {
      lowerHour = null;
      upperHour = null;
    }

    BeeLabel atpLabel = new BeeLabel("Resursų tipai:");
    addStyle(atpLabel, "atpLabel");
    container.add(atpLabel);

    final BeeListBox atpList = new BeeListBox();
    atpList.setMinSize(3);
    atpList.setMaxSize(5);
    addStyle(atpList, "attendeeTypes");
    atpList.addDoubleClickHandler(attendeeTypeWidgetHandler);
    atpList.addKeyDownHandler(attendeeTypeWidgetHandler);
    container.add(atpList);

    BeeImage atpAdd = new BeeImage(Global.getImages().add(), new Command() {
      @Override
      public void execute() {
        addAttendeeTypes(atpList);
      }
    });
    addStyle(atpAdd, "atpAdd");
    container.add(atpAdd);

    BeeImage atpDel = new BeeImage(Global.getImages().delete(), new Command() {
      @Override
      public void execute() {
        removeAttendeeType(atpList);
      }
    });
    addStyle(atpDel, "atpDel");
    container.add(atpDel);

    BeeLabel attLabel = new BeeLabel("Resursai:");
    addStyle(attLabel, "attLabel");
    container.add(attLabel);

    final BeeListBox attList = new BeeListBox();
    attList.setMinSize(3);
    attList.setMaxSize(5);
    addStyle(attList, "attendees");
    attList.addDoubleClickHandler(attendeeWidgetHandler);
    attList.addKeyDownHandler(attendeeWidgetHandler);
    container.add(attList);

    BeeImage attAdd = new BeeImage(Global.getImages().add(), new Command() {
      @Override
      public void execute() {
        addAttendees(attList);
      }
    });
    addStyle(attAdd, "attAdd");
    container.add(attAdd);

    BeeImage attDel = new BeeImage(Global.getImages().delete(), new Command() {
      @Override
      public void execute() {
        removeAttendee(attList);
      }
    });
    addStyle(attDel, "attDel");
    container.add(attDel);

    final BeeButton tableCommand = new BeeButton("Lentelė", new Command() {
      @Override
      public void execute() {
        String vCap = caption.getValue();
        
        JustDate vLd = JustDate.get(lowerDate.getDate());
        JustDate vUd = JustDate.get(upperDate.getDate());
        
        if (vLd != null && vUd != null && TimeUtils.isMeq(vLd, vUd)) {
          Global.showError("Neteisingas datų intervalas");
          return;
        }
        
        int vLh = (lowerHour == null) ? 0 : lowerHour.getIntValue();
        int vUh = (upperHour == null) ? 0 : upperHour.getIntValue();
        if (vUh > 0 && vUh <= vLh) {
          Global.showError("Neteisingas valandų intervalas");
          return;
        }
        
        BeeRow newRow = DataUtils.cloneRow(options);
        Data.setValue(viewName, newRow, COL_CAPTION, vCap);
        Data.setValue(viewName, newRow, COL_LOWER_DATE, vLd);
        Data.setValue(viewName, newRow, COL_UPPER_DATE, vUd);
        
        if (lowerHour != null) {
          Data.setValue(viewName, newRow, COL_LOWER_HOUR, vLh);
        }
        if (upperHour != null) {
          Data.setValue(viewName, newRow, COL_UPPER_HOUR, vUh);
        }
        
        Data.setValue(viewName, newRow, COL_ATTENDEE_TYPES, DataUtils.buildList(attendeeTypes));
        Data.setValue(viewName, newRow, COL_ATTENDEES, DataUtils.buildList(attendees));
        
        reportOptions.put(report, newRow);
        doReport(report, newRow);
        
        Global.closeDialog(container);
      }
    });
    addStyle(tableCommand, "tableCommand");
    container.add(tableCommand);

    attendeeTypes.clear();
    String idList = Data.getString(viewName, options, COL_ATTENDEE_TYPES);
    if (!BeeUtils.isEmpty(idList)) {
      attendeeTypes.addAll(DataUtils.parseList(idList));
      refreshAttendeeTypeWidget(atpList);
    }

    attendees.clear();
    idList = Data.getString(viewName, options, COL_ATTENDEES);
    if (!BeeUtils.isEmpty(idList)) {
      attendees.addAll(DataUtils.parseList(idList));
      refreshAttendeeWidget(attList);
    }

    Global.inputWidget(report.getCaption(), container, null, true,
        DialogConstants.STYLE_REPORT_OPTIONS, false);
  }

  private void refreshAttendeeTypeWidget(BeeListBox listBox) {
    if (listBox.getItemCount() > 0) {
      listBox.clear();
    }

    BeeRowSet rowSet = CalendarKeeper.getAttendeeTypes();
    if (rowSet != null && !attendeeTypes.isEmpty()) {
      String viewName = rowSet.getViewName();
      for (long id : attendeeTypes) {
        BeeRow row = rowSet.getRowById(id);
        listBox.addItem(Data.getString(viewName, row, COL_NAME));
      }
    }
  }

  private void refreshAttendeeWidget(BeeListBox listBox) {
    if (listBox.getItemCount() > 0) {
      listBox.clear();
    }

    BeeRowSet rowSet = CalendarKeeper.getAttendees();
    if (rowSet != null && !attendees.isEmpty()) {
      String viewName = rowSet.getViewName();
      for (long id : attendees) {
        BeeRow row = rowSet.getRowById(id);
        String item = BeeUtils.joinItems(Data.getString(viewName, row, COL_NAME),
            Data.getString(viewName, row, COL_TYPE_NAME));
        listBox.addItem(item);
      }
    }
  }

  private void removeAttendee(BeeListBox listBox) {
    if (attendees.isEmpty()) {
      return;
    }

    int index = listBox.getSelectedIndex();
    if (!BeeUtils.isIndex(attendees, index)) {
      index = attendees.size() - 1;
    }

    attendees.remove(index);
    refreshAttendeeWidget(listBox);
  }

  private void removeAttendeeType(BeeListBox listBox) {
    if (attendeeTypes.isEmpty()) {
      return;
    }

    int index = listBox.getSelectedIndex();
    if (!BeeUtils.isIndex(attendeeTypes, index)) {
      index = attendeeTypes.size() - 1;
    }

    attendeeTypes.remove(index);
    refreshAttendeeTypeWidget(listBox);
  }
  
  private void showReport(Report report, String caption, BeeRowSet rowSet) {
    String gridName = "CalendarReport" + report.name();
    GridDescription gridDescription = new GridDescription(gridName);

    gridDescription.setCaption(caption);
    gridDescription.setReadOnly(true);

    gridDescription.setHasHeaders(true);
    gridDescription.setHasFooters(false);

    for (int i = 0; i < rowSet.getNumberOfColumns(); i++) {
      String colName = rowSet.getColumn(i).getId();
      ColumnDescription columnDescription = new ColumnDescription(ColType.DATA, colName);

      switch (i) {
        case 0:
          columnDescription.setCaption("Tipas");
          break;
        case 1:
          columnDescription.setCaption("Resursas");
          break;
        default:
          columnDescription.setHorAlign(HasHorizontalAlignment.ALIGN_RIGHT.getTextAlignString());
          break;
      }

      columnDescription.setSource(colName);
      columnDescription.setSortable(true);

      gridDescription.addColumn(columnDescription);
    }

    GridPresenter presenter = new GridPresenter(gridDescription,
        rowSet.getNumberOfRows(), rowSet, Provider.Type.LOCAL, CachingPolicy.NONE,
        EnumSet.of(UiOption.REPORT));

    BeeKeeper.getScreen().updateActivePanel(presenter.getWidget());
  }
}
