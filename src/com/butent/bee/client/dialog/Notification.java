package com.butent.bee.client.dialog;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Composite;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.utils.Animation;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * Enables using popup notifications with different levels of information (warnings, info messages,
 * errors etc).
 */

public class Notification extends Composite implements NativePreviewHandler {

  /**
   * Manages notification message and it's level and text.
   */

  private class Message {
    private final Level level;
    private final List<String> lines;

    private Message(Level level, List<String> lines) {
      this.level = level;
      this.lines = lines;
    }

    private Message(Level level, String... lines) {
      this(level, Lists.newArrayList(lines));
    }

    private Level getLevel() {
      return level;
    }

    private String getLine(int index) {
      return getLines().get(index);
    }

    private int getLineCount() {
      return getLines().size();
    }

    private List<String> getLines() {
      return lines;
    }

    private String getStyleName() {
      if (getLevel() == null) {
        return STYLE_DEFAULT;
      } else {
        return "bee-Notification" + BeeUtils.proper(getLevel().getName(), null);
      }
    }
  }

  /**
   * Enables size related animations for notification messages.
   */

  private class MoleAnimation extends Animation {
    private int startSize;
    private int endSize;
    private State endState;

    @Override
    protected void onComplete() {
      if (endSize > 0) {
        getElement().getStyle().setHeight(endSize, Unit.PX);
      }
      setState(endState);
    }

    @Override
    protected void onUpdate(double progress) {
      double delta = (endSize - startSize) * progress;
      double newSize = startSize + delta;
      getElement().getStyle().setHeight(newSize, Unit.PX);
    }

    private void animate(int start, int end, int duration, State st) {
      this.startSize = start;
      this.endSize = end;
      this.endState = st;

      if (duration > 0) {
        run(duration);
      } else {
        onComplete();
      }
    }
  }

  /**
   * Contains a list of possible notification states
   */

  private enum State {
    PENDING, OPENING, SHOWING, CLOSING
  }

  public static int defaultOpenDuration = 500;
  public static int defaultCloseDuration = 500;

  private static final String STYLE_CONTAINER = "bee-NotificationContainer";
  private static final String STYLE_MESSAGES = "bee-NotificationMessages";
  private static final String STYLE_TEXT = "bee-NotificationText";
  private static final String STYLE_DEFAULT = "bee-NotificationInfo";

  private final DivElement messageContainer = Document.get().createDivElement();

  private final MoleAnimation animation = new MoleAnimation();

  private int openDuration = defaultOpenDuration;
  private int closeDuration = defaultCloseDuration;

  private HandlerRegistration previewReg = null;

  private State state = State.PENDING;

  private final LinkedList<Message> pendingMessages = Lists.newLinkedList();

  public Notification() {
    messageContainer.setId(DomUtils.createUniqueId("note-messages"));
    messageContainer.appendChild(createTextElement());

    DivElement container = Document.get().createDivElement();
    container.setId(DomUtils.createUniqueId("note-container"));
    container.setClassName(STYLE_CONTAINER);
    container.getStyle().setDisplay(Display.NONE);
    container.appendChild(messageContainer);

    Html html = new Html(container);
    initWidget(html);
  }

  public void clearPendingMessages() {
    getPendingMessages().clear();
  }

  public void config(String... lines) {
    show(Level.CONFIG, lines);
  }

  public void fine(String... lines) {
    show(Level.FINE, lines);
  }

  public void finer(String... lines) {
    show(Level.FINER, lines);
  }

  public void finest(String... lines) {
    show(Level.FINEST, lines);
  }

  public void hide() {
    switch (getState()) {
      case PENDING:
        return;
      case SHOWING:
        setState(State.CLOSING);
        getAnimation().animate(getMessageContainer().getOffsetHeight(), 0, getCloseDuration(),
            State.PENDING);
        break;
      case OPENING:
      case CLOSING:
        getAnimation().cancel();
        hideDisplay();
        setState(State.PENDING);
        break;
      default:
        Assert.untouchable();
    }
  }

  public void info(String... lines) {
    show(Level.INFO, lines);
  }

  public void onPreviewNativeEvent(NativePreviewEvent event) {
    Assert.notNull(event);
    String type = event.getNativeEvent().getType();

    if (EventUtils.isKeyDown(type) || EventUtils.isMouseDown(type)) {
      closePreview();
      hide();
    }
  }

