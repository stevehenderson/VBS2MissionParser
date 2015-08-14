/*
 * VBS2MissionParserView.java
 */
package vbs2missionparser;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import processing.xml.XMLElement;

/**
 * The application's main frame.
 */
public class VBS2MissionParserView extends FrameView {

    public enum State {

        CLASS_WAIT, READ_CLASS, READ_ARGUMENTS_CLASS
    }
    Vector<VBS2Class> classes;
    Vector<Aircraft> aircraft;

    /**
     * Parse the file...pull out outer classes
     */
    private void parseFile(File f) {
        State state = State.CLASS_WAIT;
        VBS2Class currentClass = null;
        int nest = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = null; //not declared within while loop
            while ((line = reader.readLine()) != null) {

                if (line != null) {
                    line = line.trim();
                }
                //Are we waiting for class
                if (state == State.CLASS_WAIT) {
                    //Yes we are waiting...did we just read a class tag
                    if (line.contains("class")) {

                        nest++;
                        //Yes,cool change state
                        state = State.READ_CLASS;
                        currentClass = new VBS2Class();
                        currentClass.rawText.append(line + "\n");

                        //pull the id
                        String[] temp = line.split(" ");
                        if (temp.length > 1) {
                            String id = temp[1].trim();
                            currentClass.attributes.put("ID", id);
                        }
                    }
                } else if (state == State.READ_CLASS) {

                    if (line.contains("class")) {
                        nest++;
                    }
                    //We are reading class..keep reading until we hit semi-colon
                    currentClass.rawText.append(line + "\n");
                    if (line.equals("};")) {
                        nest--;
                        if (nest == 0) {
                            state = State.CLASS_WAIT;
                            classes.add(currentClass);
                        }
                    }
                }
                //System.out.println(line);

            }

        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }


        //Parse each class
        Iterator<VBS2Class> it = classes.iterator();
        while (it.hasNext()) {
            VBS2Class nextClass = it.next();
            nextClass.parse();
        }

