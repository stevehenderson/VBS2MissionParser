package vbs2missionparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * A VBS2 class object
 */
public class VBS2Class {

    /**
     * Raw text prior to parsing.  Everthing in the class tag capsule.
     */
    public StringBuffer rawText;

    /**
     * The attributes after the parse
     */
    public HashMap attributes;

    /**
     * Any inner classes...Only tested with one deep
     */
    public Vector<VBS2Class> innerClasses;

    /**
     * Return a string rep of the class.
     *
     * @return
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CLASS:\n");
        sb.append("------\n");
        Iterator keys = this.attributes.keySet().iterator();
        while (keys.hasNext()) {
            String nextKey = (String) keys.next();
            String value = (String) this.attributes.get(nextKey);
            sb.append(nextKey + " : " + value + "\n");
        }
        return sb.toString();
    }

    /**
     * Parse the raw text of a class
     */
    public void parse() {

        Vector<VBS2Class> classes = new Vector<VBS2Class>();

        VBS2MissionParserView.State state = VBS2MissionParserView.State.CLASS_WAIT;
        int nest = 0;
        VBS2Class nestedClass = null;

        try {
            BufferedReader reader = new BufferedReader(new StringReader(this.rawText.toString()));
            String line = null; //not declared within while loop
            while ((line = reader.readLine()) != null) {

                if (line != null) {
                    line = line.trim();
                }
                //Are we waiting for class
                if (state == VBS2MissionParserView.State.CLASS_WAIT) {
                    //Yes we are waiting...did we just read a class tag
                    if (line.contains("class Arguments")) {
                        nest++;
                        if (nest > 1) {
                            //Yes,cool change state
                            state = VBS2MissionParserView.State.READ_ARGUMENTS_CLASS;
                        }
                    } else {
                        //Did we read an argument?
                        if (line.contains("=")) {
                            //Yes -- read it and add the attribut
                            //First remove the semi colon
                            line.replace(";", " ");

                            String[] att = line.split("=");

                            if (att.length > 1) {
                                String key = att[0];
                                String value = att[1];
                                value = value.replace('"', '\0');
                                value = value.replace(';', '\0');
                                this.attributes.put(key, value.trim());
                            }
                        }
                    }
                } else if (state == VBS2MissionParserView.State.READ_ARGUMENTS_CLASS) {

                    if (line.contains("=")) {
                        //Yes -- read it and add the attribut
                        //First remove the semi colon
                        line.replace(";", "");
                        String[] att = line.split("=");

                        if (att.length > 1) {
                            String key = att[0];
                            String value = att[1];
                            value = value.replace('"', '\0');
                            value = value.replace(';', '\0');
                            this.attributes.put(key, value.trim());
                        }
                    }


                    if (line.equals("};")) {
                        nest--;
                        if (nest == 0) {
                            state = VBS2MissionParserView.State.CLASS_WAIT;
                        }
                    }
                }

            }
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }

    }

    /**
     * Constructor
     */
    public VBS2Class() {
        rawText = new StringBuffer();
        attributes = new HashMap();
        innerClasses = new Vector<VBS2Class>();
    }
}
