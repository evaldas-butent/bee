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
 *         &lt;element name="ZalogujPobranieOfertyResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "zalogujPobranieOfertyResult"
})
@XmlRootElement(name = "ZalogujPobranieOfertyResponse")
public class ZalogujPobranieOfertyResponse {

  @XmlElement(name = "ZalogujPobranieOfertyResult")
  protected String zalogujPobranieOfertyResult;

  /**
   * Gets the value of the zalogujPobranieOfertyResult property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getZalogujPobranieOfertyResult() {
    return zalogujPobranieOfertyResult;
  }

  /**
   * Sets the value of the zalogujPobranieOfertyResult property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setZalogujPobranieOfertyResult(String value) {
    this.zalogujPobranieOfertyResult = value;
  }

}
