package com.butent.bee.client.modules.payroll;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.output.ReportUtils;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.css.values.FontSize;
import com.butent.bee.shared.css.values.FontWeight;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

final class PayrollHelper {

  private static final String STYLE_PREFIX = PayrollKeeper.STYLE_PREFIX + "wsf-";

  private static final String STYLE_LOGO = STYLE_PREFIX + "logo";

  private static final String STYLE_HEADER_COMPANY = STYLE_PREFIX + "companyLabel";
  private static final String STYLE_HEADER_INFO = STYLE_PREFIX + "info";
  private static final String STYLE_HEADER_OBJECT = STYLE_PREFIX + "object";

  private static final String STYLE_FOOTER = STYLE_PREFIX + "footer";
  private static final String STYLE_FOOTER_MANAGER = STYLE_PREFIX + "manager";
  private static final String STYLE_FOOTER_PREPARE = STYLE_PREFIX + "prepare";
  private static final String STYLE_FOOTER_DATE = STYLE_PREFIX + "date";
  private static final String STYLE_FOOTER_TIME = STYLE_PREFIX + "time";
  private static final String STYLE_FOOTER_TIME_CONTAINER = STYLE_PREFIX + "timeContainer";

  private static final String KEY_MILLIS = "millis";

  static String format(YearMonth ym) {
    if (ym == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return BeeUtils.joinWords(ym.getYear(), Format.renderMonthFullStandalone(ym).toLowerCase());
    }
  }

  public static void print(WorkScheduleWidget widget, BeeRow objectRow, String formName) {
    Flow mainContent = new Flow();
    Flow container = new Flow("bee-ws-Print");
    StyleUtils.setFontSize(container, 11);

    mainContent.getElement().setInnerHTML(widget.getElement().getString());
    if (formName == FORM_TIME_SHEET) {
      mainContent = renderTimeSheet(mainContent);
    }

    container.getElement().setInnerHTML(
        renderHeader(objectRow).toString()
            + mainContent.getElement().getString()
            + renderFooter(filterMonthTimeRangeCodes(widget), objectRow, formName));

    ReportUtils.getPdf(container.toString(), fileInfo -> ReportUtils.preview(fileInfo));
  }

  private static List<BeeRow> filterMonthTimeRangeCodes(WorkScheduleWidget widget) {

    List<BeeRow> result = new ArrayList<>();
    BeeRowSet wsData = widget.getWsData();

    if (!DataUtils.isEmpty(wsData)) {

      int activeYear = widget.getActiveMonth().getYear();
      int activeMonth = widget.getActiveMonth().getMonth();

      int dateIndex = wsData.getColumnIndex(COL_WORK_SCHEDULE_DATE);

      for (BeeRow row : wsData) {
        int codeYear = row.getDate(dateIndex).getYear();
        int codeMonth = row.getDate(dateIndex).getMonth();

        if (codeYear == activeYear && activeMonth == codeMonth) {
          result.add(DataUtils.cloneRow(row));
        }
      }
    }

    return result;
  }

