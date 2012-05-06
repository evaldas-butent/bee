package com.butent.bee.client.ui;

import com.google.common.collect.Lists;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.MediaBase;
import com.google.gwt.media.client.Video;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.CustomButton.Face;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.Disclosure;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.composite.RadioGroup;
import com.butent.bee.client.composite.SliderBar;
import com.butent.bee.client.composite.StringPicker;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.composite.ValueSpinner;
import com.butent.bee.client.composite.VolumeSlider;
import com.butent.bee.client.decorator.TuningFactory;
import com.butent.bee.client.dom.Dimensions;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Features;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.images.Images;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.HeaderContentFooter;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.HtmlPanel;
import com.butent.bee.client.layout.ResizePanel;
import com.butent.bee.client.layout.Scroll;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.layout.SimpleInline;
import com.butent.bee.client.layout.Span;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.layout.Stack;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.client.tree.HasTreeItems;
import com.butent.bee.client.tree.Tree;
import com.butent.bee.client.tree.TreeItem;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.view.TreeContainer;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeFrame;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.CustomWidget;
import com.butent.bee.client.widget.DateLabel;
import com.butent.bee.client.widget.DateTimeLabel;
import com.butent.bee.client.widget.DecimalLabel;
import com.butent.bee.client.widget.DoubleLabel;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.InlineInternalLink;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.client.widget.InputFile;
import com.butent.bee.client.widget.InputInteger;
import com.butent.bee.client.widget.InputLong;
import com.butent.bee.client.widget.InputNumber;
import com.butent.bee.client.widget.InputSlider;
import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.IntegerLabel;
import com.butent.bee.client.widget.InternalLink;
import com.butent.bee.client.widget.Link;
import com.butent.bee.client.widget.LongLabel;
import com.butent.bee.client.widget.Meter;
import com.butent.bee.client.widget.Progress;
import com.butent.bee.client.widget.Svg;
import com.butent.bee.client.widget.TextLabel;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.HasNumberBounds;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.HasService;
import com.butent.bee.shared.HasStage;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Launchable;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.ConditionalStyleDeclaration;
import com.butent.bee.shared.ui.HasTextDimensions;
import com.butent.bee.shared.ui.HasValueStartIndex;
import com.butent.bee.shared.ui.HasVisibleLines;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.ui.RendererDescription;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.XmlHelper;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains available form elements.
 */

public enum FormWidget {
  ABSOLUTE_PANEL("AbsolutePanel", EnumSet.of(Type.HAS_LAYERS)),
  AUDIO("Audio", EnumSet.of(Type.DISPLAY)),
  BR("br", null),
  BUTTON("Button", EnumSet.of(Type.DISPLAY)),
  CANVAS("Canvas", EnumSet.of(Type.DISPLAY)),
  CHECK_BOX("CheckBox", EnumSet.of(Type.EDITABLE)),
  CHILD_GRID("ChildGrid", EnumSet.of(Type.IS_CHILD, Type.IS_GRID)),
  COMPLEX_PANEL("ComplexPanel", EnumSet.of(Type.HAS_LAYERS)),
  CURRENCY_LABEL("CurrencyLabel", EnumSet.of(Type.DISPLAY)),
  CUSTOM("Custom", EnumSet.of(Type.IS_CUSTOM)),
  CUSTOM_CHILD("CustomChild", EnumSet.of(Type.IS_CUSTOM, Type.IS_CHILD)),
  CUSTOM_DISPLAY("CustomDisplay", EnumSet.of(Type.IS_CUSTOM, Type.DISPLAY)),
  CUSTOM_EDITABLE("CustomEditable", EnumSet.of(Type.IS_CUSTOM, Type.EDITABLE)),
  CUSTOM_FOCUSABLE("CustomFocusable", EnumSet.of(Type.IS_CUSTOM, Type.FOCUSABLE, Type.EDITABLE)),
  DATA_SELECTOR("DataSelector", EnumSet.of(Type.FOCUSABLE, Type.EDITABLE, Type.DISPLAY)),
  DATE_LABEL("DateLabel", EnumSet.of(Type.DISPLAY)),
  DATE_TIME_LABEL("DateTimeLabel", EnumSet.of(Type.DISPLAY)),
  DECIMAL_LABEL("DecimalLabel", EnumSet.of(Type.DISPLAY)),
  DECORATOR("decorator", EnumSet.of(Type.IS_DECORATOR)),
  DISCLOSURE("Disclosure", EnumSet.of(Type.HAS_CHILDREN)),
  DOUBLE_LABEL("DoubleLabel", EnumSet.of(Type.DISPLAY)),
  FLEX_TABLE("FlexTable", EnumSet.of(Type.TABLE)),
  FLOW_PANEL("FlowPanel", EnumSet.of(Type.HAS_CHILDREN)),
  FRAME("Frame", EnumSet.of(Type.DISPLAY)),
  GRID_PANEL("GridPanel", EnumSet.of(Type.IS_GRID)),
  HEADER_CONTENT_FOOTER("HeaderContentFooter", EnumSet.of(Type.PANEL)),
  HORIZONTAL_PANEL("HorizontalPanel", EnumSet.of(Type.CELL_VECTOR)),
  HR("hr", null),
  HTML_LABEL("HtmlLabel", EnumSet.of(Type.DISPLAY)),
  HTML_PANEL("HtmlPanel", EnumSet.of(Type.PANEL)),
  HYPERLINK("Hyperlink", EnumSet.of(Type.DISPLAY)),
  IMAGE("Image", EnumSet.of(Type.DISPLAY)),
  INLINE_HYPERLINK("InlineHyperlink", EnumSet.of(Type.DISPLAY)),
  INLINE_LABEL("InlineLabel", EnumSet.of(Type.IS_LABEL)),
  INPUT_AREA("InputArea", EnumSet.of(Type.FOCUSABLE, Type.EDITABLE, Type.INPUT)),
  INPUT_CURRENCY("InputCurrency", EnumSet.of(Type.FOCUSABLE, Type.EDITABLE, Type.INPUT)),
  INPUT_DATE("InputDate", EnumSet.of(Type.FOCUSABLE, Type.EDITABLE, Type.INPUT)),
  INPUT_DATE_TIME("InputDateTime", EnumSet.of(Type.FOCUSABLE, Type.EDITABLE, Type.INPUT)),
  INPUT_DECIMAL("InputDecimal", EnumSet.of(Type.FOCUSABLE, Type.EDITABLE, Type.INPUT)),
  INPUT_DOUBLE("InputDouble", EnumSet.of(Type.FOCUSABLE, Type.EDITABLE, Type.INPUT)),
  INPUT_FILE("InputFile", EnumSet.of(Type.INPUT)),
  INPUT_INTEGER("InputInteger", EnumSet.of(Type.FOCUSABLE, Type.EDITABLE, Type.INPUT)),
  INPUT_LONG("InputLong", EnumSet.of(Type.FOCUSABLE, Type.EDITABLE, Type.INPUT)),
  INPUT_SLIDER("InputSlider", EnumSet.of(Type.EDITABLE, Type.INPUT)),
  INPUT_SPINNER("InputSpinner", EnumSet.of(Type.FOCUSABLE, Type.EDITABLE, Type.INPUT)),
  INPUT_TEXT("InputText", EnumSet.of(Type.FOCUSABLE, Type.EDITABLE, Type.INPUT)),
  INTEGER_LABEL("IntegerLabel", EnumSet.of(Type.DISPLAY)),
  LABEL("Label", EnumSet.of(Type.IS_LABEL)),
  LAYOUT_PANEL("LayoutPanel", EnumSet.of(Type.HAS_LAYERS)),
  LINK("Link", EnumSet.of(Type.DISPLAY)),
  LIST_BOX("ListBox", EnumSet.of(Type.FOCUSABLE, Type.EDITABLE)),
  LONG_LABEL("LongLabel", EnumSet.of(Type.DISPLAY)),
  METER("Meter", EnumSet.of(Type.DISPLAY)),
  PROGRESS("Progress", EnumSet.of(Type.DISPLAY)),
  RADIO("Radio", EnumSet.of(Type.EDITABLE)),
  RESIZE_PANEL("ResizePanel", EnumSet.of(Type.HAS_ONE_CHILD)),
  SCROLL_PANEL("ScrollPanel", EnumSet.of(Type.HAS_ONE_CHILD)),
  SIMPLE_INLINE_PANEL("SimpleInlinePanel", EnumSet.of(Type.HAS_ONE_CHILD)),
  SIMPLE_PANEL("SimplePanel", EnumSet.of(Type.HAS_ONE_CHILD)),
  SLIDER_BAR("SliderBar", EnumSet.of(Type.EDITABLE)),
  SPAN_PANEL("SpanPanel", EnumSet.of(Type.HAS_CHILDREN)),
  SPLIT_PANEL("SplitPanel", EnumSet.of(Type.PANEL)),
  STACK_PANEL("StackPanel", EnumSet.of(Type.PANEL)),
  STRING_PICKER("StringPicker", EnumSet.of(Type.EDITABLE)),
  SVG("Svg", EnumSet.of(Type.DISPLAY)),
  TAB_BAR("TabBar", EnumSet.of(Type.DISPLAY)),
  TABBED_PAGES("TabbedPages", EnumSet.of(Type.PANEL)),
  TEXT_LABEL("TextLabel", EnumSet.of(Type.DISPLAY)),
  TOGGLE("Toggle", EnumSet.of(Type.EDITABLE)),
  VALUE_SPINNER("ValueSpinner", EnumSet.of(Type.EDITABLE)),
  VERTICAL_PANEL("VerticalPanel", EnumSet.of(Type.CELL_VECTOR)),
  VIDEO("Video", EnumSet.of(Type.DISPLAY)),
  VOLUME_SLIDER("VolumeSlider", EnumSet.of(Type.EDITABLE)),
  TREE("Tree", EnumSet.of(Type.FOCUSABLE)),
  DATA_TREE("DataTree", EnumSet.of(Type.FOCUSABLE)),
  CHILD_TREE("ChildTree", EnumSet.of(Type.IS_CHILD, Type.FOCUSABLE));

