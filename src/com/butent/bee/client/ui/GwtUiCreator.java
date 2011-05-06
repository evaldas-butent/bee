package com.butent.bee.client.ui;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.layout.Stack;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.menu.MenuBar;
import com.butent.bee.client.menu.MenuCommand;
import com.butent.bee.client.menu.MenuSeparator;
import com.butent.bee.client.tree.BeeTree;
import com.butent.bee.client.tree.BeeTreeItem;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeCheckBox;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.BeeRadioButton;
import com.butent.bee.client.widget.BeeTextArea;
import com.butent.bee.client.widget.BeeTextBox;
import com.butent.bee.shared.ui.UiButton;
import com.butent.bee.shared.ui.UiCheckBox;
import com.butent.bee.shared.ui.UiComponent;
import com.butent.bee.shared.ui.UiCreator;
import com.butent.bee.shared.ui.UiField;
import com.butent.bee.shared.ui.UiGrid;
import com.butent.bee.shared.ui.UiHorizontalLayout;
import com.butent.bee.shared.ui.UiLabel;
import com.butent.bee.shared.ui.UiListBox;
import com.butent.bee.shared.ui.UiMenuHorizontal;
import com.butent.bee.shared.ui.UiMenuVertical;
import com.butent.bee.shared.ui.UiPanel;
import com.butent.bee.shared.ui.UiRadioButton;
import com.butent.bee.shared.ui.UiStack;
import com.butent.bee.shared.ui.UiTab;
import com.butent.bee.shared.ui.UiTextArea;
import com.butent.bee.shared.ui.UiTree;
import com.butent.bee.shared.ui.UiVerticalLayout;
import com.butent.bee.shared.ui.UiWindow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * Implements {@code UiCreator} interface and contains methods creating various user interface
 * components like button, label, checkbox etc.
 */

public class GwtUiCreator implements UiCreator {

  private static Logger logger = Logger.getLogger(GwtUiCreator.class.getName());

  @Override
  public Object createButton(UiButton button) {
    BeeButton b = new BeeButton();
    b.setText(button.getCaption());
    b.setTitle(button.getId());

    String svc = button.getProperty("click_proc");
    if (!BeeUtils.isEmpty(svc)) {
      b.setService(svc);
      BeeKeeper.getBus().addClickHandler(b);
    }

    createChilds(b, button);

    return b;
  }

  @Override
  public Object createCheckBox(UiCheckBox checkBox) {
    BeeCheckBox widget = new BeeCheckBox();
    widget.setText(checkBox.getCaption());
    widget.setTitle(checkBox.getId());

    createChilds(widget, checkBox);

    return widget;
  }

  @Override
  public Object createField(UiField field) {
    BeeLayoutPanel widget = new BeeLayoutPanel();
    BeeLabel label = new BeeLabel();
    label.setText(field.getCaption());

    BeeTextBox input = new BeeTextBox();
    input.setTitle(field.getId());

    widget.add(label);
    widget.add(input);

    String prp = field.getProperty("parameters");
    if (!BeeUtils.isEmpty(prp)) {
      prp = prp.replaceFirst("[\\s,]+", "");
    }
    int pos = getUnitValue(prp);

    widget.setWidgetLeftWidth(label, 0, getUnit(null), pos, getUnit(null));
    widget.setWidgetLeftRight(input, pos, getUnit(null), 0, getUnit(null));

    return widget;
  }

  @Override
  public Object createGrid(UiGrid uiGrid) {
    Split widget = new Split();
    widget.setTitle(uiGrid.getId());

    if (!BeeUtils.isEmpty(uiGrid.getCaption())) {
      BeeLabel label = new BeeLabel();
      label.setText(uiGrid.getCaption());
      widget.addNorth(label, 20);
    }

    CompositeService.doService(GridService.NAME, "stage_dummy", widget,
        uiGrid.getProperty("parameters"));

    return widget;
  }

  @Override
  public Object createHorizontalLayout(UiHorizontalLayout layout) {
    HorizontalPanel h = new HorizontalPanel();
    h.setTitle(layout.getId());
    h.setBorderWidth(1);

    createChilds(h, layout);

    return h;
  }

  @Override
  public Object createLabel(UiLabel label) {
    BeeLabel l = new BeeLabel();
    l.setText(label.getCaption());
    l.setTitle(label.getId());

    createChilds(l, label);

    return l;
  }

  @Override
  public Object createListBox(UiListBox listBox) {
    BeeListBox widget = new BeeListBox();
    widget.setTitle(listBox.getId());

    if (listBox.hasChilds()) {
      for (UiComponent child : listBox.getChilds()) {
        widget.addItem(child.getCaption());
      }
    }
    if (BeeUtils.isEmpty(listBox.getProperty("singleVisible"))) {
      widget.setAllVisible();
    }
    return widget;
  }

