package com.butent.bee.client.grid;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;

public class HeaderCell extends AbstractCell<String> {
  
  interface Template extends SafeHtmlTemplates {
    @Template("<div class=\"bee-HeaderCellCaption\">{0}</div>")
    SafeHtml caption(SafeHtml label);

    @Template("<div class=\"{0}\"></div>")
    SafeHtml sortable(String classes);
    
    @Template("<div class=\"{0}\">{1}</div>")
    SafeHtml sorted(String classes, String sortInfo);
    
    @Template("<div id=\"{0}\" class=\"bee-HeaderCellWidthInfo\">{1}</div>")
    SafeHtml widthInfo(String id, int width);
  }
  
  private static final String STYLE_SORT_INFO = "bee-HeaderCellSortInfo";
  private static final String STYLE_SORTABLE = "bee-HeaderCellSortable";
  private static final String STYLE_ASCENDING = "bee-HeaderCellAscending";
  private static final String STYLE_DESCENDING = "bee-HeaderCellDescending";
  
  private static Template template = null;
  
  private final String widthInfoId;

  public HeaderCell() {
    super(EventUtils.EVENT_TYPE_CLICK);
    if (template == null) {
      template = GWT.create(Template.class);
    }
    widthInfoId = DomUtils.createUniqueId("width-info");
  }
  
  @Override
  public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event,
      ValueUpdater<String> valueUpdater) {
    if (!(context instanceof CellContext)) {
      super.onBrowserEvent(context, parent, value, event, valueUpdater);
      return;
    }

    CellGrid grid = ((CellContext) context).getGrid();
    if (grid == null) {
      return;
    }
    
    if (EventUtils.isClick(event) && !EventUtils.isTargetId(event.getEventTarget(), widthInfoId)) {
      EventUtils.eatEvent(event);
      grid.updateOrder(context.getColumn(), event);
    }
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    if (context instanceof CellContext) {
      renderHeader((CellContext) context, value, sb);
    } else if (value != null) {
      sb.appendEscaped(value);
    }
  }

  public void renderHeader(CellContext context, String label, SafeHtmlBuilder sb) {
    if (label != null) {
      sb.append(template.caption(SafeHtmlUtils.fromString(label)));
    }   

    CellGrid grid = context.getGrid();
    if (grid != null && grid.contains(label)) {

      Order sortOrder = grid.getSortOrder();
      int size = (sortOrder == null) ? 0 : sortOrder.getSize();
      
      if ((grid.getColumnCount() > 1 || size > 0) && grid.isSortable(label)) { 
        int sortIndex = (size > 0) ? sortOrder.getIndex(label) : BeeConst.UNDEF;
        if (sortIndex >= 0) {
          boolean ascending = sortOrder.isAscending(label);
          String classes = StyleUtils.buildClasses(STYLE_SORT_INFO,
              ascending ? STYLE_ASCENDING : STYLE_DESCENDING);
          String sortInfo = (size > 1) ? BeeUtils.toString(sortIndex + 1) : BeeConst.STRING_EMPTY;
          sb.append(template.sorted(classes, sortInfo));
        } else {
          sb.append(template.sortable(StyleUtils.buildClasses(STYLE_SORT_INFO, STYLE_SORTABLE)));
        }
      }
      
      sb.append(template.widthInfo(widthInfoId, grid.getColumnWidth(label)));
    }
  }
}