  private class HeaderAndContent {
    private final String headerTag;
    private final String headerString;
    private final Widget headerWidget;
    private final Widget content;

    private HeaderAndContent(String headerTag, String headerString, Widget headerWidget,
        Widget content) {
      this.headerTag = headerTag;
      this.headerString = headerString;
      this.headerWidget = headerWidget;
      this.content = content;
    }

    private Widget getContent() {
      return content;
    }

    private String getHeaderString() {
      return headerString;
    }

    private String getHeaderTag() {
      return headerTag;
    }

    private Widget getHeaderWidget() {
      return headerWidget;
    }

    private boolean isHeaderHtml() {
      return BeeUtils.same(getHeaderTag(), TAG_HTML);
    }

    private boolean isHeaderText() {
      return BeeUtils.same(getHeaderTag(), TAG_TEXT);
    }

    private boolean isValid() {
      if (getContent() == null) {
        return false;
      }

      if (isHeaderHtml() || isHeaderText()) {
        return !BeeUtils.isEmpty(getHeaderString());
      } else {
        return getHeaderWidget() != null;
      }
    }
  }

  /**
   * Contains a list of possible form element parameters like editable or focusable.
   */

  private enum Type {
    FOCUSABLE, EDITABLE, IS_LABEL, DISPLAY, HAS_ONE_CHILD, HAS_CHILDREN, HAS_LAYERS,
    TABLE, IS_CHILD, IS_GRID, PANEL, CELL_VECTOR, INPUT, IS_CUSTOM, IS_DECORATOR
  }

  public static final String ATTR_SCROLL_BARS = "scrollBars";
  public static final String ATTR_SPLITTER_SIZE = "splitterSize";
  public static final String ATTR_SIZE = "size";

  private static final String ATTR_STYLE_PREFIX = "stylePrefix";

  private static final String ATTR_HTML = "html";
  private static final String ATTR_TITLE = "title";
  private static final String ATTR_VISIBLE = "visible";
  private static final String ATTR_DISABLABLE = "disablable";

  private static final String ATTR_INLINE = "inline";
  private static final String ATTR_FORMAT = "format";
  private static final String ATTR_SCALE = "scale";

  private static final String ATTR_URL = "url";
  private static final String ATTR_HISTORY_TOKEN = "historyToken";
  private static final String ATTR_STAGE = "stage";
  private static final String ATTR_UNIT = "unit";
  private static final String ATTR_TAB_INDEX = "tabIndex";

  private static final String ATTR_LEFT = "left";
  private static final String ATTR_LEFT_UNIT = "leftUnit";
  private static final String ATTR_RIGHT = "right";
  private static final String ATTR_RIGHT_UNIT = "rightUnit";
  private static final String ATTR_TOP = "top";
  private static final String ATTR_TOP_UNIT = "topUnit";
  private static final String ATTR_BOTTOM = "bottom";
  private static final String ATTR_BOTTOM_UNIT = "bottomUnit";

  private static final String ATTR_COL_SPAN = "colSpan";
  private static final String ATTR_ROW_SPAN = "rowSpan";
  private static final String ATTR_CELL_PADDING = "cellPadding";
  private static final String ATTR_CELL_SPACING = "cellSpacing";
  private static final String ATTR_BORDER_WIDTH = "borderWidth";
  private static final String ATTR_WORD_WRAP = "wordWrap";
  private static final String ATTR_INDEX = "index";

  private static final String ATTR_HEADER_SIZE = "headerSize";

  private static final String ATTR_RESOURCE = "resource";
  private static final String ATTR_VERTICAL = "vertical";

  private static final String ATTR_MULTI_SELECT = "multiSelect";
  private static final String ATTR_ALL_ITEMS_VISIBLE = "allItemsVisible";
  private static final String ATTR_MIN_SIZE = "minSize";
  private static final String ATTR_MAX_SIZE = "maxSize";

  private static final String ATTR_VALUE_NUMERIC = "valueNumeric";

  private static final String ATTR_MIN = "min";
  private static final String ATTR_MAX = "max";
  private static final String ATTR_STEP = "step";

  private static final String ATTR_MIN_STEP = "minStep";
  private static final String ATTR_MAX_STEP = "maxStep";
  private static final String ATTR_CONSTRAINED = "constrained";

  private static final String ATTR_HIGH = "high";
  private static final String ATTR_LOW = "low";
  private static final String ATTR_OPTIMUM = "optimum";

  private static final String ATTR_NUM_LABELS = "numLabels";
  private static final String ATTR_NUM_TICKS = "numTicks";

  private static final String ATTR_AUTOPLAY = "autoplay";
  private static final String ATTR_CONTROLS = "controls";
  private static final String ATTR_CURRENT_TIME = "currentTime";
  private static final String ATTR_DEFAULT_PLAYBACK_RATE = "defaultPlaybackRate";
  private static final String ATTR_LOOP = "loop";
  private static final String ATTR_MUTED = "muted";
  private static final String ATTR_PLAYBACK_RATE = "playbackRate";
  private static final String ATTR_PRELOAD = "preload";
  private static final String ATTR_VOLUME = "volume";

  private static final String ATTR_REL_COLUMN = "relColumn";

  private static final String ATTR_ANIMATE = "animate";
  private static final String ATTR_OPEN = "open";

  private static final String ATTR_EVENT = "event";

  private static final String ATTR_ID = "id";

  private static final String ATTR_CHECKED = "checked";
  private static final String ATTR_MULTIPLE = "multiple";

  private static final String ATTR_DECORATOR = "decorator";
  private static final String ATTR_DEFAULT_DECORATOR = "defaultDecorator";

  private static final String TAG_CSS = "css";
  private static final String TAG_DYN_STYLE = "dynStyle";
  private static final String TAG_HANDLER = "handler";

  private static final String TAG_CALC = "calc";
  private static final String TAG_VALIDATION = "validation";
  private static final String TAG_EDITABLE = "editable";
  private static final String TAG_CARRY = "carry";

  private static final String TAG_HTML = "html";
  private static final String TAG_TEXT = "text";

  private static final String TAG_LAYER = "layer";
  private static final String TAG_ROW = "row";
  private static final String TAG_COL = "col";
  private static final String TAG_CELL = "cell";
  private static final String TAG_HEADER = "header";
  private static final String TAG_CONTENT = "content";
  private static final String TAG_FOOTER = "footer";
  private static final String TAG_STACK = "stack";
  private static final String TAG_PAGE = "page";
  private static final String TAG_OPTION = "option";
  private static final String TAG_TREE_ITEM = "TreeItem";
  private static final String TAG_TAB = "tab";

  private static final String TAG_UP_FACE = "upFace";
  private static final String TAG_DOWN_FACE = "downFace";
  private static final String TAG_UP_HOVERING_FACE = "upHoveringFace";
  private static final String TAG_DOWN_HOVERING_FACE = "downHoveringFace";
  private static final String TAG_UP_DISABLED_FACE = "upDisabledFace";
  private static final String TAG_DOWN_DISABLED_FACE = "downDisabledFace";

  public static FormWidget getByTagName(String tagName) {
    if (!BeeUtils.isEmpty(tagName)) {
      for (FormWidget widget : FormWidget.values()) {
        if (BeeUtils.same(widget.getTagName(), tagName)) {
          return widget;
        }
      }
    }
    return null;
  }

  public static boolean isFormWidget(String tagName) {
    return getByTagName(tagName) != null;
  }

  private final String tagName;
  private final Set<Type> types;

  private FormWidget(String tagName, Set<Type> types) {
    this.tagName = tagName;
    this.types = types;
  }

