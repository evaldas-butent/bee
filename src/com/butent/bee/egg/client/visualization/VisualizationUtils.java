package com.butent.bee.egg.client.visualization;

import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.egg.client.ajaxloader.AjaxLoader;
import com.butent.bee.egg.client.ajaxloader.ArrayHelper;
import com.butent.bee.egg.client.ajaxloader.AjaxLoader.AjaxLoaderOptions;

public class VisualizationUtils {
  public static void loadVisualizationApi(Runnable onLoad, String... packages) {
    loadVisualizationApi("1", onLoad, ArrayHelper.toJsArrayString(packages));
  }

  public static void loadVisualizationApi(String version, Runnable onLoad,
      JsArrayString packages) {
    AjaxLoaderOptions options = AjaxLoaderOptions.newInstance();
    options.setPackages(packages);
    AjaxLoader.loadApi("visualization", version, onLoad, options); 
  }

  public static void loadVisualizationApi(String version, Runnable onLoad,
      String... packages) {
    loadVisualizationApi(version, onLoad, ArrayHelper.toJsArrayString(packages));
  }
  
  private VisualizationUtils() {
  }
}