  public void setCloseDuration(int duration) {
    this.closeDuration = duration;
  }

  public void setOpenDuration(int duration) {
    this.openDuration = duration;
  }

  public void severe(String... lines) {
    show(Level.SEVERE, lines);
  }

  public void show(Level level, String... lines) {
    Assert.notNull(level);
    Assert.notNull(lines);
    Assert.parameterCount(lines.length, 1);

    Message message = new Message(level, lines);

    if (State.PENDING.equals(getState())) {
      setMessage(message);
      openDisplay();
    } else {
      getPendingMessages().add(message);
    }
  }

  public void warning(String... lines) {
    show(Level.WARNING, lines);
  }

  @Override
  protected void onUnload() {
    if (!BeeKeeper.getScreen().isTemporaryDetach()) {
      closePreview();
    }
    super.onUnload();
  }

  private void closePreview() {
    if (getPreviewReg() != null) {
      getPreviewReg().removeHandler();
      setPreviewReg(null);
    }
  }

  private Element createTextElement() {
    DivElement element = Document.get().createDivElement();
    element.setId(DomUtils.createUniqueId("note-text"));
    element.setClassName(STYLE_TEXT);
    return element;
  }

  private MoleAnimation getAnimation() {
    return animation;
  }

  private int getCloseDuration() {
    return closeDuration;
  }

  private DivElement getMessageContainer() {
    return messageContainer;
  }

  private int getOpenDuration() {
    return openDuration;
  }

  private LinkedList<Message> getPendingMessages() {
    return pendingMessages;
  }

  private HandlerRegistration getPreviewReg() {
    return previewReg;
  }

  private State getState() {
    return state;
  }

  private void hideDisplay() {
    getElement().getStyle().setDisplay(Display.NONE);
  }

  private void openDisplay() {
    setState(State.OPENING);
    showDisplay();
    getAnimation().animate(0, getMessageContainer().getOffsetHeight(), getOpenDuration(),
        State.SHOWING);
  }

  private void setMessage(Message message) {
    Assert.notNull(message);
    int oldCount = getMessageContainer().getChildCount();
    int pendingCount = getPendingMessages().size();
    int newCount = message.getLineCount();
    if (pendingCount > 0) {
      newCount++;
    }

    if (newCount < oldCount) {
      if (oldCount > 1) {
        for (int i = Math.max(newCount, 1); i < oldCount; i++) {
          getMessageContainer().removeChild(getMessageContainer().getLastChild());
        }
      }
      if (newCount <= 0) {
        Element.as(getMessageContainer().getChild(0)).setInnerHTML(BeeConst.STRING_EMPTY);
      }

    } else if (newCount > oldCount) {
      for (int i = oldCount; i < newCount; i++) {
        getMessageContainer().appendChild(createTextElement());
      }
    }

    if (newCount > 0) {
      for (int i = 0; i < message.getLineCount(); i++) {
        Element.as(getMessageContainer().getChild(i)).setInnerHTML(message.getLine(i));
      }
      if (pendingCount > 0) {
        String msg = BeeUtils.concat(1, BeeUtils.bracket(pendingCount), "pending...");
        Element.as(getMessageContainer().getLastChild()).setInnerHTML(msg);
      }

      getMessageContainer().setClassName(
          StyleUtils.buildClasses(STYLE_MESSAGES, message.getStyleName()));
    }
  }

  private void setPreviewReg(HandlerRegistration previewReg) {
    this.previewReg = previewReg;
  }

  private void setState(State state) {
    Assert.notNull(state, "notification state cannot be null");
    this.state = state;

    switch (state) {
      case PENDING:
        hideDisplay();
        Message message = getPendingMessages().poll();
        if (message != null) {
          setMessage(message);
          openDisplay();
        }
        break;

      case SHOWING:
        if (getPreviewReg() == null) {
          setPreviewReg(Event.addNativePreviewHandler(this));
        }
        break;

      case CLOSING:
        closePreview();
        break;

      default:
    }
  }

  private void showDisplay() {
    getElement().getStyle().setDisplay(Display.BLOCK);
  }
}