  public Widget create(Element element, String viewName, List<BeeColumn> columns,
      WidgetDescriptionCallback widgetDescriptionCallback, WidgetCallback widgetCallback) {
    Assert.notNull(element);

    String name = element.getAttribute(UiConstants.ATTR_NAME);
    if (widgetCallback != null && !widgetCallback.beforeCreateWidget(name, element)) {
      return null;
    }

    if (BeeUtils.isFalse(XmlUtils.getAttributeBoolean(element, ATTR_VISIBLE))) {
      return null;
    }

    Map<String, String> attributes = XmlUtils.getAttributes(element, false);
    List<Element> children = XmlUtils.getChildrenElements(element);

    String html = attributes.get(ATTR_HTML);
    String url;
    String format;
    String min;
    String max;
    String step;
    boolean inline;
    String stylePrefix;

    Relation relation = null;
    Widget widget = null;

    switch (this) {
      case ABSOLUTE_PANEL:
        widget = new Absolute();
        break;

      case AUDIO:
        widget = Audio.createIfSupported();
        if (widget != null) {
          DomUtils.createId(widget, "audio");
          initMedia((Audio) widget, attributes);
        }
        break;

      case BR:
        widget = new CustomWidget(Document.get().createBRElement());
        break;

      case BUTTON:
        widget = new BeeButton(html);
        break;

      case CANVAS:
        widget = Canvas.createIfSupported();
        if (widget != null) {
          DomUtils.createId(widget, "canvas");
        }
        break;

      case CHECK_BOX:
        widget = new InputBoolean(html);
        if (BeeConst.isTrue(attributes.get(ATTR_CHECKED))) {
          ((InputBoolean) widget).setValue(BeeConst.STRING_TRUE);
        }
        break;

      case CHILD_GRID:
        String relColumn = attributes.get(ATTR_REL_COLUMN);
        String source = attributes.get(UiConstants.ATTR_SOURCE);
        int sourceIndex = BeeUtils.isEmpty(source)
            ? DataUtils.ID_INDEX : DataUtils.getColumnIndex(source, columns);

        if (!BeeUtils.isEmpty(name) && !BeeUtils.isEmpty(relColumn)
            && !BeeConst.isUndef(sourceIndex)) {
          widget = new ChildGrid(name, sourceIndex, relColumn);
        }
        break;

      case COMPLEX_PANEL:
        widget = new Complex();
        break;

      case CURRENCY_LABEL:
        format = attributes.get(ATTR_FORMAT);
        inline = BeeUtils.toBoolean(attributes.get(ATTR_INLINE));
        if (BeeUtils.isEmpty(format)) {
          widget = new DecimalLabel(Format.getDefaultCurrencyFormat(), inline);
        } else {
          widget = new DecimalLabel(format, inline);
        }
        break;

      case CUSTOM:
      case CUSTOM_CHILD:
      case CUSTOM_DISPLAY:
      case CUSTOM_EDITABLE:
      case CUSTOM_FOCUSABLE:
        if (widgetCallback != null) {
          widget = widgetCallback.createCustomWidget(name, element);
        }
        break;

      case DATA_SELECTOR:
        relation = createRelation(viewName, attributes, children);
        if (relation != null) {
          widget = new DataSelector(relation, true);
        }
        break;

      case DATE_LABEL:
        format = attributes.get(ATTR_FORMAT);
        inline = BeeUtils.toBoolean(attributes.get(ATTR_INLINE));
        if (BeeUtils.isEmpty(format)) {
          widget = new DateLabel(inline);
        } else {
          widget = new DateLabel(format, inline);
        }
        break;

      case DATE_TIME_LABEL:
        format = attributes.get(ATTR_FORMAT);
        inline = BeeUtils.toBoolean(attributes.get(ATTR_INLINE));
        if (BeeUtils.isEmpty(format)) {
          widget = new DateTimeLabel(inline);
        } else {
          widget = new DateTimeLabel(format, inline);
        }
        break;

      case DECIMAL_LABEL:
        format = attributes.get(ATTR_FORMAT);
        inline = BeeUtils.toBoolean(attributes.get(ATTR_INLINE));
        if (BeeUtils.isEmpty(format)) {
          widget = new DecimalLabel(BeeUtils.toInt(attributes.get(ATTR_SCALE)), inline);
        } else {
          widget = new DecimalLabel(format, inline);
        }
        break;

      case DISCLOSURE:
        if (BeeUtils.isEmpty(html)) {
          widget = new Disclosure();
        } else {
          widget = new Disclosure(new BeeLabel(html));
        }

        String animate = attributes.get(ATTR_ANIMATE);
        if (BeeUtils.isInt(animate)) {
          ((Disclosure) widget).setAnimationDuration(BeeUtils.toInt(animate));
        }
        if (BeeConst.isTrue(attributes.get(ATTR_OPEN))) {
          ((Disclosure) widget).setOpen(true);
        }
        break;

      case DOUBLE_LABEL:
        format = attributes.get(ATTR_FORMAT);
        inline = BeeUtils.toBoolean(attributes.get(ATTR_INLINE));
        if (BeeUtils.isEmpty(format)) {
          widget = new DoubleLabel(inline);
        } else {
          widget = new DoubleLabel(format, inline);
        }
        break;

      case FLEX_TABLE:
        widget = new FlexTable();
        break;

      case FLOW_PANEL:
        widget = new Flow();
        break;

      case FRAME:
        url = attributes.get(ATTR_URL);
        if (BeeUtils.isEmpty(url)) {
          widget = new BeeFrame();
        } else {
          widget = new BeeFrame(url);
        }
        break;

      case GRID_PANEL:
        if (!BeeUtils.isEmpty(name)) {
          widget = new GridPanel(name);
        }
        break;

      case HEADER_CONTENT_FOOTER:
        widget = new HeaderContentFooter();
        break;

      case HORIZONTAL_PANEL:
        widget = new Horizontal();
        break;

      case HR:
        widget = new CustomWidget(Document.get().createHRElement());
        break;

      case HTML_LABEL:
        if (BeeUtils.isEmpty(html)) {
          widget = new Html();
        } else {
          widget = new Html(html);
        }
        break;

      case HTML_PANEL:
        if (!children.isEmpty()) {
          StringBuilder sb = new StringBuilder();
          for (Element child : children) {
            sb.append(child.toString());
          }
          widget = new HtmlPanel(sb.toString());
          children.clear();
        }
        break;

      case HYPERLINK:
        url = attributes.get(ATTR_HISTORY_TOKEN);
        widget = new InternalLink(BeeUtils.ifString(html, url), url);
        break;

      case IMAGE:
        name = attributes.get(ATTR_RESOURCE);
        if (!BeeUtils.isEmpty(name)) {
          widget = new BeeImage(Images.get(name));
        } else {
          url = attributes.get(ATTR_URL);
          if (!BeeUtils.isEmpty(url)) {
            widget = new BeeImage(url);
          } else {
            widget = new BeeImage();
          }
        }
        break;

      case INLINE_HYPERLINK:
        url = attributes.get(ATTR_HISTORY_TOKEN);
        widget = new InlineInternalLink(BeeUtils.ifString(html, url), url);
        break;

      case INLINE_LABEL:
        widget = new InlineLabel(html);
        break;

      case INPUT_AREA:
        widget = new InputArea();
        break;

      case INPUT_CURRENCY:
        widget = new InputNumber();
        ((InputNumber) widget).setNumberFormat(Format.getNumberFormat(attributes.get(ATTR_FORMAT),
            Format.getDefaultCurrencyFormat()));
        break;

      case INPUT_DATE:
        format = attributes.get(ATTR_FORMAT);
        widget = new InputDate(ValueType.DATE, Format.getDateTimeFormat(format,
            Format.getDefaultDateFormat()));
        break;

      case INPUT_DATE_TIME:
        format = attributes.get(ATTR_FORMAT);
        widget = new InputDate(ValueType.DATETIME, Format.getDateTimeFormat(format,
            Format.getDefaultDateTimeFormat()));
        break;

      case INPUT_DECIMAL:
        widget = new InputNumber();
        ((InputNumber) widget).setNumberFormat(Format.getNumberFormat(attributes.get(ATTR_FORMAT),
            Format.getDecimalFormat(BeeUtils.toInt(attributes.get(ATTR_SCALE)))));
        break;

      case INPUT_DOUBLE:
        widget = new InputNumber();
        ((InputNumber) widget).setNumberFormat(Format.getNumberFormat(attributes.get(ATTR_FORMAT),
            Format.getDefaultDoubleFormat()));
        break;

      case INPUT_FILE:
        widget = new InputFile(BeeConst.isTrue(attributes.get(ATTR_MULTIPLE)));
        if (!BeeUtils.isEmpty(name)) {
          ((InputFile) widget).setName(name.trim());
        }
        break;

      case INPUT_INTEGER:
        widget = new InputInteger();
        ((InputInteger) widget).setNumberFormat(Format.getNumberFormat(attributes.get(ATTR_FORMAT),
            Format.getDefaultIntegerFormat()));
        break;

      case INPUT_LONG:
        widget = new InputLong();
        ((InputLong) widget).setNumberFormat(Format.getNumberFormat(attributes.get(ATTR_FORMAT),
            Format.getDefaultLongFormat()));
        break;

      case INPUT_SLIDER:
        widget = new InputSlider();
        format = attributes.get(ATTR_FORMAT);
        if (!BeeUtils.isEmpty(format)) {
          ((InputSlider) widget).setNumberFormat(Format.getNumberFormat(format));
        }
        break;

      case INPUT_SPINNER:
        widget = new InputSpinner();
        format = attributes.get(ATTR_FORMAT);
        if (!BeeUtils.isEmpty(format)) {
          ((InputSpinner) widget).setNumberFormat(Format.getNumberFormat(format));
        }
        break;

      case INPUT_TEXT:
        widget = new InputText();
        break;

      case INTEGER_LABEL:
        format = attributes.get(ATTR_FORMAT);
        inline = BeeUtils.toBoolean(attributes.get(ATTR_INLINE));
        if (BeeUtils.isEmpty(format)) {
          widget = new IntegerLabel(inline);
        } else {
          widget = new IntegerLabel(format, inline);
        }
        break;

      case LABEL:
        widget = new BeeLabel(html);
        break;

      case LAYOUT_PANEL:
        widget = new BeeLayoutPanel();
        break;

      case LINK:
        url = attributes.get(ATTR_URL);
        if (!BeeUtils.isEmpty(url)) {
          widget = new Link(html, true, url);
        } else if (!BeeUtils.isEmpty(html)) {
          widget = new Link(html, true);
        } else {
          widget = new Link();
        }
        break;

      case LIST_BOX:
        widget = new BeeListBox(BeeUtils.toBoolean(attributes.get(ATTR_MULTI_SELECT)));
        String isNum = attributes.get(ATTR_VALUE_NUMERIC);
        if (BeeUtils.isBoolean(isNum)) {
          ((BeeListBox) widget).setValueNumeric(BeeUtils.toBoolean(isNum));
        } else if (ValueType.isNumeric(DataUtils
            .getColumnType(attributes.get(UiConstants.ATTR_SOURCE), columns))) {
          ((BeeListBox) widget).setValueNumeric(true);
        }
        break;

      case LONG_LABEL:
        format = attributes.get(ATTR_FORMAT);
        inline = BeeUtils.toBoolean(attributes.get(ATTR_INLINE));
        if (BeeUtils.isEmpty(format)) {
          widget = new LongLabel(inline);
        } else {
          widget = new LongLabel(format, inline);
        }
        break;

      case METER:
        min = attributes.get(ATTR_MIN);
        max = attributes.get(ATTR_MAX);

        if (Features.supportsElementMeter() && BeeUtils.isDouble(min) && BeeUtils.isDouble(max)
            && BeeUtils.toDouble(min) < BeeUtils.toDouble(max)) {
          widget = new Meter();
          ((Meter) widget).setMin(BeeUtils.toDouble(min));
          ((Meter) widget).setMax(BeeUtils.toDouble(max));

          String z = attributes.get(ATTR_LOW);
          if (BeeUtils.isDouble(z)) {
            ((Meter) widget).setLow(BeeUtils.toDouble(z));
          }
          z = attributes.get(ATTR_HIGH);
          if (BeeUtils.isDouble(z)) {
            ((Meter) widget).setHigh(BeeUtils.toDouble(z));
          }
          z = attributes.get(ATTR_OPTIMUM);
          if (BeeUtils.isDouble(z)) {
            ((Meter) widget).setOptimum(BeeUtils.toDouble(z));
          }
        }
        break;

      case PROGRESS:
        max = attributes.get(ATTR_MAX);
        if (Features.supportsElementProgress() && BeeUtils.isPositiveDouble(max)) {
          widget = new Progress(BeeUtils.toDouble(max));
        }
        break;

      case RADIO:
        widget = new RadioGroup(BeeUtils.toBoolean(attributes.get(ATTR_VERTICAL)));
        break;

      case RESIZE_PANEL:
        widget = new ResizePanel();
        break;

      case SCROLL_PANEL:
        widget = new Scroll();
        break;

      case SIMPLE_INLINE_PANEL:
        widget = new SimpleInline();
        break;

      case SIMPLE_PANEL:
        widget = new Simple();
        break;

      case SLIDER_BAR:
        min = attributes.get(ATTR_MIN);
        max = attributes.get(ATTR_MAX);
        step = attributes.get(ATTR_STEP);

        if (BeeUtils.isDouble(min) && BeeUtils.isDouble(max) && BeeUtils.isDouble(step)
            && BeeUtils.toLong(min) < BeeUtils.toLong(max) && BeeUtils.isPositiveDouble(step)) {
          widget = new SliderBar(null, BeeUtils.toDouble(min), BeeUtils.toDouble(max),
              BeeUtils.toDouble(step));

          String z = attributes.get(ATTR_NUM_LABELS);
          if (BeeUtils.isDigit(z)) {
            ((SliderBar) widget).setNumLabels(BeeUtils.toInt(z));
          }
          z = attributes.get(ATTR_NUM_TICKS);
          if (BeeUtils.isDigit(z)) {
            ((SliderBar) widget).setNumTicks(BeeUtils.toInt(z));
          }
        }
        break;

      case SPAN_PANEL:
        widget = new Span();
        break;

      case SPLIT_PANEL:
        String ss = attributes.get(ATTR_SPLITTER_SIZE);
        if (BeeUtils.isDigit(ss)) {
          widget = new Split(BeeUtils.toInt(ss));
        } else {
          widget = new Split();
        }
        break;

      case STACK_PANEL:
        widget = new Stack(StyleUtils.parseUnit(attributes.get(ATTR_UNIT), Unit.PX));
        break;

      case STRING_PICKER:
        List<String> items = XmlUtils.getChildrenText(element, HasItems.TAG_ITEM);
        if (!BeeUtils.isEmpty(items)) {
          widget = new StringPicker();
          ((StringPicker) widget).setItems(items);
        }
        break;

      case SVG:
        if (Features.supportsSvg()) {
          widget = new Svg();
        }
        break;

      case TAB_BAR:
        stylePrefix = attributes.get(ATTR_STYLE_PREFIX);
        widget = BeeUtils.isEmpty(stylePrefix) ? new TabBar() : new TabBar(stylePrefix);
        break;

      case TABBED_PAGES:
        stylePrefix = attributes.get(ATTR_STYLE_PREFIX);
        widget = BeeUtils.isEmpty(stylePrefix) ? new TabbedPages() : new TabbedPages(stylePrefix);
        break;

      case TEXT_LABEL:
        widget = new TextLabel(BeeUtils.toBoolean(attributes.get(ATTR_INLINE)));
        break;

      case TOGGLE:
        widget = new Toggle();
        break;

      case VALUE_SPINNER:
        min = attributes.get(ATTR_MIN);
        max = attributes.get(ATTR_MAX);

        if (BeeUtils.isLong(min) && BeeUtils.isLong(max)
            && BeeUtils.toLong(min) < BeeUtils.toLong(max)) {
          step = attributes.get(ATTR_STEP);
          String minStep = attributes.get(ATTR_MIN_STEP);
          String maxStep = attributes.get(ATTR_MAX_STEP);
          String constrained = attributes.get(ATTR_CONSTRAINED);

          Object pSrc = null;
          long pMin = BeeUtils.toLong(min);
          long pMax = BeeUtils.toLong(max);

          boolean hasStep = BeeUtils.isInt(step) && BeeUtils.toInt(step) > 0;
          boolean hasStepBounds = BeeUtils.isInt(minStep) && BeeUtils.isInt(maxStep)
              && BeeUtils.toInt(minStep) > 0 && BeeUtils.toInt(maxStep) >= BeeUtils.toInt(minStep);

          if (BeeUtils.isBoolean(constrained)) {
            boolean pConstr = BeeUtils.toBoolean(constrained);
            if (hasStepBounds) {
              widget = new ValueSpinner(pSrc, pMin, pMax,
                  BeeUtils.toInt(minStep), BeeUtils.toInt(maxStep), pConstr);
            } else if (hasStep) {
              int z = BeeUtils.toInt(step);
              widget = new ValueSpinner(pSrc, pMin, pMax, z, z, pConstr);
            } else {
              widget = new ValueSpinner(pSrc, pMin, pMax, pConstr);
            }

          } else if (hasStepBounds) {
            widget = new ValueSpinner(pSrc, pMin, pMax,
                BeeUtils.toInt(minStep), BeeUtils.toInt(maxStep));
          } else if (hasStep) {
            widget = new ValueSpinner(pSrc, pMin, pMax, BeeUtils.toInt(step));
          } else {
            widget = new ValueSpinner(pSrc, pMin, pMax);
          }
        }
        break;

      case VERTICAL_PANEL:
        widget = new Vertical();
        break;

      case VIDEO:
        widget = Video.createIfSupported();
        if (widget != null) {
          DomUtils.createId(widget, "video");
          initMedia((Video) widget, attributes);
        }
        break;

      case VOLUME_SLIDER:
        min = attributes.get(ATTR_MIN);
        max = attributes.get(ATTR_MAX);

        if (BeeUtils.isLong(min) && BeeUtils.isLong(max)
            && BeeUtils.toLong(min) < BeeUtils.toLong(max)) {
          step = attributes.get(ATTR_STEP);
          String minStep = attributes.get(ATTR_MIN_STEP);
          String maxStep = attributes.get(ATTR_MAX_STEP);

          Object pSrc = null;
          long pMin = BeeUtils.toLong(min);
          long pMax = BeeUtils.toLong(max);

          boolean hasStep = BeeUtils.isInt(step) && BeeUtils.toInt(step) > 0;
          boolean hasStepBounds = BeeUtils.isInt(minStep) && BeeUtils.isInt(maxStep)
              && BeeUtils.toInt(minStep) > 0 && BeeUtils.toInt(maxStep) >= BeeUtils.toInt(minStep);

          if (hasStepBounds) {
            widget = new VolumeSlider(pSrc, pMin, pMax,
                BeeUtils.toInt(minStep), BeeUtils.toInt(maxStep));
          } else if (hasStep) {
            widget = new VolumeSlider(pSrc, pMin, pMax, BeeUtils.toInt(step));
          } else {
            widget = new VolumeSlider(pSrc, pMin, pMax);
          }
        }
        break;

      case TREE:
        widget = new Tree(attributes.get("caption"));
        break;

      case DATA_TREE:
      case CHILD_TREE:
        widget = new TreeContainer(attributes.get("caption"),
            BeeUtils.toBoolean(attributes.get("hideActions")));

        ((TreeView) widget).setViewPresenter(new TreePresenter((TreeView) widget,
            attributes.get(UiConstants.ATTR_SOURCE), attributes.get("parentColumn"),
            attributes.get("orderColumn"), attributes.get("relationColumn"),
            XmlUtils.getCalculation(element, TAG_CALC),
            XmlUtils.getFirstChildElement(element, "form")));
        break;

      case DECORATOR:
        String id = attributes.get(ATTR_ID);
        if (!BeeUtils.isEmpty(id) && children.size() == 1) {
          Widget child = createIfWidget(children.get(0), viewName, columns,
              widgetDescriptionCallback, widgetCallback);
          if (child != null) {
            widget = TuningFactory.decorate(id, element, child,
                widgetDescriptionCallback.getLastWidgetDescription());
          }
          children.clear();
          attributes.clear();
        }
        break;
    }

    if (widget == null) {
      widgetDescriptionCallback.onFailure("cannot create widget", getTagName());
      return null;
    }

    String id = (widget instanceof HasId) ? ((HasId) widget).getId() : DomUtils.getId(widget);
    WidgetDescription widgetDescription = new WidgetDescription(this, id, name);
    
    if (relation != null) {
      widgetDescription.setRelation(relation);
    }

    boolean disablable = widget instanceof HasEnabled;

    if (!attributes.isEmpty()) {
      setAttributes(widget, attributes);
      widgetDescription.setAttributes(attributes);

      if (widget instanceof HasNumberBounds) {
        UiHelper.setNumberBounds((HasNumberBounds) widget, widgetDescription.getMinValue(),
            widgetDescription.getMaxValue());
      }
      if (disablable && BeeConst.isFalse(attributes.get(ATTR_DISABLABLE))) {
        disablable = false;
      }
    }

    widgetDescription.setDisablable(disablable);

    List<ConditionalStyleDeclaration> dynStyles = Lists.newArrayList();
    Calculation calc;

    if (!children.isEmpty()) {
      for (Element child : children) {
        String childTag = XmlUtils.getLocalName(child);

        if (BeeUtils.same(childTag, TAG_CSS)) {
          Global.addStyleSheet(child.getAttribute(ATTR_ID), XmlUtils.getText(child));

        } else if (BeeUtils.same(childTag, TAG_DYN_STYLE)) {
          ConditionalStyleDeclaration csd = XmlUtils.getConditionalStyle(child);
          if (csd != null) {
            dynStyles.add(csd);
          }

        } else if (BeeUtils.same(childTag, TAG_HANDLER)) {
          addHandler(widget, child.getAttribute(ATTR_EVENT), XmlUtils.getText(child));

        } else if (BeeUtils.same(childTag, RendererDescription.TAG_RENDERER)) {
          RendererDescription rendererDescription = XmlUtils.getRendererDescription(child);
          if (rendererDescription != null) {
            widgetDescription.setRendererDescription(rendererDescription);
          }
        } else if (BeeUtils.same(childTag, RendererDescription.TAG_RENDER)) {
          Calculation render = XmlUtils.getCalculation(child);
          if (render != null) {
            widgetDescription.setRender(render);
          }

        } else if (BeeUtils.same(childTag, TAG_EDITABLE)) {
          calc = XmlUtils.getCalculation(child);
          if (calc != null) {
            widgetDescription.setEditable(calc);
          }

        } else if (BeeUtils.same(childTag, TAG_VALIDATION)) {
          calc = XmlUtils.getCalculation(child);
          if (calc != null) {
            widgetDescription.setValidation(calc);
          }

        } else if (BeeUtils.same(childTag, TAG_CARRY)) {
          calc = XmlUtils.getCalculation(child);
          if (calc != null) {
            widgetDescription.setCarry(calc);
          }

        } else {
          processChild(widget, child, viewName, columns, widgetDescriptionCallback, widgetCallback);
        }
      }
    }

    if (!dynStyles.isEmpty()) {
      widgetDescription.setDynStyles(dynStyles);
    }

    if (!attributes.isEmpty()) {
      if (this == LIST_BOX && widget instanceof BeeListBox) {
        int z = BeeUtils.toInt(attributes.get(ATTR_MIN_SIZE));
        if (z > 0) {
          ((BeeListBox) widget).setMinSize(z);
        }
        z = BeeUtils.toInt(attributes.get(ATTR_MAX_SIZE));
        if (z > 0) {
          ((BeeListBox) widget).setMaxSize(z);
        }

        int cnt;
        if (BeeUtils.toBoolean(attributes.get(ATTR_ALL_ITEMS_VISIBLE))) {
          cnt = ((BeeListBox) widget).getItemCount();
        } else {
          cnt = BeeUtils.toInt(attributes.get(ATTR_SIZE));
        }
        if (cnt > 0) {
          ((BeeListBox) widget).setVisibleItemCount(cnt);
        } else {
          ((BeeListBox) widget).updateSize();
        }
      }
    }

    if (widgetCallback != null) {
      widgetCallback.afterCreateWidget(name, widget);
    }
    if (widget instanceof Launchable) {
      ((Launchable) widget).launch();
    }

    widgetDescriptionCallback.onSuccess(widgetDescription);

    String decoratorId = attributes.containsKey(ATTR_DECORATOR)
        ? attributes.get(ATTR_DECORATOR) : attributes.get(ATTR_DEFAULT_DECORATOR);
    if (!BeeUtils.isEmpty(decoratorId)) {
      Widget decorated = TuningFactory.decorate(decoratorId, element, widget, widgetDescription);
      if (decorated != null) {
        return decorated;
      }
    }

    return widget;
  }

