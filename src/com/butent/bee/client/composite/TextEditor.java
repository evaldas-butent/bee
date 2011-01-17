package com.butent.bee.client.composite;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.ui.Composite;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.BeeTextArea;
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.BeeService;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.utils.BeeUtils;

public class TextEditor extends Composite implements HasId {

  protected class SaveCommand extends BeeCommand {
    @Override
    public void execute() {
      BeeTextArea area = getTextArea();
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
      if (!Global.confirm("Save " + BeeUtils.bracket(len), path)) {
        return;
      }
      
      String digest = JsUtils.md5(v);

      ParameterList params = new ParameterList(BeeService.SERVICE_SAVE_RESOURCE);
      params.addHeaderItem(BeeService.RPC_VAR_URI, path);
      params.addHeaderItem(BeeService.RPC_VAR_MD5, digest);

      BeeKeeper.getRpc().makePostRequest(params, ContentType.RESOURCE, v);
      area.onAfterSave(digest);

      Global.inform("Sent to", path, digest);
    }
  }

  private BeeTextArea textArea = null;
  private String uri = null;

  public TextEditor(BeeResource resource) {
    BeeLayoutPanel p = new BeeLayoutPanel();
    double top = 0;
    double bottom = 0;

    String caption = BeeUtils.ifString(resource.getName(), resource.getUri());

    if (!BeeUtils.isEmpty(caption)) {
      BeeLabel label = new BeeLabel(caption);
      p.add(label);
      p.setWidgetVerticalPosition(label, Layout.Alignment.BEGIN);
      p.setWidgetLeftRight(label, 10, Unit.PCT, 10, Unit.PX);
      top = 2;
    }

    if (resource.isReadOnly()) {
      BeeLabel rd = new BeeLabel("read only");
      p.add(rd);
      p.setWidgetVerticalPosition(rd, Layout.Alignment.END);
      p.setWidgetHorizontalPosition(rd, Layout.Alignment.END);
      bottom = 1.5;
    } else if (!BeeUtils.isEmpty(resource.getUri())) {
      BeeButton button = new BeeButton("Save", new SaveCommand());
      p.add(button);
      p.setWidgetVerticalPosition(button, Layout.Alignment.END);
      p.setWidgetLeftWidth(button, 42, Unit.PCT, 16, Unit.PCT);
      bottom = 2;
    }

    BeeTextArea area = new BeeTextArea(resource);
    p.add(area);
    p.setWidgetTopBottom(area, top, Unit.EM, bottom, Unit.EM);

    initWidget(p);

    setTextArea(area);
    setUri(resource.getUri());
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "texteditor");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public BeeTextArea getTextArea() {
    return textArea;
  }

  public String getUri() {
    return uri;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setTextArea(BeeTextArea textArea) {
    this.textArea = textArea;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

}
