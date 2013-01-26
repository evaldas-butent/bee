package com.butent.bee.client.images;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

import com.butent.bee.shared.Assert;

import java.util.Map;

import elemental.client.Browser;
import elemental.html.ImageElement;

/**
 * Handles a list of images used in the system.
 */

public class Images {

  public interface Resources extends ClientBundle {

    @Source("accept.png")
    ImageResource accept();

    @Source("add.png")
    ImageResource add();

    @Source("alarm.png")
    ImageResource alarm();
    
    @Source("arrow_down.png")
    ImageResource arrowDown();

    @Source("arrow_in.png")
    ImageResource arrowIn();

    @Source("arrow_left.png")
    ImageResource arrowLeft();

    @Source("arrow_out.png")
    ImageResource arrowOut();

    @Source("arrow_right.png")
    ImageResource arrowRight();

    @Source("arrow_up.png")
    ImageResource arrowUp();

    @Source("ascending.gif")
    ImageResource ascending();

    @Source("attachment.png")
    ImageResource attachment();

    @Source("bookmark.png")
    ImageResource bookmark();

    @Source("bookmark_add.png")
    ImageResource bookmarkAdd();

    @Source("calendar.png")
    ImageResource calendar();

    @Source("cancel.png")
    ImageResource cancel();

    @Source("close.png")
    ImageResource close();

    @Source("closeSmall.png")
    ImageResource closeSmall();

    @Source("configure.png")
    ImageResource configure();

    @Source("delete.png")
    ImageResource delete();

    @Source("descending.gif")
    ImageResource descending();

    @Source("disclosureClosed.png")
    ImageResource disclosureClosed();

    @Source("disclosureOpen.png")
    ImageResource disclosureOpen();

    @Source("disconnect.png")
    ImageResource disconnect();

    @Source("edit.png")
    ImageResource edit();

    @Source("edit_add.png")
    ImageResource editAdd();

    @Source("edit_delete.png")
    ImageResource editDelete();

    @Source("error.png")
    ImageResource error();

    @Source("exit.gif")
    ImageResource exit();

    @Source("filter_delete.png")
    ImageResource filterDelete();

    @Source("first.png")
    ImageResource first();

    @Source("forward.png")
    ImageResource forward();

    @Source("forward_to.png")
    ImageResource forwardTo();

    @Source("green.gif")
    ImageResource green();

    @Source("greenSmall.gif")
    ImageResource greenSmall();

    @Source("html.png")
    ImageResource html();

    @Source("last.png")
    ImageResource last();

    @Source("loading.gif")
    ImageResource loading();

    @Source("logo1.gif")
    ImageResource logo1();

    @Source("logo2.gif")
    ImageResource logo2();

    @Source("next.png")
    ImageResource next();

    @Source("noes.png")
    ImageResource noes();

    @Source("ok.png")
    ImageResource ok();

    @Source("plane.png")
    ImageResource plane();

    @Source("play.png")
    ImageResource play();

    @Source("previous.png")
    ImageResource previous();

    @Source("question.png")
    ImageResource question();

    @Source("red.gif")
    ImageResource red();

    @Source("redo.png")
    ImageResource redo();

    @Source("redSmall.gif")
    ImageResource redSmall();

    @Source("refresh.png")
    ImageResource refresh();

    @Source("reload.png")
    ImageResource reload();

    @Source("reply_to.png")
    ImageResource replyTo();

    @Source("reply_to_all.png")
    ImageResource replyToAll();

    @Source("report.png")
    ImageResource report();

    @Source("rewind.png")
    ImageResource rewind();

    @Source("save.png")
    ImageResource save();

    @Source("search.png")
    ImageResource search();

    @Source("searchOptions.gif")
    ImageResource searchOptions();

    @Source("settings.png")
    ImageResource settings();

    @Source("silver/edit_add_small.png")
    ImageResource silverAdd();

    @Source("silver/bookmark_add_small.png")
    ImageResource silverBookmarkAdd();

    @Source("silver/close_small.png")
    ImageResource silverClose();

    @Source("silver/configure_small.png")
    ImageResource silverConfigure();

    @Source("silver/edit_delete_small.png")
    ImageResource silverDelete();

    @Source("silver/edit_small.png")
    ImageResource silverEdit();

    @Source("silver/print_small.png")
    ImageResource silverPrint();

    @Source("silver/reload_small.png")
    ImageResource silverReload();

    @Source("silver/save_small.png")
    ImageResource silverSave();

    @Source("slider.gif")
    ImageResource slider();

    @Source("sliderDisabled.gif")
    ImageResource sliderDisabled();

    @Source("sliderSliding.gif")
    ImageResource sliderSliding();

    @Source("undo.png")
    ImageResource undo();

    @Source("warning.png")
    ImageResource warning();
    
