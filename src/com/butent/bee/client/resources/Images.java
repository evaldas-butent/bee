package com.butent.bee.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

/**
 * Contains a list of images used in the system.
 */

public interface Images extends ClientBundle {
  
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

  @Source("bee.png")
  @ImageOptions(width = 60)
  ImageResource bee();

  @Source("bookmark.png")
  ImageResource bookmark();

  @Source("bookmark_add.png")
  ImageResource bookmarkAdd();
  
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

  @Source("edit_add.png")
  ImageResource editAdd();

  @Source("edit_delete.png")
  ImageResource editDelete();

  @Source("first.png")
  ImageResource first();

  @Source("forward.png")
  ImageResource forward();

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

  @Source("previous.png")
  ImageResource previous();

  @Source("refresh.png")
  ImageResource refresh();

  @Source("reload.png")
  ImageResource reload();

  @Source("rewind.png")
  ImageResource rewind();

  @Source("save.png")
  ImageResource save();

  @Source("slider.gif")
  ImageResource slider();

  @Source("sliderDisabled.gif")
  ImageResource sliderDisabled();

  @Source("sliderSliding.gif")
  ImageResource sliderSliding();
}
