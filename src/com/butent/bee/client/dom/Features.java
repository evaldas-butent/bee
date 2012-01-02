package com.butent.bee.client.dom;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;

import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

/**
 * Checks whether a user's browser support certain features like mp3, drag and drop, web sockets,
 * canvas and so on.
 */

public class Features {
  private static String nsSvg = "http://www.w3.org/2000/svg";

  private static Boolean applicationCache = null;

  private static Boolean attributeAutocomplete = null;
  private static Boolean attributeAutofocus = null;
  private static Boolean attributeList = null;
  private static Boolean attributeMax = null;
  private static Boolean attributeMin = null;
  private static Boolean attributeMultiple = null;
  private static Boolean attributePattern = null;
  private static Boolean attributePlaceholder = null;
  private static Boolean attributeRequired = null;
  private static Boolean attributeStep = null;

  private static Boolean audio = null;
  private static String audioAac = null;
  private static String audioMp3 = null;
  private static String audioVorbis = null;
  private static String audioWav = null;

  private static Boolean canvas = null;
  private static Boolean canvasText = null;

  private static Boolean contentEditable = null;

  private static Boolean dnd = null;
  private static Boolean dndEvents = null;

  private static Boolean elementCommand = null;
  private static Boolean elementDataList = null;
  private static Boolean elementDetails = null;
  private static Boolean elementDevice = null;
  private static Boolean elementMeter = null;
  private static Boolean elementOutput = null;
  private static Boolean elementProgress = null;
  private static Boolean elementTime = null;

  private static Boolean fileApi = null;
  private static Boolean geolocation = null;
  private static Boolean indexedDB = null;

  private static Boolean inputColor = null;
  private static Boolean inputDate = null;
  private static Boolean inputDatetime = null;
  private static Boolean inputDatetimeLocal = null;
  private static Boolean inputEmail = null;
  private static Boolean inputMonth = null;
  private static Boolean inputNumber = null;
  private static Boolean inputRange = null;
  private static Boolean inputSearch = null;
  private static Boolean inputTel = null;
  private static Boolean inputTime = null;
  private static Boolean inputUrl = null;
  private static Boolean inputWeek = null;

  private static Boolean localStorage = null;
  private static Boolean microdata = null;
  private static Boolean postMessage = null;
  private static Boolean selectors = null;
  private static Boolean sendAsFormData = null;
  private static Boolean serverSentEvents = null;
  private static Boolean sessionStorage = null;

  private static Boolean smil = null;
  private static Boolean svg = null;
  private static Boolean svgClipPaths = null;
  private static Boolean svgInline = null;
  private static Boolean svgInTextHtml = null;

  private static Boolean undo = null;

  private static Boolean video = null;
  private static String videoH264 = null;
  private static String videoTheora = null;
  private static String videoWebm = null;
  private static Boolean videoCaptions = null;
  private static Boolean videoPoster = null;

  private static Boolean webGl = null;
  private static Boolean webSockets = null;
  private static Boolean webWorkers = null;

  private static Boolean xhrCrossDomain = null;
  private static Boolean xhrUploadProgress = null;

  public static String getAudioAac() {
    if (!supportsAudio()) {
      return BeeConst.STRING_EMPTY;
    }
    if (audioAac == null) {
      audioAac = testAudioAac();
    }
    return audioAac;
  }

  public static String getAudioMp3() {
    if (!supportsAudio()) {
      return BeeConst.STRING_EMPTY;
    }
    if (audioMp3 == null) {
      audioMp3 = testAudioMp3();
    }
    return audioMp3;
  }

  public static String getAudioVorbis() {
    if (!supportsAudio()) {
      return BeeConst.STRING_EMPTY;
    }
    if (audioVorbis == null) {
      audioVorbis = testAudioVorbis();
    }
    return audioVorbis;
  }

