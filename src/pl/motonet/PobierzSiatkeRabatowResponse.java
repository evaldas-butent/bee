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
 *         &lt;element name="PobierzSiatkeRabatowResult" type="{http://moto-profil.pl/}ArrayOfString" minOccurs="0"/>
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
    "pobierzSiatkeRabatowResult"
})
@XmlRootElement(name = "PobierzSiatkeRabatowResponse")
public class PobierzSiatkeRabatowResponse {

  @XmlElement(name = "PobierzSiatkeRabatowResult")
  protected ArrayOfString pobierzSiatkeRabatowResult;

  /**
   * Gets the value of the pobierzSiatkeRabatowResult property.
   * 
   * @return possible object is {@link ArrayOfString }
   * 
   */
  public ArrayOfString getPobierzSiatkeRabatowResult() {
    return pobierzSiatkeRabatowResult;
  }

  /**
   * Sets the value of the pobierzSiatkeRabatowResult property.
   * 
   * @param value allowed object is {@link ArrayOfString }
   * 
   */
  public void setPobierzSiatkeRabatowResult(ArrayOfString value) {
    this.pobierzSiatkeRabatowResult = value;
  }

}
