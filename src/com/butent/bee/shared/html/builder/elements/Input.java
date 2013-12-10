package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Autocomplete;
import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.Element;

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
    setAttribute(Attributes.ACCEPT, value);
    return this;
  }

  public Input addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Input alt(String value) {
    setAttribute(Attributes.ALT, value);
    return this;
  }

  public Input autocomplete(Autocomplete autocomplete) {
    String value = (autocomplete == null) ? null : autocomplete.build();
    return autocomplete(value);
  }

  public Input autocomplete(String value) {
    setAttribute(Attributes.AUTOCOMPLETE, value);
    return this;
  }

  public Input autocompleteOff() {
    setAttribute(Attributes.AUTOCOMPLETE, Autocomplete.OFF);
    return this;
  }

  public Input autocompleteOn() {
    setAttribute(Attributes.AUTOCOMPLETE, Autocomplete.ON);
    return this;
  }

  public Input autofocus() {
    setAttribute(Attributes.AUTOFOCUS, true);
    return this;
  }

  public Input checked() {
    setAttribute(Attributes.CHECKED, true);
    return this;
  }

  public Input dirName(String value) {
    setAttribute(Attributes.DIR_NAME, value);
    return this;
  }

  public Input disabled() {
    setAttribute(Attributes.DISABLED, true);
    return this;
  }

  public Input enabled() {
    setAttribute(Attributes.DISABLED, false);
    return this;
  }

  public Input form(String value) {
    setAttribute(Attributes.FORM, value);
    return this;
  }

  public Input formAction(String value) {
    setAttribute(Attributes.FORM_ACTION, value);
    return this;
  }

  public Input formEncTypeMultipart() {
    setAttribute(Attributes.FORM_ENC_TYPE, Keywords.ENC_TYPE_MULTIPART_DATA);
    return this;
  }

  public Input formEncTypeText() {
    setAttribute(Attributes.FORM_ENC_TYPE, Keywords.ENC_TYPE_TEXT_PLAIN);
    return this;
  }

  public Input formEncTypeUrl() {
    setAttribute(Attributes.FORM_ENC_TYPE, Keywords.ENC_TYPE_URL_ENCODED);
    return this;
  }

  public Input formMethodGet() {
    setAttribute(Attributes.FORM_METHOD, Keywords.METHOD_GET);
    return this;
  }

  public Input formMethodPost() {
    setAttribute(Attributes.FORM_METHOD, Keywords.METHOD_POST);
    return this;
  }

  public Input formNoValidate() {
    setAttribute(Attributes.FORM_NO_VALIDATE, true);
    return this;
  }

  public Input formTarget(String value) {
    setAttribute(Attributes.FORM_TARGET, value);
    return this;
  }

  public Input formTargetBlank() {
    setAttribute(Attributes.FORM_TARGET, Keywords.BROWSING_CONTEXT_BLANK);
    return this;
  }

  public Input formTargetParent() {
    setAttribute(Attributes.FORM_TARGET, Keywords.BROWSING_CONTEXT_PARENT);
    return this;
  }

  public Input formTargetSelf() {
    setAttribute(Attributes.FORM_TARGET, Keywords.BROWSING_CONTEXT_SELF);
    return this;
  }

  public Input formTargetTop() {
    setAttribute(Attributes.FORM_TARGET, Keywords.BROWSING_CONTEXT_TOP);
    return this;
  }

  public Input height(int value) {
    setAttribute(Attributes.HEIGHT, value);
    return this;
  }

  public Input id(String value) {
    setId(value);
    return this;
  }

  public Input inputModeEmail() {
    setAttribute(Attributes.INPUT_MODE, Keywords.INPUT_MODE_EMAIL);
    return this;
  }

  public Input inputModeFullWidthLatin() {
    setAttribute(Attributes.INPUT_MODE, Keywords.INPUT_MODE_FULL_WIDTH_LATIN);
    return this;
  }

  public Input inputModeKana() {
    setAttribute(Attributes.INPUT_MODE, Keywords.INPUT_MODE_KANA);
    return this;
  }

  public Input inputModeKatakana() {
    setAttribute(Attributes.INPUT_MODE, Keywords.INPUT_MODE_KATAKANA);
    return this;
  }

  public Input inputModeLatin() {
    setAttribute(Attributes.INPUT_MODE, Keywords.INPUT_MODE_LATIN);
    return this;
  }

  public Input inputModeLatinName() {
    setAttribute(Attributes.INPUT_MODE, Keywords.INPUT_MODE_LATIN_NAME);
    return this;
  }

  public Input inputModeLatinProse() {
    setAttribute(Attributes.INPUT_MODE, Keywords.INPUT_MODE_LATIN_PROSE);
    return this;
  }

  public Input inputModeNumeric() {
    setAttribute(Attributes.INPUT_MODE, Keywords.INPUT_MODE_NUMERIC);
    return this;
  }

  public Input inputModeTel() {
    setAttribute(Attributes.INPUT_MODE, Keywords.INPUT_MODE_TEL);
    return this;
  }

  public Input inputModeUrl() {
    setAttribute(Attributes.INPUT_MODE, Keywords.INPUT_MODE_URL);
    return this;
  }

  public Input inputModeVerbatim() {
    setAttribute(Attributes.INPUT_MODE, Keywords.INPUT_MODE_VERBATIM);
    return this;
  }

  public Input lang(String value) {
    setLang(value);
    return this;
  }

  public Input list(String value) {
    setAttribute(Attributes.LIST, value);
    return this;
  }

  public Input max(int value) {
    setAttribute(Attributes.MAX, value);
    return this;
  }

  public Input max(String value) {
    setAttribute(Attributes.MAX, value);
    return this;
  }

  public Input maxLength(int value) {
    setAttribute(Attributes.MAX_LENGTH, value);
    return this;
  }

  public Input min(int value) {
    setAttribute(Attributes.MIN, value);
    return this;
  }

  public Input min(String value) {
    setAttribute(Attributes.MIN, value);
    return this;
  }

  public Input minLength(int value) {
    setAttribute(Attributes.MIN_LENGTH, value);
    return this;
  }

  public Input multiple() {
    setAttribute(Attributes.MULTIPLE, true);
    return this;
  }

  public Input name(String value) {
    setAttribute(Attributes.NAME, value);
    return this;
  }

  public Input onChange(String event) {
    setOnChange(event);
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
    setAttribute(Attributes.PATTERN, value);
    return this;
  }

  public Input placeholder(String value) {
    setAttribute(Attributes.PLACEHOLDER, value);
    return this;
  }

  public Input readOnly() {
    setAttribute(Attributes.READ_ONLY, true);
    return this;
  }

  public Input required() {
    setAttribute(Attributes.REQUIRED, true);
    return this;
  }

  public Input required(boolean value) {
    setAttribute(Attributes.REQUIRED, value);
    return this;
  }

  public Input size(int value) {
    setAttribute(Attributes.SIZE, value);
    return this;
  }

  public Input src(String value) {
    setAttribute(Attributes.SRC, value);
    return this;
  }

  public Input step(double value) {
    setAttribute(Attributes.STEP, value);
    return this;
  }

  public Input step(int value) {
    setAttribute(Attributes.STEP, value);
    return this;
  }

  public Input stepAny() {
    setAttribute(Attributes.STEP, STEP_ANY);
    return this;
  }

  public Input title(String value) {
    setTitle(value);
    return this;
  }

  public Input type(Type type) {
    if (type == null) {
      removeAttribute(Attributes.TYPE);
    } else {
      setAttribute(Attributes.TYPE, type.getKeyword());
    }
    return this;
  }

  public Input value(String value) {
    setAttribute(Attributes.VALUE, value);
    return this;
  }

  public Input value(int value) {
    setAttribute(Attributes.VALUE, value);
    return this;
  }

  public Input value(long value) {
    setAttribute(Attributes.VALUE, value);
    return this;
  }

  public Input width(int value) {
    setAttribute(Attributes.WIDTH, value);
    return this;
  }
}