  public static String getAudioWav() {
    if (!supportsAudio()) {
      return BeeConst.STRING_EMPTY;
    }
    if (audioWav == null) {
      audioWav = testAudioWav();
    }
    return audioWav;
  }

  public static List<Property> getInfo() {
    List<Property> lst = PropertyUtils.createProperties(
        "Application Cache", supportsApplicationCache(),

        "Attribute Autocomplete", supportsAttributeAutocomplete(),
        "Attribute Autofocus", supportsAttributeAutofocus(),
        "Attribute List", supportsAttributeList(),
        "Attribute Max", supportsAttributeMax(),
        "Attribute Min", supportsAttributeMin(),
        "Attribute Multiple", supportsAttributeMultiple(),
        "Attribute Pattern", supportsAttributePattern(),
        "Attribute Placeholder", supportsAttributePlaceholder(),
        "Attribute Required", supportsAttributeRequired(),
        "Attribute Step", supportsAttributeStep(),

        "Audio", supportsAudio(),
        "Audio Aac", getAudioAac(),
        "Audio Mp3", getAudioMp3(),
        "Audio Vorbis", getAudioVorbis(),
        "Audio Wav", getAudioWav(),

        "Canvas", supportsCanvas(),
        "Canvas Text", supportsCanvasText(),

        "Content Editable", supportsContentEditable(),

        "Dnd", supportsDnd(),
        "Dnd Events", supportsDndEvents(),

        "Element Command", supportsElementCommand(),
        "Element DataList", supportsElementDataList(),
        "Element Details", supportsElementDetails(),
        "Element Device", supportsElementDevice(),
        "Element Meter", supportsElementMeter(),
        "Element Output", supportsElementOutput(),
        "Element Progress", supportsElementProgress(),
        "Element Time", supportsElementTime(),

        "File Api", supportsFileApi(),
        "Geolocation", supportsGeolocation(),
        "Indexed DB", supportsIndexedDB(),

        "Input Color", supportsInputColor(),
        "Input Date", supportsInputDate(),
        "Input Datetime", supportsInputDatetime(),
        "Input Datetime Local", supportsInputDatetimeLocal(),
        "Input Email", supportsInputEmail(),
        "Input Month", supportsInputMonth(),
        "Input Number", supportsInputNumber(),
        "Input Range", supportsInputRange(),
        "Input Search", supportsInputSearch(),
        "Input Tel", supportsInputTel(),
        "Input Time", supportsInputTime(),
        "Input Url", supportsInputUrl(),
        "Input Week", supportsInputWeek(),

        "Local Storage", supportsLocalStorage(),
        "Microdata", supportsMicrodata(),
        "Post Message", supportsPostMessage(),
        "Send As Form Data", supportsSendAsFormData(),
        "Selectors", supportsSelectors(),
        "Server Sent Events", supportsServerSentEvents(),
        "Session Storage", supportsSessionStorage(),

        "Smil", supportsSmil(),
        "Svg", supportsSvg(),
        "Svg Clip Paths", supportsSvgClipPaths(),
        "Svg Inline", supportsSvgInline(),
        "Svg In Text Html", supportsSvgInTextHtml(),

        "Undo", supportsUndo(),

        "Video", supportsVideo(),
        "Video H264", getVideoH264(),
        "Video Theora", getVideoTheora(),
        "Video Webm", getVideoWebm(),
        "Video Captions", supportsVideoCaptions(),
        "Video Poster", supportsVideoPoster(),

        "Web Gl", supportsWebGl(),
        "Web Sockets", supportsWebSockets(),
        "Web Workers", supportsWebWorkers(),

        "Xhr Cross Domain", supportsXhrCrossDomain(),
        "Xhr Upload Progress", supportsXhrUploadProgress());

    return lst;
  }

  public static String getVideoH264() {
    if (!supportsVideo()) {
      return BeeConst.STRING_EMPTY;
    }
    if (videoH264 == null) {
      videoH264 = testVideoH264();
    }
    return videoH264;
  }

