package com.butent.bee.client.view;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.widget.BeeTextBox;
import com.butent.bee.shared.data.view.Filter;
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

  public Filter getFilter() {
    return getFilter(getValue());
  }

  public Presenter getViewPresenter() {
    return presenter;
  }

  public void setViewPresenter(Presenter presenter) {
    this.presenter = presenter;
  }
  
  private Filter getFilter(String wh) {
    Filter flt = null;

    if (!BeeUtils.isEmpty(wh)) {
      String orPattern = "\\s+[Oo][Rr]\\s+";
      String andPattern = "\\s+[Aa][Nn][Dd]\\s+";

      List<String> parts = getParts(wh, orPattern);

      if (parts.size() > 1) {
        flt = Filter.or();
      } else {
        parts = getParts(wh, andPattern);

        if (parts.size() > 1) {
          flt = Filter.and();
        }
      }
      if (parts.size() > 1) {
        for (String part : parts) {
          flt.add(getFilter(part));
        }
      } else {
        String s = unparenthesize(wh);
        String pattern = "\\s*(\\S+)\\s*(<=|>=|!=|<>)\\s*(.+)";
        boolean ok = s.matches(pattern);

        if (!ok) {
          pattern = "\\s*(\\S+)\\s*([<>=\\$])\\s*(.+)";
          ok = s.matches(pattern);
        }
        if (ok) {
          String column = s.replaceFirst(pattern, "$1");
          String operator = s.replaceFirst(pattern, "$2");
          String value = s.replaceFirst(pattern, "$3");

          flt = Filter.condition(column, operator,
              BeeUtils.isDigit(value) ? BeeUtils.val(value) : value);
        } else {
          BeeKeeper.getLog().warning("Wrong filter expression: " + s);
        }
      }
    }
    return flt;
  }

  private List<String> getParts(String expr, String pattern) {
    List<String> parts = Lists.newArrayList();

    String s = unparenthesize(expr);
    int cnt = s.split(pattern).length;
    boolean ok = false;

    for (int i = 2; i <= cnt; i++) {
      String[] pair = s.split(pattern, i);
      String right = pair[pair.length - 1];
      String left = s.substring(0, s.lastIndexOf(right)).replaceFirst(pattern + "$", "");
      ok = validPart(left);

      if (ok) {
        parts.add(left);
        parts.addAll(getParts(right, pattern));
        break;
      }
    }
    if (!ok) {
      parts.add(s);
    }
    return parts;
  }

  private String unparenthesize(String expr) {
    return expr.replaceFirst("^\\s*\\(\\s*(.*)\\s*\\)\\s*$", "$1");
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
