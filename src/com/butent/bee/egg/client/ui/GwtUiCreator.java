package com.butent.bee.egg.client.ui;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.menu.BeeMenuBar;
import com.butent.bee.egg.client.menu.BeeMenuItem;
import com.butent.bee.egg.client.menu.BeeMenuItemSeparator;
import com.butent.bee.egg.client.menu.MenuCommand;
import com.butent.bee.egg.client.widget.BeeButton;
import com.butent.bee.egg.client.widget.BeeLabel;
import com.butent.bee.egg.client.widget.BeeTextBox;
import com.butent.bee.egg.shared.ui.UiButton;
import com.butent.bee.egg.shared.ui.UiComponent;
import com.butent.bee.egg.shared.ui.UiCreator;
import com.butent.bee.egg.shared.ui.UiField;
import com.butent.bee.egg.shared.ui.UiHorizontalLayout;
import com.butent.bee.egg.shared.ui.UiLabel;
import com.butent.bee.egg.shared.ui.UiMenuHorizontal;
import com.butent.bee.egg.shared.ui.UiMenuVertical;
import com.butent.bee.egg.shared.ui.UiPanel;
import com.butent.bee.egg.shared.ui.UiVerticalLayout;
import com.butent.bee.egg.shared.ui.UiWindow;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.logging.Logger;

public class GwtUiCreator implements UiCreator {

  private static Logger logger = Logger.getLogger(GwtUiCreator.class.getName());

  @Override
  public Object createButton(UiButton button) {
    BeeButton b = new BeeButton();
    b.setText(button.getProperty("caption"));
    b.setTitle(button.getId());

    String svc = button.getProperty("service");
    if (!BeeUtils.isEmpty(svc)) {
      b.setService(svc);
      BeeKeeper.getBus().addClickHandler(b);
    }

    createChilds(b, button);

    return b;
  }

  @Override
  public Object createField(UiField field) {
    Panel p = new HorizontalPanel();
    BeeLabel l = new BeeLabel();
    l.setText(field.getProperty("caption"));

    BeeTextBox f = new BeeTextBox();
    f.setTitle(field.getId());

    p.add(l);
    p.add(f);

    createChilds(f, field);

    return p;
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
    l.setText(label.getProperty("caption"));
    l.setTitle(label.getId());

    createChilds(l, label);

    return l;
  }

  @Override
  public Object createMenuHorizontal(UiMenuHorizontal menuHorizontal) {
    BeeMenuBar rw = new BeeMenuBar();

    createMenuItems(rw, menuHorizontal);

    return rw;
  }

  @Override
  public Object createMenuVertical(UiMenuVertical menuVertical) {
    BeeMenuBar rw = new BeeMenuBar(true);

    createMenuItems(rw, menuVertical);

    return rw;
  }

  @Override
  public Object createPanel(UiPanel panel) {
    Panel p = new FlowPanel();
    p.setTitle(panel.getId());

    createChilds(p, panel);

    return p;
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
    Panel w = new FlowPanel();
    w.setTitle(window.getId());

    createChilds(w, window);

    return w;
  }

  private void createChilds(Widget cc, UiComponent u) {
    if (u.hasChilds()) {
      if (cc instanceof HasWidgets) {
        for (UiComponent c : u.getChilds()) {
          Object o = c.createInstance(this);
          ((HasWidgets) cc).add((Widget) o);
        }
      } else {
        logger.severe("Class " + cc.getClass().getName()
            + " does not support child objects");
      }
    }
  }

  private void createMenuItems(BeeMenuBar cc, UiComponent u) {
    if (u.hasChilds()) {
      for (UiComponent c : u.getChilds()) {
        String txt = c.getProperty("text");
        String sep = c.getProperty("separators");

        if (c.hasChilds()) {
          Object cw = c.createInstance(this);

          if (cw instanceof BeeMenuBar) {
            cc.addItem(new BeeMenuItem(txt, (BeeMenuBar) cw));
          } else {
            logger.severe("Class " + cw.getClass().getName()
                + " cannot be added to " + cc.getClass().getName());
          }
        } else {
          String svc = c.getProperty("service");
          String opt = c.getProperty("parameters");

          cc.addItem(new BeeMenuItem(txt, new MenuCommand(svc, opt)));
        }
        if (!BeeUtils.isEmpty(sep)) {
          cc.addSeparator(new BeeMenuItemSeparator());
        }
      }
    }
  }
}