  public static String getVideoTheora() {
    if (!supportsVideo()) {
      return BeeConst.STRING_EMPTY;
    }
    if (videoTheora == null) {
      videoTheora = testVideoTheora();
    }
    return videoTheora;
  }

  public static String getVideoWebm() {
    if (!supportsVideo()) {
      return BeeConst.STRING_EMPTY;
    }
    if (videoWebm == null) {
      videoWebm = testVideoWebm();
    }
    return videoWebm;
  }

  public static native JavaScriptObject getWindowProperty(String p) /*-{
    if (p == null || p == "") {
      return null;
    }

    var obj;
    try {
      obj = $wnd[p];
    } catch (err) {
      obj = null;
    }

    return obj;
  }-*/;

  public static native boolean isDocumentFunction(String fnc) /*-{
    if (fnc == null || fnc == "") {
      return false;
    }

    var ok;
    try {
      ok = (typeof ($doc[fnc]) == "function");
    } catch (err) {
      ok = false;
    }
    return ok;
  }-*/;

  public static native boolean isDocumentProperty(String p) /*-{
    if (p == null || p == "") {
      return false;
    }

    var ok;
    try {
      ok = !!$doc[p];
    } catch (err) {
      ok = false;
    }
    return ok;
  }-*/;

  public static boolean isEventSupported(String tagName, String eventName) {
    Assert.notEmpty(tagName);
    Assert.notEmpty(eventName);

    Element element = Document.get().createElement(tagName);
    if (element == null) {
      return false;
    }
    String event = eventName.startsWith("on") ? eventName : "on" + eventName;

    boolean ok = JsUtils.isIn(event, element);

    if (!ok) {
      element.setAttribute(eventName, BeeConst.STRING_EMPTY);
      ok = JsUtils.isFunction(element, event);

      JsUtils.clearProperty(element, event);
      element.removeAttribute(eventName);
    }

    element = null;
    return ok;
  }

  public static native boolean isInWindow(String p) /*-{
    if (p == null || p == "") {
      return false;
    } else {
      return p in $wnd;
    }
  }-*/;

  public static native boolean isNavigatorProperty(String p) /*-{
    if (p == null || p == "") {
      return false;
    }

    var ok;
    try {
      ok = !!$wnd.navigator[p];
    } catch (err) {
      ok = false;
    }
    return ok;
  }-*/;

  public static native boolean isWindowProperty(String p) /*-{
    if (p == null || p == "") {
      return false;
    }

    var ok;
    try {
      ok = !!$wnd[p];
    } catch (err) {
      ok = false;
    }
    return ok;
  }-*/;

  public static boolean supportsApplicationCache() {
    if (applicationCache == null) {
      applicationCache = testApplicationCache();
    }
    return applicationCache;
  }

  public static boolean supportsAttributeAutocomplete() {
    if (attributeAutocomplete == null) {
      attributeAutocomplete = testAttributeAutocomplete();
    }
    return attributeAutocomplete;
  }

  public static boolean supportsAttributeAutofocus() {
    if (attributeAutofocus == null) {
      attributeAutofocus = testAttributeAutofocus();
    }
    return attributeAutofocus;
  }

  public static boolean supportsAttributeList() {
    if (attributeList == null) {
      attributeList = testAttributeList();
    }
    return attributeList;
  }

  public static boolean supportsAttributeMax() {
    if (attributeMax == null) {
      attributeMax = testAttributeMax();
    }
    return attributeMax;
  }

  public static boolean supportsAttributeMin() {
    if (attributeMin == null) {
      attributeMin = testAttributeMin();
    }
    return attributeMin;
  }

  public static boolean supportsAttributeMultiple() {
    if (attributeMultiple == null) {
      attributeMultiple = testAttributeMultiple();
    }
    return attributeMultiple;
  }

