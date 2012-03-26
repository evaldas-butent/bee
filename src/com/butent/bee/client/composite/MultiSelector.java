package com.butent.bee.client.composite;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;

import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class MultiSelector extends DialogBox {
  
  public interface SelectionCallback {
    void onSelection(List<IsRow> rows);
  }

  private final SelectionCallback selectionCallback; 

  private GridPresenter presenter = null;
  
  public MultiSelector(String caption, BeeRowSet rowSet, List<String> columnNames,
      SelectionCallback selectionCallback) {
    super(caption);
    Assert.notNull(rowSet);
    Assert.isTrue(!rowSet.isEmpty());
    Assert.notEmpty(columnNames);
    Assert.notNull(selectionCallback);

    this.selectionCallback = selectionCallback;
    
    GridPresenter gp = new GridPresenter(rowSet.getViewName(), rowSet.getNumberOfRows(),
        rowSet, false, createGridDescription(rowSet, columnNames), null, null,
        EnumSet.of(UiOption.SELECTOR));
    setPresenter(gp);
    
    BeeButton button = new BeeButton("Pasirinkti");
    button.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        select();
      }
    });
    
    int width = gp.getView().getContent().getGrid().getBodyWidth();
    int height = gp.getView().getContent().getGrid().getChildrenHeight();
    
    width = BeeUtils.limit(width + DomUtils.getScrollBarWidth() + 2,
        100, Window.getClientWidth() * 3 / 4);
    height = BeeUtils.limit(height + 60 + DomUtils.getScrollBarHeight() + 2,
        100, Window.getClientHeight() * 3 / 4);

    Split panel = new Split(0);
    StyleUtils.setWidth(panel, width);
    StyleUtils.setHeight(panel, height);
    
    Flow footer = new Flow();
    footer.addStyleName(StyleUtils.NAME_FLEX_BOX_CENTER);
    footer.add(button);

    panel.addSouth(footer, 30);
    panel.add(gp.getWidget());
    
    setWidget(panel);
  }

  @Override
  public String getIdPrefix() {
    return "multi";
  }

  private GridDescription createGridDescription(BeeRowSet rowSet, List<String> columnNames) {
    String viewName = rowSet.getViewName();
    GridDescription gridDescription = new GridDescription(viewName, viewName, null, null);
    gridDescription.setReadOnly(true);

    gridDescription.setHasHeaders(false);
    gridDescription.setHasFooters(true);

    gridDescription.setSearchThreshold(DataUtils.getDefaultSearchThreshold());

    gridDescription.addColumn(new ColumnDescription(ColType.SELECTION,
        BeeUtils.createUniqueName("select-")));

    for (String colName : columnNames) {
      ColumnDescription columnDescription = new ColumnDescription(ColType.DATA, colName);
      columnDescription.setSource(colName);
      columnDescription.setSortable(true);
      columnDescription.setHasFooter(true);

      gridDescription.addColumn(columnDescription);
    }
    return gridDescription;
  }

  private GridPresenter getPresenter() {
    return presenter;
  }

  private SelectionCallback getSelectionCallback() {
    return selectionCallback;
  }

  private void select() {
    Collection<RowInfo> selectedRows = getPresenter().getView().getContent().getSelectedRows();
    if (selectedRows.isEmpty()) {
      return;
    }

    List<IsRow> result = Lists.newArrayList();
    for (RowInfo rowInfo : selectedRows) {
      IsRow row = getPresenter().getView().getContent().getGrid().getRowById(rowInfo.getId());
      if (row != null) {
        result.add(row);
      }
    }
    
    getSelectionCallback().onSelection(result);
    hide();
  }
  
  private void setPresenter(GridPresenter presenter) {
    this.presenter = presenter;
  }
}
