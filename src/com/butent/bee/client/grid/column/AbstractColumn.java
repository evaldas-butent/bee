package com.butent.bee.client.grid.column;

import com.google.common.collect.Lists;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.style.HasTextAlign;
import com.butent.bee.client.style.HasVerticalAlign;
import com.butent.bee.client.style.HasWhiteSpace;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.css.values.VerticalAlign;
import com.butent.bee.shared.css.values.WhiteSpace;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.HasValueType;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Is an abstract class for grid column classes.
 */

public abstract class AbstractColumn<C> implements HasValueType, HasOptions, HasWhiteSpace,
    HasTextAlign, HasVerticalAlign {

  private final Cell<C> cell;

  private List<String> searchBy;
  private List<String> sortBy;

  private boolean isSortable;

  private TextAlign hAlign;
  private VerticalAlign vAlign;

  private WhiteSpace whiteSpace;

  private String options;

  private final List<String> classes = Lists.newArrayList();

  private boolean instantKarma;

  public AbstractColumn(Cell<C> cell) {
    this.cell = cell;
  }

  public void addClass(String className) {
    if (!BeeUtils.isEmpty(className)) {
      this.classes.add(className);
    }
  }

  public Cell<C> getCell() {
    return cell;
  }

  public List<String> getClasses() {
    return classes;
  }

  public abstract ColType getColType();

  @Override
  public String getOptions() {
    return options;
  }

  public List<String> getSearchBy() {
    return searchBy;
  }

  public List<String> getSortBy() {
    return sortBy;
  }

  public abstract String getStyleSuffix();

  public abstract String getString(Context context, IsRow row);

  @Override
  public TextAlign getTextAlign() {
    return hAlign;
  }

  public abstract C getValue(IsRow row);

  @Override
  public VerticalAlign getVerticalAlign() {
    return vAlign;
  }

  @Override
  public WhiteSpace getWhiteSpace() {
    return whiteSpace;
  }

  public boolean instantKarma(IsRow row) {
    return instantKarma && getValue(row) != null;
  }

  public boolean isSortable() {
    return isSortable;
  }

  public void onBrowserEvent(Context context, Element elem, IsRow row, NativeEvent event) {
    cell.onBrowserEvent(context, elem, getValue(row), event, null);
  }

  public void render(Context context, IsRow row, SafeHtmlBuilder sb) {
    cell.render(context, getValue(row), sb);
  }

  public void setInstantKarma(boolean instantKarma) {
    this.instantKarma = instantKarma;
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  public void setSearchBy(List<String> searchBy) {
    this.searchBy = searchBy;
  }

  public void setSortable(boolean sortable) {
    this.isSortable = sortable;
  }

  public void setSortBy(List<String> sortBy) {
    this.sortBy = sortBy;
  }

  @Override
  public void setTextAlign(TextAlign align) {
    this.hAlign = align;
  }

  @Override
  public void setVerticalAlign(VerticalAlign align) {
    this.vAlign = align;
  }

  @Override
  public void setWhiteSpace(WhiteSpace whiteSpace) {
    this.whiteSpace = whiteSpace;
  }
}