  public String getTagName() {
    return tagName;
  }

  public boolean isChild() {
    return hasType(Type.IS_CHILD);
  }

  public boolean isDecorator() {
    return hasType(Type.IS_DECORATOR);
  }

  public boolean isDisplay() {
    return hasType(Type.DISPLAY);
  }

  public boolean isEditable() {
    return hasType(Type.EDITABLE);
  }

  public boolean isFocusable() {
    return hasType(Type.FOCUSABLE);
  }

  public boolean isGrid() {
    return hasType(Type.IS_GRID);
  }

  private void addHandler(Widget widget, String event, String handler) {
    Assert.notNull(widget);

    if (BeeUtils.isEmpty(event)) {
      BeeKeeper.getLog().warning("add handler:", NameUtils.getClassName(widget.getClass()),
          DomUtils.getId(widget), "event type not specified");
      return;
    }
    if (BeeUtils.isEmpty(handler)) {
      BeeKeeper.getLog().warning("add handler:", NameUtils.getClassName(widget.getClass()),
          DomUtils.getId(widget), event, "event handler not specified");
      return;
    }

    EventUtils.addDomHandler(widget, event, handler);
  }

  private HeaderAndContent createHeaderAndContent(Element parent, String viewName,
      List<BeeColumn> columns, WidgetDescriptionCallback wdcb, WidgetCallback widgetCallback) {
    String headerTag = null;
    String headerString = null;
    Widget headerWidget = null;
    Widget content = null;

    for (Element child : XmlUtils.getChildrenElements(parent)) {
      String childTag = XmlUtils.getLocalName(child);

      if (BeeUtils.same(childTag, TAG_TEXT)) {
        String text = XmlUtils.getText(child);
        if (!BeeUtils.isEmpty(text)) {
          headerTag = TAG_TEXT;
          headerString = text;
        }
        continue;
      }

      if (BeeUtils.same(childTag, TAG_HTML)) {
        String html = XmlUtils.getText(child);
        if (!BeeUtils.isEmpty(html)) {
          headerTag = TAG_HTML;
          headerString = html;
        }
        continue;
      }

      Widget w = createIfWidget(child, viewName, columns, wdcb, widgetCallback);
      if (w == null) {
        continue;
      }

      if (headerTag == null && headerWidget == null) {
        headerWidget = w;
      } else {
        content = w;
        break;
      }
    }
    return new HeaderAndContent(headerTag, headerString, headerWidget, content);
  }

