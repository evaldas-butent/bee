package com.butent.bee.client.resources;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

import com.butent.bee.shared.Assert;

import java.util.Map;

/**
 * Handles a list of images used in the system.
 */

public class Images {

  /**
   * Contains a list of images used in the system.
   */

  public interface Resources extends ClientBundle {

    @Source("accept.png")
    ImageResource accept();

    @Source("add.png")
    ImageResource add();

    @Source("arrowDown.png")
    ImageResource arrowDown();

    @Source("arrowDownDisabled.png")
    ImageResource arrowDownDisabled();

    @Source("arrowDownHover.png")
    ImageResource arrowDownHover();

    @Source("arrowDownPressed.png")
    ImageResource arrowDownPressed();

    @Source("arrowLeft.png")
    ImageResource arrowLeft();

    @Source("arrowLeftDisabled.png")
    ImageResource arrowLeftDisabled();

    @Source("arrowLeftHover.png")
    ImageResource arrowLeftHover();

    @Source("arrowLeftPressed.png")
    ImageResource arrowLeftPressed();

    @Source("arrowRight.png")
    ImageResource arrowRight();

    @Source("arrowRightDisabled.png")
    ImageResource arrowRightDisabled();

    @Source("arrowRightHover.png")
    ImageResource arrowRightHover();

    @Source("arrowRightPressed.png")
    ImageResource arrowRightPressed();

    @Source("arrowUp.png")
    ImageResource arrowUp();

    @Source("arrowUpDisabled.png")
    ImageResource arrowUpDisabled();

    @Source("arrowUpHover.png")
    ImageResource arrowUpHover();

    @Source("arrowUpPressed.png")
    ImageResource arrowUpPressed();

    @Source("ascending.gif")
    ImageResource ascending();

    @Source("bookmark.png")
    ImageResource bookmark();

    @Source("bookmark_add.png")
    ImageResource bookmarkAdd();

    @Source("butent.png")
    @ImageOptions(width = 61)
    ImageResource butent();

    @Source("cancel.png")
    ImageResource cancel();

    @Source("close.png")
    ImageResource close();

    @Source("configure.png")
    ImageResource configure();

    @Source("delete.png")
    ImageResource delete();

    @Source("descending.gif")
    ImageResource descending();

    @Source("edit.png")
    ImageResource edit();
    
    @Source("edit_add.png")
    ImageResource editAdd();

    @Source("edit_delete.png")
    ImageResource editDelete();
    
    @Source("exit.png")
    ImageResource exit();

    @Source("first.png")
    ImageResource first();

    @Source("forward.png")
    ImageResource forward();
    
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

    @Source("next.png")
    ImageResource next();

    @Source("noes.png")
    ImageResource noes();

    @Source("ok.png")
    ImageResource ok();

    @Source("play.png")
    ImageResource play();

    @Source("previous.png")
    ImageResource previous();

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

    @Source("rewind.png")
    ImageResource rewind();

    @Source("save.png")
    ImageResource save();

    @Source("search.png")
    ImageResource search();
    
    @Source("slider.gif")
    ImageResource slider();

    @Source("sliderDisabled.gif")
    ImageResource sliderDisabled();

    @Source("sliderSliding.gif")
    ImageResource sliderSliding();

    @Source("undo.png")
    ImageResource undo();

    @Source("yellow.gif")
    ImageResource yellow();

    @Source("yellowSmall.gif")
    ImageResource yellowSmall();
  }

  private static final Map<String, ImageResource> map = Maps.newHashMap();

  public static Resources createResources() {
    return GWT.create(Resources.class);
  }

  public static ImageResource get(String name) {
    Assert.notEmpty(name);
    return map.get(key(name));
  }

  public static void init(Resources resources) {
    Assert.notNull(resources);
    if (map.size() > 0) {
      map.clear();
    }

    map.put(key("accept"), resources.accept());

    map.put(key("add"), resources.add());

    map.put(key("arrowDown"), resources.arrowDown());
    map.put(key("arrowDownDisabled"), resources.arrowDownDisabled());
    map.put(key("arrowDownHover"), resources.arrowDownHover());
    map.put(key("arrowDownPressed"), resources.arrowDownPressed());

    map.put(key("arrowLeft"), resources.arrowLeft());
    map.put(key("arrowLeftDisabled"), resources.arrowLeftDisabled());
    map.put(key("arrowLeftHover"), resources.arrowLeftHover());
    map.put(key("arrowLeftPressed"), resources.arrowLeftPressed());

    map.put(key("arrowRight"), resources.arrowRight());
    map.put(key("arrowRightDisabled"), resources.arrowRightDisabled());
    map.put(key("arrowRightHover"), resources.arrowRightHover());
    map.put(key("arrowRightPressed"), resources.arrowRightPressed());

    map.put(key("arrowUp"), resources.arrowUp());
    map.put(key("arrowUpDisabled"), resources.arrowUpDisabled());
    map.put(key("arrowUpHover"), resources.arrowUpHover());
    map.put(key("arrowUpPressed"), resources.arrowUpPressed());

    map.put(key("ascending"), resources.ascending());

    map.put(key("bookmark"), resources.bookmark());
    map.put(key("bookmark_add"), resources.bookmarkAdd());

    map.put(key("butent"), resources.butent());

    map.put(key("cancel"), resources.cancel());
    map.put(key("close"), resources.close());

    map.put(key("configure"), resources.configure());

    map.put(key("delete"), resources.delete());

    map.put(key("descending"), resources.descending());

    map.put(key("edit"), resources.edit());

    map.put(key("edit_add"), resources.editAdd());
    map.put(key("edit_delete"), resources.editDelete());

    map.put(key("exit"), resources.exit());

    map.put(key("first"), resources.first());

    map.put(key("forward"), resources.forward());

    map.put(key("green"), resources.green());
    map.put(key("greenSmall"), resources.greenSmall());

    map.put(key("html"), resources.html());

    map.put(key("last"), resources.last());

    map.put(key("loading"), resources.loading());

    map.put(key("next"), resources.next());

    map.put(key("noes"), resources.noes());

    map.put(key("ok"), resources.ok());

    map.put(key("play"), resources.play());

    map.put(key("previous"), resources.previous());

    map.put(key("red"), resources.red());
    map.put(key("redSmall"), resources.redSmall());

    map.put(key("redo"), resources.redo());

    map.put(key("refresh"), resources.refresh());
    map.put(key("reload"), resources.reload());

    map.put(key("rewind"), resources.rewind());

    map.put(key("save"), resources.save());

    map.put(key("search"), resources.search());

    map.put(key("slider"), resources.slider());
    map.put(key("sliderDisabled"), resources.sliderDisabled());
    map.put(key("sliderSliding"), resources.sliderSliding());

    map.put(key("undo"), resources.undo());

    map.put(key("yellow"), resources.yellow());
    map.put(key("yellowSmall"), resources.yellowSmall());
  }

  private static String key(String name) {
    return name.trim().toLowerCase();
  }

  private Images() {
  }
}
