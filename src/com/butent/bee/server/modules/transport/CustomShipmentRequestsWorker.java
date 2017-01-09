package com.butent.bee.server.modules.transport;

import static com.butent.bee.shared.html.builder.Factory.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.shared.html.builder.Document;
import com.butent.bee.shared.html.builder.FertileElement;
import com.butent.bee.shared.html.builder.elements.*;
import com.butent.bee.shared.i18n.Localized;

import java.util.Arrays;

public class CustomShipmentRequestsWorker {

  public static Div renderButtonsDiv() {
    Div div = div().addClass("bee-tr-contract-main-buttons-div");

    for (ShipmentRequestStatus s : Arrays.asList(ShipmentRequestStatus.APPROVED,
        ShipmentRequestStatus.REJECTED)) {
      Form form = form()
          .methodGet()
          .append(input().type(Input.Type.HIDDEN).name("choice").value(s.name()));

      switch (s) {
        case APPROVED:
          form.append(input().type(Input.Type.SUBMIT)
              .value(Localized.dictionary().trApproveContract().toUpperCase())
              .addClass("bee-tr-contract-approved"));
          break;

        case REJECTED:
          form.append(input().type(Input.Type.SUBMIT)
              .value(Localized.dictionary().trRejectContract().toUpperCase())
              .addClass("bee-tr-contract-rejected"));
          break;

        default:
          break;
      }
      div.appendChild(form);
    }

    return div;
  }

  public static Div renderStatusDiv(String text) {
    return div().text(text.toUpperCase()).addClass("bee-tr-contract-main-div");
  }

  public static String buildDocument(FertileElement el) {
    Div mainDiv = new Div();

    Div header = div().addClass("bee-tr-contract-header");
    header.append(img().addClass("bee-tr-contract-header-img")
        .src("http://order.hoptrans.eu/Hoptrans%20logo.png"));
    mainDiv.appendChild(header);

    Div targetDiv = div().addClass("bee-tr-contract-main");
    Div container = div().addClass("bee-tr-contract-main-container")
        .append(img().src("http://order.hoptrans.eu/Schema.svg")
            .addClass("bee-tr-contract-main-img"));
    container.append(el);
    targetDiv.append(container);

    mainDiv.appendChild(targetDiv);
    mainDiv.appendChild(div().addClass("bee-tr-contract-footer").append(div()
        .addClass("bee-tr-contract-footer-line")));

    Document doc = new Document();
    doc.getHead().append(meta().encodingDeclarationUtf8(), style()
        .text(".bee-tr-contract-main {height: 68vh;}")
        .text(".bee-tr-contract-header {height: 10vh; background-color: #03260f;}")
        .text(".bee-tr-contract-header-img {width: 300px; position: absolute; left: 25%; top:3vh;}")
        .text(".bee-tr-contract-main-img {width: 300px;}")
        .text(".bee-tr-contract-main-div {position: absolute; left: 25em; top: 15vh; width: 320px; "
            + "background-color: #21d126;padding-top: 10px; padding-bottom: 10px; font-size: 15px;"
            + "color: white;border-radius: 5px; border-style: none; text-align: center;}")
        .text(".bee-tr-contract-main-buttons-div {position: absolute; left: 25em; top: 15vh;}")
        .text(".bee-tr-contract-main-container {width: 50em; position: absolute; top: 20vh; "
            + "left: 25%; border-top: 1px solid #21d126; padding-top: 50px;}")
        .text(".bee-tr-contract-footer { height: 20vh; background-color: #03260f;}")
        .text(".bee-tr-contract-footer-line {background-color: #21d126; height: 1px; "
            + "position: relative; top: 18vh;}")
        .text(".bee-tr-contract-approved {width: 320px;background-color: #21d126;padding-top: "
            + "10px;padding-bottom: 10px;font-size: 15px;color: white;border-radius: 5px;cursor: "
            + "pointer;border-style: none; margin-bottom: 20px;}")
        .text(".bee-tr-contract-rejected {width: 320px;background-color: #005c4f;padding-top: "
            + "10px;padding-bottom: 10px;font-size: 15px;color: white;border-radius: 5px;cursor: "
            + "pointer;border-style: none;}"));
    doc.getBody().append(mainDiv);

    return doc.buildLines();
  }

}
