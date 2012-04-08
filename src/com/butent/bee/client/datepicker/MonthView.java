package com.butent.bee.client.datepicker;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.impl.ElementMapperImpl;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.datepicker.DatePicker.CssClasses;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.utils.StringList;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.List;

public class MonthView extends Component {

  public class DayGrid extends Grid {

    public class Cell extends UIObject implements HasEnabled {

      private final int index;
      private final JustDate value = new JustDate();

      private final String cellStyle;
      private String dateStyle = null;

      private boolean enabled = true;

      public Cell(int index, Element elem, boolean isWeekend) {
        this.index = index;
        this.cellStyle =
            isWeekend ? StyleUtils.buildClasses(css().day(), css().dayIsWeekend()) : css().day();

        setElement(elem);
      }

      @Override
      public void addStyleName(String styleName) {
        Assert.notNull(styleName);
        setDateStyle(StyleUtils.addClassName(getDateStyle(), styleName));
        updateStyle();
      }

      public boolean isEnabled() {
        return enabled;
      }

      @Override
      public void removeStyleName(String styleName) {
        Assert.notEmpty(styleName);
        setDateStyle(StyleUtils.removeClassName(getDateStyle(), styleName));
        updateStyle();
      }

      public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        onEnabled();
      }

      private String getDateStyle() {
        return dateStyle;
      }

      private boolean isFiller() {
        return !getModel().isInCurrentMonth(value);
      }

      private void onActivate(boolean activate) {
        if (activate) {
          setHighlightedDate(value);
        }
        updateStyle();
      }

      private void onEnabled() {
        updateStyle();
      }

      private void onSelected(boolean selected) {
        if (selected) {
          if (isFiller()) {
            getDatePicker().setDate(value, true);
          } else {
            getDatePicker().setValue(value, true);
          }
        }
        updateStyle();
      }

      private void setDateStyle(String dateStyle) {
        this.dateStyle = dateStyle;
      }

      private void setText(String value) {
        DOM.setInnerText(getElement(), value);
      }

      private void update(JustDate date) {
        setEnabled(true);

        value.setDate(date);
        setText(Model.formatDayOfMonth(value));
        
        StringList styles = StringList.uniqueCaseInsensitive();
        styles.add(cellStyle);

        if (isFiller()) {
          styles.add(css().dayIsFiller());
        } else {
          String extraStyle = getDatePicker().getStyleOfDate(date);
          if (extraStyle != null) {
            styles.add(extraStyle);
          }
        }
        
        setDateStyle(StyleUtils.buildClasses(styles));
        updateStyle();
      }

      private void updateStyle() {
        StringList styles = StringList.uniqueCaseInsensitive();
        styles.add(dateStyle);

        if (getActiveCellIndex() == index) {
          styles.add(css().dayIsHighlighted());
          if (getSelectedIndex() == index) {
            styles.add(css().dayIsValueAndHighlighted());
          }
        }
        if (!isEnabled()) {
          styles.add(css().dayIsDisabled());
        }

        setStyleName(StyleUtils.buildClasses(styles));
      }
    }

    private final ElementMapperImpl<Cell> elementToCell = new ElementMapperImpl<Cell>();
    private final List<Cell> cellList = Lists.newArrayList();

    private int activeCellIndex = BeeConst.UNDEF;
    private int selectedIndex = BeeConst.UNDEF;

    public DayGrid() {
      sinkEvents(Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT);

      resize(Model.WEEKS_IN_MONTH + 1, Model.DAYS_IN_WEEK);

      CellFormatter formatter = getCellFormatter();

      for (int i = 0; i < Model.DAYS_IN_WEEK; i++) {
        setText(0, i, Model.formatDayOfWeek(i));

        if (Model.isWeekend(i + 1)) {
          formatter.setStyleName(0, i, css().weekendLabel());
        } else {
          formatter.setStyleName(0, i, css().weekdayLabel());
        }
      }
      
      int index = 0;
      for (int row = 1; row <= Model.WEEKS_IN_MONTH; row++) {
        for (int column = 0; column < Model.DAYS_IN_WEEK; column++) {
          Element element = formatter.getElement(row, column);
          Cell cell = new Cell(index++, element, Model.isWeekend(column));

          cellList.add(cell);
          elementToCell.put(cell);
        }
      }
    }

