package com.butent.bee.client.dom;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Controls scale parameters between lenght measure units and screen pixels.
 */
public class Rulers {

  private static final double unitRulerScale = 10.0;

  private static final double cmToPx = 1.0 / unitRulerScale;
  private static final double mmToPx = 0.1 / unitRulerScale;
  private static final double inToPx = 2.54 / unitRulerScale;
  private static final double ptToPx = inToPx / 72.0;
  private static final double pcToPx = ptToPx * 12.0;

  private static final SpanElement lineRuler;
  private static final DivElement areaRuler;

  private static final DivElement fixedUnitRuler;
  private static final DivElement relativeUnitRuler;

  static {
    String idPrefix = "ruler";

    lineRuler = Document.get().createSpanElement();
    DomUtils.createId(lineRuler, idPrefix);
    lineRuler.setInnerHTML(BeeConst.HTML_NBSP);

    Style style = lineRuler.getStyle();
    style.setPosition(Position.ABSOLUTE);
    style.setZIndex(-100);
    style.setTop(-100, Unit.PX);

    Document.get().getBody().appendChild(lineRuler);

    areaRuler = Document.get().createDivElement();
    DomUtils.createId(areaRuler, idPrefix);
    areaRuler.setInnerHTML(BeeConst.HTML_NBSP);

    style = areaRuler.getStyle();
    style.setPosition(Position.ABSOLUTE);
    style.setZIndex(-200);
    style.setTop(-200, Unit.PX);

    Document.get().getBody().appendChild(areaRuler);

    fixedUnitRuler = Document.get().createDivElement();
    DomUtils.createId(fixedUnitRuler, idPrefix);
    fixedUnitRuler.setInnerHTML(BeeConst.HTML_NBSP);

    style = fixedUnitRuler.getStyle();
    style.setPosition(Position.ABSOLUTE);
    style.setZIndex(-300);
    style.setTop(-300, Unit.PX);

    style.setWidth(unitRulerScale, Unit.CM);
    style.setHeight(unitRulerScale, Unit.CM);

    Document.get().getBody().appendChild(fixedUnitRuler);

    relativeUnitRuler = Document.get().createDivElement();
    DomUtils.createId(relativeUnitRuler, idPrefix);
    relativeUnitRuler.setInnerHTML(BeeConst.HTML_NBSP);

    style = relativeUnitRuler.getStyle();
    style.setPosition(Position.ABSOLUTE);
    style.setZIndex(-400);
    style.setTop(-400, Unit.PX);

    style.setWidth(unitRulerScale, Unit.EM);
    style.setHeight(unitRulerScale, Unit.EX);

    Document.get().getBody().appendChild(relativeUnitRuler);
  }

  public static Dimensions getAreaDimensions(Font font, String content, boolean asHtml) {
    return getDimensions(areaRuler, font, content, asHtml);
  }

  public static int getAreaHeight(Font font, String content, boolean asHtml) {
    return getHeight(areaRuler, font, content, asHtml);
  }

  public static int getAreaWidth(Font font, String content, boolean asHtml) {
    return getWidth(areaRuler, font, content, asHtml);
  }

  public static int getIntPixels(double value, Unit unit) {
    return BeeUtils.toInt(getPixels(value, unit));
  }

  public static int getIntPixels(double value, Unit unit, double containerSize) {
    return BeeUtils.toInt(getPixels(value, unit, containerSize));
  }

  public static int getIntPixels(double value, Unit unit, Font font) {
    return BeeUtils.toInt(getPixels(value, unit, font));
  }

  public static int getIntPixels(double value, Unit unit, Font font, double containerSize) {
    return BeeUtils.toInt(getPixels(value, unit, font, containerSize));
  }

  public static Dimensions getLineDimensions(Font font, String content, boolean asHtml) {
    return getDimensions(lineRuler, font, content, asHtml);
  }

  public static int getLineHeight(Font font, String content, boolean asHtml) {
    return getHeight(lineRuler, font, content, asHtml);
  }

  public static int getLineWidth(Font font, String content, boolean asHtml) {
    return getWidth(lineRuler, font, content, asHtml);
  }

  public static double getPixels(double value, Unit unit) {
    return getPixels(value, unit, null);
  }

  public static double getPixels(double value, Unit unit, double containerSize) {
    return getPixels(value, unit, null, containerSize);
  }

  public static double getPixels(double value, Unit unit, Font font) {
    return getPixels(value, unit, font, 0);
  }

