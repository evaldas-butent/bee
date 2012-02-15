package com.butent.bee.client.richtext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.RichTextArea.Formatter;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.EditorFactory;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.Toggle;

/**
 * Handles a rich text editor toolbar with all the buttons for formatting the text.
 */

public class RichTextToolbar extends Vertical implements HasEnabled {

  /**
   * Contains a list of necessary methods for text editing functions (bold, italic, justify,
   * hyperlink etc.
   */

  public interface Images extends ClientBundle {

    ImageResource bold();

    ImageResource createLink();

    ImageResource hr();

    ImageResource indent();

    ImageResource insertImage();

    ImageResource italic();

    ImageResource justifyCenter();

    ImageResource justifyLeft();

    ImageResource justifyRight();

    ImageResource ol();

    ImageResource outdent();

    ImageResource removeFormat();

    ImageResource removeLink();

    ImageResource strikeThrough();

    ImageResource subscript();

    ImageResource superscript();

    ImageResource ul();

    ImageResource underline();
  }

  private class EventHandler implements ClickHandler, ChangeHandler, KeyDownHandler, KeyUpHandler {

    public void onChange(ChangeEvent event) {
      Widget sender = (Widget) event.getSource();

      if (sender == backColors) {
        formatter.setBackColor(backColors.getValue(backColors.getSelectedIndex()));
        backColors.setSelectedIndex(0);
      } else if (sender == foreColors) {
        formatter.setForeColor(foreColors.getValue(foreColors.getSelectedIndex()));
        foreColors.setSelectedIndex(0);
      } else if (sender == fonts) {
        formatter.setFontName(fonts.getValue(fonts.getSelectedIndex()));
        fonts.setSelectedIndex(0);
      } else if (sender == fontSizes) {
        formatter.setFontSize(fontSizesConstants[fontSizes.getSelectedIndex() - 1]);
        fontSizes.setSelectedIndex(0);
      }
    }

    public void onClick(ClickEvent event) {
      Widget sender = (Widget) event.getSource();

      if (sender == bold) {
        bold.invert();
        formatter.toggleBold();
      } else if (sender == italic) {
        italic.invert();
        formatter.toggleItalic();
      } else if (sender == underline) {
        underline.invert();
        formatter.toggleUnderline();
      } else if (sender == subscript) {
        subscript.invert();
        formatter.toggleSubscript();
      } else if (sender == superscript) {
        superscript.invert();
        formatter.toggleSuperscript();
      } else if (sender == strikethrough) {
        strikethrough.invert();
        formatter.toggleStrikethrough();

      } else if (sender == indent) {
        formatter.rightIndent();
      } else if (sender == outdent) {
        formatter.leftIndent();
      } else if (sender == justifyLeft) {
        formatter.setJustification(RichTextArea.Justification.LEFT);
      } else if (sender == justifyCenter) {
        formatter.setJustification(RichTextArea.Justification.CENTER);
      } else if (sender == justifyRight) {
        formatter.setJustification(RichTextArea.Justification.RIGHT);

      } else if (sender == insertImage) {
        Global.inputString("Image URL", null, new StringCallback() {
          @Override
          public void onSuccess(String value) {
            formatter.insertImage(value);
          }}, "http://");

      } else if (sender == createLink) {
        Global.inputString("Link URL", null, new StringCallback() {
          @Override
          public void onSuccess(String value) {
            formatter.createLink(value);
          }}, "http://");

      } else if (sender == removeLink) {
        formatter.removeLink();

      } else if (sender == hr) {
        formatter.insertHorizontalRule();
      } else if (sender == ol) {
        formatter.insertOrderedList();
      } else if (sender == ul) {
        formatter.insertUnorderedList();

      } else if (sender == removeFormat) {
        formatter.removeFormat();
      } else if (sender == area) {
        updateStatus();

      } else if (sender == insertHtml) {
        Global.inputString("Html", new StringCallback() {
          @Override
          public void onSuccess(String value) {
            formatter.insertHTML(value);
          }});

      } else if (sender == undo) {
        formatter.undo();
      } else if (sender == redo) {
        formatter.redo();
      }
    }

