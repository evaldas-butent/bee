package com.butent.bee.client.modules.orders.ec;

import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Frame;
import com.butent.bee.shared.utils.BeeUtils;

public class OrdEcHtmlViewer extends OrdEcView {
  private final String caption;
  private final String urlKey;
  private final String htmlKey;

  OrdEcHtmlViewer(String caption, String urlKey, String htmlKey) {
    super();

    this.caption = caption;
    this.urlKey = urlKey;
    this.htmlKey = htmlKey;
  }

  @Override
  protected void createUi() {
    OrdEcKeeper.getConfiguration(input -> {
      String url = input.get(urlKey);
      String html = input.get(htmlKey);

      if (!BeeUtils.isEmpty(url)) {
        Frame frame = new Frame(url);
        EcStyles.add(frame, getPrimaryStyle(), "frame");
        add(frame);

      } else if (!BeeUtils.isEmpty(html)) {
        CustomDiv widget = new CustomDiv();
        EcStyles.add(widget, getPrimaryStyle(), "html");
        widget.setHtml(html);
        add(widget);

      } else {
        add(renderNoData(caption));
      }
    });
  }

  @Override
  protected String getPrimaryStyle() {
    return "htmlViewer";
  }
}