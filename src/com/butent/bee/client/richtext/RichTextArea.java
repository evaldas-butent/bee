package com.butent.bee.client.richtext;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHTML;

class RichTextArea extends FocusWidget implements HasHTML {

  enum FontSize {
    XX_SMALL(1),
    X_SMALL(2),
    SMALL(3),
    MEDIUM(4),
    LARGE(5),
    X_LARGE(6),
    XX_LARGE(7);

    private final int number;

    private FontSize(int number) {
      this.number = number;
    }

    int getNumber() {
      return number;
    }
  }

  interface Formatter {

    void createLink(String url);

    String getBackColor();

    String getForeColor();

    void insertHorizontalRule();

    void insertHTML(String html);

    void insertImage(String url);

    void insertOrderedList();

    void insertUnorderedList();

    boolean isBold();

    boolean isItalic();

    boolean isStrikethrough();

    boolean isSubscript();

    boolean isSuperscript();

    boolean isUnderlined();

    void leftIndent();

    boolean queryCommandSupported(String cmd);
    
    void redo();

    void removeFormat();

    void removeLink();

    void rightIndent();

    void selectAll();

    void setBackColor(String color);

    void setFontName(String name);

    void setFontSize(FontSize fontSize);

    void setForeColor(String color);

    void setJustification(Justification justification);

    void toggleBold();

    void toggleItalic();

    void toggleStrikethrough();

    void toggleSubscript();

    void toggleSuperscript();

    void toggleUnderline();

    void undo();
  }

  enum Justification {
    CENTER("JustifyCenter"),
    FULL("JustifyFull"),
    LEFT("JustifyLeft"),
    RIGHT("JustifyRight");
    
    private final String cmd;

    private Justification(String cmd) {
      this.cmd = cmd;
    }

    String getCmd() {
      return cmd;
    }
  }

  private final RichTextAreaImpl impl;

  RichTextArea() {
    this.impl = new RichTextAreaImpl();
    setElement(impl.getElement());
  }

  @Override
  public String getHTML() {
    return impl.getHTML();
  }

  @Override
  public String getText() {
    return impl.getText();
  }

  @Override
  public boolean isEnabled() {
    return impl.isEnabled();
  }

  @Override
  public void setEnabled(boolean enabled) {
    impl.setEnabled(enabled);
  }

  @Override
  public void setFocus(boolean focused) {
    if (isAttached()) {
      impl.setFocus(focused);
    }
  }

  @Override
  public void setHTML(String html) {
    impl.setHTML(html);
  }

  @Override
  public void setText(String text) {
    impl.setText(text);
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    impl.initElement();
  }

  @Override
  protected void onDetach() {
    super.onDetach();
    impl.uninitElement();
  }

  Formatter getFormatter() {
    return impl;
  }
}