  private Widget createIfWidget(Element element, String viewName, List<BeeColumn> columns,
      WidgetDescriptionCallback widgetDescriptionCallback, WidgetCallback widgetCallback) {
    if (element == null) {
      return null;
    }
    FormWidget fw = getByTagName(XmlUtils.getLocalName(element));
    if (fw == null) {
      return null;
    }
    return fw.create(element, viewName, columns, widgetDescriptionCallback, widgetCallback);
  }

  private Widget createIfWidgetOrHtmlOrText(Element element, String viewName, 
      List<BeeColumn> columns, WidgetDescriptionCallback wdcb, WidgetCallback widgetCallback) {
    if (element == null) {
      return null;
    }
    Widget widget = null;
    String tag = XmlUtils.getLocalName(element);

    if (BeeUtils.same(tag, TAG_TEXT)) {
      String text = XmlUtils.getText(element);
      if (!BeeUtils.isEmpty(text)) {
        widget = new InlineHTML(text);
      }

    } else if (BeeUtils.same(tag, TAG_HTML)) {
      String html = XmlUtils.getText(element);
      if (!BeeUtils.isEmpty(html)) {
        widget = new Html(html);
      }

    } else {
      widget = createIfWidget(element, viewName, columns, wdcb, widgetCallback);
    }
    return widget;
  }