    @Source("yellow.gif")
    ImageResource yellow();

    @Source("yellowSmall.gif")
    ImageResource yellowSmall();
  }

  private static final Map<String, ImageResource> map = Maps.newHashMap();

  private static final ImageElement imageElement = Browser.getDocument().createImageElement();

  public static String asString(ImageResource imageResource) {
    if (imageResource == null) {
      return null;
    }

    imageElement.setSrc(imageResource.getSafeUri().asString());
    return imageElement.getOuterHTML();
  }

  public static Resources createResources() {
    return GWT.create(Resources.class);
  }

  public static ImageResource get(String name) {
    Assert.notEmpty(name);
    return map.get(key(name));
  }

  public static String getHtml(String name) {
    return asString(get(name));
  }

  public static Map<String, ImageResource> getMap() {
    return map;
  }

  public static void init(Resources resources) {
    Assert.notNull(resources);
    if (!map.isEmpty()) {
      map.clear();
    }

    map.put(key("accept"), resources.accept());

    map.put(key("add"), resources.add());

    map.put(key("alarm"), resources.alarm());

    map.put(key("arrowDown"), resources.arrowDown());
    map.put(key("arrowIn"), resources.arrowIn());
    map.put(key("arrowLeft"), resources.arrowLeft());
    map.put(key("arrowOut"), resources.arrowOut());
    map.put(key("arrowRight"), resources.arrowRight());
    map.put(key("arrowUp"), resources.arrowUp());

    map.put(key("ascending"), resources.ascending());

    map.put(key("attachment"), resources.attachment());

    map.put(key("bookmark"), resources.bookmark());
    map.put(key("bookmarkAdd"), resources.bookmarkAdd());

    map.put(key("calendar"), resources.calendar());

    map.put(key("cancel"), resources.cancel());
    map.put(key("close"), resources.close());
    map.put(key("closeSmall"), resources.closeSmall());

    map.put(key("configure"), resources.configure());

    map.put(key("delete"), resources.delete());

    map.put(key("descending"), resources.descending());

    map.put(key("disclosureClosed"), resources.disclosureClosed());
    map.put(key("disclosureOpen"), resources.disclosureOpen());

    map.put(key("disconnect"), resources.disconnect());

    map.put(key("edit"), resources.edit());

    map.put(key("editAdd"), resources.editAdd());
    map.put(key("editDelete"), resources.editDelete());

    map.put(key("error"), resources.error());

    map.put(key("filterDelete"), resources.filterDelete());

    map.put(key("first"), resources.first());

    map.put(key("forward"), resources.forward());

    map.put(key("forwardTo"), resources.forwardTo());

    map.put(key("green"), resources.green());
    map.put(key("greenSmall"), resources.greenSmall());

    map.put(key("html"), resources.html());

    map.put(key("last"), resources.last());

    map.put(key("loading"), resources.loading());

    map.put(key("next"), resources.next());

    map.put(key("noes"), resources.noes());

    map.put(key("ok"), resources.ok());

    map.put(key("plane"), resources.plane());

    map.put(key("play"), resources.play());

    map.put(key("previous"), resources.previous());

    map.put(key("question"), resources.question());

    map.put(key("red"), resources.red());
    map.put(key("redSmall"), resources.redSmall());

    map.put(key("redo"), resources.redo());

    map.put(key("refresh"), resources.refresh());
    map.put(key("reload"), resources.reload());

    map.put(key("replyTo"), resources.replyTo());
    map.put(key("replyToAll"), resources.replyToAll());

    map.put(key("report"), resources.report());

    map.put(key("rewind"), resources.rewind());

    map.put(key("save"), resources.save());

    map.put(key("search"), resources.search());

    map.put(key("settings"), resources.settings());

    map.put(key("slider"), resources.slider());
    map.put(key("sliderDisabled"), resources.sliderDisabled());
    map.put(key("sliderSliding"), resources.sliderSliding());

    map.put(key("undo"), resources.undo());

    map.put(key("warning"), resources.warning());

    map.put(key("yellow"), resources.yellow());
    map.put(key("yellowSmall"), resources.yellowSmall());

    map.put(key("silverAdd"), resources.silverAdd());
    map.put(key("silverBookmarkAdd"), resources.silverBookmarkAdd());
    map.put(key("silverClose"), resources.silverClose());
    map.put(key("silverConfigure"), resources.silverConfigure());
    map.put(key("silverDelete"), resources.silverDelete());
    map.put(key("silverEdit"), resources.silverEdit());
    map.put(key("silverPrint"), resources.silverPrint());
    map.put(key("silverReload"), resources.silverReload());
    map.put(key("silverSave"), resources.silverSave());
  }

  private static String key(String name) {
    return name.trim().toLowerCase();
  }

  private Images() {
  }
}
