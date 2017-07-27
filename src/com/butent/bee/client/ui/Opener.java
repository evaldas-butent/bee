package com.butent.bee.client.ui;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.ui.WindowType;

import java.util.function.Consumer;

public final class Opener {

  public static final Opener MODAL = new Opener(WindowType.MODAL);
  public static final Opener DETACHED = new Opener(WindowType.DETACHED);

  public static final Opener NEW_TAB = new Opener(WindowType.NEW_TAB);
  private static final Opener ON_TOP = new Opener(WindowType.ON_TOP);

  public static Opener detached(Consumer<FormView> onOpen) {
    return detached(null, onOpen);
  }

  public static Opener detached(Element target, Consumer<FormView> onOpen) {
    if (target == null && onOpen == null) {
      return DETACHED;
    } else {
      return new Opener(WindowType.DETACHED, target, onOpen);
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

  public static Opener in(WindowType windowType, Element target, Consumer<FormView> onOpen) {
    if (windowType == WindowType.DETACHED) {
      return detached(target, onOpen);

    } else if (windowType == WindowType.MODAL) {
      return modal(target, onOpen);

    } else {
      return in(windowType, onOpen);
    }
  }

  public static Opener maybeCreate(WindowType windowType) {
    return (windowType == null) ? null : in(windowType, null);
  }

  public static Opener modal(Consumer<FormView> onOpen) {
    return modal(null, onOpen);
  }

  public static Opener modal(Element target, Consumer<FormView> onOpen) {
    if (target == null && onOpen == null) {
      return MODAL;
    } else {
      return new Opener(WindowType.MODAL, target, onOpen);
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
      return new Opener(WindowType.NEW_TAB, null, onOpen);
    }
  }

  public static Opener onTop(Consumer<FormView> onOpen) {
    if (onOpen == null) {
      return ON_TOP;
    } else {
      return new Opener(WindowType.ON_TOP, null, onOpen);
    }
  }

  public static Opener relativeTo(Element element) {
    return new Opener(WindowType.MODAL, element);
  }

  public static Opener relativeTo(UIObject obj) {
    if (obj == null) {
      return MODAL;
    } else {
      return relativeTo(obj.getElement());
    }
  }

  public static Opener with(WindowType windowType, PresenterCallback presenterCallback) {
    return new Opener(windowType, null, null, presenterCallback);
  }

  private final WindowType windowType;
  private final Element target;

  private final Consumer<FormView> onOpen;
  private final PresenterCallback presenterCallback;

  private Opener(WindowType windowType) {
    this(windowType, null, null, null);
  }

  private Opener(WindowType windowType, Element target) {
    this(windowType, target, null, null);
  }

  private Opener(WindowType windowType, Element target, Consumer<FormView> onOpen) {
    this(windowType, target, onOpen, null);
  }

  private Opener(WindowType windowType, Element target, Consumer<FormView> onOpen,
      PresenterCallback presenterCallback) {

    this.windowType = windowType;
    this.target = target;
    this.onOpen = onOpen;
    this.presenterCallback = presenterCallback;
  }

  public Consumer<FormView> getOnOpen() {
    return onOpen;
  }

  public Element getTarget() {
    return target;
  }

  public WindowType getWindowType() {
    return windowType;
  }

  public boolean isModal() {
    return getWindowType() == WindowType.MODAL;
  }

  public boolean isPopup() {
    return getWindowType().isPopup();
  }

  public void onCreate(Presenter presenter) {
    if (presenterCallback != null) {
      presenterCallback.onCreate(presenter);

    } else if (getWindowType() == WindowType.NEW_TAB) {
      PresenterCallback.SHOW_IN_NEW_TAB.onCreate(presenter);

    } else if (getWindowType() == WindowType.ON_TOP) {
      PresenterCallback.SHOW_IN_ACTIVE_PANEL.onCreate(presenter);
    }
  }

  public Opener normalize() {
    if (!isModal() && Popup.hasEventPreview()) {
      return new Opener(WindowType.MODAL, getTarget(), getOnOpen(), presenterCallback);
    } else {
      return this;
    }
  }
}
