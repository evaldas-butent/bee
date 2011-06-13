package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.SuggestOracle;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class SelectionOracle extends SuggestOracle {

  private static class ResponseSuggestion implements Suggestion {
    private final String replacementString;
    private final String displayString;

    private ResponseSuggestion(String replacementString) {
      this(replacementString, replacementString);
    }

    private ResponseSuggestion(String replacementString, String displayString) {
      this.replacementString = replacementString;
      this.displayString = displayString;
    }

    public String getDisplayString() {
      return displayString;
    }

    public String getReplacementString() {
      return replacementString;
    }
  }

  private final String table;
  private final String field;

  private final List<String> values = Lists.newArrayList();

  public SelectionOracle(String table, String field) {
    this.table = table;
    this.field = field;
    
    initValues();
  }

  public String getField() {
    return field;
  }

  public String getTable() {
    return table;
  }

  public List<String> getValues() {
    return values;
  }

  @Override
  public void requestSuggestions(Request request, Callback callback) {
    Assert.notNull(request);
    Assert.notNull(callback);

    String query = request.getQuery();
    int limit = request.getLimit();

    List<Suggestion> suggestions = Lists.newArrayList();
    int truncated = 0;

    for (String value : getValues()) {
      if (BeeUtils.isEmpty(query) || BeeUtils.context(query, value)) {
        if (limit > 0 && suggestions.size() >= limit) {
          truncated++;
        } else {
          suggestions.add(new ResponseSuggestion(value));
        }
      }
    }

    BeeKeeper.getLog().info("oracle request", query, limit, suggestions.size());
    
    Response response = new Response(suggestions);
    response.setMoreSuggestionsCount(truncated);

    callback.onSuggestionsReady(request, response);
  }
  
  private void initValues() {
    Queries.getColumnValues(getTable(), getField(), null, true, new Queries.RowSetCallback() {
      public void onFailure(String reason) {
      }
      
      public void onSuccess(BeeRowSet result) {
        getValues().clear();
        String value;
        for (IsRow row : result.getRows().getList()) {
          value = row.getString(0);
          if (!BeeUtils.isEmpty(value)) {
            getValues().add(value);
          }
        }
      }
    });
  }

}