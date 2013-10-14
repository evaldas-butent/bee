package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.Element;
import com.butent.bee.shared.html.builder.Keywords;

public class Input extends Element {

  public enum Type {
    HIDDEN("hidden"),
    TEXT("text"),
    SEARCH("search"),
    TEL("tel"),
    URL("url"),
    EMAIL("email"),
    PASSWORD("password"),
    DATE_TIME("datetime"),
    DATE("date"),
    MONTH("month"),
    WEEK("week"),
    TIME("time"),
    DATE_TIME_LOCAL("datetime-local"),
    NUMBER("number"),
    RANGE("range"),
    COLOR("color"),
    CHECK_BOX("checkbox"),
    RADIO("radio"),
    FILE("file"),
    SUBMIT("submit"),
    IMAGE("image"),
    RESET("reset"),
    BUTTON("button");

    private final String keyword;

    private Type(String keyword) {
      this.keyword = keyword;
    }

    public String getKeyword() {
      return keyword;
    }
  }

  private static final String STEP_ANY = "any";

  public Input() {
    super();
  }

  public Input accept(String value) {
    setAttribute(Attribute.ACCEPT, value);
    return this;
  }

  public Input addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Input alt(String value) {
    setAttribute(Attribute.ALT, value);
    return this;
  }

  public Input autocomplete(String value) {
    setAttribute(Attribute.AUTOCOMPLETE, value);
    return this;
  }

  public Input autocompleteOff() {
    setAttribute(Attribute.AUTOCOMPLETE, Keywords.AUTOCOMPLETE_OFF);
    return this;
  }

  public Input autocompleteOn() {
    setAttribute(Attribute.AUTOCOMPLETE, Keywords.AUTOCOMPLETE_ON);
    return this;
  }

  public Input autofocus() {
    setAttribute(Attribute.AUTOFOCUS, true);
    return this;
  }

  public Input checked() {
    setAttribute(Attribute.CHECKED, true);
    return this;
  }

  public Input dirName(String value) {
    setAttribute(Attribute.DIR_NAME, value);
    return this;
  }

  public Input disabled() {
    setAttribute(Attribute.DISABLED, true);
    return this;
  }

  public Input enabled() {
    setAttribute(Attribute.DISABLED, false);
    return this;
  }

  public Input form(String value) {
    setAttribute(Attribute.FORM, value);
    return this;
  }

  public Input formAction(String value) {
    setAttribute(Attribute.FORM_ACTION, value);
    return this;
  }

  public Input formEncTypeMultipart() {
    setAttribute(Attribute.FORM_ENC_TYPE, Keywords.ENC_TYPE_MULTIPART_DATA);
    return this;
  }

  public Input formEncTypeText() {
    setAttribute(Attribute.FORM_ENC_TYPE, Keywords.ENC_TYPE_TEXT_PLAIN);
    return this;
  }

  public Input formEncTypeUrl() {
    setAttribute(Attribute.FORM_ENC_TYPE, Keywords.ENC_TYPE_URL_ENCODED);
    return this;
  }

  public Input formMethodGet() {
    setAttribute(Attribute.FORM_METHOD, Keywords.METHOD_GET);
    return this;
  }

  public Input formMethodPost() {
    setAttribute(Attribute.FORM_METHOD, Keywords.METHOD_POST);
    return this;
  }

  public Input formNoValidate() {
    setAttribute(Attribute.FORM_NO_VALIDATE, true);
    return this;
  }

  public Input formTarget(String value) {
    setAttribute(Attribute.FORM_TARGET, value);
    return this;
  }

  public Input formTargetBlank() {
    setAttribute(Attribute.FORM_TARGET, Keywords.BROWSING_CONTEXT_BLANK);
    return this;
  }

  public Input formTargetParent() {
    setAttribute(Attribute.FORM_TARGET, Keywords.BROWSING_CONTEXT_PARENT);
    return this;
  }

  public Input formTargetSelf() {
    setAttribute(Attribute.FORM_TARGET, Keywords.BROWSING_CONTEXT_SELF);
    return this;
  }

  public Input formTargetTop() {
    setAttribute(Attribute.FORM_TARGET, Keywords.BROWSING_CONTEXT_TOP);
    return this;
  }

  public Input height(int value) {
    setAttribute(Attribute.HEIGHT, value);
    return this;
  }

  public Input id(String value) {
    setId(value);
    return this;
  }

  public Input inputModeEmail() {
    setAttribute(Attribute.INPUT_MODE, Keywords.INPUT_MODE_EMAIL);
    return this;
  }

