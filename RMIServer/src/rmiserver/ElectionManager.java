package rmiserver;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import rmi.RMI;

/**
 *
 * @author up705759
 */
public class ElectionManager {

    private RMI rmi;
    private String currentLeaderIp;
    private final String[] replicaIPs;
    private final String[] partitionIPs;
    public ArrayList<String> activeReplicas;
    
    private final paramReader params;
    private final int noOfReplicas, noOfPartitions;
    private final String myIP;
    private final GUI gui;
    private boolean isPartition = false;
    public boolean isLeader = false;

    public ElectionManager(String[] replicaips, GUI gui) throws RemoteException, NotBoundException, MalformedURLException, IOException, InterruptedException, ParserConfigurationException, SAXException, URISyntaxException {

        this.replicaIPs = replicaips;
        //this.currentLeaderIp = getFirstLeader(replicaips);
        // get the highest ip address and set as initial leader
        this.currentLeaderIp = null;

        this.myIP = getMyIp();

        this.params = new paramReader("partitions.xml", "replicas.xml");
        this.noOfReplicas = this.replicaIPs.length;

        this.partitionIPs = params.getPartitions();
        noOfPartitions = this.partitionIPs.length;

        for (int i = 0; i < partitionIPs.length; i++) {
            if (partitionIPs[i].matches(myIP)) {

                isPartition = true;

            }

        }

        this.gui = gui;

        go();
    }

