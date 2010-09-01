package com.butent.bee.egg.client.grid;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.utils.BeeDuration;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeColumn;
import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;

import com.google.gwt.user.client.ui.Widget;

import com.google.gwt.view.client.ListDataProvider;

public class BeeGrid {

  public BeeGrid() {
  }

  public Widget simpleGrid(final String[] colNames, final String[][] data) {
    final int c = colNames.length;
    final int r = data.length;

    final ListDataProvider<Integer> adapter = new ListDataProvider<Integer>();
    final CellTable<Integer> table = new CellTable<Integer>(r);
    adapter.addDataDisplay(table);

    for (int i = 0; i < r; ++i) {
      adapter.getList().add(i);
    }

    for (int j = 0; j < c; j++) {
      final int k = j;

      table.addColumn(new TextColumn<Integer>() {
        @Override
        public String getValue(Integer row) {
          return data[row][k];
        }
      }, colNames[j]);

    }

    return table;
  }

  public Widget createGrid(final int c, final JsArrayString data) {
    Assert.isTrue(c > 1);
    Assert.notNull(data);

    int len = data.length();
    Assert.isTrue(len >= c * 2);

    final String[] head = new String[c];
    BeeColumn z = new BeeColumn();

    for (int i = 0; i < c; i++) {
      z.deserialize(data.get(i));
      head[i] = z.getName();
    }

    final int r = len / c - 1;

    final String foot = BeeUtils.concat(1, r, '*', c, '=', len - c);

    final ListDataProvider<Integer> adapter = new ListDataProvider<Integer>();
    final CellTable<Integer> table = new CellTable<Integer>(r);
    adapter.addDataDisplay(table);

    BeeDuration dur = new BeeDuration("adapter " + r);
    for (int i = 0; i < r; i++) {
      adapter.getList().add(i);
    }
    BeeKeeper.getLog().finish(dur);

    dur.restart("add cols " + c);
    for (int j = 0; j < c; j++) {
      final int k = j;

      String s = BeeUtils.iif(j == 0, foot, j == c - 1, new BeeDate().toLog(),
          BeeUtils.bracket(j + 1));

      table.addColumn(new TextColumn<Integer>() {
        public String getValue(Integer row) {
          return data.get((row + 1) * c + k);
        }
      }, head[j], s);
    }
    BeeKeeper.getLog().finish(dur);

    return table;
  }
}