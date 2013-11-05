package com.butent.bee.client.composite;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Callback;
import com.butent.bee.client.animation.AnimationState;
import com.butent.bee.client.animation.RafCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.PreviewHandler;
import com.butent.bee.client.event.Previewer;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.CustomComplex;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputRange;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Progress;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

public final class SlideDeck extends CustomComplex implements PreviewHandler {

  private final class Animation extends RafCallback {

    private int prevPosition;
    private int nextPosition;

    private Style prevStyle;
    private Style nextStyle;

    private Size size;

    private int stepMillis;
    private int cycleMillis;

    private AnimationState state;
    private boolean transition;

    private Animation() {
      super(Integer.MAX_VALUE);
      setState(AnimationState.NOT_STARTED);
    }

    @Override
    public void start() {
      updateActiveWrapper(getPosition());

      setPrevPosition(getPosition());
      setNextPosition(BeeConst.UNDEF);

      setPrevStyle(getActiveWrapper().getElement().getStyle());
      setNextStyle(getPassiveWrapper().getElement().getStyle());

      setSize(new Size(canvas.getOffsetWidth(), canvas.getOffsetHeight()));

      setStepMillis(getDisplayMillis() + getTransitionMillis());
      setCycleMillis(getStepMillis() * slides.size());

      setTransition(false);
      setState(AnimationState.RUNNING);

      super.start();
    }

    @Override
    protected void onComplete() {
    }

    @Override
    protected boolean run(double elapsed) {
      if (!isRunning()) {
        return false;
      }

      double progress = elapsed % getCycleMillis();
      updateProgress(progress / getCycleMillis() * (slides.size() - 1));

      double stepElapsed = elapsed % getStepMillis();
      if (stepElapsed > getDisplayMillis()) {
        if (!isTransition()) {
          setTransition(true);
          setNextPosition(BeeUtils.rotateForwardExclusive(getPrevPosition(), 0, slides.size()));
          updatePassiveWrapper(getNextPosition());
        }

        double transitionValue = (stepElapsed - getDisplayMillis()) / getTransitionMillis();
        getEffect().apply(getPrevStyle(), getNextStyle(), getSize(), transitionValue);

      } else if (isTransition()) {
        setTransition(false);

        setPrevPosition(getNextPosition());
        setNextPosition(BeeConst.UNDEF);

        setActiveWrapperIndex(getPassiveWrapperIndex());

        setPrevStyle(getActiveWrapper().getElement().getStyle());
        setNextStyle(getPassiveWrapper().getElement().getStyle());
      }

      return true;
    }

    private void cancel() {
      setState(AnimationState.CANCELED);
      if (getPrevStyle() != null) {
        getEffect().reset(getPrevStyle(), getNextStyle());
      }
    }

    private int getCycleMillis() {
      return cycleMillis;
    }

    private int getNextPosition() {
      return nextPosition;
    }

    private Style getNextStyle() {
      return nextStyle;
    }

    private int getPrevPosition() {
      return prevPosition;
    }

    private Style getPrevStyle() {
      return prevStyle;
    }

    private Size getSize() {
      return size;
    }

    private AnimationState getState() {
      return state;
    }

    private int getStepMillis() {
      return stepMillis;
    }

//    private boolean isPaused() {
//      return getState() == AnimationState.PAUSED;
//    }

    private boolean isRunning() {
      return getState() == AnimationState.RUNNING;
    }

    private boolean isTransition() {
      return transition;
    }

    private void setCycleMillis(int cycleMillis) {
      this.cycleMillis = cycleMillis;
    }

    private void setNextPosition(int nextPosition) {
      this.nextPosition = nextPosition;
    }

    private void setNextStyle(Style nextStyle) {
      this.nextStyle = nextStyle;
    }

    private void setPrevPosition(int prevPosition) {
      this.prevPosition = prevPosition;
    }

    private void setPrevStyle(Style prevStyle) {
      this.prevStyle = prevStyle;
    }

    private void setSize(Size size) {
      this.size = size;
    }

    private void setState(AnimationState state) {
      this.state = state;
    }

    private void setStepMillis(int stepMillis) {
      this.stepMillis = stepMillis;
    }

    private void setTransition(boolean transition) {
      this.transition = transition;
    }

    private void suspend() {
      setState(AnimationState.PAUSED);
    }
  }

