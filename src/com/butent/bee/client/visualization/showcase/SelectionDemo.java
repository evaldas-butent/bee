package com.butent.bee.client.visualization.showcase;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Label;

import com.butent.bee.client.visualization.Selectable;
import com.butent.bee.client.visualization.Selection;
import com.butent.bee.client.visualization.events.SelectHandler;

class SelectionDemo extends SelectHandler {
  private final Selectable viz;
  private final Label label;

  SelectionDemo(Selectable viz, Label label) {
    this.viz = viz;
    this.label = label;
  }

  @Override
  public void onSelect(SelectEvent event) {
    StringBuffer b = new StringBuffer();
    JsArray<Selection> s = getSelections();
    for (int i = 0; i < s.length(); ++i) {
      if (s.get(i).isCell()) {
        b.append(" cell ");
        b.append(s.get(i).getRow());
        b.append(":");
        b.append(s.get(i).getColumn());
      } else if (s.get(i).isRow()) {
        b.append(" row ");
        b.append(s.get(i).getRow());
      } else {
        b.append(" column ");
        b.append(s.get(i).getColumn());
      }
    }
    label.setText("selection changed " + b.toString()); 
  }

  private JsArray<Selection> getSelections() {
    return viz.getSelections();
  }
}