  @Override
  public Object createMenuHorizontal(UiMenuHorizontal menuHorizontal) {
    MenuBar widget = new MenuBar(
        BeeUtils.isEmpty(menuHorizontal.getParent()) ? 0 : 1);
    widget.setTitle(menuHorizontal.getId());

    createMenuItems(widget, menuHorizontal.getChilds());

    return widget;
  }

  @Override
  public Object createMenuVertical(UiMenuVertical menuVertical) {
    MenuBar widget = new MenuBar(
        BeeUtils.isEmpty(menuVertical.getParent()) ? 0 : 1, true);
    widget.setTitle(menuVertical.getId());

    createMenuItems(widget, menuVertical.getChilds());

    return widget;
  }

  @Override
  public Object createPanel(UiPanel panel) {
    Panel p = new FlowPanel();
    p.setTitle(panel.getId());

    createChilds(p, panel);

    return p;
  }

  @Override
  public Object createRadioButton(UiRadioButton radioButton) {
    Panel widget = null;
    if ("[H]".equals(radioButton.getProperty("opt_layout"))) {
      widget = new Horizontal();
    } else {
      widget = new Vertical();
    }
    String id = radioButton.getId();
    widget.setTitle(id);

    for (String r : BeeUtils.split(radioButton.getProperty("parameters"), ",")) {
      widget.add(new BeeRadioButton(id,
          r.replaceFirst("[\"'\\[]", "").replaceFirst("[|\"'\\]].*", "")));
    }
    return widget;
  }

  @Override
  public Object createStack(UiStack stack) {
    Stack widget = new Stack(Unit.EM);
    widget.setTitle(stack.getId());

    if (stack.hasChilds()) {
      for (UiComponent child : stack.getChilds()) {
        Object childWidget = child.createInstance(this);

        if (childWidget instanceof Widget) {
          widget.add((Widget) childWidget, child.getCaption(), 2);
        } else {
          logger.severe("Class " + childWidget.getClass().getName()
              + " cannot be added to " + widget.getClass().getName());
        }
      }
    }
    return widget;
  }

  @Override
  public Object createTab(UiTab tab) {
    TabbedPages widget = new TabbedPages(20, Unit.PX);
    widget.setTitle(tab.getId());

    if (tab.hasChilds()) {
      for (UiComponent child : tab.getChilds()) {
        Object childWidget = child.createInstance(this);

        if (childWidget instanceof Widget) {
          widget.add((Widget) childWidget, child.getCaption());
        } else {
          logger.severe("Class " + childWidget.getClass().getName()
              + " cannot be added to " + widget.getClass().getName());
        }
      }
    }
    return widget;
  }

  @Override
  public Object createTextArea(UiTextArea textArea) {
    BeeTextArea widget = new BeeTextArea();
    widget.setText(textArea.getCaption());
    widget.setTitle(textArea.getId());

    createChilds(widget, textArea);

    return widget;
  }

  @Override
  public Object createTree(UiTree tree) {
    BeeTree widget = new BeeTree();
    widget.setTitle(tree.getId());

    if (tree.hasChilds()) {
      for (UiComponent child : tree.getChilds()) {
        BeeTreeItem item = new BeeTreeItem(child.getCaption());

        if (child.hasChilds()) {
          Object childWidget = child.createInstance(this);

          if (childWidget instanceof Widget) {
            item.addItem((Widget) childWidget);
          } else {
            logger.severe("Class " + childWidget.getClass().getName()
                + " cannot be added to " + widget.getClass().getName());
          }
        }
        widget.addItem(item);
      }
    }
    return widget;
  }

  @Override
  public Object createVerticalLayout(UiVerticalLayout layout) {
    VerticalPanel v = new VerticalPanel();
    v.setTitle(layout.getId());
    v.setBorderWidth(1);

    createChilds(v, layout);

    return v;
  }

  @Override
  public Object createWindow(UiWindow window) {
    BeeLayoutPanel w = new BeeLayoutPanel();
    w.setTitle(window.getId());

    createChilds(w, window);

    return w;
  }

  private void createChilds(Widget parentWidget, UiComponent parent) {
    if (parent.hasChilds()) {
      if (parentWidget instanceof HasWidgets) {
        for (UiComponent child : parent.getChilds()) {
          Widget childWidget = (Widget) child.createInstance(this);

          ((HasWidgets) parentWidget).add(childWidget);

          if (parentWidget instanceof LayoutPanel) {
            setLayout((LayoutPanel) parentWidget, childWidget, child);
          }
        }
      } else {
        logger.severe("Class " + parentWidget.getClass().getName()
            + " does not support child objects");
      }
    }
  }