  public static boolean supportsAttributePattern() {
    if (attributePattern == null) {
      attributePattern = testAttributePattern();
    }
    return attributePattern;
  }

  public static boolean supportsAttributePlaceholder() {
    if (attributePlaceholder == null) {
      attributePlaceholder = testAttributePlaceholder();
    }
    return attributePlaceholder;
  }

  public static boolean supportsAttributeRequired() {
    if (attributeRequired == null) {
      attributeRequired = testAttributeRequired();
    }
    return attributeRequired;
  }

  public static boolean supportsAttributeStep() {
    if (attributeStep == null) {
      attributeStep = testAttributeStep();
    }
    return attributeStep;
  }

  public static boolean supportsAudio() {
    if (audio == null) {
      audio = testAudio();
    }
    return audio;
  }

  public static boolean supportsCanvas() {
    if (canvas == null) {
      canvas = testCanvas();
    }
    return canvas;
  }

  public static boolean supportsCanvasText() {
    if (canvasText == null) {
      canvasText = testCanvasText();
    }
    return canvasText;
  }

  public static boolean supportsContentEditable() {
    if (contentEditable == null) {
      contentEditable = testContentEditable();
    }
    return contentEditable;
  }

  public static boolean supportsDnd() {
    if (dnd == null) {
      dnd = testDnd();
    }
    return dnd;
  }

  public static boolean supportsDndEvents() {
    if (dndEvents == null) {
      dndEvents = testDndEvents();
    }
    return dndEvents;
  }

  public static boolean supportsElementCommand() {
    if (elementCommand == null) {
      elementCommand = testElementCommand();
    }
    return elementCommand;
  }

  public static boolean supportsElementDataList() {
    if (elementDataList == null) {
      elementDataList = testElementDataList();
    }
    return elementDataList;
  }

  public static boolean supportsElementDetails() {
    if (elementDetails == null) {
      elementDetails = testElementDetails();
    }
    return elementDetails;
  }

  public static boolean supportsElementDevice() {
    if (elementDevice == null) {
      elementDevice = testElementDevice();
    }
    return elementDevice;
  }

  public static boolean supportsElementMeter() {
    if (elementMeter == null) {
      elementMeter = testElementMeter();
    }
    return elementMeter;
  }

  public static boolean supportsElementOutput() {
    if (elementOutput == null) {
      elementOutput = testElementOutput();
    }
    return elementOutput;
  }

  public static boolean supportsElementProgress() {
    if (elementProgress == null) {
      elementProgress = testElementProgress();
    }
    return elementProgress;
  }

  public static boolean supportsElementTime() {
    if (elementTime == null) {
      elementTime = testElementTime();
    }
    return elementTime;
  }

  public static boolean supportsFileApi() {
    if (fileApi == null) {
      fileApi = testFileApi();
    }
    return fileApi;
  }

  public static boolean supportsGeolocation() {
    if (geolocation == null) {
      geolocation = testGeolocation();
    }
    return geolocation;
  }

  public static boolean supportsIndexedDB() {
    if (indexedDB == null) {
      indexedDB = testIndexedDB();
    }
    return indexedDB;
  }

  public static boolean supportsInputColor() {
    if (inputColor == null) {
      inputColor = testInputColor();
    }
    return inputColor;
  }

  public static boolean supportsInputDate() {
    if (inputDate == null) {
      inputDate = testInputDate();
    }
    return inputDate;
  }

  public static boolean supportsInputDatetime() {
    if (inputDatetime == null) {
      inputDatetime = testInputDatetime();
    }
    return inputDatetime;
  }

  public static boolean supportsInputDatetimeLocal() {
    if (inputDatetimeLocal == null) {
      inputDatetimeLocal = testInputDatetimeLocal();
    }
    return inputDatetimeLocal;
  }

  public static boolean supportsInputEmail() {
    if (inputEmail == null) {
      inputEmail = testInputEmail();
    }
    return inputEmail;
  }

