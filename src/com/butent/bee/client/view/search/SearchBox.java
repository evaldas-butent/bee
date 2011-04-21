package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.widget.BeeTextBox;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.filter.ColumnIsEmptyFilter;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.ComparisonFilter.Operator;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class SearchBox extends BeeTextBox implements SearchView {
  private Presenter presenter = null;

  public SearchBox() {
    super();
    DomUtils.setSearch(this);
    DomUtils.setPlaceholder(this, "filter...");
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "search");
  }

  @Override
  public String getDefaultStyleName() {
    return "bee-SearchBox";
  }

  public Filter getFilter(List<? extends IsColumn> columns) {
    StringBuilder sb = new StringBuilder();

    if (!BeeUtils.isEmpty(columns)) {
      for (IsColumn column : columns) {
        if (sb.length() > 0) {
          sb.append("|");
        }
        sb.append(column.getId().toLowerCase());
      }
    }
    return getFilter(getValue(), sb.toString());
  }

  public Presenter getViewPresenter() {
    return presenter;
  }

  public void setViewPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  private Filter getFilter(String wh, String colPattern) {
    Filter flt = null;

    if (!BeeUtils.isEmpty(wh)) {
      List<String> parts = getParts(wh, "\\s+[oO][rR]\\s+");

      if (parts.size() > 1) {
        flt = CompoundFilter.or();
      } else {
        parts = getParts(wh, "\\s+[aA][nN][dD]\\s+");

        if (parts.size() > 1) {
          flt = CompoundFilter.and();
        }
      }
      if (!BeeUtils.isEmpty(flt)) {
        for (String part : parts) {
          ((CompoundFilter) flt).add(getFilter(part, colPattern));
        }
      } else {
        String ptrn;

        if (BeeUtils.isEmpty(colPattern)) {
          ptrn = "[a-z_]\\w*";
        } else {
          ptrn = colPattern;
        }
        String s = parts.get(0).toLowerCase();
        String pattern = "\\s*(" + ptrn + ")\\s*(" + Operator.getPattern(true) + ")\\s*(.*)";
        boolean ok = s.matches(pattern);

        if (!ok) {
          pattern = "\\s*(" + ptrn + ")\\s*(" + Operator.getPattern(false) + ")\\s*(.*)";
          ok = s.matches(pattern);
        }
        if (ok) {
          String column = s.replaceFirst(pattern, "$1");
          String operator = s.replaceFirst(pattern, "$2");
          String value = s.replaceFirst(pattern, "$3");

          if (BeeUtils.isEmpty(value)) {
            flt = new ColumnIsEmptyFilter(column);

          } else if (!BeeUtils.isEmpty(colPattern) && value.matches("^(" + colPattern + ")$")) {
            flt = ComparisonFilter.compareWithColumn(column, operator, value);

          } else {
            flt = ComparisonFilter.compareWithValue(column, operator,
                BeeUtils.isNumeric(value) ? BeeUtils.toDouble(value) : value);
          }
        } else {
          BeeKeeper.getLog().warning("Wrong filter expression: " + s);
        }
      }
    }
    return flt;
  }

  private List<String> getParts(String expr, String pattern) {
    List<String> parts = Lists.newArrayList();

    String s = expr.replaceFirst("^\\s*\\(\\s*(.+)\\s*\\)\\s*$", "$1");
    int cnt = s.split(pattern).length;
    boolean ok = false;

    for (int i = 2; i <= cnt; i++) {
      String[] pair = s.split(pattern, i);
      String right = pair[pair.length - 1];
      String left = s.substring(0, s.lastIndexOf(right)).replaceFirst(pattern + "$", "");

      if (validPart(left)) {
        parts.add(left);
        parts.addAll(getParts(right, pattern));
        ok = true;

      } else if (validPart(right)) {
        parts.addAll(getParts(left, pattern));
        parts.add(right);
        ok = true;
      }
      if (ok) {
        break;
      }
    }
    if (!ok) {
      parts.add(s);
    }
    return parts;
  }

  private boolean validPart(String expr) {
    String wh = expr;
    String regex = "^(.*)\\((.*)\\)(.*)$";

    while (wh.matches(regex)) {
      wh = wh.replaceFirst(regex, "$1" + "$2" + "$3");
    }
    return !wh.matches(".*[\\(\\)].*");
  }
}
