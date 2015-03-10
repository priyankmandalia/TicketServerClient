import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
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
    ElectionManager em;

    String serverIPs[] = {"148.197.40.156", "148.197.41.49"};

    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException, IOException, InterruptedException {

        startServer(Integer.parseInt(args[0]));

    }

    public RMIServer() throws RemoteException, NotBoundException, MalformedURLException, IOException, InterruptedException {

        super();

        events.add(new Event("Glastonbury 2015", "A major UK music and contemporary performance arts festival"));
        events.add(new Event("Greatbury 2015", "A major UK music and contemporary performance arts festival"));
        events.add(new Event("Gusbury 2015", "A major UK music and contemporary performance arts festival"));
        events.add(new Event("Priyankbury 2015", "A major UK music and contemporary performance arts festival"));

        gui = new GUI("RMI Server");

        em = new ElectionManager(serverIPs);

    }

    public static void startServer(int port) throws RemoteException, NotBoundException, MalformedURLException, IOException, InterruptedException {

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

    private Event getEventByExactName(String event) {

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
    public boolean addEvent(String name, String description) throws RemoteException {

        gui.addStringAndUpdate("event added - " + name + "," + description);
        this.events.add(new Event(name, description));

        try {
            updateServers();
        } catch (NotBoundException ex) {
            Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
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

    public void updateServers() throws RemoteException, NotBoundException {

        for (int i = 0; i < serverIPs.length; i++) {

            connectServer(serverIPs[i]);
            rmi.replicate(events);

        }

    }

    private static void connectServer(String ip) throws RemoteException, NotBoundException {

        Registry reg = LocateRegistry.getRegistry(ip, 1099);
        rmi = (RMI) reg.lookup("server");
        String text = rmi.getData("output");
        System.out.println(text);

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
    public String agreeLeader(String senderIP) throws RemoteException {

       
        if (getDoubleIPAddress(em.getCurrentLeaderIp()) < getDoubleIPAddress(senderIP)) {

            em.setCurrentLeaderIp(senderIP);
            try {
                em.connectServer(senderIP);
            } catch (NotBoundException ex) {
                Logger.getLogger(RMIServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            em.setHeartbeat(true);

        }
        
        return em.getCurrentLeaderIp();
    }

    private double getDoubleIPAddress(String ip) {

        return Double.parseDouble(ip.replace(".", ""));

    }

}