  public Input inputModeFullWidthLatin() {
    setAttribute(Attribute.INPUT_MODE, Keywords.INPUT_MODE_FULL_WIDTH_LATIN);
    return this;
  }

  public Input inputModeKana() {
    setAttribute(Attribute.INPUT_MODE, Keywords.INPUT_MODE_KANA);
    return this;
  }

  public Input inputModeKatakana() {
    setAttribute(Attribute.INPUT_MODE, Keywords.INPUT_MODE_KATAKANA);
    return this;
  }

  public Input inputModeLatin() {
    setAttribute(Attribute.INPUT_MODE, Keywords.INPUT_MODE_LATIN);
    return this;
  }

  public Input inputModeLatinName() {
    setAttribute(Attribute.INPUT_MODE, Keywords.INPUT_MODE_LATIN_NAME);
    return this;
  }

  public Input inputModeLatinProse() {
    setAttribute(Attribute.INPUT_MODE, Keywords.INPUT_MODE_LATIN_PROSE);
    return this;
  }

  public Input inputModeNumeric() {
    setAttribute(Attribute.INPUT_MODE, Keywords.INPUT_MODE_NUMERIC);
    return this;
  }

  public Input inputModeTel() {
    setAttribute(Attribute.INPUT_MODE, Keywords.INPUT_MODE_TEL);
    return this;
  }

  public Input inputModeUrl() {
    setAttribute(Attribute.INPUT_MODE, Keywords.INPUT_MODE_URL);
    return this;
  }

  public Input inputModeVerbatim() {
    setAttribute(Attribute.INPUT_MODE, Keywords.INPUT_MODE_VERBATIM);
    return this;
  }

  public Input lang(String value) {
    setLang(value);
    return this;
  }

  public Input list(String value) {
    setAttribute(Attribute.LIST, value);
    return this;
  }

  public Input max(int value) {
    setAttribute(Attribute.MAX, value);
    return this;
  }

  public Input max(String value) {
    setAttribute(Attribute.MAX, value);
    return this;
  }

  public Input maxLength(int value) {
    setAttribute(Attribute.MAX_LENGTH, value);
    return this;
  }

  public Input min(int value) {
    setAttribute(Attribute.MIN, value);
    return this;
  }

  public Input min(String value) {
    setAttribute(Attribute.MIN, value);
    return this;
  }

  public Input minLength(int value) {
    setAttribute(Attribute.MIN_LENGTH, value);
    return this;
  }

  public Input multiple() {
    setAttribute(Attribute.MULTIPLE, true);
    return this;
  }

  public Input name(String value) {
    setAttribute(Attribute.NAME, value);
    return this;
  }

  public Input onKeyDown(String event) {
    setOnKeyDown(event);
    return this;
  }

  public Input onKeyPress(String event) {
    setOnKeyPress(event);
    return this;
  }

  public Input onKeyUp(String event) {
    setOnKeyUp(event);
    return this;
  }

  public Input pattern(String value) {
    setAttribute(Attribute.PATTERN, value);
    return this;
  }

  public Input placeholder(String value) {
    setAttribute(Attribute.PLACEHOLDER, value);
    return this;
  }

  public Input readOnly() {
    setAttribute(Attribute.READ_ONLY, true);
    return this;
  }

  public Input required() {
    setAttribute(Attribute.REQUIRED, true);
    return this;
  }

  public Input required(boolean value) {
    setAttribute(Attribute.REQUIRED, value);
    return this;
  }

  public Input size(int value) {
    setAttribute(Attribute.SIZE, value);
    return this;
  }

  public Input src(String value) {
    setAttribute(Attribute.SRC, value);
    return this;
  }

  public Input step(double value) {
    setAttribute(Attribute.STEP, value);
    return this;
  }

  public Input step(int value) {
    setAttribute(Attribute.STEP, value);
    return this;
  }

  public Input stepAny() {
    setAttribute(Attribute.STEP, STEP_ANY);
    return this;
  }

  public Input title(String value) {
    setTitle(value);
    return this;
  }

  public Input type(Type type) {
    if (type == null) {
      removeAttribute(Attribute.TYPE);
    } else {
      setAttribute(Attribute.TYPE, type.getKeyword());
    }
    return this;
  }

  public Input value(String value) {
    setAttribute(Attribute.VALUE, value);
    return this;
  }

  public Input width(int value) {
    setAttribute(Attribute.WIDTH, value);
    return this;
  }
}
