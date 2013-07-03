package com.butent.bee.client.dialog;

import com.google.gwt.resources.client.ImageResource;

import com.butent.bee.client.Global;

public enum Icon {
  INFORMATION {
    @Override
    public ImageResource getImageResource() {
      return Global.getImages().information();
    }
  },
  QUESTION {
    @Override
    public ImageResource getImageResource() {
      return Global.getImages().question();
    }
  },
  WARNING {
    @Override
    public ImageResource getImageResource() {
      return Global.getImages().warning();
    }
  },
  ALARM {
    @Override
    public ImageResource getImageResource() {
      return Global.getImages().alarm();
    }
  },
  ERROR {
    @Override
    public ImageResource getImageResource() {
      return Global.getImages().error();
    }
  };

  public abstract ImageResource getImageResource();
}
