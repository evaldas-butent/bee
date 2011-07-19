package com.butent.bee.shared.data;

import com.butent.bee.server.Config;
import com.butent.bee.server.ui.XmlSqlDesigner;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.utils.BeeUtils;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class TestXmlSqlDesigner {
  @Test
  public void testRead() {
    String resource = Config.getPath("SqlDesigner.xml", true);

    if (!BeeUtils.isEmpty(resource)) {
      try {
        XmlSqlDesigner view = XmlUtils.unmarshal(XmlSqlDesigner.class, resource, null);

        Marshaller marshaller = JAXBContext.newInstance(XmlSqlDesigner.class).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        marshaller.marshal(view, System.out);

      } catch (JAXBException e) {
        String err;

        if (!BeeUtils.isEmpty(e.getLinkedException())) {
          err = e.getLinkedException().getMessage();
        } else {
          err = e.getMessage();
        }
        System.out.println("JAXBException: " + err);
      } catch (Exception e) {
        System.out.println("Exception: " + e.getMessage());
      }
    }
  }
}