  public static boolean supportsInputMonth() {
    if (inputMonth == null) {
      inputMonth = testInputMonth();
    }
    return inputMonth;
  }

  public static boolean supportsInputNumber() {
    if (inputNumber == null) {
      inputNumber = testInputNumber();
    }
    return inputNumber;
  }

  public static boolean supportsInputRange() {
    if (inputRange == null) {
      inputRange = testInputRange();
    }
    return inputRange;
  }

  public static boolean supportsInputSearch() {
    if (inputSearch == null) {
      inputSearch = testInputSearch();
    }
    return inputSearch;
  }

  public static boolean supportsInputTel() {
    if (inputTel == null) {
      inputTel = testInputTel();
    }
    return inputTel;
  }

  public static boolean supportsInputTime() {
    if (inputTime == null) {
      inputTime = testInputTime();
    }
    return inputTime;
  }

  public static boolean supportsInputType(String type) {
    Assert.notEmpty(type);
    return testInputType(type);
  }

  public static boolean supportsInputUrl() {
    if (inputUrl == null) {
      inputUrl = testInputUrl();
    }
    return inputUrl;
  }

  public static boolean supportsInputWeek() {
    if (inputWeek == null) {
      inputWeek = testInputWeek();
    }
    return inputWeek;
  }

  public static boolean supportsLocalStorage() {
    if (localStorage == null) {
      localStorage = testLocalStorage();
    }
    return localStorage;
  }

  public static boolean supportsMicrodata() {
    if (microdata == null) {
      microdata = testMicrodata();
    }
    return microdata;
  }

  public static boolean supportsPostMessage() {
    if (postMessage == null) {
      postMessage = testPostMessage();
    }
    return postMessage;
  }

  public static boolean supportsSelectors() {
    if (selectors == null) {
      selectors = testSelectors();
    }
    return selectors;
  }

  public static boolean supportsSendAsFormData() {
    if (sendAsFormData == null) {
      sendAsFormData = testSendAsFormData();
    }
    return sendAsFormData;
  }

  public static boolean supportsServerSentEvents() {
    if (serverSentEvents == null) {
      serverSentEvents = testServerSentEvents();
    }
    return serverSentEvents;
  }

  public static boolean supportsSessionStorage() {
    if (sessionStorage == null) {
      sessionStorage = testSessionStorage();
    }
    return sessionStorage;
  }

  public static boolean supportsSmil() {
    if (smil == null) {
      smil = testSmil();
    }
    return smil;
  }

  public static boolean supportsSvg() {
    if (svg == null) {
      svg = testSvg();
    }
    return svg;
  }

  public static boolean supportsSvgClipPaths() {
    if (svgClipPaths == null) {
      svgClipPaths = testSvgClipPaths();
    }
    return svgClipPaths;
  }

  public static boolean supportsSvgInline() {
    if (svgInline == null) {
      svgInline = testSvgInline();
    }
    return svgInline;
  }

  public static boolean supportsSvgInTextHtml() {
    if (svgInTextHtml == null) {
      svgInTextHtml = testSvgInTextHtml();
    }
    return svgInTextHtml;
  }

  public static boolean supportsUndo() {
    if (undo == null) {
      undo = testUndo();
    }
    return undo;
  }

  public static boolean supportsVideo() {
    if (video == null) {
      video = testVideo();
    }
    return video;
  }

  public static boolean supportsVideoCaptions() {
    if (!supportsVideo()) {
      return false;
    }
    if (videoCaptions == null) {
      videoCaptions = testVideoCaptions();
    }
    return videoCaptions;
  }

  public static boolean supportsVideoPoster() {
    if (!supportsVideo()) {
      return false;
    }
    if (videoPoster == null) {
      videoPoster = testVideoPoster();
    }
    return videoPoster;
  }

