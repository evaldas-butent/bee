package com.butent.bee.client.render;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;

import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

public class FileLinkRenderer extends AbstractCellRenderer {

  private static final AnchorElement anchorElement = Document.get().createAnchorElement();

  private final int idIndex;
  private final int captionIndex;

  public FileLinkRenderer(int idIndex, int captionIndex) {
    super(null);

    this.idIndex = idIndex;
    this.captionIndex = captionIndex;
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }

    Long id = row.getLong(idIndex);
    String text = row.getString(captionIndex);

    if (!DataUtils.isId(id) || BeeUtils.isEmpty(text)) {
      return null;
    }

    anchorElement.setHref(FileUtils.getUrl(text, id));
    anchorElement.setInnerText(text);

    return anchorElement.getString();
  }
}