  private Widget createOneChild(Element parent, String viewName, List<BeeColumn> columns,
      WidgetDescriptionCallback widgetDescriptionCallback, WidgetCallback widgetCallback) {
    for (Element child : XmlUtils.getChildrenElements(parent)) {
      Widget widget = createIfWidget(child, viewName, columns, widgetDescriptionCallback,
          widgetCallback);
      if (widget != null) {
        return widget;
      }
    }
    return null;
  }
  
  private Relation createRelation(String viewName, Map<String, String> attributes,
      List<Element> children) {
    boolean debug = Global.isDebug();

    Relation relation = XmlUtils.getRelation(attributes, children);

    String source = attributes.get(UiConstants.ATTR_SOURCE);
    List<String> renderColumns = 
        NameUtils.toList(attributes.get(RendererDescription.ATTR_RENDER_COLUMNS));

    if (debug) {
      if (!BeeUtils.isEmpty(source)) {
        BeeKeeper.getLog().debug(UiConstants.ATTR_SOURCE, source);
      }
      if (!BeeUtils.isEmpty(renderColumns)) {
        BeeKeeper.getLog().debug(RendererDescription.ATTR_RENDER_COLUMNS, renderColumns);
      }
    }
    
    Holder<String> sourceHolder = Holder.of(source);
    Holder<List<String>> listHolder = Holder.of(renderColumns);
    
    relation.initialize(Global.getDataInfoProvider(), viewName, sourceHolder, listHolder);
    if (relation.getViewName() == null) {
      BeeKeeper.getLog().severe("Cannot create relation");
      BeeKeeper.getLog().severe(viewName, source, renderColumns);

      BeeKeeper.getLog().info("attributes:");
      BeeKeeper.getLog().debugMap(attributes);
      BeeKeeper.getLog().info("relation:");
      BeeKeeper.getLog().debugCollection(relation.getInfo());
      BeeKeeper.getLog().addSeparator();

      return null;
    }
    
    if (debug) {
      BeeKeeper.getLog().debugCollection(relation.getInfo());
    }

    source = sourceHolder.get();
    renderColumns = listHolder.get();
    
    if (!BeeUtils.isEmpty(source)) {
      attributes.put(UiConstants.ATTR_SOURCE, source);
      if (debug) {
        BeeKeeper.getLog().debug(UiConstants.ATTR_SOURCE, source);
      }
    }
    if (!BeeUtils.isEmpty(renderColumns)) {
      attributes.put(RendererDescription.ATTR_RENDER_COLUMNS, XmlHelper.getList(renderColumns));
      if (debug) {
        BeeKeeper.getLog().debug(RendererDescription.ATTR_RENDER_COLUMNS, renderColumns);
      }
    }

    if (debug) {
      BeeKeeper.getLog().addSeparator();
    }

    return relation;
  }

  private boolean createTableCell(HtmlTable table, Element element, int row, int col,
      String viewName, List<BeeColumn> columns, WidgetDescriptionCallback wdcb,
      WidgetCallback widgetCallback) {
    boolean ok = false;
    String tag = XmlUtils.getLocalName(element);

    if (BeeUtils.same(tag, TAG_TEXT)) {
      String text = XmlUtils.getText(element);
      ok = !BeeUtils.isEmpty(text);
      if (ok) {
        table.setText(row, col, text);
      }

    } else if (BeeUtils.same(tag, TAG_HTML)) {
      String html = XmlUtils.getText(element);
      ok = !BeeUtils.isEmpty(html);
      if (ok) {
        table.setHTML(row, col, html);
      }

    } else {
      Widget widget = createIfWidget(element, viewName, columns, wdcb, widgetCallback);
      ok = (widget != null);
      if (ok) {
        table.setWidget(row, col, widget);
      }
    }
    return ok;
  }

  private Edges getEdges(Element element) {
    return new Edges(XmlUtils.getAttributeDouble(element, ATTR_TOP),
        XmlUtils.getAttributeUnit(element, ATTR_TOP_UNIT),
        XmlUtils.getAttributeDouble(element, ATTR_RIGHT),
        XmlUtils.getAttributeUnit(element, ATTR_RIGHT_UNIT),
        XmlUtils.getAttributeDouble(element, ATTR_BOTTOM),
        XmlUtils.getAttributeUnit(element, ATTR_BOTTOM_UNIT),
        XmlUtils.getAttributeDouble(element, ATTR_LEFT),
        XmlUtils.getAttributeUnit(element, ATTR_LEFT_UNIT));
  }

  private Pair<String, BeeImage> getFaceOptions(Element element) {
    String html = element.getAttribute(ATTR_HTML);
    BeeImage image = null;

    String name = element.getAttribute(ATTR_RESOURCE);
    if (!BeeUtils.isEmpty(name)) {
      ImageResource resource = Images.get(name);
      if (resource != null) {
        image = new BeeImage(resource);
      }
    }
    if (image == null) {
      String url = element.getAttribute(ATTR_URL);
      if (!BeeUtils.isEmpty(url)) {
        image = new BeeImage(url);
      }
    }
    return Pair.of(html, image);
  }

  private Set<Type> getTypes() {
    return types;
  }

  private boolean hasChildren() {
    return hasType(Type.HAS_CHILDREN);
  }

  private boolean hasLayers() {
    return hasType(Type.HAS_LAYERS);
  }

  private boolean hasOneChild() {
    return hasType(Type.HAS_ONE_CHILD);
  }

  private boolean hasType(Type type) {
    if (type == null || getTypes() == null) {
      return false;
    }
    return getTypes().contains(type);
  }

  private void initMedia(MediaBase widget, Map<String, String> attributes) {
    if (attributes == null || attributes.isEmpty()) {
      widget.setAutoplay(false);
      widget.setControls(true);
      return;
    }

    String value = attributes.get(ATTR_AUTOPLAY);
    widget.setAutoplay(BeeConst.isTrue(value));

    value = attributes.get(ATTR_CONTROLS);
    widget.setControls(BeeUtils.isBoolean(value) ? BeeUtils.toBoolean(value) : true);

    value = attributes.get(ATTR_PRELOAD);
    if (!BeeUtils.isEmpty(value)) {
      widget.setPreload(value);
    }

    value = attributes.get(ATTR_DEFAULT_PLAYBACK_RATE);
    if (BeeUtils.isPositiveDouble(value)) {
      widget.setDefaultPlaybackRate(BeeUtils.toDouble(value));
    }

    value = attributes.get(ATTR_URL);
    if (!BeeUtils.isEmpty(value)) {
      widget.setSrc(value);
    }

    value = attributes.get(ATTR_CURRENT_TIME);
    if (BeeUtils.isPositiveDouble(value)) {
      widget.setCurrentTime(BeeUtils.toDouble(value));
    }

    value = attributes.get(ATTR_PLAYBACK_RATE);
    if (BeeUtils.isPositiveDouble(value)) {
      widget.setPlaybackRate(BeeUtils.toDouble(value));
    }

    value = attributes.get(ATTR_LOOP);
    if (BeeUtils.isBoolean(value)) {
      widget.setLoop(BeeUtils.toBoolean(value));
    }

    value = attributes.get(ATTR_MUTED);
    if (BeeUtils.isBoolean(value)) {
      widget.setMuted(BeeUtils.toBoolean(value));
    }

    value = attributes.get(ATTR_VOLUME);
    if (BeeUtils.isDouble(value, BeeConst.DOUBLE_ZERO, true, BeeConst.DOUBLE_ONE, true)) {
      widget.setCurrentTime(BeeUtils.toDouble(value));
    }
  }

  private boolean isCellVector() {
    return hasType(Type.CELL_VECTOR);
  }

  private boolean isTable() {
    return hasType(Type.TABLE);
  }