  public static boolean supportsWebGl() {
    if (webGl == null) {
      webGl = testWebGl();
    }
    return webGl;
  }

  public static boolean supportsWebSockets() {
    if (webSockets == null) {
      webSockets = testWebSockets();
    }
    return webSockets;
  }

  public static boolean supportsWebWorkers() {
    if (webWorkers == null) {
      webWorkers = testWebWorkers();
    }
    return webWorkers;
  }

  public static boolean supportsXhrCrossDomain() {
    if (xhrCrossDomain == null) {
      xhrCrossDomain = testXhrCrossDomain();
    }
    return xhrCrossDomain;
  }

  public static boolean supportsXhrUploadProgress() {
    if (xhrUploadProgress == null) {
      xhrUploadProgress = testXhrUploadProgress();
    }
    return xhrUploadProgress;
  }

  private static boolean testApplicationCache() {
    return isWindowProperty("applicationCache");
  }

  private static boolean testAttributeAutocomplete() {
    return testInputAttribute("autocomplete");
  }

  private static boolean testAttributeAutofocus() {
    return testInputAttribute("autofocus");
  }

  private static boolean testAttributeList() {
    return testInputAttribute("list");
  }

  private static boolean testAttributeMax() {
    return testInputAttribute("max");
  }

  private static boolean testAttributeMin() {
    return testInputAttribute("min");
  }

  private static boolean testAttributeMultiple() {
    return testInputAttribute("multiple");
  }

  private static boolean testAttributePattern() {
    return testInputAttribute("pattern");
  }

  private static boolean testAttributePlaceholder() {
    return testInputAttribute("placeholder");
  }

  private static boolean testAttributeRequired() {
    return testInputAttribute("required");
  }

  private static boolean testAttributeStep() {
    return testInputAttribute("step");
  }

  private static boolean testAudio() {
    Element element = Document.get().createElement(DomUtils.TAG_AUDIO);
    boolean ok;

    if (element == null) {
      ok = false;
    } else {
      ok = JsUtils.isFunction(element, "canPlayType");
      element = null;
    }

    return ok;
  }

  private static String testAudioAac() {
    String z = testAudioType("audio/x-m4a;");
    if (BeeUtils.isEmpty(z) || BeeUtils.same(z, BeeConst.NO)) {
      z = testAudioType("audio/aac;");
    }
    if (BeeUtils.isEmpty(z) || BeeUtils.same(z, BeeConst.NO)) {
      z = testAudioType("audio/mp4; codecs=\"mp4a.40.2\"");
    }

    return z;
  }

  private static String testAudioMp3() {
    return testAudioType("audio/mpeg;");
  }

  private static native String testAudioType(String type) /*-{
    if (type == null || type == "") {
      return "";
    }

    var el = $doc.createElement('audio');
    var v = "";

    try {
      if (typeof el["canPlayType"] == "function") {
        v = el.canPlayType(type);
      }
    } catch (err) {
      v = null;
    }

    el = null;
    return v;
  }-*/;

  private static String testAudioVorbis() {
    return testAudioType("audio/ogg; codecs=\"vorbis\"");
  }

  private static String testAudioWav() {
    return testAudioType("audio/wav; codecs=\"1\"");
  }

  private static native boolean testCanvas() /*-{
    var elem = $doc.createElement('canvas');
    var ok = !!(elem.getContext && elem.getContext('2d'));
    elem = null;
    return ok;
  }-*/;

  private static native boolean testCanvasText() /*-{
    var elem = $doc.createElement('canvas');
    var ok = false;

    try {
      ok = !!(elem.getContext && typeof elem.getContext('2d').fillText == "function");
    } catch (err) {
      ok = false;
    }

    elem = null;
    return ok;
  }-*/;

  private static boolean testContentEditable() {
    return JsUtils.isIn("isContentEditable",
        DomUtils.createElement(DomUtils.TAG_SPAN));
  }

