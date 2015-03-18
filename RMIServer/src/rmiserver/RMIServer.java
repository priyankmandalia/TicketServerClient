package rmiserver;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import rmi.*;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author up705759
 */
public class RMIServer extends UnicastRemoteObject implements RMI {

    ArrayList<Event> events = new ArrayList<>();
    Color purple = new Color(78, 49, 104);
    GUI gui;
    static RMI rmi;
    ElectionManager replicaElectionManager;

    String replicaIPs[];
    String partitionIPs[];
    private final String myIP;
    private int amountOfEvents;
    private int numberOfClientsConnected;
    private int indexOfReplica = 0;
    private boolean connectedToLeader = false;
    //private String[] actualReplicas;

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException, IOException, InterruptedException, ParserConfigurationException, SAXException, URISyntaxException {

        startServer(1099);

    }

    public static void startServer(int port) throws RemoteException, NotBoundException, MalformedURLException, IOException, InterruptedException, ParserConfigurationException, SAXException, URISyntaxException {

        // java rmi server binding flow, using this class
        // as its a sub class of unicast remote object
        try {
            Registry reg = LocateRegistry.createRegistry(port);
            reg.rebind("server", new RMIServer());
            System.out.println("reg.rebind");
        } catch (RemoteException | NotBoundException ex1) {
            Logger.getLogger(ElectionManager.class.getName()).log(Level.SEVERE, null, ex1);
        }

    }

    public RMIServer() throws RemoteException, NotBoundException, MalformedURLException, IOException, InterruptedException, ParserConfigurationException, SAXException, URISyntaxException {

        super();

        this.events.add(new Event("Glastonbury 2015", "A major UK music and contemporary performance arts festival"));
        this.events.add(new Event("Greatbury 2015", "A major UK music and contemporary performance arts festival"));
        this.events.add(new Event("Gusbury 2015", "A major UK music and contemporary performance arts festival"));
        this.events.add(new Event("Priyankbury 2015", "A major UK music and contemporary performance arts festival"));

        this.gui = new GUI("RMI Server");

        this.myIP = getMyIp();

        paramReader params = new paramReader("partitions.xml", "replicas.xml");
        replicaIPs = params.getReplicas();
        partitionIPs = params.getPartitions();

        this.replicaElectionManager = new ElectionManager(replicaIPs, gui); //Change replicaIPs to actualReplicas
        gui.addStringAndUpdate("Replica Manager Running");
        //this.partitionElectionManager = new ElectionManager(partitionIPs, RMI.PARTITION, gui);
        //gui.addStringAndUpdate("Partition Manager Running");

    }

    public static String getMyIp() throws MalformedURLException, IOException {

        URL whatismyip = new URL("http://checkip.amazonaws.com/");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        //you get the IP as a String
        String ip = in.readLine();
        return ip;

    }

    @Override
    public Event getEventByExactName(String event) {

        // loop through events and compare titles exactly to the input string
        gui.addStringAndUpdate("Precise event queried: " + event);
        Event result = null;
        for (Event eventi : events) {

            String title = eventi.getTitle();
            System.out.println(title);
            if (title.matches(event)) {

                result = eventi;

            }
        }
        return result;
    }

    private String getMaxStorageServer() throws RemoteException, NotBoundException {

        int lowest = -1;
        int eventsAmount;
        String resultIP = null;
        // loop throgh all other partitions, return the 
        // one with the least records
        for (String ip : partitionIPs) {

            connectServer(ip);
            eventsAmount = rmi.getNumberOfEvents();
            if (eventsAmount < lowest || lowest < 0) {

                lowest = eventsAmount;
                resultIP = ip;

            }

        }

        return resultIP;
    }

    public void updateReplicas() throws RemoteException, NotBoundException {
        //getReplicas(noofreplicas, noofreplicas); //assign replicas to each Partition leader
        // loop through replicas and replicate own events
        ArrayList<String> replicas = replicaElectionManager.getActiveReplicas();
        gui.addStringAndUpdate("replicas to update - " + replicas.get(0));
        for (String replicaIP : replicas) {

            connectServer(replicaIP);
            rmi.replicate(events);

        }

    }

