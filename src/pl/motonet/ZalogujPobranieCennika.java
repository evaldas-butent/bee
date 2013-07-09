package pl.motonet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="fiks" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="motonet" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="wersja" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="plik" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "fiks",
    "motonet",
    "wersja",
    "plik"
})
@XmlRootElement(name = "ZalogujPobranieCennika")
public class ZalogujPobranieCennika {

  protected String fiks;
  protected String motonet;
  protected String wersja;
  protected String plik;

  /**
   * Gets the value of the fiks property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getFiks() {
    return fiks;
  }

  /**
   * Gets the value of the motonet property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getMotonet() {
    return motonet;
  }

  /**
   * Gets the value of the plik property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getPlik() {
    return plik;
  }

  /**
   * Gets the value of the wersja property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getWersja() {
    return wersja;
  }

  /**
   * Sets the value of the fiks property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setFiks(String value) {
    this.fiks = value;
  }

  /**
   * Sets the value of the motonet property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setMotonet(String value) {
    this.motonet = value;
  }

  /**
   * Sets the value of the plik property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setPlik(String value) {
    this.plik = value;
  }

  /**
   * Sets the value of the wersja property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setWersja(String value) {
    this.wersja = value;
  }

}
