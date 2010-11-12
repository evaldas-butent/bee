package com.butent.bee.egg.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;

public interface Images extends ClientBundle {
  @Source("ascending.gif")
  ImageResource ascending();

  @Source("bee.png")
  @ImageOptions(width = 60)
  ImageResource bee();
  
  @Source("close.png")
  ImageResource close();

  @Source("descending.gif")
  ImageResource descending();
}
