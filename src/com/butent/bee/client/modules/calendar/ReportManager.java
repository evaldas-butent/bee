package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.RendererFactory;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.modules.calendar.CalendarConstants.Report;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

class ReportManager {

  private static final String STYLE_PREFIX = "bee-cal-ReportOptions-";

  private final Map<Report, BeeRow> reportOptions = Maps.newHashMap();

  ReportManager() {
    super();
  }

  void register() {
    for (final Report report : Report.values()) {
      Global.addReport(report.getCaption(), new Command() {
        @Override
        public void execute() {
          onSelectReport(report);
        }
      });
    }
  }

  private void addStyle(Widget widget, String styleName) {
    widget.addStyleName(STYLE_PREFIX + styleName);
  }

  private Editor createDateEditor(ValueType type) {
    if (ValueType.DATE.equals(type)) {
      return new InputDate();
    } else {
      return new InputDateTime();
    }
  }

  private void doReport(final Report report, final BeeRow row) {
    ParameterList params = CalendarKeeper.createRequestParameters(SVC_DO_REPORT);
    params.addQueryItem(PARAM_REPORT, report.ordinal());

    BeeRowSet rowSet = new BeeRowSet(VIEW_REPORT_OPTIONS, Data.getColumns(VIEW_REPORT_OPTIONS));
    rowSet.addRow(row);

    BeeKeeper.getRpc().sendText(params, Codec.beeSerialize(rowSet), new ResponseCallback() {
      @Override
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
      @Override
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

    final Editor lowerDate = createDateEditor(Data.getColumnType(viewName, COL_LOWER_DATE));
    lowerDate.setValue(Data.getString(viewName, options, COL_LOWER_DATE));
    addStyle(lowerDate.asWidget(), "lowerDate");
    container.add(lowerDate);

    BeeLabel udLabel = new BeeLabel("Data iki:");
    addStyle(udLabel, "udLabel");
    container.add(udLabel);

    final Editor upperDate = createDateEditor(Data.getColumnType(viewName, COL_UPPER_DATE));
    upperDate.setValue(Data.getString(viewName, options, COL_UPPER_DATE));
    addStyle(upperDate.asWidget(), "upperDate");
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

    Relation atpRel = Relation.create(VIEW_ATTENDEE_TYPES, Lists.newArrayList(COL_NAME));
    atpRel.disableNewRow();
    final MultiSelector atpSelector = MultiSelector.createAutonomous(atpRel,
        RendererFactory.createRenderer(VIEW_ATTENDEE_TYPES, Lists.newArrayList(COL_NAME)));

    atpSelector.render(Data.getString(viewName, options, COL_ATTENDEE_TYPES));
    addStyle(atpSelector, "attendeeTypes");
    container.add(atpSelector);

    BeeLabel attLabel = new BeeLabel("Resursai:");
    addStyle(attLabel, "attLabel");
    container.add(attLabel);

    Relation attRel = Relation.create(VIEW_ATTENDEES, Lists.newArrayList(COL_NAME, COL_TYPE_NAME));
    attRel.disableNewRow();

    final MultiSelector attSelector = MultiSelector.createAutonomous(attRel,
        RendererFactory.createRenderer(VIEW_ATTENDEES, Lists.newArrayList(COL_NAME)));

    attSelector.render(Data.getString(viewName, options, COL_ATTENDEES));
    addStyle(attSelector, "attendees");
    container.add(attSelector);

    final BeeButton tableCommand = new BeeButton("Lentelė", new Command() {
      @Override
      public void execute() {
        String vCap = caption.getValue();

        JustDate vLd = TimeUtils.parseDate(lowerDate.getValue());
        JustDate vUd = TimeUtils.parseDate(upperDate.getValue());

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

        Data.setValue(viewName, newRow, COL_ATTENDEE_TYPES, atpSelector.getValue());
        Data.setValue(viewName, newRow, COL_ATTENDEES, attSelector.getValue());

        reportOptions.put(report, newRow);
        doReport(report, newRow);

        Global.closeDialog(container);
      }
    });
    addStyle(tableCommand, "tableCommand");
    container.add(tableCommand);

    DialogBox dialog = DialogBox.create(report.getCaption(), DialogConstants.STYLE_REPORT_OPTIONS);
    dialog.setWidget(container);

    dialog.setAnimationEnabled(true);
    dialog.center();

    caption.setFocus(true);
  }

  private void showReport(Report report, String caption, BeeRowSet rowSet) {
    String gridName = "CalendarReport" + report.name();
    GridDescription gridDescription = new GridDescription(gridName);

    gridDescription.setCaption(caption);
    gridDescription.setReadOnly(true);

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

    Collection<UiOption> uiOptions = EnumSet.of(UiOption.REPORT);
    
    GridView gridView = GridFactory.createGridView(gridDescription,
        GridFactory.getSupplierKey(gridName, null), rowSet.getColumns(), uiOptions);
    gridView.initData(rowSet.getNumberOfRows(), rowSet);
    
    GridPresenter presenter = new GridPresenter(gridDescription, gridView,
        rowSet.getNumberOfRows(), rowSet, Provider.Type.LOCAL, CachingPolicy.NONE, uiOptions);

    BeeKeeper.getScreen().updateActivePanel(presenter.getWidget());
  }
}
