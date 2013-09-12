package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import com.butent.bee.client.grid.cell.FooterCell;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.HasDateTimeFormat;
import com.butent.bee.client.i18n.HasNumberFormat;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.HasScale;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.FooterDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

public class ColumnFooter extends Header<String> implements HasHorizontalAlignment,
    HasDateTimeFormat, HasNumberFormat, HasScale, HasOptions {

  private String html;
  
  private boolean reduce; 
  
  private CellSource cellSource;
  private Evaluator rowEvaluator;

  private HorizontalAlignmentConstant horizontalAlignment;

  private DateTimeFormat dateTimeFormat;
  private NumberFormat numberFormat;

  private int scale = BeeConst.UNDEF;

  private String options;

  public ColumnFooter(CellSource cellSource, AbstractColumn<?> column,
      ColumnDescription columnDescription, List<? extends IsColumn> dataColumns) {

    super(new FooterCell());
    
    this.cellSource = cellSource;

    init(column, columnDescription, dataColumns);
  }

  public CellSource getCellSource() {
    return cellSource;
  }

  @Override
  public DateTimeFormat getDateTimeFormat() {
    return dateTimeFormat;
  }

  @Override
  public HorizontalAlignmentConstant getHorizontalAlignment() {
    return horizontalAlignment;
  }

  public String getHtml() {
    return html;
  }

  @Override
  public NumberFormat getNumberFormat() {
    return numberFormat;
  }

  @Override
  public String getOptions() {
    return options;
  }

  public Evaluator getRowEvaluator() {
    return rowEvaluator;
  }

  @Override
  public int getScale() {
    return scale;
  }

  @Override
  public String getValue() {
    return getHtml();
  }

  public boolean reduce() {
    return reduce;
  }

  @Override
  public void render(Context context, SafeHtmlBuilder sb) {
    if (reduce() && context instanceof CellContext) {
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

  public void setCellSource(CellSource cellSource) {
    this.cellSource = cellSource;
  }

  @Override
  public void setDateTimeFormat(DateTimeFormat dateTimeFormat) {
    this.dateTimeFormat = dateTimeFormat;
  }

  @Override
  public void setHorizontalAlignment(HorizontalAlignmentConstant align) {
    this.horizontalAlignment = align;
  }

  public void setHtml(String html) {
    this.html = html;
  }

  @Override
  public void setNumberFormat(NumberFormat numberFormat) {
    this.numberFormat = numberFormat;
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  public void setReduce(boolean reduce) {
    this.reduce = reduce;
  }
  
  public void setRowEvaluator(Evaluator rowEvaluator) {
    this.rowEvaluator = rowEvaluator;
  }

  @Override
  public void setScale(int scale) {
    this.scale = scale;
  }

  protected String getRowValue(IsRow row) {
    if (row == null) {
      return null;
    } else if (getRowEvaluator() != null) {
      getRowEvaluator().update(row);
      return getRowEvaluator().evaluate();
    } else if (getCellSource() != null) {
      return getCellSource().getString(row);
    } else {
      return null;
    }
  }

  protected void init(AbstractColumn<?> column, ColumnDescription columnDescription,
      List<? extends IsColumn> dataColumns) {
    Assert.notNull(column);
    Assert.notNull(columnDescription);

    FooterDescription footerDescription = columnDescription.getFooterDescription();

    if (footerDescription != null) {
      if (!BeeUtils.isEmpty(footerDescription.getText())) {
        setHtml(LocaleUtils.maybeLocalize(footerDescription.getText()));
      } else if (!BeeUtils.isEmpty(footerDescription.getHtml())) {
        setHtml(footerDescription.getHtml());
      }

      if (!BeeUtils.isEmpty(footerDescription.getHorAlign())) {
        UiHelper.setHorizontalAlignment(this, footerDescription.getHorAlign());
      }
      
      if (footerDescription.getScale() != null) {
        setScale(footerDescription.getScale());
      }

      if (!BeeUtils.isEmpty(footerDescription.getOptions())) {
        setOptions(footerDescription.getOptions());
      }

      String sumExpr = footerDescription.getSum();
      if (!BeeUtils.isEmpty(sumExpr)) {
        Calculation calculation;
        if (sumExpr.trim().length() <= 1 && !NameUtils.isIdentifier(sumExpr)) {
          calculation = columnDescription.getRender();
        } else {
          calculation = new Calculation(sumExpr, null);
        }
        
        if (calculation != null) {
          setRowEvaluator(Evaluator.create(calculation, null, dataColumns));
        }
        
        if (calculation != null || getCellSource() != null) {
          setReduce(true);
        }

        if (getHorizontalAlignment() == null) {
          setHorizontalAlignment(ALIGN_RIGHT);
        }
      }
    }

    if (getHorizontalAlignment() == null && column.getHorizontalAlignment() != null) {
      setHorizontalAlignment(column.getHorizontalAlignment());
    }
    
    if (column instanceof HasDateTimeFormat) {
      setDateTimeFormat(((HasDateTimeFormat) column).getDateTimeFormat());
    }
    if (getNumberFormat() == null && column instanceof HasNumberFormat) {
      setNumberFormat(((HasNumberFormat) column).getNumberFormat());
    }
    
    if (BeeConst.isUndef(getScale())) {
      if (column instanceof HasScale) {
        setScale(((HasScale) column).getScale());
      } else if (columnDescription.getScale() != null) {
        setScale(columnDescription.getScale());
      } else if (getCellSource() != null) {
        setScale(getCellSource().getScale());
      }
    }
  }

  protected Double sum(List<IsRow> data) {
    double total = BeeConst.DOUBLE_ZERO;
    int count = 0;

    for (IsRow row : data) {
      Double value = BeeUtils.toDoubleOrNull(getRowValue(row));
      if (BeeUtils.isDouble(value) && !BeeUtils.isZero(value)) {
        total += value;
        count++;
      }
    }

    LogUtils.getRootLogger().debug("footer",
        (getCellSource() == null) ? null : getCellSource().getName(), count, total);

    return (count > 0) ? total : null;
  }
}
