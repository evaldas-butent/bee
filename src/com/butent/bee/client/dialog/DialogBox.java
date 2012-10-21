package com.butent.bee.client.dialog;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
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

  public static DialogBox create(String caption) {
    return create(caption, STYLE_DIALOG);
  }
  
  public static DialogBox create(String caption, String styleName) {
    DialogBox dialog = new DialogBox(caption, styleName);
    dialog.addDefaultCloseBox();
    return dialog;
  }

  public static DialogBox withoutCloseBox(String caption, String styleName) {
    return new DialogBox(caption, styleName);
  }
  
  private final Complex container = new Complex(Position.RELATIVE);
  private final Vertical layout = new Vertical();
  private final Widget header;

  protected DialogBox(String caption) {
    this(caption, STYLE_DIALOG);
  }

  protected DialogBox(String caption, String styleName) {
    super(false, true, BeeUtils.notEmpty(styleName, STYLE_DIALOG));

    this.header = new Html(LocaleUtils.maybeLocalize(caption));
    header.addStyleName(STYLE_CAPTION);

    layout.addStyleName(STYLE_LAYOUT);

    layout.add(header);
    container.add(layout);
    
    enableDragging();
  }

  public void addChild(Widget widget) {
    container.add(widget);
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
  
  protected void addDefaultCloseBox() {
    BeeImage close = new BeeImage(Global.getImages().silverClose(), new ScheduledCommand() {
      @Override
      public void execute() {
        hide();
      }
    });
    
    close.addStyleName(STYLE_CLOSE);
    addChild(close);
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
