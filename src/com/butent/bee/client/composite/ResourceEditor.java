package com.butent.bee.client.composite;

import com.google.gwt.user.client.ui.Composite;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.LayoutPanel;
import com.butent.bee.client.layout.Layout.Alignment;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.shared.Resource;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.ui.CssUnit;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

/**
 * Implements a text area editor user interface component.
 */
public class ResourceEditor extends Composite implements IdentifiableWidget, HasCaption {

  protected class SaveCommand extends Command {
    @Override
    public void execute() {
      InputArea area = getTextArea();
      if (!area.isValueChanged()) {
        Global.inform("Value has not changed", area.getDigest());
        return;
      }

      String v = area.getValue();
      if (BeeUtils.isEmpty(v)) {
        Global.inform("Value is empty, not saved");
        return;
      }

      String path = getUri();
      if (BeeUtils.isEmpty(path)) {
        Global.showError("Unknown URI");
        return;
      }

      int len = v.length();
      if (!Global.nativeConfirm("Save " + BeeUtils.bracket(len), path)) {
        return;
      }

      String digest = Codec.md5(v);

      ParameterList params = new ParameterList(Service.SAVE_RESOURCE);
      params.addHeaderItem(Service.RPC_VAR_URI, path);
      params.addHeaderItem(Service.RPC_VAR_MD5, digest);

      BeeKeeper.getRpc().makePostRequest(params, ContentType.RESOURCE, v);
      area.onAfterSave(digest);

      Global.inform("Sent to", path, digest);
    }
  }

  private final InputArea textArea;
  private final String uri;
  private final String caption;

  public ResourceEditor(Resource resource) {
    LayoutPanel p = new LayoutPanel();
    double top = 0;
    double bottom = 0;

    this.textArea = new InputArea(resource);
    this.uri = resource.getUri();

    this.caption = BeeUtils.notEmpty(BeeUtils.getSuffix(uri, '/'), BeeUtils.getSuffix(uri, '\\'),
        uri);

    Label label = new Label(uri);
    p.add(label);
    p.setWidgetVerticalPosition(label, Alignment.BEGIN);
    p.setWidgetLeftRight(label, 10, CssUnit.PX, 10, CssUnit.PX);
    top = 2;

    if (resource.isReadOnly()) {
      Label rd = new Label("read only");
      p.add(rd);
      p.setWidgetVerticalPosition(rd, Alignment.END);
      p.setWidgetHorizontalPosition(rd, Alignment.END);
      bottom = 1.5;
    } else if (!BeeUtils.isEmpty(resource.getUri())) {
      Button button = new Button("Save", new SaveCommand());
      p.add(button);
      p.setWidgetVerticalPosition(button, Alignment.END);
      p.setWidgetLeftWidth(button, 42, CssUnit.PCT, 16, CssUnit.PCT);
      bottom = 2;
    }

    p.add(textArea);
    p.setWidgetTopBottom(textArea, top, CssUnit.EM, bottom, CssUnit.EM);
    
    p.getElement().getStyle().clearPosition();

    initWidget(p);
    DomUtils.createId(this, getIdPrefix());
  }

  @Override
  public String getCaption() {
    return caption;
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "resource-editor";
  }

  public InputArea getTextArea() {
    return textArea;
  }

  public String getUri() {
    return uri;
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}
