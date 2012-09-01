package com.butent.bee.client.dialog;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

public class DialogBox extends Popup implements Printable {

  private static final String STYLE_DIALOG = "bee-DialogBox";

  private static final String STYLE_LAYOUT = "bee-Dialog-layout";
  private static final String STYLE_CAPTION = "bee-Dialog-caption";
  private static final String STYLE_CONTENT = "bee-Dialog-content";
  private static final String STYLE_CLOSE = "bee-Dialog-close";

  private final Complex container = new Complex(Position.RELATIVE);
  private final Vertical layout = new Vertical();
  private final Widget header;

  public DialogBox(boolean autoHide, boolean modal, String caption, String styleName) {
    super(autoHide, modal, BeeUtils.notEmpty(styleName, STYLE_DIALOG));
    
    this.header = new Html(LocaleUtils.maybeLocalize(caption));
    header.addStyleName(STYLE_CAPTION);
    
    this.layout.addStyleName(STYLE_LAYOUT);

    this.layout.add(header);
    this.container.add(layout);

    BeeImage close = new BeeImage(Global.getImages().silverClose(),
        new Scheduler.ScheduledCommand() {
          @Override
          public void execute() {
            hide();
          }
        });
    close.addStyleName(STYLE_CLOSE);
    this.container.add(close);

    enableDragging();
  }

  public DialogBox(String caption, String styleName) {
    this(false, true, caption, styleName);
  }

  public DialogBox(String caption) {
    this(caption, STYLE_DIALOG);
  }

  public void addChild(Widget widget) {
    this.container.add(widget);
  }

  @Override
  public String getIdPrefix() {
    return "dialog";
  }

  @Override
  public Element getPrintElement() {
    return layout.getElement();
  }

  @Override
  public boolean onPrint(Element source, Element target) {
    return true;
  }

  @Override
  public void setWidget(Widget w) {
    Assert.notNull(w);
    w.addStyleName(STYLE_CONTENT);
    layout.add(w);

    super.setWidget(container);
  }

  @Override
  protected boolean isCaptionEvent(NativeEvent event) {
    EventTarget target = event.getEventTarget();
    if (Element.is(target)) {
      return header.asWidget().getElement().getParentElement().isOrHasChild(Element.as(target));
    }
    return false;
  }
}