  private void processChild(Widget parent, Element child, String viewName, List<BeeColumn> columns,
      WidgetDescriptionCallback wdcb, WidgetCallback widgetCallback) {
    String childTag = XmlUtils.getLocalName(child);

    if (hasLayers()) {
      if (BeeUtils.same(childTag, TAG_LAYER) && parent instanceof HasWidgets) {
        Widget w = createOneChild(child, viewName, columns, wdcb, widgetCallback);

        if (w != null) {
          ((HasWidgets) parent).add(w);

          Edges edges = getEdges(child);
          Dimensions dimensions = XmlUtils.getDimensions(child);

          if (parent instanceof BeeLayoutPanel) {
            ((BeeLayoutPanel) parent).setHorizontalLayout(w,
                edges.getLeftValue(), edges.getLeftUnit(),
                edges.getRightValue(), edges.getRightUnit(),
                dimensions.getWidthValue(), dimensions.getWidthUnit());
            ((BeeLayoutPanel) parent).setVerticalLayout(w,
                edges.getTopValue(), edges.getTopUnit(),
                edges.getBottomValue(), edges.getBottomUnit(),
                dimensions.getHeightValue(), dimensions.getHeightUnit());
          } else {
            if (!edges.isEmpty()) {
              StyleUtils.makeAbsolute(w);
              edges.applyPosition(w);
            }
            if (!dimensions.isEmpty()) {
              dimensions.applyTo(w);
            }
          }
        }
      }

    } else if (isTable() && parent instanceof HtmlTable) {
      HtmlTable table = (HtmlTable) parent;

      if (BeeUtils.same(childTag, TAG_COL)) {
        String idx = child.getAttribute(ATTR_INDEX);
        if (BeeUtils.isDigit(idx)) {
          int c = BeeUtils.toInt(idx);

          Double width = XmlUtils.getAttributeDouble(child, HasDimensions.ATTR_WIDTH);
          if (BeeUtils.isPositive(width)) {
            table.getColumnFormatter().setWidth(c, StyleUtils.toCssLength(width,
                XmlUtils.getAttributeUnit(child, HasDimensions.ATTR_WIDTH_UNIT, Unit.PX)));
          }
          StyleUtils.updateAppearance(table.getColumnFormatter().getElement(c),
              child.getAttribute(UiConstants.ATTR_CLASS),
              child.getAttribute(UiConstants.ATTR_STYLE));
        }

      } else if (BeeUtils.same(childTag, TAG_ROW)) {
        int r = table.getRowCount();
        int c = 0;

        for (Element cell : XmlUtils.getChildrenElements(child)) {
          if (XmlUtils.tagIs(cell, TAG_CELL)) {
            for (Element cellContent : XmlUtils.getChildrenElements(cell)) {
              if (createTableCell(table, cellContent, r, c, viewName, columns, wdcb,
                  widgetCallback)) {
                break;
              }
            }
            setTableCellAttributes(table, cell, r, c);
            c++;
          } else if (createTableCell(table, cell, r, c, viewName, columns, wdcb, widgetCallback)) {
            c++;
          }
        }
        if (c > 0) {
          setTableRowAttributes(table, child, r);
        }

      } else {
        createTableCell(table, child, table.getRowCount(), 0, viewName, columns, wdcb,
            widgetCallback);
      }

    } else if (isCellVector() && parent instanceof HasWidgets) {
      Widget w = null;
      if (BeeUtils.same(childTag, TAG_CELL)) {
        for (Element cellContent : XmlUtils.getChildrenElements(child)) {
          w = createIfWidgetOrHtmlOrText(cellContent, viewName, columns, wdcb, widgetCallback);
          if (w != null) {
            break;
          }
        }
      } else {
        w = createIfWidgetOrHtmlOrText(child, viewName, columns, wdcb, widgetCallback);
      }

      if (w != null) {
        ((HasWidgets) parent).add(w);
        if (parent instanceof CellPanel && BeeUtils.same(childTag, TAG_CELL)) {
          setVectorCellAttributes((CellPanel) parent, child, w);
        }
      }

    } else if (hasOneChild()) {
      Widget w = createIfWidget(child, viewName, columns, wdcb, widgetCallback);
      if (w != null && parent instanceof HasOneWidget) {
        ((HasOneWidget) parent).setWidget(w);
      }

    } else if (hasChildren()) {
      Widget w = createIfWidget(child, viewName, columns, wdcb, widgetCallback);
      if (w != null && parent instanceof HasWidgets) {
        ((HasWidgets) parent).add(w);
      }

    } else if (this == SPLIT_PANEL) {
      Widget w = createOneChild(child, viewName, columns, wdcb, widgetCallback);
      if (w != null && parent instanceof Split) {
        ScrollBars sb = XmlUtils.getAttributeScrollBars(child, ATTR_SCROLL_BARS, ScrollBars.NONE);
        Direction direction = NameUtils.getConstant(Direction.class, childTag);

        if (direction == Direction.CENTER) {
          ((Split) parent).add(w, sb);
        } else if (Split.validDirection(direction, false)) {
          Integer size = XmlUtils.getAttributeInteger(child, ATTR_SIZE);
          Integer splitterSize = XmlUtils.getAttributeInteger(child, ATTR_SPLITTER_SIZE);
          if (BeeUtils.isPositive(size)) {
            ((Split) parent).add(w, direction, size, sb, splitterSize);
          }
        }
      }

    } else if (this == STACK_PANEL && BeeUtils.same(childTag, TAG_STACK)) {
      Double headerSize = XmlUtils.getAttributeDouble(child, ATTR_HEADER_SIZE);
      HeaderAndContent hc = createHeaderAndContent(child, viewName, columns, wdcb, widgetCallback);

      if (BeeUtils.isPositive(headerSize) && hc != null && hc.isValid()
          && parent instanceof Stack) {
        if (hc.isHeaderText() || hc.isHeaderHtml()) {
          ((Stack) parent).add(hc.getContent(), hc.getHeaderString(), hc.isHeaderHtml(),
              headerSize);
        } else {
          ((Stack) parent).add(hc.getContent(), hc.getHeaderWidget(), headerSize);
        }
      }

    } else if (this == TABBED_PAGES && BeeUtils.same(childTag, TAG_PAGE)) {
      HeaderAndContent hc = createHeaderAndContent(child, viewName, columns, wdcb, widgetCallback);

      if (hc != null && hc.isValid() && parent instanceof TabbedPages) {
        if (hc.isHeaderText() || hc.isHeaderHtml()) {
          ((TabbedPages) parent).add(hc.getContent(), hc.getHeaderString(), hc.isHeaderHtml());
        } else {
          ((TabbedPages) parent).add(hc.getContent(), hc.getHeaderWidget());
        }
      }

    } else if (this == RADIO && BeeUtils.same(childTag, TAG_OPTION)) {
      String opt = XmlUtils.getText(child);
      if (!BeeUtils.isEmpty(opt) && parent instanceof RadioGroup) {
        ((RadioGroup) parent).addOption(opt, true);
      }

    } else if (this == LIST_BOX && BeeUtils.same(childTag, HasItems.TAG_ITEM)) {
      String item = XmlUtils.getText(child);
      if (!BeeUtils.isEmpty(item) && parent instanceof BeeListBox) {
        ((BeeListBox) parent).addItem(item);
      }

    } else if (this == HEADER_CONTENT_FOOTER) {
      Widget w = createOneChild(child, viewName, columns, wdcb, widgetCallback);
      if (w != null && parent instanceof HeaderContentFooter) {
        if (BeeUtils.same(childTag, TAG_HEADER)) {
          ((HeaderContentFooter) parent).setHeaderWidget(w);
        } else if (BeeUtils.same(childTag, TAG_CONTENT)) {
          ((HeaderContentFooter) parent).setContentWidget(w);
        } else if (BeeUtils.same(childTag, TAG_FOOTER)) {
          ((HeaderContentFooter) parent).setFooterWidget(w);
        }
      }

    } else if (this == TOGGLE && parent instanceof CustomButton
        && BeeUtils.inListSame(childTag, TAG_UP_FACE, TAG_DOWN_FACE, TAG_UP_HOVERING_FACE,
            TAG_DOWN_HOVERING_FACE, TAG_UP_DISABLED_FACE, TAG_DOWN_DISABLED_FACE)) {
      setFace((CustomButton) parent, childTag, child);

    } else if (this == TREE && parent instanceof Tree) {
      processTree((Tree) parent, child);

    } else if (this == TAB_BAR && parent instanceof TabBar && BeeUtils.same(childTag, TAG_TAB)) {
      for (Element tabContent : XmlUtils.getChildrenElements(child)) {
        if (XmlUtils.tagIs(tabContent, TAG_TEXT)) {
          String text = XmlUtils.getText(tabContent);
          if (!BeeUtils.isEmpty(text)) {
            ((TabBar) parent).addItem(text);
            break;
          }
        }

        if (XmlUtils.tagIs(tabContent, TAG_HTML)) {
          String html = XmlUtils.getText(tabContent);
          if (!BeeUtils.isEmpty(html)) {
            ((TabBar) parent).addItem(html, true);
            break;
          }
        }

        Widget w = createIfWidget(tabContent, viewName, columns, wdcb, widgetCallback);
        if (w != null) {
          ((TabBar) parent).addItem(w);
          break;
        }
      }
    }
  }

  private void processTree(HasTreeItems parent, Element child) {
    if (!XmlUtils.tagIs(child, TAG_TREE_ITEM)) {
      return;
    }
    TreeItem item = new TreeItem(child.getAttribute(ATTR_HTML));
    parent.addItem(item);

    for (Element chld : XmlUtils.getChildrenElements(child)) {
      processTree(item, chld);
    }
  }

