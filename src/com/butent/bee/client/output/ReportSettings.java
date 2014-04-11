package com.butent.bee.client.output;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

public class ReportSettings {

  @SuppressWarnings("unused")
  private static final class Item implements HasCaption {
    private final long id;
    private final Report report;

    private String caption;
    private ReportParameters parameters;

    private Item(long id, Report report, String caption, ReportParameters parameters) {
      this.id = id;
      this.report = report;
      this.caption = caption;
      this.parameters = parameters;
    }

    @Override
    public String getCaption() {
      return caption;
    }
    
    private void open() {
      report.open(parameters);
    }
  }

  private final List<Item> items = new ArrayList<>();

  public ReportSettings() {
    super();
  }

  public void bookmark(final Report report, String caption, final ReportParameters parameters) {
    Assert.notNull(report);
    if (!DataUtils.isId(BeeKeeper.getUser().getUserId())) {
      return;
    }

    int maxLength = Data.getColumnPrecision(VIEW_REPORT_SETTINGS, COL_RS_CAPTION);

    Global.inputString(Localized.getConstants().bookmarkName(), null, new StringCallback() {
      @Override
      public void onSuccess(String value) {
        addItem(report, value, parameters);
      }
    }, caption, maxLength);
  }

  public void load(String serialized) {
    if (!items.isEmpty()) {
      items.clear();
    }

    if (BeeUtils.isEmpty(serialized)) {
      return;
    }

    BeeRowSet rowSet = BeeRowSet.restore(serialized);
    if (DataUtils.isEmpty(rowSet)) {
      return;
    }

    int reportIndex = rowSet.getColumnIndex(COL_RS_REPORT);
    Assert.nonNegative(reportIndex, COL_RS_REPORT);

    int captionIndex = rowSet.getColumnIndex(COL_RS_CAPTION);
    Assert.nonNegative(captionIndex, COL_RS_CAPTION);

    int paramIndex = rowSet.getColumnIndex(COL_RS_PARAMETERS);
    Assert.nonNegative(paramIndex, COL_RS_PARAMETERS);

    for (BeeRow row : rowSet) {
      Report report = Report.parse(row.getString(reportIndex));

      if (report != null) {
        Item item = new Item(row.getId(), report, row.getString(captionIndex),
            ReportParameters.restore(row.getString(paramIndex)));
        items.add(item);
      }
    }
  }

  private void addItem(final Report report, final String caption,
      final ReportParameters parameters) {
    
    List<BeeColumn> columns = Data.getColumns(VIEW_REPORT_SETTINGS,
        Lists.newArrayList(COL_RS_USER, COL_RS_REPORT, COL_RS_CAPTION, COL_RS_PARAMETERS));
    List<String> values = Lists.newArrayList(BeeUtils.toString(BeeKeeper.getUser().getUserId()),
        report.getReportName(), BeeUtils.trim(caption), Codec.beeSerialize(parameters));
    
    Queries.insert(VIEW_REPORT_SETTINGS, columns, values, null, new RowCallback() {
      @Override
      public void onSuccess(BeeRow row) {
        Item item = new Item(row.getId(), report, BeeUtils.trim(caption), parameters);
        items.add(item);
      }
    });
  }
}