    private static void connectServer(String ip) throws RemoteException, NotBoundException {

        // java rmi connect flow
        Registry reg = LocateRegistry.getRegistry(ip, 1099);
        rmi = (RMI) reg.lookup("server");

    }

    private double getDoubleIPAddress(String ip) {

        return Double.parseDouble(ip.replace(".", ""));

    }

    //*********************RMI METHODS*********************
    @Override
    public String getData(String text) throws RemoteException {

        return "Great " + text;

    }

    @Override
    public ArrayList<String> searchEvents(String query) throws RemoteException {

        ArrayList<String> results = new ArrayList();
        gui.addStringAndUpdate("Events searched for - " + query);
        ArrayList<Event> allSystemEvents = getEvents();
        if (!query.matches("")) {

            for (Event event : allSystemEvents) {

                String title = event.getTitle();
                String description = event.getDescription();
                if (title.contains(query) || description.contains(query)) {

                    results.add(title);

                }
            }
        } else {

            results.addAll(getEventTitles());

        }

        return results;

    }

    @Override
    public ArrayList<String> getBookings(String event) {

        gui.addStringAndUpdate("Bookings returned for - " + event);
        return getEventByExactName(event).getBookings();

    }

    @Override
    public boolean book(String event, String customer, int amount) throws RemoteException, NotBoundException {

        Event e = getEventByExactName(event);
        if (e == null) {

            for (int i = 0; i < partitionIPs.length; i++) {

                connectServer(partitionIPs[i]);
                e = rmi.getEventByExactName(event);
                if (e != null) {

                    if(rmi.book(event, customer, amount)){
                    
                        return true;
                    
                    }
                }
            }
        } else {

            int size = e.getBookingSize();
            e.addBooking(amount + " tickets for " + customer);
            if (size < e.getBookingSize()) {

                gui.addStringAndUpdate(customer + " booked " + amount + " tickets to " + event);
                updateReplicas();
                return true;

            }

        }

        return false;

    }

