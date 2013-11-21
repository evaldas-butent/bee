package com.butent.bee.client.view.grid;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.WidgetCreationCallback;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.List;

public final class ExtWidget {

  public enum Component {
    HEADER, FOOTER, SCROLLER, CONTENT
  }

  private static final BeeLogger logger = LogUtils.getLogger(ExtWidget.class);

  private static final String ATTR_PRECEDES = "precedes";
  private static final String ATTR_HIDABLE = "hidable";

  public static ExtWidget create(String xml, String viewName, List<BeeColumn> dataColumns,
      WidgetCreationCallback creationCallback, GridInterceptor gridInterceptor) {
    Document doc = XmlUtils.parse(xml);
    if (doc == null) {
      return null;
    }

    Element root = doc.getDocumentElement();
    if (root == null) {
      logger.severe("ext widget: document element not found", xml);
      return null;
    }
    if (gridInterceptor != null && !gridInterceptor.onLoadExtWidget(root)) {
      return null;
    }

    String tagName = XmlUtils.getLocalName(root);
    Direction direction = EnumUtils.getEnumByName(Direction.class, tagName);
    if (!Split.validDirection(direction, false)) {
      logger.severe("ext widget: invalid root tag name", tagName);
      return null;
    }

    int size = BeeUtils.unbox(XmlUtils.getAttributeInteger(root, FormWidget.ATTR_SIZE));
    if (size <= 0) {
      logger.severe("ext widget size must be positive integer");
      return null;
    }

    IdentifiableWidget widget =
        FormFactory.createWidget(root, viewName, dataColumns, creationCallback,
            gridInterceptor, "create ext widget:");
    if (widget == null) {
      return null;
    }

    Integer splSize = XmlUtils.getAttributeInteger(root, FormWidget.ATTR_SPLITTER_SIZE);

    Component precedes = EnumUtils.getEnumByName(Component.class, root.getAttribute(ATTR_PRECEDES));
    boolean hidable = !BeeUtils.isFalse(XmlUtils.getAttributeBoolean(root, ATTR_HIDABLE));

    return new ExtWidget(widget, direction, size, splSize, precedes, hidable);
  }

  private final IdentifiableWidget widget;

  private final Direction direction;
  private final int size;
  private final Integer splSize;

  private final Component precedes;
  private final boolean hidable;

  private ExtWidget(IdentifiableWidget widget, Direction direction, int size, Integer splSize,
      Component precedes, boolean hidable) {
    super();
    this.widget = widget;
    this.direction = direction;
    this.size = size;
    this.splSize = splSize;
    this.precedes = precedes;
    this.hidable = hidable;
  }

  public Direction getDirection() {
    return direction;
  }

  public int getSize() {
    return size;
  }

  public Integer getSplSize() {
    return splSize;
  }

  public int getTotalSize() {
    return getSize() + BeeUtils.toNonNegativeInt(getSplSize());
  }

  public IdentifiableWidget getWidget() {
    return widget;
  }

  public boolean isHidable() {
    return hidable;
  }

  public boolean precedesFooter() {
    return Component.FOOTER.equals(precedes);
  }

  public boolean precedesHeader() {
    return Component.HEADER.equals(precedes);
  }
}
