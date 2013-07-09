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
 *         &lt;element name="aktualna_wersja" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "aktualnaWersja"
})
@XmlRootElement(name = "PobierzNowaWersjeOferta")
public class PobierzNowaWersjeOferta {

  @XmlElement(name = "aktualna_wersja")
  protected String aktualnaWersja;

  /**
   * Gets the value of the aktualnaWersja property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getAktualnaWersja() {
    return aktualnaWersja;
  }

  /**
   * Sets the value of the aktualnaWersja property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setAktualnaWersja(String value) {
    this.aktualnaWersja = value;
  }

}
