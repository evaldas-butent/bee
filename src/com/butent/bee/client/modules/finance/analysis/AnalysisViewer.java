package com.butent.bee.client.modules.finance.analysis;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.AbstractRow;
import com.butent.bee.shared.modules.finance.analysis.AnalysisResults;
import com.butent.bee.shared.modules.finance.analysis.AnalysisValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.stream.Collectors;

class AnalysisViewer extends Flow implements HasCaption {

  private static final String STYLE_PREFIX = BeeConst.CSS_CLASS_PREFIX + "fin-AnalysisViewer-";

  private static final String STYLE_CONTAINER = STYLE_PREFIX + "container";
  private static final String STYLE_TABLE = STYLE_PREFIX + "table";

  private final AnalysisResults results;

  AnalysisViewer(AnalysisResults results) {
    super(STYLE_CONTAINER);

    this.results = results;
    render();
  }

  @Override
  public String getCaption() {
    return results.getHeaderString(COL_ANALYSIS_NAME);
  }

  private void render() {
    Horizontal performance = new Horizontal();

    performance.add(renderMillis(results.getInitStart()));
    performance.add(renderDuration(results.getValidateStart() - results.getInitStart()));

    performance.add(renderMillis(results.getValidateStart()));
    performance.add(renderDuration(results.getComputeStart() - results.getValidateStart()));

    performance.add(renderMillis(results.getComputeStart()));
    performance.add(renderDuration(results.getComputeEnd() - results.getComputeStart()));

    performance.add(renderMillis(results.getComputeEnd()));
    performance.add(renderDuration(results.getComputeEnd() - results.getInitStart()));

    add(performance);

    add(render("columns", results.getColumns().stream()
        .map(AbstractRow::getId).collect(Collectors.toList())));
    add(render("rows", results.getRows().stream()
        .map(AbstractRow::getId).collect(Collectors.toList())));

    if (!results.getColumnSplitTypes().isEmpty()) {
      add(render("cst", results.getColumnSplitTypes()));
      add(render("csv", results.getColumnSplitValues()));
    }
    if (!results.getRowSplitTypes().isEmpty()) {
      add(render("rst", results.getRowSplitTypes()));
      add(render("rsv", results.getRowSplitValues()));
    }

    HtmlTable table = new HtmlTable(STYLE_TABLE);

    int r = 0;
    int c = 0;

    table.setText(r, c++, "cid");
    table.setText(r, c++, "rid");

    table.setText(r, c++, "cpvi");
    table.setText(r, c++, "csti");
    table.setText(r, c++, "csvi");

    table.setText(r, c++, "rpvi");
    table.setText(r, c++, "rsti");
    table.setText(r, c++, "rsvi");

    table.setText(r, c++, "actual");
    table.setText(r, c, "budget");

    r++;

    for (AnalysisValue av : results.getValues()) {
      c = 0;

      table.setText(r, c++, String.valueOf(av.getColumnId()));
      table.setText(r, c++, String.valueOf(av.getRowId()));

      table.setText(r, c++, BeeUtils.toStringOrNull(av.getColumnParentValueIndex()));
      table.setText(r, c++, BeeUtils.toStringOrNull(av.getColumnSplitTypeIndex()));
      table.setText(r, c++, BeeUtils.toStringOrNull(av.getColumnSplitValueIndex()));

      table.setText(r, c++, BeeUtils.toStringOrNull(av.getRowParentValueIndex()));
      table.setText(r, c++, BeeUtils.toStringOrNull(av.getRowSplitTypeIndex()));
      table.setText(r, c++, BeeUtils.toStringOrNull(av.getRowSplitValueIndex()));

      table.setText(r, c++, av.getActualValue());
      table.setText(r, c, av.getBudgetValue());

      r++;
    }

    add(table);
  }

  private static Widget renderMillis(long millis) {
    return new Label(TimeUtils.renderDateTime(millis, true));
  }

  private static Widget renderDuration(long millis) {
    return new Label(TimeUtils.renderMillis(millis));
  }

  private static Widget render(String label, Object obj) {
    return new Label(BeeUtils.joinWords(label, obj));
  }
}
