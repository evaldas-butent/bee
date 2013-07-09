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
 *         &lt;element name="fiks" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="motonet" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numer" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="numer_paczki" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
    "numer",
    "numerPaczki"
})
@XmlRootElement(name = "ZwrocPaczkeOferty")
public class ZwrocPaczkeOferty {

  protected String fiks;
  protected String motonet;
  protected String numer;
  @XmlElement(name = "numer_paczki")
  protected int numerPaczki;

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
   * Gets the value of the numer property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getNumer() {
    return numer;
  }

  /**
   * Gets the value of the numerPaczki property.
   * 
   */
  public int getNumerPaczki() {
    return numerPaczki;
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
   * Sets the value of the numer property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setNumer(String value) {
    this.numer = value;
  }

  /**
   * Sets the value of the numerPaczki property.
   * 
   */
  public void setNumerPaczki(int value) {
    this.numerPaczki = value;
  }

}
