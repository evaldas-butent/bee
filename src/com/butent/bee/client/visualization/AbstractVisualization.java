package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * is an abstract class and describes such necessary processes for visualizations like creation,
 * registration and drawing.
 */

public abstract class AbstractVisualization<E extends AbstractDrawOptions> extends Composite {

  /**
   * Requires to have {@code create} method.
   */

  public interface VisualizationFactory {
    AbstractVisualization<?> create();
  }

  public static final native void registerVisualization(String name, VisualizationFactory factory) /*-{
    $wnd[name] = function(container) {
      this.gwt_vis = @com.butent.bee.client.visualization.AbstractVisualization::createVisualization(Lcom/butent/bee/client/visualization/AbstractVisualization$VisualizationFactory;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/user/client/Element;)(factory, this, container);
    }
    // TODO gwt compiler warning
    //    $wnd[name].prototype.draw = function(data, options) {
    //      this.gwt_vis.@com.butent.bee.client.visualization.AbstractVisualization::draw(Lcom/butent/bee/client/visualization/AbstractDataTable;Lcom/google/gwt/visualization/client/AbstractDrawOptions;)(data, options);
    //    }
  }-*/;

  private static AbstractVisualization<?> createVisualization(VisualizationFactory factory,
      JavaScriptObject jsVisualization, Element container) {
    AbstractVisualization<?> visualization = factory.create();
    visualization.jsVisualization = jsVisualization;

    RootPanel.get(container.getId()).add(visualization);

    if (visualization instanceof Selectable) {
      registerSelectFunctions(jsVisualization);
    }
    return visualization;
  }

  private static native void fireSelectionEvent(JavaScriptObject jso) /*-{
    $wnd.google.visualization.events.trigger(jso, 'select', null);
  }-*/;

  private static native void registerSelectFunctions(JavaScriptObject jso) /*-{
    jso.getSelection = function() {
      return this.gwt_vis.@com.butent.bee.client.visualization.Selectable::getSelections()();
    }
    jso.setSelection = function(selection) {
      this.gwt_vis.@com.butent.bee.client.visualization.Selectable::setSelections(Lcom/google/gwt/core/client/JsArray;)(selection);
    }
  }-*/;

  protected JavaScriptObject jsVisualization;

  public abstract void draw(AbstractDataTable data, E options);

  public void fireSelectionEvent() {
    fireSelectionEvent(this.jsVisualization);
  }

  public JavaScriptObject getJso() {
    return jsVisualization;
  }
}
