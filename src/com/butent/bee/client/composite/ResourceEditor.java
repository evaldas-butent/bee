package com.butent.bee.client.composite;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.output.Printable;
import com.butent.bee.client.output.Printer;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.HeaderSilverImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Resource;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.EnumSet;

/**
 * Implements a text area editor user interface component.
 */
public class ResourceEditor extends Flow implements Presenter, View, Printable {

  private static final BeeLogger logger = LogUtils.getLogger(ResourceEditor.class);

  private static final String STYLE_PREFIX = "bee-ResourceEditor-";

  private final String uri;

  private final HeaderView headerView;
  private final InputArea textArea;

  private boolean enabled = true;

  public ResourceEditor(Resource resource) {
    super();
    addStyleName(STYLE_PREFIX + "view");

    this.uri = resource.getUri();

    String caption = BeeUtils.notEmpty(BeeUtils.getSuffix(uri, BeeConst.CHAR_SLASH),
        BeeUtils.getSuffix(uri, BeeConst.CHAR_BACKSLASH), uri);

    EnumSet<Action> actions = EnumSet.of(Action.PRINT, Action.CLOSE);
    if (!resource.isReadOnly()) {
      actions.add(Action.SAVE);
    }

    this.headerView = new HeaderSilverImpl();
    headerView.create(caption, false, true, EnumSet.of(UiOption.ROOT), actions, Action.NO_ACTIONS,
        Action.NO_ACTIONS);

    if (!BeeUtils.isEmpty(uri) && !uri.equals(caption)) {
      headerView.setCaptionTitle(uri);
    }

    headerView.setViewPresenter(this);
    add(headerView);

    this.textArea = new InputArea(resource);
    textArea.addStyleName(STYLE_PREFIX + "inputArea");

    Simple wrapper = new Simple(textArea);
    wrapper.addStyleName(STYLE_PREFIX + "wrapper");

    add(wrapper);
  }

  @Override
  public String getCaption() {
    return headerView.getCaption();
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
  public String getIdPrefix() {
    return "resource-editor";
  }

  @Override
  public View getMainView() {
    return this;
  }

  @Override
  public Element getPrintElement() {
    Element el = Document.get().createDivElement();
    el.addClassName(STYLE_PREFIX + "print");
    el.setInnerText(textArea.getValue());
    return el;
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
        save();
        break;

      case PRINT:
        Printer.print(this);
        break;

      case CANCEL:
      case CLOSE:
        BeeKeeper.getScreen().closeWidget(this);
        break;

      default:
        logger.warning(NameUtils.getName(this), action, "not implemented");
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean onPrint(Element source, Element target) {
    return true;
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

  private void save() {
    if (!textArea.isValueChanged()) {
      Global.showInfo(getCaption(), Lists.newArrayList("Value has not changed",
          textArea.getDigest()));
      return;
    }

    final String v = textArea.getValue();
    if (BeeUtils.isEmpty(v)) {
      Global.showInfo("Value is empty, not saved");
      return;
    }

    if (BeeUtils.isEmpty(uri)) {
      Global.showError("Unknown URI");
      return;
    }

    Global.confirm(getCaption(), Icon.QUESTION, Lists.newArrayList(uri,
        Localized.getConstants().saveChanges()), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        final String digest = Codec.md5(v);
        
        ParameterList params = new ParameterList(Service.SAVE_RESOURCE);
        params.addQueryItem(Service.RPC_VAR_URI, Codec.encodeBase64(uri));
        params.addQueryItem(Service.RPC_VAR_MD5, digest);
        
        BeeKeeper.getRpc().makePostRequest(params, ContentType.RESOURCE, v, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            if (!response.hasErrors()) {
              textArea.onAfterSave(digest);
            }
          }
        });
      }
    });
  }
}