  public static double getPixels(double value, Unit unit, Font font, double containerSize) {
    Assert.notNull(unit);

    if (isUnitSizeFixed(unit)) {
      return value * getFixedUnitSizeInPixels(unit);
    } else if (isUnitFontDependent(unit)) {
      return value * getFontDependentUnitSizeInPixels(unit, font);
    } else if (isUnitContainerDependent(unit)) {
      return value * getContainerDependentUnitSizeInPixels(unit, containerSize);
    } else {
      Assert.untouchable();
      return 0;
    }
  }

  private static double getContainerDependentUnitSizeInPixels(Unit unit, double containerSize) {
    switch (unit) {
      case PCT:
        return containerSize / 100.0;
      default:
        Assert.untouchable();
        return 0;
    }
  }

  private static Dimensions getDimensions(Element ruler, Font font, String content, boolean asHtml) {
    if (content == null) {
      return null;
    }

    prepareRuler(ruler, font, content, asHtml);

    int width = ruler.getOffsetWidth();
    int height = ruler.getOffsetHeight();

    resetRuler(ruler, font);

    return new Dimensions(width, height);
  }

  private static double getFixedUnitSizeInPixels(Unit unit) {
    switch (unit) {
      case PX:
        return 1;
      case CM:
        return fixedUnitRuler.getOffsetWidth() * cmToPx;
      case MM:
        return fixedUnitRuler.getOffsetWidth() * mmToPx;
      case IN:
        return fixedUnitRuler.getOffsetWidth() * inToPx;
      case PT:
        return fixedUnitRuler.getOffsetWidth() * ptToPx;
      case PC:
        return fixedUnitRuler.getOffsetWidth() * pcToPx;
      default:
        Assert.untouchable();
        return 0;
    }
  }

  private static double getFontDependentUnitSizeInPixels(Unit unit, Font font) {
    double size;
    prepareRuler(relativeUnitRuler, font);

    switch (unit) {
      case EM:
        size = relativeUnitRuler.getOffsetWidth() / unitRulerScale;
        break;
      case EX:
        size = relativeUnitRuler.getOffsetHeight() / unitRulerScale;
        break;
      default:
        Assert.untouchable();
        size = 0;
    }

    resetRuler(relativeUnitRuler, font);
    return size;
  }

  private static int getHeight(Element ruler, Font font, String content, boolean asHtml) {
    if (content == null) {
      return 0;
    }

    prepareRuler(ruler, font, content, asHtml);
    int height = ruler.getOffsetHeight();
    resetRuler(ruler, font);

    return height;
  }

  private static int getWidth(Element ruler, Font font, String content, boolean asHtml) {
    if (content == null) {
      return 0;
    }

    prepareRuler(ruler, font, content, asHtml);
    int width = ruler.getOffsetWidth();
    resetRuler(ruler, font);

    return width;
  }

  private static boolean inList(Unit unit, Unit... units) {
    if (unit == null || units == null) {
      return false;
    }
    return ArrayUtils.contains(units, unit);
  }

  private static boolean isUnitContainerDependent(Unit unit) {
    return inList(unit, Unit.PCT);
  }

  private static boolean isUnitFontDependent(Unit unit) {
    return inList(unit, Unit.EM, Unit.EX);
  }

  private static boolean isUnitSizeFixed(Unit unit) {
    return inList(unit, Unit.PX, Unit.CM, Unit.MM, Unit.IN, Unit.PC, Unit.PT);
  }

  private static void prepareRuler(Element ruler, Font font) {
    if (font != null) {
      font.applyTo(ruler);
    }
  }

  private static void prepareRuler(Element ruler, Font font, String content, boolean asHtml) {
    if (font != null) {
      font.applyTo(ruler);
    }
    if (content != null) {
      if (asHtml) {
        StyleUtils.setWhiteSpace(ruler, StyleUtils.WhiteSpace.NORMAL);
        ruler.setInnerHTML(content);
      } else {
        StyleUtils.setWhiteSpace(ruler, StyleUtils.WhiteSpace.PRE);
        ruler.setInnerText(content);
      }
    }
  }

  private static void resetRuler(Element ruler, Font font) {
    if (font != null) {
      font.removeFrom(ruler);
    }
    ruler.getStyle().clearWhiteSpace();
    ruler.setInnerHTML(BeeConst.HTML_NBSP);
  }

  private Rulers() {
  }
}
