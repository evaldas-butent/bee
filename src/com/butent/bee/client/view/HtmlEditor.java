package com.butent.bee.client.view;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.DecisionCallback;
import com.butent.bee.client.dialog.DialogConstants;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.richtext.RichTextEditor;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Frame;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumSet;

public class HtmlEditor extends Flow implements Presenter, View, Printable {

  private static final BeeLogger logger = LogUtils.getLogger(HtmlEditor.class);

  private static final String STYLE_PREFIX = "bee-HtmlEditor-";

  private static final String STYLE_VIEW = STYLE_PREFIX + "view";
  private static final String STYLE_CANVAS = STYLE_PREFIX + "canvas";

  private static final String STYLE_COMMAND = STYLE_PREFIX + "command";
  private static final String STYLE_FRAME = STYLE_PREFIX + "frame";
  private static final String STYLE_LABEL = STYLE_PREFIX + "label";

  private static final String STYLE_SUFFIX_URL = "-url";
  private static final String STYLE_SUFFIX_HTML = "-html";
  private static final String STYLE_SUFFIX_TEXT = "-text";

  private static final String STYLE_SUFFIX_EMPTY = "-empty";
  private static final String STYLE_SUFFIX_NOT_EMPTY = "-notEmpty";

  private final String caption;

  private final String oldUrl;
  private final String oldHtml;

  private String currentUrl;
  private String currentHtml;

  private final HeaderView headerView;
  private final Flow canvas;

  private final Frame urlFrame;

  private final CustomDiv htmlLabel;
  private final CustomDiv textLabel;

  private final InputArea inputArea;
  private final RichTextEditor richText;

  private final BiConsumer<String, String> onSave;

  private boolean enabled = true;

  public HtmlEditor(String caption, String url, String html, BiConsumer<String, String> onSave) {
    super();
    addStyleName(STYLE_VIEW);

    this.caption = caption;

    this.oldUrl = url;
    this.oldHtml = html;

    this.currentUrl = url;
    this.currentHtml = html;

    this.onSave = onSave;

    this.headerView = new HeaderSilverImpl();
    headerView.create(caption, false, true, EnumSet.of(UiOption.ROOT),
        EnumSet.of(Action.SAVE, Action.PRINT, Action.CLOSE), Action.NO_ACTIONS, Action.NO_ACTIONS);

    headerView.setViewPresenter(this);
    add(headerView);

    this.canvas = new Flow();
    canvas.addStyleName(STYLE_CANVAS);

    this.urlFrame = new Frame();
    urlFrame.addStyleName(STYLE_FRAME + STYLE_SUFFIX_URL);
    canvas.add(urlFrame);

    this.htmlLabel = new CustomDiv(STYLE_LABEL + STYLE_SUFFIX_HTML);
    canvas.add(htmlLabel);

    this.textLabel = new CustomDiv(STYLE_LABEL + STYLE_SUFFIX_TEXT);
    canvas.add(textLabel);

    add(canvas);

    this.inputArea = new InputArea();
    inputArea.addStyleName(STYLE_PREFIX + "inputArea");

    this.richText = new RichTextEditor(true);
    richText.addStyleName(STYLE_PREFIX + "richText");

    initHeader();
    initCanvas();
  }

  @Override
  public String getCaption() {
    return caption;
  }

  @Override
  public String getEventSource() {
    return null;
  }

  @Override
  public HeaderView getHeader() {
    return headerView;
  }

  @Override
  public View getMainView() {
    return this;
  }

  @Override
  public Element getPrintElement() {
    return getElement();
  }

  @Override
  public Presenter getViewPresenter() {
    return this;
  }

  @Override
  public IdentifiableWidget getWidget() {
    return this;
  }

  @Override
  public String getWidgetId() {
    return getId();
  }

