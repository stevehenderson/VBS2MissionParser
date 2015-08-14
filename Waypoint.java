package vbs2missionparser;

import processing.xml.*;

/**
 * A Waypoint represents a control point along a route.  An Aircraft will fly from Waypoint
 * to Waypoint.  At each Waypoint, the aircraft can change speed.  A Waypoint can also be useed
 * as an anchor for a ResponseSet.
 *
 *
 * <br><br>
 * <ul>References</ul>
 * {@link <Aircraft> [Aircraft]}
 * {@link <Route> [Route]}
 * {@link <ResponseSet> [ResponseSet]}
 *
 */
public class Waypoint {

    /**
     * A unique id for the Aircraft
     */
    int id;
    String name;
    int prev;
    int next;
    /**
     * The x position of the waypoint in the airspace.
     */
    double posX;
    /**
     * The y position of the waypoint in the airspace.
     */
    double posY;
    /**
     * The z position (altitude) of the waypoint in the airspace.
     */
    double posZ;
    /**
     * The new speed the aircraft will assume when reaching the airspace.
     */
    double newSpeed;

    /**
     * Returns the id of the Waypoint
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the x position of the waypoint in the airspace.
     */
    public double getPosX() {
        return posX;
    }

    /**
     * Returns the x position of the waypoint in the airspace.
     */
    public double getPosY() {
        return posY;
    }

    /**
     * Returns the x position of the waypoint in the airspace.
     */
    public double getPosZ() {
        return posZ;
    }

    /**
     * Returns the speed setting the aircraftr wll assume when reaching the waypoint.
     */
    public double getNewSpeed() {
        return newSpeed;
    }

    /**
     * Sets the id of the Waypoint
     */
    public void setId(int i) {
        id = i;
    }

    /**
     * Sets the x position of the Waypoint
     */
    public void setPosX(double  i) {
        posX = i;
    }

    /**
     * Sets the y position of the Waypoint
     */
    public void setPosY(double  i) {
        posY = i;
    }

    /**
     * Sets the z position of the Waypoint
     */
    public void setPosZ(double  i) {
        posZ = i;
    }

    /**
     * Sets the speed the aircraft will adopt when reaching the Waypoint
     */
    public void setNewSpeed(double  i) {
        newSpeed = i;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int  getNext() {
        return next;
    }

    public void setNext(int  next) {
        this.next = next;
    }

    public int getPrev() {
        return prev;
    }

    public void setPrev(int  prev) {
        this.prev = prev;
    }

    /**
     * An internal helper class to parse string tags
     */
    private String parseXMLString(XMLElement f, String tag) {
        XMLElement e = f.getChild(tag);
        if (e != null) {
            return e.getContent();
        } else {
            System.out.println("WAYPOINT:Didn't find tag " + tag + "..driving on..");
            return "no_xml_parse";
        }
    }

    /**
     * An internal helper class to parse integer tags
     */
    private int parseXMLInteger(XMLElement f, String tag) {
        XMLElement e = f.getChild(tag);
        if (e != null) {
            int result = Integer.parseInt(e.getContent());
            return result;
        } else {
            System.out.println("WAYPOINT:Didn't find tag " + tag + "..driving on..");
            return -888;
        }
    }

     /**
     * An internal helper class to parse integer tags
     */
    private double parseXMLDouble(XMLElement f, String tag) {
        XMLElement e = f.getChild(tag);
        if (e != null) {
            double result = Double.parseDouble(e.getContent());
            return result;
        } else {
            System.out.println("WAYPOINT:Didn't find tag " + tag + "..driving on..");
            return -888.0;
        }
    }

    /**
     * Loads a waypoint from an XML Element. Returns 1 on success.
     */
    public int loadFromXMLElement(XMLElement xml) {

        int result = 1;

        id = parseXMLInteger(xml, "id");

        System.out.println("\t\tLoading Waypoint " + id);

        newSpeed = parseXMLInteger(xml, "new_speed");
        posX = parseXMLDouble(xml, "pos_x");
        posY = parseXMLDouble(xml, "pos_y");
        posZ = parseXMLDouble(xml, "pos_z");

        return result;

    }

    /**
     * Returns an XMl representation of the Waypoint
     */
    public String toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<waypoint>");
        sb.append("<id>" + id + "</id>");
        sb.append("<name>" + name + "</name>");
        sb.append("<pos_x>" + posX + "</pos_x>");
        sb.append("<pos_y>" + posY + "</pos_y>");
        sb.append("<pos_z>" + posZ + "</pos_z>");
        sb.append("<new_speed>" + newSpeed + "</new_speed>");

        sb.append("</waypoint>");
        return sb.toString();
    }

    /**
     * Creates a new waypoint, at 0,0,0, with zero speed and no ResponseSet
     */
    public Waypoint() {
        id=-1;
        name="unset";
        posX = 0;
        posY = 0;
        posZ = 0;
        newSpeed = 0;

    }
}