    @Override
    public ArrayList<String> getEventTitles() throws RemoteException {

        gui.addStringAndUpdate("List of events returned");
        ArrayList<String> titles = new ArrayList();

        for (Event eventi : events) {

            titles.add(eventi.getTitle());

        }

        for (int i = 0; i < partitionIPs.length; i++) {

            try {
                connectServer(partitionIPs[i]);
            } catch (NotBoundException ex) {
                Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            titles.addAll(rmi.serverGetEventTitles());

        }

        return titles;

    }

    @Override
    public boolean addEvent(String name, String description) throws RemoteException, NotBoundException {

        // get the server with the least event records, then
        // then if it is another server, call that servers addEvent method
        // with the same imput params.
        String maxStorageServer = getMaxStorageServer();
        if (maxStorageServer.matches(myIP)) {

            amountOfEvents++;
            gui.addStringAndUpdate("event added - " + name + "," + description);
            this.events.add(new Event(name, description));

            try {
                updateReplicas();
            } catch (NotBoundException ex) {
                Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {

            connectServer(maxStorageServer);
            rmi.addEvent(name, description);
            gui.addStringAndUpdate("event re routed, to be added in - " + maxStorageServer);

        }

        return false;

    }

    @Override
    public ArrayList<String> getServers() {

        ArrayList<String> result = new ArrayList<>();
        result.add("server1");
        result.add("server2");

        gui.addStringAndUpdate("List of servers returned");

        return result;

    }

    @Override
    public void replicate(ArrayList<Event> latest) throws RemoteException {
        gui.addStringAndUpdate("Replicated");
        this.events = latest;
        gui.addStringAndUpdate("Server Updated");

    }

    @Override
    public boolean isRunning() throws RemoteException {

        return true;
    }

    @Override
    public String agreeLeader(String senderIP) throws RemoteException {

//        String IP = null;
//
//        if (getDoubleIPAddress(replicaElectionManager.getCurrentLeaderIp()) < getDoubleIPAddress(senderIP)) {
//
//            try {
//                replicaElectionManager.setCurrentLeaderIp(senderIP);
//            } catch (NotBoundException ex) {
//                Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            try {
//                replicaElectionManager.connectServer(senderIP);
//            } catch (NotBoundException ex) {
//                Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            replicaElectionManager.setHeartbeat(true);
//            IP = replicaElectionManager.getCurrentLeaderIp();
//
//        }
//
//        gui.addStringAndUpdate("Leader Compared");
        return null;
    }

    @Override
    public String[] getIPaddresses() throws RemoteException {

        gui.addStringAndUpdate("List of Server IP Addresses returned");

        return partitionIPs;
    }

    @Override
    public int getNumberOfEvents() throws RemoteException {
        gui.addStringAndUpdate("Number of events returned");
        return amountOfEvents;
    }

    @Override
    public int getNumberOfConnections() throws RemoteException {
        gui.addStringAndUpdate("Number of connections returned");
        return numberOfClientsConnected;

    }

    @Override
    public void notifyConnected() throws RemoteException {
        gui.addStringAndUpdate("Client Connected");
        numberOfClientsConnected++;

    }

    @Override
    public void notifyDisconnected() throws RemoteException {
        gui.addStringAndUpdate("Client Disconnected");
        numberOfClientsConnected--;

    }

    @Override
    public String getReadServer() throws RemoteException {
        gui.addStringAndUpdate("Read Server Assigned To Client");
        //getReplicas(noofreplicas, noofreplicas); //assign replicas to each Partition leader
        if (indexOfReplica == replicaElectionManager.getActiveReplicas().size() - 1) {

            indexOfReplica = 0;

        } else {

            if (indexOfReplica <= replicaElectionManager.getActiveReplicas().size() - 1) {
                indexOfReplica++;
            }

        }
        gui.addStringAndUpdate("index - " + indexOfReplica + ",replica - " + replicaElectionManager.getActiveReplicas().get(indexOfReplica));
        return replicaElectionManager.getActiveReplicas().get(indexOfReplica);

    }

    @Override
    public boolean claimAsReplica(String ip) throws RemoteException {

        if (!replicaElectionManager.isLeader) {
            
            try {
                
                replicaElectionManager.setCurrentLeaderIp(ip);
                connectedToLeader = true;
                gui.addStringAndUpdate("Claimed As Replica By:" + ip);
                return true;
                
            } catch (NotBoundException ex) {
                gui.addStringAndUpdate("claimasreplica fail - " + ex.getMessage());
                return false;
            }
        }

        return false;

    }

    @Override
    public ArrayList<String> serverGetEventTitles() throws RemoteException {
        ArrayList<String> titles = new ArrayList();

        for (Event eventi : events) {

            titles.add(eventi.getTitle());

        }

        return titles;
    }

    @Override
    public ArrayList<Event> serverGetEvents() throws RemoteException {

        return events;

    }

    @Override
    public ArrayList<Event> getEvents() throws RemoteException {

        gui.addStringAndUpdate("List of events returned");
        ArrayList<Event> results = events;

        for (int i = 0; i < partitionIPs.length; i++) {

            try {
                connectServer(partitionIPs[i]);
            } catch (NotBoundException ex) {
                Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            results.addAll(rmi.serverGetEvents());

        }

        return results;

    }

    @Override
    public String getWriteServer() throws RemoteException {
        gui.addStringAndUpdate("write server assigned -" + replicaElectionManager.getCurrentLeaderIp());
        return replicaElectionManager.getCurrentLeaderIp();
    }

    @Override
    public ArrayList<String> getActiveReplicas() throws RemoteException {
        return replicaElectionManager.getActiveReplicas();
    }

}
