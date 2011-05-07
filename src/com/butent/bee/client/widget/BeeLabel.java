package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Label;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.utils.HasCommand;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements standard label user interface component.
 */

public class BeeLabel extends Label implements HasId, HasCommand {
  private BeeCommand command = null;

  public BeeLabel() {
    super();
    init();
  }

  public BeeLabel(Element element) {
    super(element);
    init();
  }

  public BeeLabel(Object obj) {
    this(BeeUtils.transform(obj));
  }

  public BeeLabel(String text) {
    super(text);
    init();
  }

  public BeeLabel(HorizontalAlignmentConstant align) {
    this();
    setHorizontalAlignment(align);
  }

  public BeeLabel(String text, BeeCommand cmnd) {
    this(text);

    if (cmnd != null) {
      setCommand(cmnd);
      BeeKeeper.getBus().addClickHandler(this);
    }
  }

  public BeeLabel(String text, boolean wordWrap) {
    super(text, wordWrap);
    init();
  }

  public BeeLabel(String text, HorizontalAlignmentConstant align) {
    this(text);
    setHorizontalAlignment(align);
  }

  public void createId() {
    DomUtils.createId(this, DomUtils.LABEL_ID_PREFIX);
  }

  public BeeCommand getCommand() {
    return command;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setCommand(BeeCommand command) {
    this.command = command;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    createId();
    setStyleName("bee-Label");
  }
}
