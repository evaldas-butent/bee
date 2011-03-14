package com.butent.bee.client.language;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public enum Language {
  AFRIKAANS("af"),
  ALBANIAN("sq"),
  AMHARIC("am"),
  ARABIC("ar"),
  ARMENIAN("hy"),
  AZERBAIJANI("az"),
  BASQUE("eu"),
  BELARUSIAN("be"),
  BENGALI("bn"),
  BIHARI("bh"),
  BULGARIAN("bg"),
  BURMESE("my"),
  CATALAN("ca"),
  CHEROKEE("chr"),
  CHINESE("zh"),
  CHINESE_SIMPLIFIED("zh-CN"),
  CHINESE_TRADITIONAL("zh-TW"),
  CROATIAN("hr"),
  CZECH("cs"),
  DANISH("da"),
  DHIVEHI("dv"),
  DUTCH("nl"),
  ENGLISH("en"),
  ESPERANTO("eo"),
  ESTONIAN("et"),
  FILIPINO("tl"),
  FINNISH("fi"),
  FRENCH("fr"),
  GALICIAN("gl"),
  GEORGIAN("ka"),
  GERMAN("de"),
  GREEK("el"),
  GUARANI("gn"),
  GUJARATI("gu"),
  HEBREW("iw"),
  HINDI("hi"),
  HUNGARIAN("hu"),
  ICELANDIC("is"),
  INDONESIAN("id"),
  INUKTITUT("iu"),
  IRISH("ga"),
  ITALIAN("it"),
  JAPANESE("ja"),
  KANNADA("kn"),
  KAZAKH("kk"),
  KHMER("km"),
  KOREAN("ko"),
  KURDISH("ku"),
  KYRGYZ("ky"),
  LAOTHIAN("lo"),
  LATVIAN("lv"),
  LITHUANIAN("lt"),
  MACEDONIAN("mk"),
  MALAY("ms"),
  MALAYALAM("ml"),
  MALTESE("mt"),
  MARATHI("mr"),
  MONGOLIAN("mn"),
  NEPALI("ne"),
  NORWEGIAN("no"),
  ORIYA("or"),
  PASHTO("ps"),
  PERSIAN("fa"),
  POLISH("pl"),
  PORTUGUESE("pt-PT"),
  PUNJABI("pa"),
  ROMANIAN("ro"),
  RUSSIAN("ru"),
  SANSKRIT("sa"),
  SERBIAN("sr"),
  SINDHI("sd"),
  SINHALESE("si"),
  SLOVAK("sk"),
  SLOVENIAN("sl"),
  SPANISH("es"),
  SWAHILI("sw"),
  SWEDISH("sv"),
  TAJIK("tg"),
  TAMIL("ta"),
  TAGALOG("tl"),
  TELUGU("te"),
  THAI("th"),
  TIBETAN("bo"),
  TURKISH("tr"),
  UKRAINIAN("uk"),
  URDU("ur"),
  UZBEK("uz"),
  UIGHUR("ug"),
  VIETNAMESE("vi"),
  UNKNOWN(""),
  WELSH("cy"),
  YIDDISH("yi");
  
  public static class SupportedLanguages extends JavaScriptObject {
    protected SupportedLanguages() {
    }

    public final native String getLanguageCode(String name) /*-{
      return this[name];
    }-*/;

    public final native String getLanguageName(String languageCode) /*-{
      if (!this.__lookupByCode) {
        this.__lookupByCode = {};
        for (var prop in this) {
          if (prop.match(/^[A-Z_]+$/)) {
            this.__lookupByCode[this[prop]] = prop;
          }
        }
      }
      return this.__lookupByCode[languageCode];
    }-*/;

    public final native JsArrayString getLanguages() /*-{
      var result = [];
      for (var prop in this) {
        if (prop.match(/^[A-Z_]+$/)) {
          result.push(prop);
        }
      }
      return result;
    }-*/;
  }
  private static Map<String, Language> codes = Maps.newHashMap();
  
  static {
    for (Language lang : Language.values()) {
      codes.put(BeeUtils.normalize(lang.getLangCode()), lang);
    }
  }

  public static Language getByCode(String code) {
    Assert.notEmpty(code);
    return codes.get(BeeUtils.normalize(code));
  }

  public static native SupportedLanguages nativeSupportedLangauges() /*-{
    return $wnd.google.language.Languages;
  }-*/;

  private String langCode;

  private Language(String langCode) {
    this.langCode = langCode;
  }

  public String getLangCode() {
    return langCode;
  }
}