    @Override
    public void onBrowserEvent(Event event) {
      switch (DOM.eventGetType(event)) {
        case Event.ONCLICK: {
          Cell cell = getCell(event);
          if (isActive(cell)) {
            setSelected(cell);
          }
          break;
        }

        case Event.ONMOUSEOUT: {
          Element e = DOM.eventGetFromElement(event);
          if (e != null) {
            Cell cell = elementToCell.get(e);
            if (cell != null && cell.index == getActiveCellIndex()) {
              activateCell(cell, false);
            }
          }
          break;
        }

        case Event.ONMOUSEOVER: {
          Element e = DOM.eventGetToElement(event);
          if (e != null) {
            Cell cell = elementToCell.get(e);
            if (isActive(cell)) {
              activateCell(cell, true);
            }
          }
          break;
        }
      }
    }

    @Override
    protected void onUnload() {
      setActiveCellIndex(BeeConst.UNDEF);
    }

    private void activateCell(Cell cell, boolean activate) {
      if (activate && cell.index == getActiveCellIndex()) {
        return;
      }
      setActiveCellIndex(activate ? cell.index : BeeConst.UNDEF); 
      cell.onActivate(activate);
    }

    private int getActiveCellIndex() {
      return activeCellIndex;
    }

    private Cell getCell(Event event) {
      Element td = getEventTargetCell(event);
      return td != null ? elementToCell.get(td) : null;
    }

    private Cell getCell(int index) {
      return cellList.get(index);
    }

    private int getNumCells() {
      return cellList.size();
    }

    private Cell getSelectedCell() {
      return (getSelectedIndex() >= 0) ? getCell(getSelectedIndex()) : null;
    }

    private int getSelectedIndex() {
      return selectedIndex;
    }

    private boolean isActive(Cell cell) {
      return cell != null && cell.isEnabled();
    }

    private void setActiveCellIndex(int activeCellIndex) {
      this.activeCellIndex = activeCellIndex;
    }

    private void setSelected(Cell cell) {
      if (cell.index == getSelectedIndex()) {
        return;
      }
      
      Cell last = getSelectedCell();
      if (last != null) {
        last.onSelected(false);
      }
      
      setSelectedIndex(cell.index);
      cell.onSelected(true);
    }

    private void setSelectedIndex(int selectedIndex) {
      this.selectedIndex = selectedIndex;
    }
  }

  private final CssClasses cssClasses;
  private final DayGrid grid;

  private final JustDate firstDisplayed = new JustDate();
  private final JustDate lastDisplayed = new JustDate();

  public MonthView(CssClasses cssClasses) {
    this.cssClasses = cssClasses;
    this.grid = new DayGrid();
  }

  public void addStyleToDate(String styleName, JustDate date) {
    Assert.state(getDatePicker().isDateVisible(date));
    getCell(date).addStyleName(styleName);
  }

  public JustDate getFirstDate() {
    return firstDisplayed;
  }

  public JustDate getLastDate() {
    return lastDisplayed;
  }

  public boolean isDateEnabled(JustDate date) {
    Assert.state(getDatePicker().isDateVisible(date));
    return getCell(date).isEnabled();
  }

  @Override
  public void refresh() {
    JustDate start = getModel().getCurrentMonth();
    int incr = (start.getDow() == 1) ? -1 : 0;
    firstDisplayed.setDate(TimeUtils.startOfWeek(start, incr));

    int days = firstDisplayed.getDays();
    for (int i = 0; i < grid.getNumCells(); i++) {
      lastDisplayed.setDays(days + i);
      grid.getCell(i).update(lastDisplayed);
    }
  }

  public void removeStyleFromDate(String styleName, JustDate date) {
    if (getDatePicker().isDateVisible(date)) {
      getCell(date).removeStyleName(styleName);
    }
  }

  public void setEnabledOnDate(boolean enabled, JustDate date) {
    Assert.state(getDatePicker().isDateVisible(date));
    getCell(date).setEnabled(enabled);
  }

  @Override
  public void setup() {
    initWidget(grid);
    grid.setStyleName(css().days());
  }

  private CssClasses css() {
    return cssClasses;
  }
 
  private DayGrid.Cell getCell(JustDate date) {
    int index = date.getDays() - firstDisplayed.getDays();
    DayGrid.Cell cell = grid.getCell(index);
    Assert.state(cell.value.equals(date));
    return cell;
  }

  private void setHighlightedDate(JustDate date) {
    getDatePicker().setHighlightedDate(date);
  }
}
