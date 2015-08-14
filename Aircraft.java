/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vbs2missionparser;

import processing.xml.*;

/**
* An aircraft is any flying object of interest in the airspace
* of the simulation
*/
public class Aircraft {

        /**
        * Aircraft Collision with UAV(cadet request)
        */

        int aircraftCollision;

	/**
	* A unique id for the Aircraft
	*/
	int id;

        String name;

	/**
	* A route the aircraft flies through the airspace
	*/
	Route aircraftRoute;

	/**
	* The current speed of the aircraft.  Represents pixels per clock tick.
	*/
	double currentSpeed;

	/**
	* The current x coordinate of the aircraft
	*/
	double current_x;

	/**
	* The current y coordinate of the aircraft
	*/
	double current_y;

	/**
	* The current z coordinate of the aircraft
	*/
	double current_z;

	/**
	* The next waypoint the aircraft will encounter on the route
	*/
	Waypoint nextWaypoint;

	int recurseCount = 0;

	/**
	* Sets the route for the aircraft
	*/
	public void setRoute(Route r) {
		aircraftRoute = r;
	}

	/**
	* Returns the current x coordinate of the aircraft
	*/
	public double  getCurrent_x() {
		return current_x;
	}

	/**
	* Returns the current y coordinate of the aircraft
	*/
	public double getCurrent_y() {
		return current_y;
	}

	/**
	* Returns the current z coordinate of the aircraft
	*/
	public double getCurrent_z() {
		return current_z;
	}


	public Route getRoute() {
    return aircraftRoute;
  }
	/**
	* Sets the speed of the aircraft
	*/
	public void setCurrentSpeed(double s) {
		currentSpeed = s;
	}

	/**
	* Sets the current x coordinate of the aircraft
	*/
	public void setCurrent_x(double  x) {
		current_x = x;
	}

	/**
	* Sets the current y coordinate of the aircraft
	*/
	public void setCurrent_y(double  y) {
		current_y = y;
	}

	/**
	* Sets the current z coordinate of the aircraft
	*/
	public void setCurrent_z(double  z) {
		current_z = z;
	}

	/**
	* Sets the next waypoint of the aircraft
	*/
	public void setNextWaypoint(Waypoint nw) {
		nextWaypoint = nw;
	}

	/**
	* Sets the id of the aircraft
	*/
	public void setId(int i) {
		id = i;
	}

	/**
	* Returns the id of the aircraft
	*/
	public int getId() {
		return id;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

        public boolean equals(Object o) {
            boolean result = false;
            if(o instanceof Aircraft) {
                Aircraft other = (Aircraft)o;
                result = other.getId()==this.id;
            }
            return result;

        }

	/**
	* An internal helper class to parse string tags
	*/
	private String parseXMLString(XMLElement f, String tag) {
		XMLElement e = f.getChild(tag);
	  if(e !=null) {
	  	return e.getContent();
	  } else {
	  	System.out.println("AIRCRAFT:Didn't find tag " + tag + "..driving on..");
	  	return "no_xml_parse";
	  }
	}

	/**
	* An internal helper class to parse integer tags
	*/
	private int parseXMLInteger(XMLElement f, String tag) {
		XMLElement e = f.getChild(tag);
	  if(e !=null) {
	  	int result = Integer.parseInt(e.getContent());
	  	return result;
	  } else {
	  	System.out.println("AIRCRAFT:Didn't find tag " + tag + "..driving on..");
	  	return -888;
	  }
	}

        /**
	* An internal helper class to parse integer tags
	*/
	private double parseXMLDouble(XMLElement f, String tag) {
		XMLElement e = f.getChild(tag);
	  if(e !=null) {
	  	double result = Double.parseDouble(e.getContent());
	  	return result;
	  } else {
	  	System.out.println("AIRCRAFT:Didn't find tag " + tag + "..driving on..");
	  	return -888.0;
	  }
	}

	/**
	* Loads an aircraft from an XML Element. Returns 1 on success.
	*/
	public int loadFromXMLElement(XMLElement xml) {

		int result = 1;

	  id = parseXMLInteger(xml, "id");

	  System.out.println("Loading Aircraft # " + id);

	  currentSpeed = parseXMLDouble(xml, "current_speed");
	  current_x = parseXMLDouble(xml, "current_x");
	  current_y = parseXMLDouble(xml, "current_y");
	  current_z = parseXMLDouble(xml, "current_z");


	  XMLElement e = xml.getChild("next_waypoint");
	  if(e !=null) {
	  	nextWaypoint = new Waypoint();
	  	nextWaypoint.loadFromXMLElement(e);
	  } else {
	  	//TODO:  Warning?
	  }

	  e = xml.getChild("route");
	  if(e !=null) {
	  	aircraftRoute = new Route();
	  	aircraftRoute.loadFromXMLElement(e);
	  } else {
	  	//TODO:  Warning?
	  }

		return result;

	}

	/**
	* Returns an XML representation of the aircraft as a String
	*/
	public String toXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<aircraft>");
		sb.append("<id>" + id + "</id>");
                sb.append("<name>" + name + "</name>");
		sb.append("<current_speed>" + currentSpeed + "</current_speed>");
		sb.append("<current_x>" + current_x + "</current_x>");
		sb.append("<current_y>" + current_y + "</current_y>");
		sb.append("<current_z>" + current_z + "</current_z>");
		sb.append(aircraftRoute.toXML());
		if(nextWaypoint!=null) {
			sb.append(nextWaypoint.toXML());
		}
		sb.append("</aircraft>");
		return sb.toString();
	}

	/**
	* Creats an aircraft.  Sets a random id, initialize a blank route
	*/
	public Aircraft() {
		id = -1;
                name = "unset";
		aircraftRoute = new Route();
	}
}
