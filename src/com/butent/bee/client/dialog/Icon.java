package com.butent.bee.client.dialog;

import com.google.gwt.resources.client.ImageResource;

import com.butent.bee.client.Global;
import com.butent.bee.shared.font.FontAwesome;

public enum Icon {
  INFORMATION {
    @Override
    public ImageResource getImageResource() {
      return Global.getImages().information();
    }

    @Override
    public FontAwesome getIcon() {
      return FontAwesome.INFO;
    }
  },
  QUESTION {
    @Override
    public ImageResource getImageResource() {
      return Global.getImages().question();
    }

    @Override
    public FontAwesome getIcon() {
      return FontAwesome.QUESTION;
    }
  },
  WARNING {
    @Override
    public ImageResource getImageResource() {
      return Global.getImages().warning();
    }

    @Override
    public FontAwesome getIcon() {
      return FontAwesome.EXCLAMATION;
    }
  },
  ALARM {
    @Override
    public ImageResource getImageResource() {
      return Global.getImages().alarm();
    }

    @Override
    public FontAwesome getIcon() {
      return FontAwesome.EXCLAMATION_CIRCLE;
    }
  },
  ERROR {
    @Override
    public ImageResource getImageResource() {
      return Global.getImages().error();
    }

    @Override
    public FontAwesome getIcon() {
      return FontAwesome.TIMES_CIRCLE;
    }
  };

  public abstract ImageResource getImageResource();

  public abstract FontAwesome getIcon();
}
