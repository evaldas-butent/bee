package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Attribute;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.Keywords;

public class Textarea extends FertileElement {

  private static final String WRAP_HARD = "hard";
  private static final String WRAP_SOFT = "soft";

  public Textarea() {
    super();
  }

  public Textarea addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Textarea autocomplete(String value) {
    setAttribute(Attribute.AUTOCOMPLETE, value);
    return this;
  }

  public Textarea autocompleteOff() {
    setAttribute(Attribute.AUTOCOMPLETE, Keywords.AUTOCOMPLETE_OFF);
    return this;
  }

  public Textarea autocompleteOn() {
    setAttribute(Attribute.AUTOCOMPLETE, Keywords.AUTOCOMPLETE_ON);
    return this;
  }

  public Textarea autofocus() {
    setAttribute(Attribute.AUTOFOCUS, true);
    return this;
  }

  public Textarea cols(int value) {
    setAttribute(Attribute.COLS, value);
    return this;
  }

  

  public Textarea dirName(String value) {
    setAttribute(Attribute.DIRNAME, value);
    return this;
  }

  public Textarea disabled() {
    setAttribute(Attribute.DISABLED, true);
    return this;
  }

  public Textarea enabled() {
    setAttribute(Attribute.DISABLED, false);
    return this;
  }

  public Textarea form(String value) {
    setAttribute(Attribute.FORM, value);
    return this;
  }

  public Textarea id(String value) {
    setId(value);
    return this;
  }

  public Textarea inputModeEmail() {
    setAttribute(Attribute.INPUTMODE, Keywords.INPUT_MODE_EMAIL);
    return this;
  }

  public Textarea inputModeFullWidthLatin() {
    setAttribute(Attribute.INPUTMODE, Keywords.INPUT_MODE_FULL_WIDTH_LATIN);
    return this;
  }

  public Textarea inputModeKana() {
    setAttribute(Attribute.INPUTMODE, Keywords.INPUT_MODE_KANA);
    return this;
  }

  public Textarea inputModeKatakana() {
    setAttribute(Attribute.INPUTMODE, Keywords.INPUT_MODE_KATAKANA);
    return this;
  }

  public Textarea inputModeLatin() {
    setAttribute(Attribute.INPUTMODE, Keywords.INPUT_MODE_LATIN);
    return this;
  }

  public Textarea inputModeLatinName() {
    setAttribute(Attribute.INPUTMODE, Keywords.INPUT_MODE_LATIN_NAME);
    return this;
  }

  public Textarea inputModeLatinProse() {
    setAttribute(Attribute.INPUTMODE, Keywords.INPUT_MODE_LATIN_PROSE);
    return this;
  }

  public Textarea inputModeNumeric() {
    setAttribute(Attribute.INPUTMODE, Keywords.INPUT_MODE_NUMERIC);
    return this;
  }

  public Textarea inputModeTel() {
    setAttribute(Attribute.INPUTMODE, Keywords.INPUT_MODE_TEL);
    return this;
  }

  public Textarea inputModeUrl() {
    setAttribute(Attribute.INPUTMODE, Keywords.INPUT_MODE_URL);
    return this;
  }

  public Textarea inputModeVerbatim() {
    setAttribute(Attribute.INPUTMODE, Keywords.INPUT_MODE_VERBATIM);
    return this;
  }

  public Textarea lang(String value) {
    setLang(value);
    return this;
  }

  public Textarea maxLength(int value) {
    setAttribute(Attribute.MAXLENGTH, value);
    return this;
  }

  public Textarea minLength(int value) {
    setAttribute(Attribute.MINLENGTH, value);
    return this;
  }

  public Textarea name(String value) {
    setAttribute(Attribute.NAME, value);
    return this;
  }

  public Textarea placeholder(String value) {
    setAttribute(Attribute.PLACEHOLDER, value);
    return this;
  }

  public Textarea readOnly() {
    setAttribute(Attribute.READONLY, true);
    return this;
  }

  public Textarea required() {
    setAttribute(Attribute.REQUIRED, true);
    return this;
  }

  public Textarea rows(int value) {
    setAttribute(Attribute.ROWS, value);
    return this;
  }

  public Textarea text(String text) {
    super.appendText(text);
    return this;
  }

  public Textarea title(String value) {
    setTitle(value);
    return this;
  }

  public Textarea wrapHard() {
    setAttribute(Attribute.WRAP, WRAP_HARD);
    return this;
  }

  public Textarea wrapSoft() {
    setAttribute(Attribute.WRAP, WRAP_SOFT);
    return this;
  }
}