  private enum Effect {
    FADE {
      @Override
      public void apply(Style prev, Style next, Size size, double value) {
        prev.setOpacity(1 - value);
        next.setOpacity(value);
      }

      @Override
      public void reset(Style prev, Style next) {
        prev.clearOpacity();
        next.clearOpacity();
      }
    },
    SLIDE {
      @Override
      public void apply(Style prev, Style next, Size size, double value) {
        prev.setLeft(-value * size.getWidth(), Unit.PX);
        next.setLeft((1 - value) * size.getWidth(), Unit.PX);
      }

      @Override
      public void reset(Style prev, Style next) {
        prev.setLeft(0, Unit.PX);
        next.setLeft(0, Unit.PX);
      }
    },
    FLIP {
      @Override
      public void apply(Style prev, Style next, Size size, double value) {
        prev.setTop(value * size.getHeight(), Unit.PX);
        next.setTop((value - 1) * size.getHeight(), Unit.PX);
      }

      @Override
      public void reset(Style prev, Style next) {
        prev.setTop(0, Unit.PX);
        next.setTop(0, Unit.PX);
      }
    };

    public abstract void apply(Style prev, Style next, Size size, double value);

    public abstract void reset(Style prev, Style next);

//    private void end(Style prev, Style next, Size size) {
//      apply(prev, next, size, BeeConst.DOUBLE_ONE);
//    }
//
//    private void start(Style prev, Style next, Size size) {
//      apply(prev, next, size, BeeConst.DOUBLE_ZERO);
//    }
  }

  private static final class Slide {
    private final String source;

    private final int naturalWidth;
    private final int naturalHeight;

    private Slide(String source, int naturalWidth, int naturalHeight) {
      this.source = source;

      this.naturalWidth = naturalWidth;
      this.naturalHeight = naturalHeight;
    }

    private void embed(Widget display) {
      StyleUtils.setBackgroundImage(display, source);
    }
  }

  private static final int MIN_WIDTH = 200;
  private static final int MIN_HEIGHT = 200;

  private static final int TN_HEIGHT = 70;
  private static final int CONTROLS_HEIGHT = 30;

  private static final int TN_HEIGHT_MARGIN = DomUtils.getScrollBarHeight() + 3;

  private static final double MAX_WIDTH_FACTOR = 0.9;
  private static final double MAX_HEIGHT_FACTOR = 0.9;

  private static final String STYLE_PREFIX = "bee-SlideDeck-";

  private static final String STYLE_TN_ACTIVE = STYLE_PREFIX + "tn-active";
  private static final String STYLE_NAV_DISABLED = STYLE_PREFIX + "nav-disabled";

  private static final String STYLE_PLAYING = STYLE_PREFIX + "playing";

  public static void create(List<String> sources, Callback<SlideDeck> callback) {
    int maxWidth = BeeUtils.round(Window.getClientWidth() * MAX_WIDTH_FACTOR);
    int maxHeight = Math.min(BeeUtils.round(Window.getClientHeight() * MAX_HEIGHT_FACTOR),
        Window.getClientHeight() - DialogBox.HEADER_HEIGHT);

    if (maxWidth < MIN_WIDTH || maxHeight < MIN_HEIGHT) {
      callback.onFailure("browser window is suffering from being too small");
    } else {
      create(sources, new Size(maxWidth, maxHeight), callback);
    }
  }

  public static void create(final List<String> sources, final Size maxSize,
      final Callback<SlideDeck> callback) {

    Assert.notEmpty(sources);
    Assert.notNull(callback);

    getNaturalSizes(sources, new Consumer<Size[]>() {
      @Override
      public void accept(Size[] input) {
        List<Slide> list = Lists.newArrayList();
        for (int i = 0; i < sources.size(); i++) {
          list.add(new Slide(sources.get(i), input[i].getWidth(), input[i].getHeight()));
        }

        SlideDeck slideDeck = new SlideDeck(list, maxSize);
        callback.onSuccess(slideDeck);
      }
    });
  }

  private static void getNaturalSizes(List<String> sources, final Consumer<Size[]> callback) {
    final Size[] sizes = new Size[sources.size()];
    final Holder<Integer> latch = Holder.of(0);

    for (int i = 0; i < sources.size(); i++) {
      final int index = i;

      Rulers.getImageNaturalSize(sources.get(i), new Consumer<Size>() {
        @Override
        public void accept(Size input) {
          sizes[index] = input;

          latch.set(latch.get() + 1);
          if (latch.get() >= sizes.length) {
            callback.accept(sizes);
          }
        }
      });
    }
  }

  private final List<Slide> slides;

  private final Flow thumbnails;
  private final Flow canvas;
  private final Flow controls;

  private final Widget[] wrappers = new Widget[2];
  private int activeWrapperIndex;

  private String playId;
  private String progressId;
  private String backwardId;
  private String labelId;
  private String forwardId;