    public void onKeyDown(KeyDownEvent event) {
      if (UiHelper.isSave(event.getNativeEvent())) {
        EventUtils.eatEvent(event.getNativeEvent());
        accept.execute();
      }
    }

    public void onKeyUp(KeyUpEvent event) {
      Widget sender = (Widget) event.getSource();
      if (sender == area) {
        updateStatus();
      }
    }
  }

  private static final RichTextArea.FontSize[] fontSizesConstants = new RichTextArea.FontSize[] {
      RichTextArea.FontSize.XX_SMALL, RichTextArea.FontSize.X_SMALL,
      RichTextArea.FontSize.SMALL, RichTextArea.FontSize.MEDIUM,
      RichTextArea.FontSize.LARGE, RichTextArea.FontSize.X_LARGE,
      RichTextArea.FontSize.XX_LARGE};

  private Images images = (Images) GWT.create(Images.class);
  private EventHandler handler = new EventHandler();

  private RichTextArea area;
  private Formatter formatter;

  private Horizontal firstRow = new Horizontal();
  private Horizontal secondRow = new Horizontal();

  private Toggle bold;
  private Toggle italic;
  private Toggle underline;
  private Toggle subscript;
  private Toggle superscript;
  private Toggle strikethrough;

  private BeeImage indent;
  private BeeImage outdent;
  private BeeImage justifyLeft;
  private BeeImage justifyCenter;
  private BeeImage justifyRight;
  private BeeImage hr;
  private BeeImage ol;
  private BeeImage ul;
  private BeeImage insertImage;
  private BeeImage createLink;
  private BeeImage removeLink;
  private BeeImage removeFormat;
  private BeeImage insertHtml;
  private BeeImage undo;
  private BeeImage redo;

  private BeeListBox backColors;
  private BeeListBox foreColors;
  private BeeListBox fonts;
  private BeeListBox fontSizes;

  private final BeeCommand accept;

  public RichTextToolbar(Editor editor, RichTextArea richText) {
    this.area = richText;
    this.formatter = richText.getFormatter();

    this.accept = new EditorFactory.Accept(editor);

    firstRow.add(new BeeImage(Global.getImages().save(), this.accept));
    firstRow.add(createSpacer(1.0, Unit.EM));

    firstRow.add(undo = createButton(Global.getImages().undo(), "Undo"));
    firstRow.add(redo = createButton(Global.getImages().redo(), "Redo"));
    firstRow.add(removeFormat = createButton(images.removeFormat(), "Remove Formatting"));
    firstRow.add(createSpacer());

    firstRow.add(bold = createToggle(images.bold(), "Toggle Bold"));
    firstRow.add(italic = createToggle(images.italic(), "Toggle Italic"));
    firstRow.add(underline = createToggle(images.underline(), "Toggle Underline"));
    firstRow.add(subscript = createToggle(images.subscript(), "Toggle Subscript"));
    firstRow.add(superscript = createToggle(images.superscript(), "Toggle Superscript"));
    firstRow.add(strikethrough = createToggle(images.strikeThrough(), "Toggle Strikethrough"));
    firstRow.add(createSpacer());

    firstRow.add(justifyLeft = createButton(images.justifyLeft(), "Left Justify"));
    firstRow.add(justifyCenter = createButton(images.justifyCenter(), "Center"));
    firstRow.add(justifyRight = createButton(images.justifyRight(), "Right Justify"));
    firstRow.add(indent = createButton(images.indent(), "Indent Right"));
    firstRow.add(outdent = createButton(images.outdent(), "Indent Left"));
    firstRow.add(createSpacer());

    firstRow.add(insertHtml = createButton(Global.getImages().html(), "Insert HTML"));
    firstRow.add(hr = createButton(images.hr(), "Insert Horizontal Rule"));
    firstRow.add(ol = createButton(images.ol(), "Insert Ordered List"));
    firstRow.add(ul = createButton(images.ul(), "Insert Unordered List"));
    firstRow.add(insertImage = createButton(images.insertImage(), "Insert Image"));
    firstRow.add(createSpacer());

    firstRow.add(createLink = createButton(images.createLink(), "Create Link"));
    firstRow.add(removeLink = createButton(images.removeLink(), "Remove Link"));
    firstRow.add(createSpacer(1.0, Unit.EM));

    firstRow.add(new BeeImage(Global.getImages().close(), new EditorFactory.Cancel(editor)));

    secondRow.add(backColors = createColorList("Background"));
    secondRow.add(foreColors = createColorList("Foreground"));
    secondRow.add(fonts = createFontList());
    secondRow.add(fontSizes = createFontSizes());

    add(firstRow);
    add(secondRow);
    firstRow.setStyleName("bee-RichTextToolbar-row");
    secondRow.setStyleName("bee-RichTextToolbar-row");

    richText.addKeyDownHandler(handler);
    richText.addKeyUpHandler(handler);
    richText.addClickHandler(handler);
  }