  private static Flow renderFooter(List<BeeRow> rowSet, BeeRow objectRow, String formName) {
    Flow footer = new Flow();
    footer.addStyleName(STYLE_FOOTER);

    if (rowSet.size() > 0) {
      if (formName == FORM_WORK_SCHEDULE) {
        Flow timeboard = new Flow();
        Flow timeContainer = new Flow(STYLE_FOOTER_TIME_CONTAINER);

        Flow timeLabels = new Flow(STYLE_FOOTER_TIME);
        timeLabels.add(new Label("Darbo laikas:"));
        timeLabels.add(new Label("Poilsio laikas:"));

        timeboard.add(timeLabels);

        List<String> codeList = new ArrayList<>();

        for (BeeRow r : rowSet) {
          Flow div = new Flow(STYLE_FOOTER_TIME);
          String code = r.getString(Data.getColumnIndex(VIEW_WORK_SCHEDULE, COL_TR_CODE));

          if (!codeList.contains(code) && !BeeUtils.isEmpty(code)) {
            String timeFrom = r.getString(Data.getColumnIndex(VIEW_WORK_SCHEDULE, ALS_TR_FROM));
            String timeUntil = r.getString(Data.getColumnIndex(VIEW_WORK_SCHEDULE, ALS_TR_UNTIL));
            String workTime = BeeUtils.join("-", code, timeFrom, timeUntil) + " val.";
            div.add(new Label(workTime));

            String restTime =
                r.getString(Data.getColumnIndex(VIEW_WORK_SCHEDULE, COL_TC_DESCRIPTION));

            if (!BeeUtils.isEmpty(restTime)) {
              div.add(new Label(restTime));
            }

            timeContainer.add(div);
            codeList.add(code);
          }
        }

        timeboard.add(timeContainer);
        footer.add(timeboard);
      }
    }

    Label prepare = new Label("Parengė:");
    prepare.addStyleName(STYLE_FOOTER_PREPARE);
    footer.add(prepare);

    String manager = "Objektų Vadybininkas(ė) ";
    String firstName =
        objectRow.getString(Data.getColumnIndex(VIEW_LOCATIONS, ALS_LOCATION_MANAGER_FIRST_NAME));
    String lastName =
        objectRow.getString(Data.getColumnIndex(VIEW_LOCATIONS, ALS_LOCATION_MANAGER_LAST_NAME));
    String mobile =
        objectRow.getString(Data.getColumnIndex(VIEW_LOCATIONS, ALS_LOCATION_MANAGER_MOBILE));

    if (!BeeUtils.isEmpty(firstName)) {
      manager += firstName + " ";
    }
    if (!BeeUtils.isEmpty(lastName)) {
      manager += lastName + " ";
    }
    if (!BeeUtils.isEmpty(mobile)) {
      manager += "mob.tel. " + mobile;
    }

    Label managerLbl = new Label(manager);
    managerLbl.addStyleName(STYLE_FOOTER_MANAGER);
    footer.add(managerLbl);

    Label date = new Label(new JustDate(TimeUtils.nowMillis()).toString());
    date.addStyleName(STYLE_FOOTER_DATE);
    footer.add(date);

    return footer;
  }

  private static Flow renderHeader(BeeRow objectRow) {
    Flow header = new Flow();

    Label companyLabel = new Label("UAB VITARESTA");
    companyLabel.addStyleName(STYLE_HEADER_COMPANY);
    header.add(companyLabel);

    Flow object = new Flow(STYLE_HEADER_OBJECT);

    Label objectLabel =
        new Label(objectRow.getString(Data.getColumnIndex(VIEW_LOCATIONS, COL_LOCATION_NAME)));
    StyleUtils.setFontSize(objectLabel, FontSize.LARGER);
    StyleUtils.buildFontWeight(FontWeight.BOLD);
    object.add(objectLabel);

    String address = objectRow.getString(Data.getColumnIndex(VIEW_LOCATIONS, COL_LOCATION_ADDRESS));
    String city = objectRow.getString(Data.getColumnIndex(VIEW_LOCATIONS, ALS_LOCATION_CITY_NAME));
    String contactInfo = BeeUtils.join(",", address, city);

    if (!BeeUtils.isEmpty(contactInfo)) {
      Label contactLbl = new Label(contactInfo);
      StyleUtils.setFontSize(contactLbl, FontSize.SMALL);
      object.add(contactLbl);
    }

    header.add(object);

    Flow info = new Flow(STYLE_HEADER_INFO);
    info.add(new Label("Tvirtina:"));
    info.add(new Label("Padalinio vadovė:"));
    info.add(new Label("DSSK atstovė:"));

    header.add(info);

    Image logo = new Image("images/vitaresta.png");
    logo.addStyleName(STYLE_LOGO);
    header.add(logo);

    return header;
  }

  private static Flow renderTimeSheet(Flow content) {

    List<Element> days =
        Selectors.getElementsByClassName(content.getElement(), "bee-payroll-ws-day-content");

    for (Element day : days) {
      List<Element> items = Selectors.getElementsWithDataProperty(day, KEY_MILLIS);

      Long millis = 0L;

      if (items.size() > 1) {
        for (int i = 0; i < items.size(); i++) {
          millis += DomUtils.getDataPropertyLong(items.get(i), KEY_MILLIS);

          if (i != 0) {
            items.get(i).getStyle().setDisplay(Display.NONE);
          }
        }
        items.get(0).setInnerHTML(
            TimeUtils.renderTime(millis, false));

      } else if (items.size() == 1) {
        items.get(0).setInnerHTML(
            TimeUtils.renderTime(DomUtils.getDataPropertyLong(items.get(0), KEY_MILLIS), false));
      }
    }

    return content;
  }

  private PayrollHelper() {
  }
}