  @Override
  public void handleAction(Action action) {
    switch (action) {
      case SAVE:
        saveAndClose();
        break;

      case PRINT:
        Printer.print(this);
        break;

      case CLOSE:
        if (changed()) {
          DecisionCallback callback = new DecisionCallback() {
            @Override
            public void onConfirm() {
              saveAndClose();
            }

            @Override
            public void onDeny() {
              close();
            }
          };

          Global.decide(getCaption(), Lists.newArrayList(Localized.constants.saveChanges()),
              callback, DialogConstants.DECISION_YES);

        } else {
          close();
        }
        break;

      default:
        logger.info(getCaption(), action, "not implemented");
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean onPrint(Element source, Element target) {
    if (headerView.asWidget().getElement().isOrHasChild(source)) {
      return headerView.onPrint(source, target);
    } else {
      return true;
    }
  }

  @Override
  public void onResize() {
    super.onResize();
    updateSizes();
  }

  @Override
  public void onViewUnload() {
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void setEventSource(String eventSource) {
  }

  @Override
  public void setViewPresenter(Presenter viewPresenter) {
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    updateSizes();
  }

  private boolean changed() {
    return !BeeUtils.equalsTrim(oldUrl, getCurrentUrl())
        || !BeeUtils.equalsTrim(oldHtml, getCurrentHtml());
  }

  private void close() {
    BeeKeeper.getScreen().closeWidget(this);
  }

  private void editHtml() {
    richText.setValue(getCurrentHtml());

    Global.inputWidget("Html", richText, new InputCallback() {
      @Override
      public void onSuccess() {
        if (!BeeUtils.equalsTrim(richText.getValue(), getCurrentHtml())) {
          updateHtml(richText.getValue());
        }
      }
    });
  }

  private void editText() {
    inputArea.setValue(getCurrentHtml());

    Global.inputWidget("Text", inputArea, new InputCallback() {
      @Override
      public void onSuccess() {
        if (!BeeUtils.equalsTrim(inputArea.getValue(), getCurrentHtml())) {
          updateHtml(inputArea.getValue());
        }
      }
    });
  }

  private void editUrl() {
    Global.inputString("Url", null, new StringCallback(false) {
      @Override
      public void onSuccess(String value) {
        if (!BeeUtils.equalsTrim(value, getCurrentUrl())) {
          updateUrl(value);
        }
      }
    }, getCurrentUrl());
  }

  private String getCurrentHtml() {
    return currentHtml;
  }

  private String getCurrentUrl() {
    return currentUrl;
  }

  private boolean hasHtml() {
    return !BeeUtils.isEmpty(getCurrentHtml());
  }

  private boolean hasUrl() {
    return !BeeUtils.isEmpty(getCurrentUrl());
  }

  private void initCanvas() {
    if (hasUrl()) {
      urlFrame.setUrl(getCurrentUrl());
      urlFrame.addStyleName(STYLE_FRAME + STYLE_SUFFIX_URL + STYLE_SUFFIX_NOT_EMPTY);

    } else {
      urlFrame.addStyleName(STYLE_FRAME + STYLE_SUFFIX_URL + STYLE_SUFFIX_EMPTY);
      StyleUtils.setSize(urlFrame, 0, 0);
    }

    if (hasHtml()) {
      htmlLabel.setHTML(getCurrentHtml());
      htmlLabel.addStyleName(STYLE_LABEL + STYLE_SUFFIX_HTML + STYLE_SUFFIX_NOT_EMPTY);

      textLabel.setText(getCurrentHtml());
      textLabel.addStyleName(STYLE_LABEL + STYLE_SUFFIX_TEXT + STYLE_SUFFIX_NOT_EMPTY);

    } else {
      htmlLabel.addStyleName(STYLE_LABEL + STYLE_SUFFIX_HTML + STYLE_SUFFIX_EMPTY);
      textLabel.addStyleName(STYLE_LABEL + STYLE_SUFFIX_TEXT + STYLE_SUFFIX_EMPTY);
    }
  }

  private void initHeader() {
    Button urlCommamnd = new Button("Url", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        editUrl();
      }
    });

    urlCommamnd.addStyleName(STYLE_COMMAND);
    urlCommamnd.addStyleName(STYLE_COMMAND + STYLE_SUFFIX_URL);

    headerView.addCommandItem(urlCommamnd);

    Button htmlCommamnd = new Button("Html", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        editHtml();
      }
    });

    htmlCommamnd.addStyleName(STYLE_COMMAND);
    htmlCommamnd.addStyleName(STYLE_COMMAND + STYLE_SUFFIX_HTML);

    headerView.addCommandItem(htmlCommamnd);

