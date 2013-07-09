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
 *         &lt;element name="ZwrocGrupyRabatoweResult" type="{http://moto-profil.pl/}ArrayOfString" minOccurs="0"/>
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
    "zwrocGrupyRabatoweResult"
})
@XmlRootElement(name = "ZwrocGrupyRabatoweResponse")
public class ZwrocGrupyRabatoweResponse {

  @XmlElement(name = "ZwrocGrupyRabatoweResult")
  protected ArrayOfString zwrocGrupyRabatoweResult;

  /**
   * Gets the value of the zwrocGrupyRabatoweResult property.
   * 
   * @return possible object is {@link ArrayOfString }
   * 
   */
  public ArrayOfString getZwrocGrupyRabatoweResult() {
    return zwrocGrupyRabatoweResult;
  }

  /**
   * Sets the value of the zwrocGrupyRabatoweResult property.
   * 
   * @param value allowed object is {@link ArrayOfString }
   * 
   */
  public void setZwrocGrupyRabatoweResult(ArrayOfString value) {
    this.zwrocGrupyRabatoweResult = value;
  }

}
