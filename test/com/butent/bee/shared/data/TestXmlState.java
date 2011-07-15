package com.butent.bee.shared.data;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.SystemBean.SysObject;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.utils.BeeUtils;

import org.junit.Test;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.SchemaFactory;

public class TestXmlState {
  @Test
  public void testRead() {
    String resource = Config.getPath("states/Visible.state", true);
    String schemaSource = Config.getSchemaPath(SysObject.STATE.getSchema());

    if (!BeeUtils.isEmpty(resource)) {
      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

      try {
        XmlState state = XmlUtils.unmarshal(XmlState.class, resource, schemaSource);

        Marshaller marshaller = JAXBContext.newInstance(XmlState.class).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setSchema(schemaFactory.newSchema(new File(schemaSource)));

        marshaller.marshal(state, System.out);

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