  public boolean isEnabled() {
    return DomUtils.isEnabled(this);
  }

  public void setEnabled(boolean enabled) {
    DomUtils.enableChildren(this, enabled);
  }

  public void updateStatus() {
    bold.setDown(formatter.isBold());
    italic.setDown(formatter.isItalic());
    underline.setDown(formatter.isUnderlined());
    subscript.setDown(formatter.isSubscript());
    superscript.setDown(formatter.isSuperscript());
    strikethrough.setDown(formatter.isStrikethrough());
  }

  private BeeImage createButton(ImageResource img, String tip) {
    BeeImage ib = new BeeImage(img);
    ib.addClickHandler(handler);
    ib.setTitle(tip);
    return ib;
  }

  private BeeListBox createColorList(String caption) {
    BeeListBox lb = new BeeListBox();
    lb.addChangeHandler(handler);
    lb.setVisibleItemCount(1);

    lb.addItem(caption);
    lb.addItem("White", "white");
    lb.addItem("Black", "black");
    lb.addItem("Red", "red");
    lb.addItem("Green", "green");
    lb.addItem("Yellow", "yellow");
    lb.addItem("Blue", "blue");
    return lb;
  }

  private BeeListBox createFontList() {
    BeeListBox lb = new BeeListBox();
    lb.addChangeHandler(handler);
    lb.setVisibleItemCount(1);

    lb.addItem("Font", "");
    lb.addItem("Normal", "");
    lb.addItem("Times New Roman", "Times New Roman");
    lb.addItem("Arial", "Arial");
    lb.addItem("Courier New", "Courier New");
    lb.addItem("Georgia", "Georgia");
    lb.addItem("Trebuchet", "Trebuchet");
    lb.addItem("Verdana", "Verdana");
    return lb;
  }

  private BeeListBox createFontSizes() {
    BeeListBox lb = new BeeListBox();
    lb.addChangeHandler(handler);
    lb.setVisibleItemCount(1);

    lb.addItem("Size");
    lb.addItem("XX-Small");
    lb.addItem("X-Small");
    lb.addItem("Small");
    lb.addItem("Medium");
    lb.addItem("Large");
    lb.addItem("X-Large");
    lb.addItem("XX-Large");
    return lb;
  }

  private Widget createSpacer() {
    return createSpacer(5.0, Unit.PX);
  }

  private Widget createSpacer(Double width, Unit unit) {
    Html spacer = new Html();
    spacer.setWidth(StyleUtils.toCssLength(width, unit));
    return spacer;
  }

  private Toggle createToggle(ImageResource img, String tip) {
    Toggle tb = new Toggle(new Image(img));
    tb.addClickHandler(handler);
    tb.setTitle(tip);
    return tb;
  }
}
