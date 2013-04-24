package com.butent.bee.client.visualization.showcase;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates left side tab panel for navigation across visualization demos.
 */

public class LeftTabPanel extends Composite implements IdentifiableWidget {
  /**
   * Requires implementing classes to have a {@code getWidget} method.
   */

  public interface WidgetProvider {
    Widget getWidget();
  }

  private static void setWidget(Simple simple, WidgetProvider provider) {
    simple.clear();
    simple.add(provider.getWidget());
  }

  private final Map<String, WidgetProvider> cogs = new HashMap<String, WidgetProvider>();
  private final Horizontal main = new Horizontal();
  private final BeeListBox left = new BeeListBox();
  private final Simple right = new Simple();

  public LeftTabPanel() {
    initWidget(main);
    main.setBorderSpacing(15);
    main.add(left);
    main.add(right);

    left.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        String name = left.getValue(left.getSelectedIndex());
        setWidget(right, cogs.get(name));
      }
    });
  }

  public void add(final WidgetProvider cog, String title) {
    cogs.put(title, cog);
    left.addItem(title);
  }

  @Override
  public String getId() {
    return main.getId();
  }

  @Override
  public String getIdPrefix() {
    return main.getIdPrefix();
  }

  public void init(String name) {
    left.setAllVisible();
    if (!BeeUtils.isEmpty(name)) {
      left.setValue(name);
      setWidget(right, cogs.get(name));
    }
  }

  @Override
  public void setId(String id) {
    main.setId(id);
  }

  public void setWidget(WidgetProvider provider) {
    setWidget(right, provider);
  }
}
