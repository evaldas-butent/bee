package com.butent.bee.client.modules.transport.charts;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Selectors;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.transport.charts.ChartData.Item;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

class FilterHelper {
  
  abstract static class DialogCallback extends Callback<DialogBox> {
  }

  private static final String STYLE_PREFIX = "bee-tr-chart-filter-";

  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog";

  private static final String STYLE_CONTAINER = STYLE_PREFIX + "container";
  private static final String STYLE_DATA_WRAPPER = STYLE_PREFIX + "dataWrapper";
  private static final String STYLE_PANEL = STYLE_PREFIX + "panel";
  private static final String STYLE_CAPTION = STYLE_PREFIX + "caption";

  private static final String STYLE_TABLE_WRAPPER = STYLE_PREFIX + "tableWrapper";
  private static final String STYLE_TABLE = STYLE_PREFIX + "table";
  private static final String STYLE_SELECTED = STYLE_PREFIX + "selected";

  private static final String STYLE_COMMAND_GROUP = STYLE_PREFIX + "commandGroup";
  private static final String STYLE_COMMAND_CLEAR = STYLE_PREFIX + "commandClear";
  private static final String STYLE_COMMAND_FILTER = STYLE_PREFIX + "commandFilter";
  
  static void openDialog(final List<ChartData> data, final DialogCallback callback) {
    boolean ok = false;
    for (ChartData cd : data) {
      if (cd.size() > 1) {
        ok = true;
        break;
      }
    }

    if (!ok) {
      BeeKeeper.getScreen().notifyWarning("Nėra ką filtruoti");
      return;
    }
    
    final Flow dataWrapper = new Flow();
    dataWrapper.addStyleName(STYLE_DATA_WRAPPER);
    
    for (ChartData cd : data) {
      if (cd.isEmpty()) {
        continue;
      }
      
      final HtmlTable table = new HtmlTable();
      table.addStyleName(STYLE_TABLE);
      
      final List<Item> items = cd.getList();
      int row = 0;
      
      for (Item item : items) {
        table.setText(row, 0, item.getName());
        
        if (item.isSelected()) {
          table.getRowFormatter().addStyleName(row, STYLE_SELECTED);
        }
        
        row++;
      }

      Flow panel = new Flow();
      panel.addStyleName(STYLE_PANEL);

      BeeLabel caption = new BeeLabel(cd.getType().getCaption());
      caption.addStyleName(STYLE_CAPTION);
      
      caption.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          invertSelection(items, table);
        }
      });
      
      panel.add(caption);
      
      Binder.addClickHandler(table, new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Element target = EventUtils.getEventTargetElement(event);
          TableRowElement rowElement = DomUtils.getParentRow(target, true);

          if (rowElement != null && table.getElement().isOrHasChild(rowElement)) {
            int index = rowElement.getRowIndex();
            
            if (BeeUtils.isIndex(items, index)) {
              Item item = items.get(index);
              if (item.isSelected()) {
                rowElement.removeClassName(STYLE_SELECTED);
              } else {
                rowElement.addClassName(STYLE_SELECTED);
              }
              
              item.invert();
            }
          }
        }
      });
      
      Simple wrapper = new Simple(table);
      wrapper.addStyleName(STYLE_TABLE_WRAPPER);
      
      panel.add(wrapper);
      
      dataWrapper.add(panel);
    }

    Flow container = new Flow();
    container.addStyleName(STYLE_CONTAINER);
    
    container.add(dataWrapper);

    Flow commands = new Flow();
    commands.addStyleName(STYLE_COMMAND_GROUP);

    BeeButton clear = new BeeButton("Išvalyti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        for (ChartData cd : data) {
          cd.setSelected(false);
        }
        
        NodeList<Element> nodes = 
            Selectors.getNodes(dataWrapper, Selectors.classSelector(STYLE_SELECTED));
        if (nodes != null) {
          StyleUtils.removeClassName(nodes, STYLE_SELECTED);
        }
      }
    });
    clear.addStyleName(STYLE_COMMAND_CLEAR);
    commands.add(clear);

    final DialogBox dialog = DialogBox.create("Filtras", STYLE_DIALOG);
    
    BeeButton filter = new BeeButton("Filtruoti", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        callback.onSuccess(dialog);
      }
    });
    filter.addStyleName(STYLE_COMMAND_FILTER);
    commands.add(filter);
    
    container.add(commands);
    
    dialog.setWidget(container);
    
    dialog.setHideOnEscape(true);
    dialog.setAnimationEnabled(true);
    
    dialog.center();
    filter.setFocus(true);
  }
  
  private static void invertSelection(Collection<Item> items, HtmlTable table) {
    int row = 0;
    
    for (Item item : items) {
      boolean selected = !item.isSelected();
      
      item.setSelected(selected);
      table.getRowFormatter().setStyleName(row, STYLE_SELECTED, selected);

      row++;
    }
  }
}
