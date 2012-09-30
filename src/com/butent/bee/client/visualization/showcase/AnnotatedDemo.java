package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.visualization.AbstractDataTable.ColumnType;
import com.butent.bee.client.visualization.DataTable;
import com.butent.bee.client.visualization.events.RangeChangeHandler;
import com.butent.bee.client.visualization.visualizations.AnnotatedTimeLine;
import com.butent.bee.client.visualization.visualizations.AnnotatedTimeLine.Options;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements demonstration of an annotated time line visualization.
 */

public class AnnotatedDemo implements LeftTabPanel.WidgetProvider {
  private AnnotatedTimeLine chart;
  private final BeeLabel status = new BeeLabel();
  private final BeeLabel rangeStatus = new BeeLabel();
  private Vertical widget = new Vertical();

  public AnnotatedDemo() {
    Options options = Options.create();
    options.setDisplayAnnotations(true);
    options.setDateFormat("yy.MM.dd");

    DataTable data = DataTable.create();
    data.addColumn(ColumnType.DATE, "Date");
    data.addColumn(ColumnType.NUMBER, "Arbūzai");
    data.addColumn(ColumnType.STRING, "title1");
    data.addColumn(ColumnType.STRING, "text1");
    data.addColumn(ColumnType.NUMBER, "Morkos");
    data.addColumn(ColumnType.STRING, "title2");
    data.addColumn(ColumnType.STRING, "text2");

    int rows = BeeUtils.randomInt(30, 100);
    data.addRows(rows);

    DateTime now = new DateTime();
    long start = new DateTime(now.getYear(), now.getMonth(), now.getDom()).getTime();
    start -= TimeUtils.MILLIS_PER_DAY * (rows + 1);

    for (int i = 0; i < rows; i++) {
      data.setDateTime(i, 0, new DateTime(start += TimeUtils.MILLIS_PER_DAY));
      int x = BeeUtils.randomInt(0, 300);
      int y = BeeUtils.randomInt(100, 200);
      data.setValue(i, 1, x);
      data.setValue(i, 4, y);

      if (x < 10) {
        data.setValue(i, 2, (i % 2 == 0) ? "Viską pardavėm" : "Petras suvalgė");
      } else if (x > 290) {
        data.setValue(i, 2, "Gali sprogti");
      }

      if (i == rows / 3 || i == rows * 2 / 3) {
        data.setValue(i, 5, (i == rows / 3) ? "Šviežios" : "Skanios");
      }
    }

    status.setText("not ready");
    chart = new AnnotatedTimeLine(data, options, "600px", "300px");

    widget.add(chart);
    widget.add(status);
    widget.add(rangeStatus);

    addHandlers();
  }

  private void addHandlers() {
    chart.addRangeChangeHandler(new RangeChangeHandler() {
      @Override
      public void onRangeChange(RangeChangeEvent event) {
        rangeStatus.setText(BeeUtils.joinWords(event.getStart(), event.getEnd()));
      }
    });
    chart.addReadyHandler(new ReadyDemo(status));
  }

  public Widget getWidget() {
    return widget;
  }
}