  private int position = BeeConst.UNDEF;
  private boolean cycle;

  private final Animation animation;

  private Effect effect = Effect.FADE;

  private int displayMillis = 2000;
  private int transitionMillis = 1000;

  private SlideDeck(List<Slide> slides, Size maxSize) {
    super(Document.get().createDivElement(), STYLE_PREFIX + "container");
    this.slides = slides;

    this.animation = new Animation();

    this.thumbnails = new Flow(STYLE_PREFIX + "thumbnails");
    add(thumbnails);

    this.canvas = new Flow(STYLE_PREFIX + "canvas");

    for (int i = 0; i < wrappers.length; i++) {
      wrappers[i] = new CustomDiv(STYLE_PREFIX + "wrapper");
      canvas.add(wrappers[i]);
    }
    add(canvas);

    this.controls = new Flow(STYLE_PREFIX + "controls");
    add(controls);

    renderThumbnails();
    renderControls();

    doSizing(maxSize);

    setActiveWrapperIndex(0);
    activateSlide(0);
  }

  public boolean cycle() {
    return cycle;
  }

  @Override
  public String getIdPrefix() {
    return "slide-deck";
  }

  public int getWidth() {
    return StyleUtils.getWidth(this);
  }

  public void handleKeyboard() {
    Previewer.ensureRegistered(this);
  }

  @Override
  public boolean isModal() {
    return false;
  }

  @Override
  public void onEventPreview(NativePreviewEvent event) {
    if (EventUtils.isKeyDown(event.getNativeEvent().getType())) {
      int keyCode = event.getNativeEvent().getKeyCode();

      switch (keyCode) {
        case KeyCodes.KEY_LEFT:
        case KeyCodes.KEY_UP:
          activateSlide(BeeUtils.rotateBackwardExclusive(getPosition(), 0, slides.size()));
          break;

        case KeyCodes.KEY_RIGHT:
        case KeyCodes.KEY_DOWN:
          activateSlide(BeeUtils.rotateForwardExclusive(getPosition(), 0, slides.size()));
          break;

        case KeyCodes.KEY_HOME:
        case KeyCodes.KEY_PAGEUP:
          activateSlide(0);
          break;

        case KeyCodes.KEY_END:
        case KeyCodes.KEY_PAGEDOWN:
          activateSlide(slides.size() - 1);
          break;

        case KeyCodes.KEY_ENTER:
        case BeeConst.CHAR_SPACE:
          togglePlay();
          break;
      }
    }
  }

  public void setCycle(boolean cycle) {
    this.cycle = cycle;
  }

  @Override
  protected void onUnload() {
    super.onUnload();

    Previewer.ensureUnregistered(this);
    if (animation.isRunning()) {
      animation.cancel();
    }
  }

  private void activateSlide(int index) {
    if (animation.isRunning()) {
      togglePlay();
    }

    if (getPosition() != index && BeeUtils.isIndex(slides, index)) {
      if (BeeUtils.isIndex(slides, getPosition())) {
        getThumbnail(getPosition()).removeStyleName(STYLE_TN_ACTIVE);
      }

      if (BeeUtils.isIndex(slides, index)) {
        setPosition(index);

        updateCanvas(index);
        Widget thumbnail = getThumbnail(index);
        if (thumbnail != null) {
          thumbnail.addStyleName(STYLE_TN_ACTIVE);
        }

        updateProgress(index);
        updateLabel(index + 1);
        updateNavigation(index);
      }
    }
  }

  private void doSizing(Size maxSize) {
    int canvasWidth = MIN_WIDTH;
    int canvasHeight = MIN_HEIGHT;

    for (Slide slide : slides) {
      canvasWidth = Math.max(canvasWidth, slide.naturalWidth);
      canvasHeight = Math.max(canvasHeight, slide.naturalHeight);
    }

    if (maxSize != null) {
      if (maxSize.getWidth() > 0) {
        canvasWidth = Math.min(canvasWidth, maxSize.getWidth());
      }

      if (maxSize.getHeight() > TN_HEIGHT + CONTROLS_HEIGHT) {
        canvasHeight = Math.min(canvasHeight, maxSize.getHeight() - TN_HEIGHT - CONTROLS_HEIGHT);
      }
    }

    StyleUtils.setWidth(this, canvasWidth);

    StyleUtils.setSize(thumbnails, canvasWidth, TN_HEIGHT);
    StyleUtils.setSize(canvas, canvasWidth, canvasHeight);
    StyleUtils.setSize(controls, canvasWidth, CONTROLS_HEIGHT);

    int tnWidth = 0;
    int maxHeight = TN_HEIGHT - TN_HEIGHT_MARGIN;

    for (int i = 0; i < slides.size(); i++) {
      int width = slides.get(i).naturalWidth;
      int height = slides.get(i).naturalHeight;

      if (height > maxHeight) {
        width = width * maxHeight / height + 1;
        height = maxHeight;
      }

      Widget thumbnail = getThumbnail(i);
      if (thumbnail != null) {
        StyleUtils.setSize(thumbnail, width, height);
      }

      tnWidth += width;
    }

    String styleSuffix = (tnWidth > canvasWidth) ? "over" : "under";
    thumbnails.addStyleName(STYLE_PREFIX + "thumbnails-" + styleSuffix);

    for (Widget wrapper : wrappers) {
      StyleUtils.setSize(wrapper, canvasWidth, canvasHeight);
    }
  }

