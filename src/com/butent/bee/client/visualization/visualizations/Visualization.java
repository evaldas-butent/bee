package com.butent.bee.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.visualization.AbstractDataTable;
import com.butent.bee.client.visualization.AbstractDrawOptions;
import com.butent.bee.shared.Assert;

/**
 * Is an abstract class for all visualizations in this package, contains methods structure for
 * successful management of a particular visualization.
 */

public abstract class Visualization<OptionsType extends AbstractDrawOptions> extends Widget
    implements IdentifiableWidget {
  private AbstractDataTable dataTable;
  private OptionsType options;
  private JavaScriptObject jso;

  public Visualization() {
    Element div = DOM.createDiv();
    jso = createJso(div);
    setElement(div);
    setStyleName("viz-container");
    DomUtils.createId(this, getIdPrefix());
  }

  public Visualization(AbstractDataTable data, OptionsType options) {
    this();
    this.options = options;
    this.dataTable = data;
  }

  public final native void draw(AbstractDataTable data) /*-{
    this.@com.butent.bee.client.visualization.visualizations.Visualization::jso.draw(data, {});
  }-*/;

  public final native void draw(AbstractDataTable data, OptionsType opt) /*-{
    this.@com.butent.bee.client.visualization.visualizations.Visualization::jso.draw(data, opt);
  }-*/;

  public AbstractDataTable getDataTable() {
    return dataTable;
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "viz";
  }
  
  public JavaScriptObject getJso() {
    return jso;
  }

  public OptionsType getOptions() {
    return options;
  }

  public void refresh() {
    draw();
  }

  public void setDataTable(AbstractDataTable dataTable) {
    this.dataTable = dataTable;
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setOptions(OptionsType options) {
    this.options = options;
  }

  public void updateDataTable(AbstractDataTable table) {
    Assert.notNull(table);
    setDataTable(table);
    draw();
  }

  public void updateOptions(OptionsType opt) {
    setOptions(opt);
    draw();
  }

  protected abstract JavaScriptObject createJso(Element div);

  @Override
  protected void onLoad() {
    draw();
  }

  private void draw() {
    if (dataTable != null) {
      if (options == null) {
        draw(dataTable);
      } else {
        draw(dataTable, options);
      }
    }
  }
}
