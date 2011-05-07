package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.visualization.Query;
import com.butent.bee.client.visualization.Query.Callback;
import com.butent.bee.client.visualization.QueryResponse;
import com.butent.bee.client.visualization.visualizations.Table;
import com.butent.bee.client.widget.BeeLabel;

/**
 * Implements a demonstration of query results showing table visualization.
 */

public class TableDemo implements LeftTabPanel.WidgetProvider {
  private Vertical panel = new Vertical();

  public TableDemo() {
    String dataUrl = "http://spreadsheets.google.com/tq?key=prll1aQH05yQqp_DKPP9TNg&pub=1";
    Query query = Query.create(dataUrl);

    query.send(new Callback() {
      public void onResponse(QueryResponse response) {
        if (response.isError()) {
          Window.alert("Error in query: " + response.getMessage() + ' '
              + response.getDetailedMessage());
          return;
        }

        Table viz = new Table();
        Table.Options options = Table.Options.create();
        options.setShowRowNumber(true);
        viz.draw(response.getDataTable(), options);
        BeeLabel status = new BeeLabel();
        viz.addSelectHandler(new SelectionDemo(viz, status));
        panel.add(viz);
        panel.add(status);
      }
    });
  }

  public Widget getWidget() {
    return panel;
  }
}