  private void editSettings() {
    String stylePrefix = STYLE_PREFIX + "config-";

    HtmlTable table = new HtmlTable(stylePrefix + "table");

    int row = 0;
    table.setHtml(row, 0, "Effect");

    final BeeListBox effectWidget = new BeeListBox();
    effectWidget.addCaptions(Effect.class);
    if (getEffect() != null) {
      effectWidget.setSelectedIndex(getEffect().ordinal());
    }

    effectWidget.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        Effect eff = NameUtils.getEnumByIndex(Effect.class, effectWidget.getSelectedIndex());
        if (eff != null) {
          setEffect(eff);
        }
      }
    });

    table.setWidgetAndStyle(row, 1, effectWidget, stylePrefix + "effect");

    row++;

    table.setHtml(row, 0, "Display");
    InputRange displayWidget = new InputRange(100, 10000, 100);
    displayWidget.setValue(getDisplayMillis());

    displayWidget.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        int millis = BeeUtils.toInt(event.getValue());
        if (millis > 0) {
          setDisplayMillis(millis);
        }
      }
    });

    table.setWidgetAndStyle(row, 1, displayWidget, stylePrefix + "display");

    row++;

    table.setHtml(row, 0, "Transition");
    InputRange transitionWidget = new InputRange(100, 10000, 100);
    transitionWidget.setValue(getTransitionMillis());

    transitionWidget.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        int millis = BeeUtils.toInt(event.getValue());
        if (millis > 0) {
          setTransitionMillis(millis);
        }
      }
    });

    table.setWidgetAndStyle(row, 1, transitionWidget, stylePrefix + "transition");

    final Popup popup = new Popup(OutsideClick.CLOSE, stylePrefix + "popup");

    Flow panel = new Flow(stylePrefix + "panel");
    panel.add(table);

    FaLabel close = new FaLabel(FontAwesome.TIMES);
    close.addStyleName(stylePrefix + "close");

    close.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        popup.close();
      }
    });

    panel.add(close);

    popup.setWidget(panel);

    popup.setHideOnEscape(true);
    popup.setAnimationEnabled(true);

    popup.showRelativeTo(controls.getElement());
  }

  private Widget getActiveWrapper() {
    return wrappers[getActiveWrapperIndex()];
  }

  private int getActiveWrapperIndex() {
    return activeWrapperIndex;
  }

  private String getBackwardId() {
    return backwardId;
  }

  private int getDisplayMillis() {
    return displayMillis;
  }

  private Effect getEffect() {
    return effect;
  }

  private String getForwardId() {
    return forwardId;
  }

  private String getLabelId() {
    return labelId;
  }

  private Widget getPassiveWrapper() {
    return wrappers[getPassiveWrapperIndex()];
  }

  private int getPassiveWrapperIndex() {
    return 1 - getActiveWrapperIndex();
  }

  private String getPlayId() {
    return playId;
  }

  private int getPosition() {
    return position;
  }

  private String getProgressId() {
    return progressId;
  }

  private Widget getThumbnail(int index) {
    return thumbnails.getWidget(index);
  }

  private int getTransitionMillis() {
    return transitionMillis;
  }

  private void renderControls() {
    FaLabel play = new FaLabel(FontAwesome.PLAY);
    play.addStyleName(STYLE_PREFIX + "play");

    play.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        togglePlay();
      }
    });

    setPlayId(play.getId());
    controls.add(play);

    Progress progress = new Progress(slides.size() - 1);
    progress.addStyleName(STYLE_PREFIX + "progress");

    progress.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Element relativeElement = event.getRelativeElement();
        int x = event.getX();

        if (x > 0 && relativeElement != null && relativeElement.getOffsetWidth() > slides.size()) {
          int pos = x / (relativeElement.getOffsetWidth() / slides.size());
          activateSlide(pos);
        }
      }
    });

    setProgressId(progress.getId());
    controls.add(progress);

    FaLabel backward = new FaLabel(FontAwesome.BACKWARD);
    backward.addStyleName(STYLE_PREFIX + "backward");

    backward.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (getPosition() > 0) {
          activateSlide(getPosition() - 1);
        } else if (cycle()) {
          activateSlide(slides.size() - 1);
        }
      }
    });

    setBackwardId(backward.getId());
    controls.add(backward);

    Label label = new Label();
    label.addStyleName(STYLE_PREFIX + "label");
    setLabelId(label.getId());
    controls.add(label);

    FaLabel forward = new FaLabel(FontAwesome.FORWARD);
    forward.addStyleName(STYLE_PREFIX + "forward");

    forward.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (getPosition() < slides.size() - 1) {
          activateSlide(getPosition() + 1);
        } else if (cycle()) {
          activateSlide(0);
        }
      }
    });

    setForwardId(forward.getId());
    controls.add(forward);

    FaLabel settings = new FaLabel(FontAwesome.COG);
    settings.addStyleName(STYLE_PREFIX + "settings");

    settings.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        editSettings();
      }
    });

    controls.add(settings);
  }

  private void renderThumbnails() {
    ClickHandler clickHandler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        activateSlide(DomUtils.getDataIndexInt(EventUtils.getEventTargetElement(event)));
      }
    };

    for (int i = 0; i < slides.size(); i++) {
      CustomDiv thumbnail = new CustomDiv(STYLE_PREFIX + "tn");
      StyleUtils.setBackgroundImage(thumbnail, slides.get(i).source);

      DomUtils.setDataIndex(thumbnail.getElement(), i);
      thumbnail.addClickHandler(clickHandler);

      thumbnails.add(thumbnail);
    }
  }

  private void setActiveWrapperIndex(int activeWrapperIndex) {
    this.activeWrapperIndex = activeWrapperIndex;
  }

  private void setBackwardId(String backwardId) {
    this.backwardId = backwardId;
  }

  private void setDisplayMillis(int displayMillis) {
    this.displayMillis = displayMillis;
  }

  private void setEffect(Effect effect) {
    this.effect = effect;
  }

  private void setForwardId(String forwardId) {
    this.forwardId = forwardId;
  }

  private void setLabelId(String labelId) {
    this.labelId = labelId;
  }

  private void setPlayId(String playId) {
    this.playId = playId;
  }

  private void setPosition(int position) {
    this.position = position;
  }

  private void setProgressId(String progressId) {
    this.progressId = progressId;
  }

  private void setTransitionMillis(int transitionMillis) {
    this.transitionMillis = transitionMillis;
  }

  private void togglePlay() {
    if (animation.isRunning()) {
      removeStyleName(STYLE_PLAYING);
      animation.suspend();
    } else {
      addStyleName(STYLE_PLAYING);
      animation.start();
    }

    Widget play = DomUtils.getChildById(controls, getPlayId());
    if (play instanceof FaLabel) {
      ((FaLabel) play).setChar(animation.isRunning() ? FontAwesome.PAUSE : FontAwesome.PLAY);
    }
  }

  private void updateActiveWrapper(int slideIndex) {
    slides.get(slideIndex).embed(getActiveWrapper());
  }

  private void updateCanvas(int slideIndex) {
    slides.get(slideIndex).embed(canvas);
  }

  private void updateLabel(int value) {
    Widget label = DomUtils.getChildById(controls, getLabelId());
    if (label != null) {
      String separator = BeeConst.STRING_SPACE + BeeConst.STRING_SLASH + BeeConst.STRING_SPACE;
      label.getElement().setInnerText(BeeUtils.join(separator, value, slides.size()));
    }
  }

  private void updateNavigation(int value) {
    if (!cycle()) {
      Widget backward = DomUtils.getChildById(controls, getBackwardId());
      if (backward != null) {
        backward.setStyleName(STYLE_NAV_DISABLED, value <= 0);
      }

      Widget forward = DomUtils.getChildById(controls, getForwardId());
      if (forward != null) {
        forward.setStyleName(STYLE_NAV_DISABLED, value >= slides.size() - 1);
      }
    }
  }

  private void updatePassiveWrapper(int slideIndex) {
    slides.get(slideIndex).embed(getPassiveWrapper());
  }

  private void updateProgress(double value) {
    Widget progress = DomUtils.getChildById(controls, getProgressId());
    if (progress instanceof Progress) {
      ((Progress) progress).setValue(value);
    }
  }
}