  private void setAttributes(Widget widget, Map<String, String> attributes) {
    for (Map.Entry<String, String> attr : attributes.entrySet()) {
      String name = attr.getKey();
      String value = attr.getValue();
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      if (BeeUtils.same(name, UiConstants.ATTR_CLASS)) {
        StyleUtils.updateClasses(widget, value);
      } else if (BeeUtils.same(name, UiConstants.ATTR_STYLE)) {
        StyleUtils.updateStyle(widget.getElement().getStyle(), value);

      } else if (BeeUtils.same(name, ATTR_TITLE)) {
        widget.setTitle(value);

      } else if (BeeUtils.same(name, HasOptions.ATTR_OPTIONS)) {
        if (widget instanceof HasOptions) {
          ((HasOptions) widget).setOptions(value);
        }

      } else if (BeeUtils.same(name, ATTR_TAB_INDEX)) {
        if (widget instanceof Focusable) {
          ((Focusable) widget).setTabIndex(BeeUtils.toInt(value));
        }

      } else if (BeeUtils.same(name, UiConstants.ATTR_HORIZONTAL_ALIGNMENT)) {
        if (widget instanceof HasHorizontalAlignment) {
          UiHelper.setHorizontalAlignment((HasHorizontalAlignment) widget, value);
        }
      } else if (BeeUtils.same(name, UiConstants.ATTR_VERTICAL_ALIGNMENT)) {
        if (widget instanceof HasVerticalAlignment) {
          UiHelper.setVerticalAlignment((HasVerticalAlignment) widget, value);
        }

      } else if (BeeUtils.same(name, ATTR_BORDER_WIDTH) && BeeUtils.isDigit(value)) {
        if (widget instanceof HtmlTable) {
          ((HtmlTable) widget).setBorderWidth(BeeUtils.toInt(value));
        } else if (widget instanceof CellPanel) {
          ((CellPanel) widget).setBorderWidth(BeeUtils.toInt(value));
        }

      } else if (BeeUtils.same(name, ATTR_CELL_PADDING)) {
        if (widget instanceof HtmlTable && BeeUtils.isDigit(value)) {
          ((HtmlTable) widget).setCellPadding(BeeUtils.toInt(value));
        }
      } else if (BeeUtils.same(name, ATTR_CELL_SPACING) && BeeUtils.isDigit(value)) {
        if (widget instanceof HtmlTable) {
          ((HtmlTable) widget).setCellSpacing(BeeUtils.toInt(value));
        } else if (widget instanceof CellPanel) {
          ((CellPanel) widget).setSpacing(BeeUtils.toInt(value));
        }

      } else if (BeeUtils.same(name, HasService.ATTR_SERVICE)) {
        if (widget instanceof HasService) {
          ((HasService) widget).setService(value);
        }
      } else if (BeeUtils.same(name, ATTR_STAGE)) {
        if (widget instanceof HasStage) {
          ((HasStage) widget).setStage(value);
        }

      } else if (BeeUtils.same(name, ATTR_MIN)) {
        if (widget instanceof InputInteger && BeeUtils.isInt(value)) {
          ((InputInteger) widget).setMinValue(BeeUtils.toInt(value));
        }
      } else if (BeeUtils.same(name, ATTR_MAX)) {
        if (widget instanceof InputInteger && BeeUtils.isInt(value)) {
          ((InputInteger) widget).setMaxValue(BeeUtils.toInt(value));
        }
      } else if (BeeUtils.same(name, ATTR_STEP)) {
        if (widget instanceof InputInteger && BeeUtils.isDigit(value)
            && BeeUtils.toInt(value) > 0) {
          ((InputInteger) widget).setStepValue(BeeUtils.toInt(value));
        }

      } else if (BeeUtils.same(name, HasValueStartIndex.ATTR_VALUE_START_INDEX)
          && BeeUtils.isDigit(value)) {
        if (widget instanceof HasValueStartIndex) {
          ((HasValueStartIndex) widget).setValueStartIndex(BeeUtils.toInt(value));
        }

      } else if (BeeUtils.same(name, HasVisibleLines.ATTR_VISIBLE_LINES)) {
        if (widget instanceof HasVisibleLines && BeeUtils.isPositiveInt(value)) {
          ((HasVisibleLines) widget).setVisibleLines(BeeUtils.toInt(value));
        }
      } else if (BeeUtils.same(name, HasTextDimensions.ATTR_CHARACTER_WIDTH)) {
        if (widget instanceof HasTextDimensions && BeeUtils.isPositiveInt(value)) {
          ((HasTextDimensions) widget).setCharacterWidth(BeeUtils.toInt(value));
        }

      } else if (BeeUtils.same(name, HasItems.ATTR_ITEM_KEY)) {
        if (widget instanceof AcceptsCaptions) {
          ((AcceptsCaptions) widget).addCaptions(value);
        }
      }
    }
  }

  private void setFace(CustomButton button, String faceName, Element element) {
    Pair<String, BeeImage> options = getFaceOptions(element);
    if (options == null) {
      return;
    }

    String html = options.getA();
    BeeImage image = options.getB();
    if (BeeUtils.isEmpty(html) && image == null) {
      return;
    }

    Face face = null;
    if (BeeUtils.same(faceName, TAG_UP_FACE)) {
      face = button.getUpFace();
    } else if (BeeUtils.same(faceName, TAG_DOWN_FACE)) {
      face = button.getDownFace();
    } else if (BeeUtils.same(faceName, TAG_UP_HOVERING_FACE)) {
      face = button.getUpHoveringFace();
    } else if (BeeUtils.same(faceName, TAG_DOWN_HOVERING_FACE)) {
      face = button.getDownHoveringFace();
    } else if (BeeUtils.same(faceName, TAG_UP_DISABLED_FACE)) {
      face = button.getUpDisabledFace();
    } else if (BeeUtils.same(faceName, TAG_DOWN_DISABLED_FACE)) {
      face = button.getDownDisabledFace();
    }
    if (face == null) {
      return;
    }

    if (image != null) {
      face.setImage(image);
    } else if (!BeeUtils.isEmpty(face)) {
      face.setHTML(html);
    }
  }

  private void setTableCellAttributes(HtmlTable table, Element element, int row, int col) {
    String z = element.getAttribute(UiConstants.ATTR_HORIZONTAL_ALIGNMENT);
    if (!BeeUtils.isEmpty(z)) {
      HorizontalAlignmentConstant horAlign = UiHelper.parseHorizontalAlignment(z);
      if (horAlign != null) {
        table.getCellFormatter().setHorizontalAlignment(row, col, horAlign);
      }
    }

    z = element.getAttribute(UiConstants.ATTR_VERTICAL_ALIGNMENT);
    if (!BeeUtils.isEmpty(z)) {
      VerticalAlignmentConstant vertAlign = UiHelper.parseVerticalAlignment(z);
      if (vertAlign != null) {
        table.getCellFormatter().setVerticalAlignment(row, col, vertAlign);
      }
    }

    Dimensions dimensions = XmlUtils.getDimensions(element);
    z = dimensions.getCssWidth();
    if (BeeUtils.isEmpty(z)) {
      table.getCellFormatter().setWidth(row, col, z);
    }
    z = dimensions.getCssHeight();
    if (!BeeUtils.isEmpty(z)) {
      table.getCellFormatter().setHeight(row, col, z);
    }

    if (XmlUtils.tagIs(element, TAG_CELL)) {
      z = element.getAttribute(ATTR_WORD_WRAP);
      if (BeeUtils.isBoolean(z)) {
        table.getCellFormatter().setWordWrap(row, col, BeeUtils.toBoolean(z));
      }

      StyleUtils.updateAppearance(table.getCellFormatter().getElement(row, col),
          element.getAttribute(UiConstants.ATTR_CLASS),
          element.getAttribute(UiConstants.ATTR_STYLE));

      if (table instanceof FlexTable) {
        String span = element.getAttribute(ATTR_COL_SPAN);
        if (BeeUtils.toInt(span) > 1) {
          ((FlexTable) table).getFlexCellFormatter().setColSpan(row, col, BeeUtils.toInt(span));
        }
        span = element.getAttribute(ATTR_ROW_SPAN);
        if (BeeUtils.toInt(span) > 1) {
          ((FlexTable) table).getFlexCellFormatter().setRowSpan(row, col, BeeUtils.toInt(span));
        }
      }
    }
  }

  private void setTableRowAttributes(HtmlTable table, Element element, int row) {
    String z = element.getAttribute(UiConstants.ATTR_VERTICAL_ALIGNMENT);
    if (!BeeUtils.isEmpty(z)) {
      VerticalAlignmentConstant vertAlign = UiHelper.parseVerticalAlignment(z);
      if (vertAlign != null) {
        table.getRowFormatter().setVerticalAlign(row, vertAlign);
      }
    }

    if (XmlUtils.tagIs(element, TAG_ROW)) {
      StyleUtils.updateAppearance(table.getRow(row), element.getAttribute(UiConstants.ATTR_CLASS),
          element.getAttribute(UiConstants.ATTR_STYLE));
    }
  }

  private void setVectorCellAttributes(CellPanel parent, Element element, Widget cellContent) {
    String z = element.getAttribute(UiConstants.ATTR_HORIZONTAL_ALIGNMENT);
    if (!BeeUtils.isEmpty(z)) {
      HorizontalAlignmentConstant horAlign = UiHelper.parseHorizontalAlignment(z);
      if (horAlign != null) {
        parent.setCellHorizontalAlignment(cellContent, horAlign);
      }
    }

    z = element.getAttribute(UiConstants.ATTR_VERTICAL_ALIGNMENT);
    if (!BeeUtils.isEmpty(z)) {
      VerticalAlignmentConstant vertAlign = UiHelper.parseVerticalAlignment(z);
      if (vertAlign != null) {
        parent.setCellVerticalAlignment(cellContent, vertAlign);
      }
    }

    Dimensions dimensions = XmlUtils.getDimensions(element);
    z = dimensions.getCssWidth();
    if (!BeeUtils.isEmpty(z)) {
      parent.setCellWidth(cellContent, z);
    }
    z = dimensions.getCssHeight();
    if (!BeeUtils.isEmpty(z)) {
      parent.setCellHeight(cellContent, z);
    }

    if (XmlUtils.tagIs(element, TAG_CELL)) {
      StyleUtils.updateAppearance(DOM.getParent(cellContent.getElement()),
          element.getAttribute(UiConstants.ATTR_CLASS),
          element.getAttribute(UiConstants.ATTR_STYLE));
    }
  }
}
