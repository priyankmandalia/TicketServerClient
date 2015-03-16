/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import rmi.RMI;

/**
 *
 * @author up705759
 */
public class ElectionManager {

    private RMI rmi;
    private String currentLeaderIp;
    boolean heartbeat = true;
    private final String ipaddresses[];
    private final boolean replicaOrPartition;

    public ElectionManager(String[] ipaddresses, boolean replicaOrPartition) throws RemoteException, NotBoundException, MalformedURLException, IOException, InterruptedException {

        this.ipaddresses = ipaddresses;
        this.replicaOrPartition = replicaOrPartition;
        // get the highest ip address and set as initial leader
        this.currentLeaderIp = getFirstLeader();

        go();
    }

    private void go() throws RemoteException, NotBoundException, IOException, InterruptedException {

        Thread t = new Thread(new Runnable() {

            int serversAlive;

            @Override
            public void run() {
                try {
                    
                    // get own IP
                    String myIP = getMyIp();
                    // loop through all other ip's, connect and compare own ip with their leader ip
                    // to check if this server needs to bully
                    for (String ip : ipaddresses) {

                        if (!ip.matches(myIP)) {

                            if (connectServer(ip)) {

                                serversAlive++;
                                String comparedLeader = rmi.agreeLeader(myIP, replicaOrPartition);
                                if (!currentLeaderIp.matches(comparedLeader) && !myIP.matches(comparedLeader)) {

                                    currentLeaderIp = comparedLeader;

                                }
                            }
                        }
                    }
                    // check if this leader is or has become the leader
                    // if so, stop the the heartbeat as this is only for 
                    // ensuring leader is responding
                    if (serversAlive == 0) {

                        // election has gone wrong, this becomes leader
                        System.out.println("Error, couldnt find leader");
                        System.out.println("This server has assumed leader");
                        currentLeaderIp = myIP;
                        heartbeat = false;

                    } else if (currentLeaderIp.matches(myIP)) {

                        // this is the leader
                        heartbeat = false;
                        System.out.println("This Server is leader");

                    }
                    //loop forever, if not leader continuously check if leader is alive
                    // if leader, dont check but stay in loop incase of bully situation
                    while (true) {

                        if (heartbeat) {

                            try {

                                // check if leader is alive 
                                if (rmi.isRunning()) {

                                    System.out.println("Leader " + currentLeaderIp + " is running");

                                }
                                // every 2 seconds
                                Thread.sleep(2000);

                            } catch (RemoteException ex) {

                                try {
                                    // leader has crashed, connection to leader raised exception
                                    // connect to next highest ip
                                    connectToRunnerUp();
                                    System.out.println("Leader crashed");
                                } catch (RemoteException ex1) {
                                    Logger.getLogger(ElectionManager.class.getName()).log(Level.SEVERE, null, ex1);
                                } catch (NotBoundException ex1) {
                                    Logger.getLogger(ElectionManager.class.getName()).log(Level.SEVERE, null, ex1);
                                }

                            } catch (InterruptedException ex) {

                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        });
        // start thread defined above
        t.start();

    }

    private String getNextLeader(String currentLeader) {

        int index = 0;
        double highest = 0;
        // loop through all ip addresses, converting to doubles for value comparison
        // reuturn the highest found, do not include current leader as this has crashed
        for (int i = 0; i < ipaddresses.length; i++) {

            if (getDoubleIPAddress(ipaddresses[i]) > highest && !currentLeader.matches(ipaddresses[i])) {

                highest = getDoubleIPAddress(ipaddresses[i]);
                index = i;

            }

        }
        return ipaddresses[index];

    }

    private String getFirstLeader() {

        int index = 0;
        double highest = 0;
        // as in getNextLeader(), find highest ip in list, without any restrictions
        for (int i = 0; i < ipaddresses.length; i++) {

            if (getDoubleIPAddress(ipaddresses[i]) > highest) {

                highest = getDoubleIPAddress(ipaddresses[i]);
                index = i;

            }

        }

        return ipaddresses[index];

    }

    private void connectToRunnerUp() throws RemoteException, NotBoundException {
        
        // get next highest leader and connect to it
        currentLeaderIp = getNextLeader(currentLeaderIp);
        connectServer(currentLeaderIp);

    }

    public boolean connectServer(String ipaddress) throws RemoteException, NotBoundException {

        try {
            // complete the RMI connet flow, return true on success
            Registry reg = LocateRegistry.getRegistry(ipaddress, 1099);
            rmi = (RMI) reg.lookup("server");
            System.out.println("rmi found");
            return true;

        } catch (RemoteException | NotBoundException e) {

            System.out.println(e.getMessage());
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

    public boolean isHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(boolean heartbeat) {
        this.heartbeat = heartbeat;
    }

    public String getCurrentLeaderIp() {
        return currentLeaderIp;
    }

    public void setCurrentLeaderIp(String currentLeaderIp) {
        this.currentLeaderIp = currentLeaderIp;
    }

    private double getDoubleIPAddress(String ip) {

        return Double.parseDouble(ip.replace(".", ""));

    }

}
