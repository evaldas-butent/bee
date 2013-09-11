package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import com.butent.bee.client.grid.cell.FooterCell;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.FooterDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class ColumnFooter extends Header<String> implements HasHorizontalAlignment {

  private final String columnId;
  private final FooterDescription description;

  private String html;
  private HorizontalAlignmentConstant horizontalAlignment;

  private Evaluator rowEvaluator;

  public ColumnFooter(String columnId, FooterDescription footerDescription) {
    super(new FooterCell());

    this.columnId = columnId;
    this.description = footerDescription;
  }

  @Override
  public HorizontalAlignmentConstant getHorizontalAlignment() {
    return horizontalAlignment;
  }

  public String getHtml() {
    return html;
  }

  public Evaluator getRowEvaluator() {
    return rowEvaluator;
  }

  @Override
  public String getValue() {
    return getHtml();
  }

  public void init(List<? extends IsColumn> dataColumns) {
    if (description != null) {
      if (!BeeUtils.isEmpty(description.getText())) {
        setHtml(LocaleUtils.maybeLocalize(description.getText()));
      } else if (!BeeUtils.isEmpty(description.getHtml())) {
        setHtml(description.getHtml());
      }

      if (!BeeUtils.isEmpty(description.getHorAlign())) {
        UiHelper.setHorizontalAlignment(this, description.getHorAlign());
      }

      if (!BeeUtils.isEmpty(description.getSum())) {
        if (!"*".equals(description.getSum())) {
          setRowEvaluator(Evaluator.create(new Calculation(description.getSum(), null), null,
              dataColumns));
        }

        if (getHorizontalAlignment() == null) {
          setHorizontalAlignment(ALIGN_RIGHT);
        }
      }
    }
  }

  @Override
  public void render(Context context, SafeHtmlBuilder sb) {
    if (getRowEvaluator() != null && context instanceof CellContext) {
      List<IsRow> data = ((CellContext) context).getGrid().getRowData();
      if (!BeeUtils.isEmpty(data)) {
        Double value = sum(data);
        if (value != null) {
          getCell().render(context, BeeUtils.toString(value), sb);
          return;
        }
      }
    }

    super.render(context, sb);
  }

  @Override
  public void setHorizontalAlignment(HorizontalAlignmentConstant align) {
    this.horizontalAlignment = align;
  }

  public void setHtml(String html) {
    this.html = html;
  }

  public void setRowEvaluator(Evaluator rowEvaluator) {
    this.rowEvaluator = rowEvaluator;
  }

  protected Double sum(List<IsRow> data) {
    double total = BeeConst.DOUBLE_ZERO;
    int count = 0;

    for (IsRow row : data) {
      getRowEvaluator().update(row);
      Double value = BeeUtils.toDoubleOrNull(getRowEvaluator().evaluate());
      
      if (BeeUtils.isDouble(value) && !BeeUtils.isZero(value)) {
        total += value;
        count++;
      }
    }

    LogUtils.getRootLogger().debug("footer", columnId, count, total);

    return (count > 0) ? total : null;
  }
}
