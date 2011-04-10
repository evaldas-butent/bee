package com.butent.bee.client.view;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.widget.BeeTextBox;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.utils.ArrayUtils;
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

  public IsCondition getCondition(String table) {
    String wh = getValue();
    if (BeeUtils.isEmpty(wh)) {
      return null;
    }

    IsCondition condition = null;
    String[] words = BeeUtils.split(wh, 1);
    int cnt = ArrayUtils.length(words);

    String field = ArrayUtils.getQuietly(words, 0);
    if (cnt <= 1) {
      return SqlUtils.isNotNull(table, field);
    }

    String op = null;
    Object value = null;

    if (cnt == 2) {
      value = words[1];
    } else if (cnt >= 3) {
      op = words[1];
      value = ArrayUtils.join(words, 1, 2);
    }
    if (BeeUtils.isDigit((String) value)) {
      value = BeeUtils.val((String) value);
    }
    if (!BeeUtils.isEmpty(op)) {
      if (op.equals("=")) {
        condition = SqlUtils.equal(table, field, value);
      } else if (op.equals("<")) {
        condition = SqlUtils.less(table, field, value);
      } else if (op.equals("<=")) {
        condition = SqlUtils.lessEqual(table, field, value);
      } else if (op.equals(">")) {
        condition = SqlUtils.more(table, field, value);
      } else if (op.equals(">=")) {
        condition = SqlUtils.moreEqual(table, field, value);
      } else if (op.equals("!=") || op.equals("<>")) {
        condition = SqlUtils.notEqual(table, field, value);
      }
    } else if (!BeeUtils.isEmpty(value)) {
      condition = SqlUtils.contains(table, field, value);
    }
    condition = SqlUtils.or(SqlUtils.isNull(table, field),
        SqlUtils.and(SqlUtils.isNotNull(table, field), condition));

    return condition;
  }

  @Override
  public String getDefaultStyleName() {
    return "bee-SearchBox";
  }
}