  private static boolean testDnd() {
    return JsUtils.isIn("draggable", DomUtils.createElement(DomUtils.TAG_SPAN));
  }

  private static boolean testDndEvents() {
    String tg = DomUtils.TAG_DIV;
    return isEventSupported(tg, "drag") && isEventSupported(tg, "dragstart")
        && isEventSupported(tg, "dragenter")
        && isEventSupported(tg, "dragover")
        && isEventSupported(tg, "dragleave") && isEventSupported(tg, "dragend")
        && isEventSupported(tg, "drop");
  }

  private static boolean testElementCommand() {
    return JsUtils.isIn("type", DomUtils.createElement("command"));
  }

  private static boolean testElementDataList() {
    return JsUtils.isIn("options", DomUtils.createElement("datalist"));
  }

  private static boolean testElementDetails() {
    return JsUtils.isIn("open", DomUtils.createElement("details"));
  }

  private static boolean testElementDevice() {
    return JsUtils.isIn("type", DomUtils.createElement("device"));
  }

  private static boolean testElementMeter() {
    return JsUtils.isIn("value", DomUtils.createElement(DomUtils.TAG_METER));
  }

  private static boolean testElementOutput() {
    return JsUtils.isIn("value", DomUtils.createElement("output"));
  }

  private static boolean testElementProgress() {
    return JsUtils.isIn("value", DomUtils.createElement(DomUtils.TAG_PROGRESS));
  }

  private static boolean testElementTime() {
    return JsUtils.isIn("valueAsDate", DomUtils.createElement("time"));
  }

  private static native boolean testFileApi() /*-{
    return typeof FileReader != 'undefined';
  }-*/;

  private static boolean testGeolocation() {
    return isNavigatorProperty("geolocation");
  }

  private static boolean testIndexedDB() {
    return isWindowProperty("indexedDB");
  }

  private static boolean testInputAttribute(String attr) {
    Element element = DomUtils.createElement(DomUtils.TAG_INPUT);
    return JsUtils.isIn(attr, element);
  }

  private static boolean testInputColor() {
    return testInputType("color");
  }

  private static boolean testInputDate() {
    return testInputType("date");
  }

  private static boolean testInputDatetime() {
    return testInputType("datetime");
  }

  private static boolean testInputDatetimeLocal() {
    return testInputType("datetime-local");
  }

  private static boolean testInputEmail() {
    return testInputType("email");
  }

  private static boolean testInputMonth() {
    return testInputType("month");
  }

  private static boolean testInputNumber() {
    return testInputType("number");
  }

  private static boolean testInputRange() {
    return testInputType("range");
  }

  private static boolean testInputSearch() {
    return testInputType("search");
  }

  private static boolean testInputTel() {
    return testInputType("tel");
  }

  private static boolean testInputTime() {
    return testInputType("time");
  }

  private static boolean testInputType(String type) {
    if (BeeUtils.isEmpty(type)) {
      return false;
    }
    Element element = DomUtils.createElement(DomUtils.TAG_INPUT);
    element.setAttribute(DomUtils.ATTRIBUTE_TYPE, type);

    return BeeUtils.same(element.getAttribute(DomUtils.ATTRIBUTE_TYPE), type);
  }

  private static boolean testInputUrl() {
    return testInputType("url");
  }

  private static boolean testInputWeek() {
    return testInputType("week");
  }

  private static boolean testLocalStorage() {
    return getWindowProperty("localStorage") != null;
  }

  private static boolean testMicrodata() {
    return isDocumentProperty("getItems");
  }

  private static boolean testPostMessage() {
    return isWindowProperty("postMessage");
  }

  private static boolean testSelectors() {
    return isDocumentFunction("querySelector") && isDocumentFunction("querySelectorAll");
  }

  private static boolean testSendAsFormData() {
    return isWindowProperty("FormData");
  }

