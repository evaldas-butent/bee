package com.butent.bee.client.screen;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.canvas.CanvasDemo;
import com.butent.bee.client.cli.CliWorker;
import com.butent.bee.client.composite.VolumeSlider;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.LayoutPanel;
import com.butent.bee.client.logging.ClientLogManager;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.style.StyleUtils.FontSize;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.visualization.showcase.Showcase;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Handles tablet computer size screen implementation.
 */

public class Tablet extends Mobile {

  private class SvgCommand extends Command {
    private int type;

    private SvgCommand(int type) {
      super();
      this.type = type;
    }

    @Override
    public void execute() {
      String opt = BeeUtils.joinWords("t" + type,
        "c" + minCount.getValue() + "-" + maxCount.getValue(),
        "r" + minRadius.getValue() + "-" + maxRadius.getValue(),
        "s" + colorStep.getValue(),
        "o" + BeeUtils.toString(minOpacity.getLong() / 100.0) + "-"
            + BeeUtils.toString(maxOpacity.getLong() / 100.0));
      CliWorker.execute("svg " + opt);
    }
  }

  private VolumeSlider minCount;
  private VolumeSlider maxCount;
  private VolumeSlider minRadius;
  private VolumeSlider maxRadius;
  private VolumeSlider colorStep;
  private VolumeSlider minOpacity;
  private VolumeSlider maxOpacity;

  public Tablet() {
    super();
  }

  @Override
  protected int addLogToggle(LayoutPanel panel) {
    final Toggle toggle = new Toggle("Hide Log", "Show Log", "toggleLog");
    StyleUtils.setFontSize(toggle, FontSize.SMALL);
    StyleUtils.setHorizontalPadding(toggle, 2);

    toggle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ClientLogManager.setPanelVisible(toggle.isDown());
        toggle.invert();
      }
    });

    panel.addRightWidthTop(toggle, 3, 76, 1);
    toggle.setDown(true);
    return 80;
  }

  @Override
  protected int getWestWidth() {
    return 160;
  }

  @Override
  protected IdentifiableWidget initWest() {
    HtmlTable grid = new HtmlTable();
    int r = 0;
    int c = 2;
    int w = 70;
    int h = 20;

    grid.setHTML(r, 0, "Count:");
    r++;

    minCount = new VolumeSlider(10, 1, 1000);
    minCount.setPixelSize(w, h);
    grid.setWidget(r, 0, minCount);
    maxCount = new VolumeSlider(100, 1, 10000);
    maxCount.setPixelSize(w, h);
    grid.setWidget(r, 1, maxCount);
    r++;

    grid.setHTML(r, 0, "Radius:");
    r++;

    minRadius = new VolumeSlider(10, 1, 1000);
    minRadius.setPixelSize(w, h);
    grid.setWidget(r, 0, minRadius);
    maxRadius = new VolumeSlider(100, 1, 1000);
    maxRadius.setPixelSize(w, h);
    grid.setWidget(r, 1, maxRadius);
    r++;

    grid.setHTML(r, 0, "Color Step:");
    r++;

    colorStep = new VolumeSlider(16, 1, 256);
    colorStep.setPixelSize(w, h);
    grid.setWidget(r, 0, colorStep);
    r++;

    grid.setHTML(r, 0, "Opacity:");
    r++;

    minOpacity = new VolumeSlider(50, 0, 100);
    minOpacity.setPixelSize(w, h);
    grid.setWidget(r, 0, minOpacity);
    maxOpacity = new VolumeSlider(100, 0, 100);
    maxOpacity.setPixelSize(w, h);
    grid.setWidget(r, 1, maxOpacity);
    r++;

    BeeButton rect = new BeeButton("SVG Rectangles", new SvgCommand(0));
    grid.setWidget(r, 0, rect);
    grid.alignCenter(r, 0);
    grid.getCellFormatter().setColSpan(r, 0, c);
    r++;

    BeeButton circle = new BeeButton("SVG Circles", new SvgCommand(1));
    grid.setWidget(r, 0, circle);
    grid.alignCenter(r, 0);
    grid.getCellFormatter().setColSpan(r, 0, c);
    r++;

    BeeButton ellipse = new BeeButton("SVG Ellipses", new SvgCommand(2));
    grid.setWidget(r, 0, ellipse);
    grid.alignCenter(r, 0);
    grid.getCellFormatter().setColSpan(r, 0, c);
    r++;
    
    BeeButton canvas = new BeeButton("Canvas Demo", new Command() {
      @Override
      public void execute() {
        new CanvasDemo().start();
      }
    });
    grid.setWidget(r, 0, canvas);
    grid.alignCenter(r, 0);
    grid.getCellFormatter().setColSpan(r, 0, c);
    r++;

    BeeButton visual = new BeeButton("Visualization", new Command() {
      @Override
      public void execute() {
        Showcase.open();
      }
    });
    grid.setWidget(r, 0, visual);
    grid.alignCenter(r, 0);
    grid.getCellFormatter().setColSpan(r, 0, c);
    r++;

    BeeButton cornify = new BeeButton("Cornify", new Command() {
      @Override
      public void execute() {
        CliWorker.execute("cornify 5 1000");
      }
    });
    grid.setWidget(r, 0, cornify);
    grid.alignCenter(r, 0);
    grid.getCellFormatter().setColSpan(r, 0, c);
    r++;

    return grid;
  }
}