    Button textCommamnd = new Button("Text", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        editText();
      }
    });

    textCommamnd.addStyleName(STYLE_COMMAND);
    textCommamnd.addStyleName(STYLE_COMMAND + STYLE_SUFFIX_TEXT);

    headerView.addCommandItem(textCommamnd);
  }

  private void saveAndClose() {
    onSave.accept(getCurrentUrl(), getCurrentHtml());
    close();
  }

  private void setCurrentHtml(String currentHtml) {
    this.currentHtml = currentHtml;
  }

  private void setCurrentUrl(String currentUrl) {
    this.currentUrl = currentUrl;
  }

  private void updateHtml(String newHtml) {
    boolean had = hasHtml();
    setCurrentHtml(BeeUtils.trim(newHtml));

    if (hasHtml() != had) {
      updateSizes();
    }

    if (hasHtml()) {
      htmlLabel.setHTML(getCurrentHtml());
      textLabel.setText(getCurrentHtml());

      if (!had) {
        htmlLabel.removeStyleName(STYLE_LABEL + STYLE_SUFFIX_HTML + STYLE_SUFFIX_EMPTY);
        textLabel.removeStyleName(STYLE_LABEL + STYLE_SUFFIX_TEXT + STYLE_SUFFIX_EMPTY);
      }
      htmlLabel.addStyleName(STYLE_LABEL + STYLE_SUFFIX_HTML + STYLE_SUFFIX_NOT_EMPTY);
      textLabel.addStyleName(STYLE_LABEL + STYLE_SUFFIX_TEXT + STYLE_SUFFIX_NOT_EMPTY);

    } else if (had) {
      htmlLabel.setHTML(BeeConst.STRING_EMPTY);
      textLabel.setText(BeeConst.STRING_EMPTY);

      htmlLabel.removeStyleName(STYLE_LABEL + STYLE_SUFFIX_HTML + STYLE_SUFFIX_NOT_EMPTY);
      textLabel.removeStyleName(STYLE_LABEL + STYLE_SUFFIX_TEXT + STYLE_SUFFIX_NOT_EMPTY);

      htmlLabel.addStyleName(STYLE_LABEL + STYLE_SUFFIX_HTML + STYLE_SUFFIX_EMPTY);
      textLabel.addStyleName(STYLE_LABEL + STYLE_SUFFIX_TEXT + STYLE_SUFFIX_EMPTY);
    }
  }

  private void updateSizes() {
    if (!hasUrl() && !hasHtml()) {
      return;
    }

    int width = BeeUtils.positive(canvas.getOffsetWidth(),
        BeeKeeper.getScreen().getActivePanelWidth());
    int height = BeeUtils.positive(canvas.getOffsetHeight(),
        BeeKeeper.getScreen().getActivePanelHeight());

    int margin = 5;

    if (width <= 20 || height <= 20) {
      return;
    }

    if (hasUrl() && hasHtml()) {
      StyleUtils.setLeft(urlFrame, margin);
      StyleUtils.setTop(urlFrame, margin);
      StyleUtils.setSize(urlFrame, width / 2 - margin * 2, height - margin * 2);

      StyleUtils.setLeft(htmlLabel, width / 2 + margin);
      StyleUtils.setTop(htmlLabel, margin);
      StyleUtils.setSize(htmlLabel, width / 2 - margin * 2, height / 2 - margin * 2);

      StyleUtils.setLeft(textLabel, width / 2 + margin);
      StyleUtils.setTop(textLabel, height / 2 + margin);
      StyleUtils.setSize(textLabel, width / 2 - margin * 2, height / 2 - margin * 2);

    } else if (hasUrl()) {
      StyleUtils.setLeft(urlFrame, margin);
      StyleUtils.setTop(urlFrame, margin);
      StyleUtils.setSize(urlFrame, width - margin * 2, height - margin * 2);

      StyleUtils.setSize(htmlLabel, 0, 0);
      StyleUtils.setSize(textLabel, 0, 0);

    } else if (hasHtml()) {
      StyleUtils.setSize(urlFrame, 0, 0);

      StyleUtils.setLeft(htmlLabel, margin);
      StyleUtils.setTop(htmlLabel, margin);
      StyleUtils.setSize(htmlLabel, width / 2 - margin * 2, height - margin * 2);

      StyleUtils.setLeft(textLabel, width / 2 + margin);
      StyleUtils.setTop(textLabel, margin);
      StyleUtils.setSize(textLabel, width / 2 - margin * 2, height - margin * 2);
    }
  }

  private void updateUrl(String newUrl) {
    boolean had = hasUrl();
    setCurrentUrl(BeeUtils.trim(newUrl));

    if (hasUrl() != had) {
      updateSizes();
    }

    if (hasUrl()) {
      urlFrame.setUrl(getCurrentUrl());
      if (!had) {
        urlFrame.removeStyleName(STYLE_FRAME + STYLE_SUFFIX_URL + STYLE_SUFFIX_EMPTY);
      }
      urlFrame.addStyleName(STYLE_FRAME + STYLE_SUFFIX_URL + STYLE_SUFFIX_NOT_EMPTY);

    } else if (had) {
      urlFrame.setUrl(BeeConst.STRING_EMPTY);
      urlFrame.removeStyleName(STYLE_FRAME + STYLE_SUFFIX_URL + STYLE_SUFFIX_NOT_EMPTY);
      urlFrame.addStyleName(STYLE_FRAME + STYLE_SUFFIX_URL + STYLE_SUFFIX_EMPTY);
    }
  }
}
