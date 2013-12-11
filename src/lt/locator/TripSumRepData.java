
package lt.locator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tripSumRepData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tripSumRepData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="CANDataAvailable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="canAverageFuelUsage" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="canDistanceInKm" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="canFuelUsedInLiters" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="canOdometerValueEndInKm" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="canOdometerValueStartInKm" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="counterFuelUsedInLiters" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="cunterFuelDataAvailable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="driveTimeInHours" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="gpsAverageFuelUsage" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="gpsDistanceInKm" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="gpsFuelUsageAvailable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stopTimeInHours" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tripSumRepData", propOrder = {
    "canDataAvailable",
    "canAverageFuelUsage",
    "canDistanceInKm",
    "canFuelUsedInLiters",
    "canOdometerValueEndInKm",
    "canOdometerValueStartInKm",
    "counterFuelUsedInLiters",
    "cunterFuelDataAvailable",
    "driveTimeInHours",
    "gpsAverageFuelUsage",
    "gpsDistanceInKm",
    "gpsFuelUsageAvailable",
    "name",
    "stopTimeInHours"
})
public class TripSumRepData {

    @XmlElement(name = "CANDataAvailable")
    protected boolean canDataAvailable;
    protected double canAverageFuelUsage;
    protected double canDistanceInKm;
    protected double canFuelUsedInLiters;
    protected double canOdometerValueEndInKm;
    protected double canOdometerValueStartInKm;
    protected double counterFuelUsedInLiters;
    protected boolean cunterFuelDataAvailable;
    protected double driveTimeInHours;
    protected double gpsAverageFuelUsage;
    protected double gpsDistanceInKm;
    protected boolean gpsFuelUsageAvailable;
    protected String name;
    protected double stopTimeInHours;

    /**
     * Gets the value of the canDataAvailable property.
     * 
     */
    public boolean isCANDataAvailable() {
        return canDataAvailable;
    }

    /**
     * Sets the value of the canDataAvailable property.
     * 
     */
    public void setCANDataAvailable(boolean value) {
        this.canDataAvailable = value;
    }

    /**
     * Gets the value of the canAverageFuelUsage property.
     * 
     */
    public double getCanAverageFuelUsage() {
        return canAverageFuelUsage;
    }

    /**
     * Sets the value of the canAverageFuelUsage property.
     * 
     */
    public void setCanAverageFuelUsage(double value) {
        this.canAverageFuelUsage = value;
    }

    /**
     * Gets the value of the canDistanceInKm property.
     * 
     */
    public double getCanDistanceInKm() {
        return canDistanceInKm;
    }

    /**
     * Sets the value of the canDistanceInKm property.
     * 
     */
    public void setCanDistanceInKm(double value) {
        this.canDistanceInKm = value;
    }

    /**
     * Gets the value of the canFuelUsedInLiters property.
     * 
     */
    public double getCanFuelUsedInLiters() {
        return canFuelUsedInLiters;
    }

    /**
     * Sets the value of the canFuelUsedInLiters property.
     * 
     */
    public void setCanFuelUsedInLiters(double value) {
        this.canFuelUsedInLiters = value;
    }

    /**
     * Gets the value of the canOdometerValueEndInKm property.
     * 
     */
    public double getCanOdometerValueEndInKm() {
        return canOdometerValueEndInKm;
    }

    /**
     * Sets the value of the canOdometerValueEndInKm property.
     * 
     */
    public void setCanOdometerValueEndInKm(double value) {
        this.canOdometerValueEndInKm = value;
    }

    /**
     * Gets the value of the canOdometerValueStartInKm property.
     * 
     */
    public double getCanOdometerValueStartInKm() {
        return canOdometerValueStartInKm;
    }

    /**
     * Sets the value of the canOdometerValueStartInKm property.
     * 
     */
    public void setCanOdometerValueStartInKm(double value) {
        this.canOdometerValueStartInKm = value;
    }

    /**
     * Gets the value of the counterFuelUsedInLiters property.
     * 
     */
    public double getCounterFuelUsedInLiters() {
        return counterFuelUsedInLiters;
    }

    /**
     * Sets the value of the counterFuelUsedInLiters property.
     * 
     */
    public void setCounterFuelUsedInLiters(double value) {
        this.counterFuelUsedInLiters = value;
    }

    /**
     * Gets the value of the cunterFuelDataAvailable property.
     * 
     */
    public boolean isCunterFuelDataAvailable() {
        return cunterFuelDataAvailable;
    }

    /**
     * Sets the value of the cunterFuelDataAvailable property.
     * 
     */
    public void setCunterFuelDataAvailable(boolean value) {
        this.cunterFuelDataAvailable = value;
    }

    /**
     * Gets the value of the driveTimeInHours property.
     * 
     */
    public double getDriveTimeInHours() {
        return driveTimeInHours;
    }

    /**
     * Sets the value of the driveTimeInHours property.
     * 
     */
    public void setDriveTimeInHours(double value) {
        this.driveTimeInHours = value;
    }

    /**
     * Gets the value of the gpsAverageFuelUsage property.
     * 
     */
    public double getGpsAverageFuelUsage() {
        return gpsAverageFuelUsage;
    }

    /**
     * Sets the value of the gpsAverageFuelUsage property.
     * 
     */
    public void setGpsAverageFuelUsage(double value) {
        this.gpsAverageFuelUsage = value;
    }

    /**
     * Gets the value of the gpsDistanceInKm property.
     * 
     */
    public double getGpsDistanceInKm() {
        return gpsDistanceInKm;
    }

    /**
     * Sets the value of the gpsDistanceInKm property.
     * 
     */
    public void setGpsDistanceInKm(double value) {
        this.gpsDistanceInKm = value;
    }

    /**
     * Gets the value of the gpsFuelUsageAvailable property.
     * 
     */
    public boolean isGpsFuelUsageAvailable() {
        return gpsFuelUsageAvailable;
    }

    /**
     * Sets the value of the gpsFuelUsageAvailable property.
     * 
     */
    public void setGpsFuelUsageAvailable(boolean value) {
        this.gpsFuelUsageAvailable = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the stopTimeInHours property.
     * 
     */
    public double getStopTimeInHours() {
        return stopTimeInHours;
    }

    /**
     * Sets the value of the stopTimeInHours property.
     * 
     */
    public void setStopTimeInHours(double value) {
        this.stopTimeInHours = value;
    }

}
