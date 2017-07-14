package com.butent.bee.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dialog.Modality;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.ui.WindowType;

import java.util.function.Consumer;

public final class Opener {

  public static final Opener MODAL = new Opener(Modality.ENABLED, null);
  public static final Opener DETACHED = new Opener(Modality.DISABLED, null);

  public static final Opener NEW_TAB = new Opener(PresenterCallback.SHOW_IN_NEW_TAB);
  private static final Opener ON_TOP = new Opener(PresenterCallback.SHOW_IN_ACTIVE_PANEL);

  public static Opener detached(Consumer<FormView> onOpen) {
    if (onOpen == null) {
      return DETACHED;
    } else {
      return new Opener(Modality.DISABLED, null, null, onOpen);
    }
  }

  public static Opener in(WindowType windowType, Consumer<FormView> onOpen) {
    Assert.notNull(windowType);

    switch (windowType) {
      case NEW_TAB:
        return newTab(onOpen);
      case ON_TOP:
        return onTop(onOpen);
      case DETACHED:
        return detached(onOpen);
      case MODAL:
        return modal(onOpen);
    }

    Assert.untouchable();
    return null;
  }

  public static Opener modal(Consumer<FormView> onOpen) {
    if (onOpen == null) {
      return MODAL;
    } else {
      return new Opener(Modality.ENABLED, null, null, onOpen);
    }
  }

  public static Opener modeless() {
    if (BeeKeeper.getUser().openInNewTab()) {
      return NEW_TAB;
    } else {
      return ON_TOP;
    }
  }

  public static Opener newTab(Consumer<FormView> onOpen) {
    if (onOpen == null) {
      return NEW_TAB;
    } else {
      return new Opener(null, null, NEW_TAB.getPresenterCallback(), onOpen);
    }
  }

  public static Opener onTop(Consumer<FormView> onOpen) {
    if (onOpen == null) {
      return ON_TOP;
    } else {
      return new Opener(null, null, ON_TOP.getPresenterCallback(), onOpen);
    }
  }

  public static Opener relativeTo(Element element) {
    return new Opener(Modality.ENABLED, element);
  }

  public static Opener relativeTo(UIObject obj) {
    if (obj == null) {
      return MODAL;
    } else {
      return relativeTo(obj.getElement());
    }
  }

  public static Opener with(PresenterCallback callback) {
    Assert.notNull(callback);
    return new Opener(callback);
  }

  private final Modality modality;
  private final Element target;

  private final PresenterCallback presenterCallback;
  private final Consumer<FormView> onOpen;

  private Opener(Modality modality, Element target) {
    this(modality, target, null, null);
  }

  private Opener(Modality modality, Element target, PresenterCallback presenterCallback,
      Consumer<FormView> onOpen) {

    this.modality = modality;
    this.target = target;
    this.presenterCallback = presenterCallback;
    this.onOpen = onOpen;
  }

  private Opener(PresenterCallback presenterCallback) {
    this(null, null, presenterCallback, null);
  }

  public Modality getModality() {
    return modality;
  }

  public Consumer<FormView> getOnOpen() {
    return onOpen;
  }

  public PresenterCallback getPresenterCallback() {
    return presenterCallback;
  }

  public Element getTarget() {
    return target;
  }
}