        //FINALLY -- Deump the classes to the cmd line
        it = classes.iterator();
        while (it.hasNext()) {
            VBS2Class nextClass = it.next();
            System.out.println(nextClass);
        }
    }

    private int getTrailerId(String s) {
        int result = -88;
        if (s == null) {
            return result;
        }
        String[] parts = s.split("_");
        if (parts.length > 2) {
            result = Integer.parseInt(parts[2]);
        }
        return result;
    }

    /**
     * Parses this:  [2492.95996, 2466.46387, 13.86000]
     * @param s
     * @return
     */
    private double[] getCoordinates(String s) {
        double[] result = new double[3];
        result[0] = 0.0;
        result[1] = 0.0;
        result[2] = 0.0;

        s = s.replace('[', '\0');
        s = s.replace(']', '\0');

        String[] parts = s.split(",");
        if (parts.length > 0) {
            result[0] = Double.parseDouble(parts[0]);
        }
        if (parts.length > 1) {
            result[1] = Double.parseDouble(parts[1]);
        }
        if (parts.length > 2) {
            result[2] = Double.parseDouble(parts[2]);
        }

        return result;
    }

    /**
     * This method converts the VBS2Classes to objects in the ATC model
     */
    private void classesToObjects() {

        int id = -1;
        Iterator<VBS2Class> it = classes.iterator();
        while (it.hasNext()) {
            VBS2Class nextClass = it.next();

            System.out.println("Checking class " + nextClass);

            if (nextClass.attributes.containsKey("objectType")) {

                //First pull the id
                if (nextClass.attributes.containsKey("ID")) {
                    String idString = (String) nextClass.attributes.get("ID");
                    id = getTrailerId(idString);
                }

                //Read an aircraft
                if (nextClass.attributes.get("objectType").equals("unit")) {

                    Aircraft a = new Aircraft();
                    String name = (String) nextClass.attributes.get("NAME");
                    a.setId(id);
                    a.setName(name);
                    String positionString = (String) nextClass.attributes.get("POSITION");
                    double[] coords = getCoordinates(positionString);
                    a.setCurrent_x(coords[0]);
                    a.setCurrent_y(coords[1]);
                    a.setCurrent_z(coords[2]);
                    aircraft.add(a);

                }

                //Read a waypoint
                if (nextClass.attributes.get("objectType").equals("waypoint")) {

                    Waypoint w = new Waypoint();
                    String name = (String) nextClass.attributes.get("DESCRIPTION");
                    w.setId(id);
                    w.setName(name);
                    String positionString = (String) nextClass.attributes.get("POSITION");
                    double[] coords = getCoordinates(positionString);
                    w.setPosX(coords[0]);
                    w.setPosY(coords[1]);
                    w.setPosZ(coords[2]);
                    String prevS = (String) nextClass.attributes.get("PREVTASK");
                    int prevId = getTrailerId(prevS);
                    w.setPrev(prevId);
                    String nextS = (String) nextClass.attributes.get("NEXTTASK");
                    int nextId = getTrailerId(nextS);
                    w.setNext(nextId);

                    //get owning aircraft
                    String parentS = (String) nextClass.attributes.get("PARENT_UNIT");
                    if (parentS != null) {
                        int aircraftId = getTrailerId(parentS);
                        Aircraft dummy = new Aircraft();
                        dummy.setId(aircraftId);
                        if (aircraft.contains(dummy)) {
                            Aircraft theAircraft = aircraft.get(aircraft.indexOf(dummy));
                            theAircraft.getRoute().insertWaypoint(w, theAircraft.getRoute().getWaypoints().size());
                        }
                    }

                }

            }


        }
    }

    private File selectSourceFile() {
        File file = null;
        JFileChooser fc = new JFileChooser("Select source biedi file");
        fc.setDialogTitle("Select source biedi file");
        fc.setFileFilter(new BiediFileFilter());
        int returnVal = fc.showOpenDialog(VBS2MissionParserApp.getApplication().getMainFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        } else {
            //Do nothing
        }
        return file;
    }

    private File selectXMLFile() {
        File file = null;
        JFileChooser fc = new JFileChooser("Select destination XML file");
        fc.setDialogTitle("Select destination XML file");
        fc.setFileFilter(new XMLFileFilter());
        int returnVal = fc.showOpenDialog(VBS2MissionParserApp.getApplication().getMainFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        } else {
            //Do nothing
        }
        return file;
    }

    private void doit() {
        parseFile(selectSourceFile());
        classesToObjects();
        Iterator<Aircraft> it = aircraft.iterator();
        StringBuffer sb = new StringBuffer();
        sb.append("<scenario><id>0</id><name>unset_scenario</name>");
        while (it.hasNext()) {
            Aircraft nextone = it.next();
            sb.append(nextone.toXML());
        }
        sb.append("</scenario>");
        XMLElement e = new XMLElement(sb.toString());
        System.out.println(e.toString(true));

        File outFile = selectXMLFile();

        try {
            // Create file
            FileWriter fstream = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(e.toString());
            //Close the output stream
            out.close();
        } catch (Exception exx) { 
            System.err.println("Error: " + exx.getMessage());
        }
    }

    public VBS2MissionParserView(SingleFrameApplication app) {
        super(app);

        aircraft = new Vector<Aircraft>();
        classes = new Vector<VBS2Class>();

        initComponents();
        
        doit();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = VBS2MissionParserApp.getApplication().getMainFrame();
            aboutBox = new VBS2MissionParserAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        VBS2MissionParserApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 254, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(vbs2missionparser.VBS2MissionParserApp.class).getContext().getResourceMap(VBS2MissionParserView.class);
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(vbs2missionparser.VBS2MissionParserApp.class).getContext().getActionMap(VBS2MissionParserView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 230, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JSeparator statusPanelSeparator;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