  private void createMenuItems(MenuBar menu, Collection<UiComponent> childs) {
    if (!BeeUtils.isEmpty(childs)) {
      for (UiComponent child : childs) {
        String txt = child.getCaption();
        String sep = child.getProperty("separators");

        if (!BeeUtils.isEmpty(sep)) {
          menu.addSeparator(new MenuSeparator());
        }
        if (child.hasChilds()) {
          Object childWidget = child.createInstance(this);

          if (childWidget instanceof MenuBar) {
            menu.addItem(txt, (MenuBar) childWidget);
          } else {
            logger.severe("Class " + childWidget.getClass().getName()
                + " cannot be added to " + menu.getClass().getName());
          }
        } else {
          String svc = child.getProperty("service");
          String opt = child.getProperty("parameters");

          menu.addItem(txt, new MenuCommand(svc, opt));
        }
      }
    }
  }

  private int getBottom(UiComponent u) {
    String bottom = u.getProperty("bottom");
    return getUnitValue(bottom);
  }

  private Unit getBottomUnit(UiComponent u) {
    Unit bottomUnit = getUnit(u.getProperty("bottom"));
    return bottomUnit;
  }

  private int getHeight(UiComponent u) {
    String height = u.getProperty("height");
    return getUnitValue(height);
  }

  private Unit getHeightUnit(UiComponent u) {
    Unit heightUnit = getUnit(u.getProperty("height"));
    return heightUnit;
  }

  private int getLeft(UiComponent u) {
    String left = u.getProperty("left");
    return getUnitValue(left);
  }

  private Unit getLeftUnit(UiComponent u) {
    Unit leftUnit = getUnit(u.getProperty("left"));
    return leftUnit;
  }

  private int getRight(UiComponent u) {
    String right = u.getProperty("right");
    return getUnitValue(right);
  }

  private Unit getRightUnit(UiComponent u) {
    Unit rightUnit = getUnit(u.getProperty("right"));
    return rightUnit;
  }

  private int getTop(UiComponent u) {
    String top = u.getProperty("top");
    return getUnitValue(top);
  }

  private Unit getTopUnit(UiComponent u) {
    Unit topUnit = getUnit(u.getProperty("top"));
    return topUnit;
  }

  private Unit getUnit(String measure) {
    if (!BeeUtils.isEmpty(measure)) {
      String value = measure.replaceFirst("^\\d+", "");

      if (!BeeUtils.isEmpty(value)) {
        for (Unit unit : Unit.values()) {
          if (value.equals(unit.getType())) {
            return unit;
          }
        }
        logger.warning("Unknown measure unit: " + measure);
      }
    }
    return Unit.PX;
  }

  private int getUnitValue(String measure) {
    String value = null;

    if (!BeeUtils.isEmpty(measure)) {
      value = measure.replaceFirst("\\D.*", "");
    }
    return BeeUtils.toInt(value);
  }

  private int getWidth(UiComponent u) {
    String width = u.getProperty("width");
    return getUnitValue(width);
  }

  private Unit getWidthUnit(UiComponent u) {
    Unit widthUnit = getUnit(u.getProperty("width"));
    return widthUnit;
  }

  private void setLayout(LayoutPanel widget, Widget childWidget,
      UiComponent child) {
    int left = getLeft(child);
    int right = getRight(child);
    int width = getWidth(child);

    if (BeeUtils.isEmpty(width)) {
      widget.setWidgetLeftRight(childWidget, left, getLeftUnit(child), right,
          getRightUnit(child));
    } else {
      if (BeeUtils.isEmpty(right)) {
        widget.setWidgetLeftWidth(childWidget, left, getLeftUnit(child), width,
            getWidthUnit(child));
      } else {
        widget.setWidgetRightWidth(childWidget, right, getRightUnit(child),
            width, getWidthUnit(child));
      }
    }

    int top = getTop(child);
    int bottom = getBottom(child);
    int height = getHeight(child);

    if (BeeUtils.isEmpty(height)) {
      widget.setWidgetTopBottom(childWidget, top, getTopUnit(child), bottom,
          getBottomUnit(child));
    } else {
      if (BeeUtils.isEmpty(bottom)) {
        widget.setWidgetTopHeight(childWidget, top, getTopUnit(child), height,
            getHeightUnit(child));
      } else {
        widget.setWidgetBottomHeight(childWidget, bottom, getBottomUnit(child),
            height, getHeightUnit(child));
      }
    }
  }
}