  private static native boolean testServerSentEvents() /*-{
    return typeof EventSource !== 'undefined';
  }-*/;

  private static boolean testSessionStorage() {
    return getWindowProperty("sessionStorage") != null;
  }

  private static boolean testSmil() {
    Element element = DomUtils.createElementNs(nsSvg, "animate");
    if (element == null) {
      return false;
    }

    return BeeUtils.context("svg", JsUtils.transform(element));
  }

  private static boolean testSvg() {
    Element element = DomUtils.createElementNs(nsSvg, "svg");
    return JsUtils.isFunction(element, "createSVGRect");
  }

  private static boolean testSvgClipPaths() {
    Element element = DomUtils.createElementNs(nsSvg, "clipPath");
    if (element == null) {
      return false;
    }

    return BeeUtils.context("svg", JsUtils.transform(element));
  }

  private static boolean testSvgInline() {
    Element element = Document.get().createElement(DomUtils.TAG_DIV);
    element.setInnerHTML("<svg/>");
    Node node = element.getFirstChild();

    return BeeUtils.same(DomUtils.getNamespaceUri(node), nsSvg);
  }

  private static native boolean testSvgInTextHtml() /*-{
    var el = $doc.createElement('div');
    el.innerHTML = '<svg></svg>';
    return !!($wnd.SVGSVGElement && el.firstChild instanceof $wnd.SVGSVGElement);
  }-*/;

  private static native boolean testUndo() /*-{
    return typeof UndoManager !== 'undefined';
  }-*/;

  private static boolean testVideo() {
    Element element = DomUtils.createElement(DomUtils.TAG_VIDEO);
    return JsUtils.isFunction(element, "canPlayType");
  }

  private static boolean testVideoCaptions() {
    return JsUtils.isIn("src", DomUtils.createElement("track"));
  }

  private static String testVideoH264() {
    String h264 = "video/mp4; codecs=\"avc1.42E01E'";

    String z = testVideoType(h264 + "\"");
    if (BeeUtils.isEmpty(z) || BeeUtils.same(z, BeeConst.NO)) {
      z = testVideoType(h264 + ", mp4a.40.2\"");
    }

    return z;
  }

  private static boolean testVideoPoster() {
    return JsUtils.isIn("poster", DomUtils.createElement(DomUtils.TAG_VIDEO));
  }

  private static String testVideoTheora() {
    return testVideoType("video/ogg; codecs=\"theora\"");
  }

  private static native String testVideoType(String type) /*-{
    var elem = $doc.createElement('video');
    if (elem == null || elem == undefined) {
      return "";
    }

    if (!!elem.canPlayType) {
      return elem.canPlayType(type);
    } else {
      return "";
    }
  }-*/;

  private static String testVideoWebm() {
    return testVideoType("video/webm; codecs=\"vp8, vorbis\"");
  }

  private static native boolean testWebGl() /*-{
    var elem = $doc.createElement('canvas');
    if (elem == null || elem == undefined || !elem.getContext) {
      return false;
    }

    var ok = false;
    try {
      if (elem.getContext('webgl')) {
        ok = true;
      } else if (elem.getContext('experimental-webgl')) {
        ok = true;
      }
    } catch (err) {
      ok = false;
    }

    return ok;
  }-*/;

  private static boolean testWebSockets() {
    return isInWindow("WebSocket");
  }

  private static boolean testWebWorkers() {
    return isWindowProperty("Worker");
  }

  private static native boolean testXhrCrossDomain() /*-{
    if ($wnd.XMLHttpRequest) {
      return "withCredentials" in new $wnd.XMLHttpRequest;
    } else {
      return false;
    }
  }-*/;

  private static native boolean testXhrUploadProgress() /*-{
    if ($wnd.XMLHttpRequest) {
      return "upload" in new $wnd.XMLHttpRequest;
    } else {
      return false;
    }
  }-*/;

  private Features() {
  }
}
