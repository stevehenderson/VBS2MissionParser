package vbs2missionparser;

import java.util.ArrayList;
import processing.xml.*;

/**
* A Route represents a path the aircraft will traverse through the
* airspace of the simulation.  The aircraft will "appear" at
* the first waypoint in the route, and dissapear at the last waypoint.
*/
class Route {

	/**
	* A unique id for the route.
	*/
	int id;

	/**
	* A collection of waypoints on the Route*/
	ArrayList waypoints;

	/**
	* returns the id of the Route*/
	public int getId() { return id; }

	/**
	* Returns the collection of underlying waypoints
	* that comprise the route.
	*/
	public ArrayList getWaypoints() { return waypoints;	}

	/**
	* sets the id of the Route
	*/
	public void setId(int i) { id=i;}


	/**
	* sets the waypoints for the Route
	*/
	public void setWaypoints(ArrayList wps) { waypoints=wps; }


	/**
	 *  This method traverses the route until it finds currentWaypoint.
	 *  It the returns the next waypoint on the route (if any).  If none,
	 *  a null is returned.
	 */
	public Waypoint getFollowonWaypoint(Waypoint currentWaypoint) {

      if(currentWaypoint==null) {
        //Try to set the result to the first waypoint
        return (Waypoint) waypoints.get(0);
      }

      Waypoint result = null;

      for(int i=0; i < waypoints.size(); i++) {
		    Waypoint nextCandidate = (Waypoint) waypoints.get(i);
		    System.out.println("checking current " + currentWaypoint.getId() + " and next " + nextCandidate.getId());
		    if(nextCandidate.getId()==currentWaypoint.getId()) {
		        System.out.println("!!match..");
            //We have the current one...can we get one more?
            if((i+1) < waypoints.size()) {
              //Yes!  set it as the return type
              result=(Waypoint) waypoints.get(i+1);
              System.out.println("returning .." + result.getId());
              return result;
            }  else {
              System.out.println("size issues..");
            }
        } else {
          System.out.println("no match..");
        }
		  }
      return null;
  }


	/**
	* Inserts a waypoint into the route at the indicated position.  If position exceeds the
	* the current count in the collection, the waypoint will be added to the end (so use
	a big number to force it to the end). NOTE:  the first position (startpoint) is 0 (zero).
	*/
	public void insertWaypoint(Waypoint wp, int position) {
		System.out.println("DEBUG: trying to add a waypoint");
		ArrayList newList = new ArrayList();
		boolean added=false;
		for(int i=0; i < waypoints.size(); i++) {
			if(i==position) {
				//Add the new guy
				newList.add(wp);
				System.out.println("DEBUG: added a waypoint 1");

				//Add the existing
				newList.add((Waypoint) waypoints.get(i));
				added=true;
			} else {
				//Add the existing
				newList.add((Waypoint) waypoints.get(i));
			}
		}

		//Did we still not add the new guy?
		if(added==false)  {
			//Yes -- add it now.  We don't care about position
			newList.add(wp);
			System.out.println("DEBUG: added a waypoint 2");
		}

		//finally copy the new list to the real list
		waypoints=newList;
	}

	/**
	* Removes a waypoint (based on id match) from the route.  Returns 1 upon success.
	*/
	public int removeWaypoint(Waypoint wp) {
		ArrayList newList = new ArrayList();
		boolean removed=false;
		for(int i=0; i < waypoints.size(); i++) {
			Waypoint nextWaypoint = (Waypoint) waypoints.get(i);
			if(nextWaypoint.getId()==wp.getId()) {
				//Found it -- don't copt to the new array
				removed=true;
			} else {
				newList.add(nextWaypoint);
			}
		}
		if(removed==true) {
			//Need to replace the real waypoint collection with the new one
			waypoints=newList;
			return 1;
		} else {
			return 0;
		}
	}


/**
	* An internal helper class to parse string tags
	*/
	private String parseXMLString(XMLElement f, String tag) {
		XMLElement e = f.getChild(tag);
	  if(e !=null) {
	  	return e.getContent();
	  } else {
	  	System.out.println("ROUTE:Didn't find tag " + tag + "..driving on..");
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
	  	System.out.println("ROUTE:Didn't find tag " + tag + "..driving on..");
	  	return -888;
	  }
	}

	/**
	* Loads a route from an XML Element. Returns 1 on success.
	*/
	public int loadFromXMLElement(XMLElement xml) {

		int result = 1;

	  id = parseXMLInteger(xml, "id");

	  System.out.println("\tLoading Route " + id);

	  XMLElement[] wElements = xml.getChildren("waypoint");
	  if(wElements.length > 0) {
	  	waypoints = new ArrayList();
		  for(int i=0; i < wElements.length; i++) {
		  	Waypoint nextWaypoint = new Waypoint();
		  	nextWaypoint.loadFromXMLElement(wElements[i]);
		  	waypoints.add(nextWaypoint);
		  }
	  } else {
	  	System.out.println("ROUTE:WARNING-Failed to find any <waypoint> tags...driving on!");
	  }

		return result;
	}

	/**
	* Returns an XML representation of the Route and all encapsulated
	* classes
	*/
	public String toXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<route>");
		sb.append("<id>" + id + "</id>");
		for(int i=0; i < waypoints.size(); i++) {
			Waypoint nextWaypoint = (Waypoint) waypoints.get(i);
			sb.append(nextWaypoint.toXML());
		}
		sb.append("</route>");
		return sb.toString();
	}

	/**
	* Create a new Route.  Assign a random id, and initialize
	* an empty collection of waypoints.
	*/
  public Route() {
  	id = -1;
  	waypoints = new ArrayList();
  }

}

