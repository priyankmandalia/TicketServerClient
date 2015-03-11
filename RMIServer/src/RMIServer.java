import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


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

    ArrayList<Event> events = new ArrayList<Event>();
    Color purple = new Color(78, 49, 104);
    GUI gui;
    static RMI rmi;
    ElectionManager replicaElectionManager, partitionElectionManager;

    String replicaIPs[] = {"148.197.40.156", "109.152.211.4"};
    String partitionIPs[] = {"148.197.40.156", "109.152.211.4"};
    boolean partitionKeeperRunning = true;
    private final String myIP;
    private int amountOfEvents;

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException, IOException, InterruptedException {

        startServer(Integer.parseInt(args[0]));

    }
    
    public static void startServer(int port) throws RemoteException, NotBoundException, MalformedURLException, IOException, InterruptedException {

        // java rmi server binding flow, using this class
        // as its a sub class of unicast remote object
        try {
            Registry reg = LocateRegistry.createRegistry(port);
            reg.rebind("server", new RMIServer());
            System.out.println("reg.rebind");
        } catch (RemoteException ex1) {
            Logger.getLogger(ElectionManager.class.getName()).log(Level.SEVERE, null, ex1);
        } catch (NotBoundException ex1) {
            Logger.getLogger(ElectionManager.class.getName()).log(Level.SEVERE, null, ex1);
        }

    }

    public RMIServer() throws RemoteException, NotBoundException, MalformedURLException, IOException, InterruptedException {

        super();

        this.events.add(new Event("Glastonbury 2015", "A major UK music and contemporary performance arts festival"));
        this.events.add(new Event("Greatbury 2015", "A major UK music and contemporary performance arts festival"));
        this.events.add(new Event("Gusbury 2015", "A major UK music and contemporary performance arts festival"));
        this.events.add(new Event("Priyankbury 2015", "A major UK music and contemporary performance arts festival"));

        this.gui = new GUI("RMI Server");
        
        this.myIP = getMyIp();

        this.replicaElectionManager = new ElectionManager(replicaIPs, RMI.REPLICA);
        this.partitionElectionManager = new ElectionManager(partitionIPs, RMI.PARTITION);

    }
    
    public static String getMyIp() throws MalformedURLException, IOException {

        URL whatismyip = new URL("http://checkip.amazonaws.com/");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        //you get the IP as a String
        String ip = in.readLine();
        return ip;

    }

    private Event getEventByExactName(String event) {
        
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
        int amountOfEvents = 0;
        String resultIP = null;
        // loop throgh all other partitions, return the 
        // one with the least records
        for(String ip : partitionIPs){
        
            connectServer(ip);
            amountOfEvents = rmi.getNumberOfEvents();
            if(amountOfEvents < lowest || lowest < 0){
            
                lowest = amountOfEvents;
                resultIP = ip;
            
            }
        
        }
        
        return resultIP;
    }
    
    public void updateReplicas() throws RemoteException, NotBoundException {
        
        // loop through replicas and replicate own events
        for (String replicaIP : replicaIPs) {
            
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
    public ArrayList<String> searchEvents(String query) {

        ArrayList<String> results = new ArrayList();
        gui.addStringAndUpdate("Events searched for - " + query);
        if (!query.matches("")) {

            for (Event event : events) {

                String title = event.getTitle();
                String description = event.getDescription();
                if (title.contains(query) || description.contains(query)) {

                    results.add(title);

                }
            }
        } else {

            results.addAll(getEvents());

        }

        return results;

    }
    
    @Override
    public ArrayList<String> getBookings(String event) {

        gui.addStringAndUpdate("Bookings returned for - " + event);
        return getEventByExactName(event).getBookings();

    }

    @Override
    public boolean book(String event, String customer, int amount) {

        Event e = getEventByExactName(event);
        int size = e.getBookingSize();
        e.addBooking(amount + " tickets for " + customer);
        if (size < e.getBookingSize()) {

            gui.addStringAndUpdate(customer + " booked " + amount + " tickets to " + event);
            return true;

        }

        return false;

    }

    @Override
    public ArrayList<String> getEvents() {

        gui.addStringAndUpdate("List of events returned");
        ArrayList<String> titles = new ArrayList();

        for (Event eventi : events) {

            titles.add(eventi.getTitle());

        }

        return titles;

    }

    @Override
    public boolean addEvent(String name, String description) throws RemoteException, NotBoundException{
        
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

        }else{
        
            connectServer(maxStorageServer);
            rmi.addEvent(name, description);
            gui.addStringAndUpdate("event re routed, to be added in - " + maxStorageServer);
        
        }

        return false;

    }

    @Override
    public ArrayList<String> getServers() {

        ArrayList<String> result = new ArrayList<String>();
        result.add("server1");
        result.add("server2");

        gui.addStringAndUpdate("List of servers returned");

        return result;

    }

    @Override
    public void replicate(ArrayList<Event> latest) throws RemoteException {

        this.events = latest;
        gui.addStringAndUpdate("Server Updated");

    }

    @Override
    public boolean isRunning() throws RemoteException {
        return true;
    }

    @Override
    public String agreeLeader(String senderIP, boolean repicaOrPartition) throws RemoteException {
        
        // check weather incoming request is asking to agree on 
        // replica leader or partition leader
        if (repicaOrPartition) {
            
            if (getDoubleIPAddress(replicaElectionManager.getCurrentLeaderIp()) < getDoubleIPAddress(senderIP)) {
                
                replicaElectionManager.setCurrentLeaderIp(senderIP);
                try {
                    replicaElectionManager.connectServer(senderIP);
                } catch (NotBoundException ex) {
                    Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                replicaElectionManager.setHeartbeat(true);

            }
            
        } else {

            if (getDoubleIPAddress(partitionElectionManager.getCurrentLeaderIp()) < getDoubleIPAddress(senderIP)) {
                
                partitionElectionManager.setCurrentLeaderIp(senderIP);
                try {
                    partitionElectionManager.connectServer(senderIP);
                } catch (NotBoundException ex) {
                    Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                partitionElectionManager.setHeartbeat(true);

            }

        }
        
        return replicaElectionManager.getCurrentLeaderIp();
    }

    @Override
    public String[] getIPaddresses() throws RemoteException {
        
         gui.addStringAndUpdate("List of Server IP Addresses returned");
        
        return replicaIPs;
    }

    @Override
    public int getNumberOfEvents() throws RemoteException {
        return amountOfEvents;
    }

}
