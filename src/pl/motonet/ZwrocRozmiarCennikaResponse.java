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
 *         &lt;element name="ZwrocRozmiarCennikaResult" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="liczba_paczek" type="{http://www.w3.org/2001/XMLSchema}int"/>
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
    "zwrocRozmiarCennikaResult",
    "liczbaPaczek"
})
@XmlRootElement(name = "ZwrocRozmiarCennikaResponse")
public class ZwrocRozmiarCennikaResponse {

  @XmlElement(name = "ZwrocRozmiarCennikaResult")
  protected int zwrocRozmiarCennikaResult;
  @XmlElement(name = "liczba_paczek")
  protected int liczbaPaczek;

  /**
   * Gets the value of the liczbaPaczek property.
   * 
   */
  public int getLiczbaPaczek() {
    return liczbaPaczek;
  }

  /**
   * Gets the value of the zwrocRozmiarCennikaResult property.
   * 
   */
  public int getZwrocRozmiarCennikaResult() {
    return zwrocRozmiarCennikaResult;
  }

  /**
   * Sets the value of the liczbaPaczek property.
   * 
   */
  public void setLiczbaPaczek(int value) {
    this.liczbaPaczek = value;
  }

  /**
   * Sets the value of the zwrocRozmiarCennikaResult property.
   * 
   */
  public void setZwrocRozmiarCennikaResult(int value) {
    this.zwrocRozmiarCennikaResult = value;
  }

}