    private void go() throws RemoteException, NotBoundException, IOException, InterruptedException {

        Thread t = new Thread(new Runnable() {

            int serversAlive;

            @Override
            public void run() {

                // get own IP
                String myIP = null;
                try {
                    myIP = getMyIp();
                } catch (IOException ex) {
                    gui.addStringAndUpdate("get my ip fail - " + ex.getMessage());
                }
                // loop through all other ip's, connect and compare own ip with their leader ip
                // to check if this server needs to bully
//                    for (String ip : replicaIPs) {
//
//                        if (!ip.matches(myIP)) {
//
//                            if (connectServer(ip)) {
//
//                                serversAlive++;
//                                String comparedLeader = rmi.agreeLeader(myIP);
//                                if (!currentLeaderIp.matches(comparedLeader) && !myIP.matches(comparedLeader)) {
//
//                                    currentLeaderIp = comparedLeader;
//
//                                }
//                            }
//                        }
//                    }
                // check if this leader is or has become the leader
                // if so, stop the the heartbeat as this is only for 
                // ensuring leader is responding
                if (/*serversAlive == 0 &&*/isPartition) {

                    // election has gone wrong, this becomes leader
                    //gui.addStringAndUpdate("Error, couldnt find leader");
                    gui.addStringAndUpdate("This server is Leader");
                    currentLeaderIp = myIP;
                    isLeader = true;
                    claimReplicas(noOfPartitions, noOfReplicas);

                } else {

                    gui.addStringAndUpdate("This server is Replica");
                    isLeader = false;

                }/* else if (currentLeaderIp.matches(myIP) && isPartition) {

                 // this is the leader
                 heartbeat = false;
                 gui.addStringAndUpdate("This Server is leader");
                 getReplicas(noOfPartitions, noOfReplicas);
                        
                        

                 }*/
                //loop forever, if not leader continuously check if leader is alive
                // if leader, dont check but stay in loop incase of bully situation

                while (true) {

                    //gui.addStringAndUpdate(currentLeaderIp);
                    if(isLeader){
                    
                        
                        
                    }else if (currentLeaderIp != null) {

                        try {
                            // check if leader is alive
                            if (rmi.isRunning()) {

                                //activeReplicas.addAll(rmi.getActiveReplicas());
                                gui.addStringAndUpdate("Leader " + currentLeaderIp + " is running");

                            }
                        } catch (RemoteException ex) {
                            
                            // leader has crashed, connection to leader raised exception
                            // connect to next highest ip
                            gui.addStringAndUpdate("Leader crashed or corrupt");
                            try {
                                connectToRunnerUp();
                            } catch (RemoteException ex1) {
                                Logger.getLogger(ElectionManager.class.getName()).log(Level.SEVERE, null, ex1);
                            } catch (NotBoundException ex1) {
                                Logger.getLogger(ElectionManager.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                            
                        }
                        
                        try {
                            // every 2 seconds
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ElectionManager.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }

            }
        });
        // start thread defined above
        t.start();

    }

    // get all the replicas and distrubute based on the number of Partition leaders 
    private void claimReplicas(int leaders, int replicas) {

        int needed = replicas / leaders;
        gui.addStringAndUpdate("Attempting to claim " + needed + " of " + replicas + " Replicas");
        activeReplicas = new ArrayList<>();
        
            for (String replicaIP : replicaIPs) {
                gui.addStringAndUpdate("trying - " + replicaIP);
                try {
                    if (!replicaIP.matches(myIP) && activeReplicas.size() < needed) {
                        gui.addStringAndUpdate("Connecting to possible replica");
                        System.out.println(replicaIP);
                        connectServer(replicaIP);
                        if (rmi.claimAsReplica(myIP)) {
                            gui.addStringAndUpdate("Found Replica");
                            activeReplicas.add(replicaIP);
                            
                        }
                    }
                } catch (RemoteException | NotBoundException ex) {
                    gui.addStringAndUpdate(ex.getMessage());
                }
            }
        

            }

    public ArrayList<String> getActiveReplicas() {

        return activeReplicas;

    }
    
    private double getTimeStamp(){
        Calendar cal = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        cal.getTime();
        return  Double.parseDouble(sdf.format(cal.getTime()).replace(":", ""));
        
    }

    private String getNextLeader(String currentLeader) {

        int index = 0;
        double highest = 0;
        // loop through all ip addresses, converting to doubles for value comparison
        // reuturn the highest found, do not include current leader as this has crashed
        gui.addStringAndUpdate("loop for - " + activeReplicas.size());
        for (int i = 0; i < activeReplicas.size(); i++) {
            gui.addStringAndUpdate("for loop" + i);

            if (getDoubleIPAddress(activeReplicas.get(i)) > highest && !currentLeader.matches(activeReplicas.get(i))) {
                gui.addStringAndUpdate("if");
                highest = getDoubleIPAddress(activeReplicas.get(i));
                index = i;

            }

        }
        gui.addStringAndUpdate("next leader found");
        return activeReplicas.get(index);

    }

    private String getFirstLeader(String ips[]) {

        int index = 0;
        double highest = 0;
        // as in getNextLeader(), find highest ip in list, without any restrictions
        for (int i = 0; i < ips.length; i++) {

            System.out.println(ips[i]);
            if (getDoubleIPAddress(ips[i]) > highest) {

                highest = getDoubleIPAddress(ips[i]);
                index = i;

            }

        }

        return ips[index];

    }

    private void connectToRunnerUp() throws RemoteException, NotBoundException {

        // get next highest leader and connect to it
        claimReplicas(noOfPartitions, noOfReplicas);
        gui.addStringAndUpdate("1");
        gui.addStringAndUpdate(currentLeaderIp);
        String temp = getNextLeader(currentLeaderIp);
        currentLeaderIp = temp;
        gui.addStringAndUpdate("2");
        connectServer(currentLeaderIp);
        gui.addStringAndUpdate("3");
        if (currentLeaderIp.matches(myIP)) {

            gui.addStringAndUpdate("This is the new leader");
            isLeader = true;


        } else {

            gui.addStringAndUpdate("New leader is - " + currentLeaderIp);
            isLeader = false;


        }

    }

    public boolean connectServer(String ipaddress) throws RemoteException, NotBoundException {

        try {
            // complete the RMI connet flow, return true on success
            Registry reg = LocateRegistry.getRegistry(ipaddress, 1099);
            rmi = (RMI) reg.lookup("server");
            gui.addStringAndUpdate("rmi found");
            return true;

        } catch (RemoteException | NotBoundException e) {

            gui.addStringAndUpdate(e.getMessage());
            return false;

        }
    }

    public static String getMyIp() throws MalformedURLException, IOException {

        URL whatismyip = new URL("http://checkip.amazonaws.com/");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        //you get the IP as a String
        String ip = in.readLine();
        return ip;

    }

    public String getCurrentLeaderIp() {
        return currentLeaderIp;
    }

    public void setCurrentLeaderIp(String currentLeaderIp) throws RemoteException, NotBoundException {
        
        this.currentLeaderIp = currentLeaderIp;
        connectServer(currentLeaderIp);
        this.isLeader = false;
        
    }

    private double getDoubleIPAddress(String ip) {

        return Double.parseDouble(ip.replace(".", ""));

    }

}
