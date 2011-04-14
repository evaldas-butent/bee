package com.butent.bee.client.view;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.widget.BeeTextBox;
import com.butent.bee.shared.data.Filter;
import com.butent.bee.shared.utils.BeeUtils;

public class SearchBox extends BeeTextBox implements SearchView {

  public SearchBox() {
    super();
    DomUtils.setSearch(this);
    DomUtils.setPlaceholder(this, "filter...");
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "search");
  }

  public Filter getCondition() {
    return getFilter(getValue());
  }

  @Override
  public String getDefaultStyleName() {
    return "bee-SearchBox";
  }

  private Filter getFilter(String wh) {
    Filter flt = null;

    if (!BeeUtils.isEmpty(wh)) {
      String orPattern = "\\s+[Oo][Rr]\\s+";
      String andPattern = "\\s+[Aa][Nn][Dd]\\s+";

      if (wh.matches(".+" + orPattern + ".+")) {
        flt = Filter.or();

        for (String xpr : wh.split(orPattern)) {
          flt.add(getFilter(xpr));
        }
      } else if (wh.matches(".+" + andPattern + ".+")) {
        flt = Filter.and();

        for (String xpr : wh.split(andPattern)) {
          flt.add(getFilter(xpr.trim()));
        }
      } else {
        String pattern = "\\s*(\\S+)\\s*(<=|>=|!=|<>)\\s*(.+)";
        boolean ok = wh.matches(pattern);

        if (!ok) {
          pattern = "\\s*(\\S+)\\s*([<>=\\$])\\s*(.+)";
          ok = wh.matches(pattern);
        }
        if (ok) {
          String column = wh.replaceFirst(pattern, "$1");
          String operator = wh.replaceFirst(pattern, "$2");
          String value = wh.replaceFirst(pattern, "$3");

          flt = Filter.condition(column, operator,
              BeeUtils.isDigit(value) ? BeeUtils.val(value) : value);
        } else {
          BeeKeeper.getLog().warning("Wrong filter expression: " + wh);
        }
      }
    }
    return flt;
  }
}
