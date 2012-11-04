package com.butent.bee.client.visualization.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.ajaxloader.ArrayHelper;
import com.butent.bee.client.visualization.AbstractDataTable;
import com.butent.bee.client.visualization.AbstractDrawOptions;
import com.butent.bee.client.visualization.LegendPosition;

/**
 * Implements image chart visualization.
 */

public class ImageChart extends Visualization<ImageChart.Options> {

  /**
   * Enables using an annotation column in image chart.
   */

  public static class AnnotationColumn extends JavaScriptObject {

    /**
     * Contains a list of possible priorities in image chart.
     */
    public enum Priority {
      LOW, MEDIUM, HIGH
    }

    public static native AnnotationColumn create(int index, int size) /*-{
      return {
        column : index,
        size : size
      };
    }-*/;

    protected AnnotationColumn() {
    }

    public final native void setColor(String color) /*-{
      this.color = color;
    }-*/;

    public final void setDrawFlag(boolean draw) {
      if (draw) {
        setType("flag");
      } else {
        setType("text");
      }
    }

    public final native void setPositionColumn(int index) /*-{
      this.positionColumn = index;
    }-*/;

    public final void setPriority(Priority priority) {
      setPriority(priority.toString().toLowerCase());
    }

    private native void setPriority(String priority) /*-{
      this.priority = priority;
    }-*/;

    private native void setType(String type) /*-{
      this.type = type;
    }-*/;
  }
  /**
   * Sets option values for image chart visualization.
   */
  public static class Options extends AbstractDrawOptions {
    public static Options create() {
      return JavaScriptObject.createObject().cast();
    }

    protected Options() {
    }

    public final native void setAnnotationColumns(JsArray<AnnotationColumn> columns) /*-{
      this.annotationColumns = columns;
    }-*/;

    public final native void setColor(String color) /*-{
      this.color = color;
    }-*/;

    public final native void setColors(JsArrayString colors) /*-{
      this.colors = colors;
    }-*/;

    public final void setColors(String... colors) {
      setColors(ArrayHelper.toJsArrayString(colors));
    }

    public final native void setFill(boolean fill) /*-{
      this.fill = fill;
    }-*/;

    public final native void setHeight(int height) /*-{
      this.height = height;
    }-*/;

    public final void setLegend(LegendPosition position) {
      setLegend(position.toString());
    }

    public final native void setMax(double max) /*-{
      this.max = max;
    }-*/;

    public final native void setMin(double min) /*-{
      this.min = min;
    }-*/;

    public final native void setShowCategoryLabels(boolean show) /*-{
      this.showCategoryLabels = show;
    }-*/;

    public final native void setShowValueLabels(boolean show) /*-{
      this.showValueLabels = show;
    }-*/;

    public final void setSize(int width, int height) {
      setWidth(width);
      setHeight(height);
    }

    public final native void setTitle(String title) /*-{
      this.title = title;
    }-*/;

    public final native void setWidth(int width) /*-{
      this.width = width;
    }-*/;

    private native void setLegend(String legend) /*-{
      this.legend = legend;
    }-*/;
  }

  public static final String PACKAGE = "imagechart";

  public ImageChart() {
    super();
  }

  public ImageChart(AbstractDataTable data, Options options) {
    super(data, options);
  }

  @Override
  protected native JavaScriptObject createJso(Element parent) /*-{
    return new $wnd.google.visualization.ImageChart(parent);
  }-*/;
}
