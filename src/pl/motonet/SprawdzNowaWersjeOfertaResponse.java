package pl.motonet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SprawdzNowaWersjeOfertaResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sprawdzNowaWersjeOfertaResult"
})
@XmlRootElement(name = "SprawdzNowaWersjeOfertaResponse")
public class SprawdzNowaWersjeOfertaResponse {

  @XmlElement(name = "SprawdzNowaWersjeOfertaResult")
  protected String sprawdzNowaWersjeOfertaResult;

  /**
   * Gets the value of the sprawdzNowaWersjeOfertaResult property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getSprawdzNowaWersjeOfertaResult() {
    return sprawdzNowaWersjeOfertaResult;
  }

  /**
   * Sets the value of the sprawdzNowaWersjeOfertaResult property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setSprawdzNowaWersjeOfertaResult(String value) {
    this.sprawdzNowaWersjeOfertaResult = value;
  }

}
