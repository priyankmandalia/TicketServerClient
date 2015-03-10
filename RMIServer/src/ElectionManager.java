/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author up705759
 */
public class ElectionManager implements RMI{
    private RMI rmi;
    public String currentLeaderIp;

    public String getCurrentLeaderIp() {
        return currentLeaderIp;
    }

    public void setCurrentLeaderIp(String currentLeaderIp) {
        this.currentLeaderIp = currentLeaderIp;
    }
    private final String ipaddresses[];
    private boolean isLeader;
    
    public ElectionManager( String[] ipaddresses) throws RemoteException, NotBoundException, MalformedURLException, IOException, InterruptedException{
        
        this.ipaddresses = ipaddresses;
        // get the highest ip address and set as initial leader
        this.currentLeaderIp = getFirstLeader();
        // get own IP
        String myIP = getMyIp();
        // loop through all other ip's, connect and compare own ip with their leader ip
        // to check if this server needs to bully
        for(String ip : ipaddresses){
            
            if(!ip.matches(myIP)){
                
                while(!connectServer(ip)){
                
                    Thread.sleep(500);
                    System.out.println("waiting to connect");
                
                }
                String comparedLeader = rmi.agreeLeader(myIP);
                if (!this.currentLeaderIp.matches(comparedLeader)) {

                    this.currentLeaderIp = comparedLeader;

                }
            
            }
            
        }
         
        if(this.currentLeaderIp == null){ 
            
            // election has gone wrong
            System.out.println("Error, couldnt find leader");
            
        
        }else if(this.currentLeaderIp.matches(myIP)){
        
            // this is the leader
            System.out.println("This Server is leader");
            System.out.println("Leader is - " + currentLeaderIp);
        
        }else{  
            
            // election succeeded, this is not leader
            // start heartbeat
            checkLeader();
        
        } 
        
    }
    
    private double getDoubleIPAddress(String ip){
          
        return Double.parseDouble(ip.replace(".", ""));
    
    }
    
    @Override
    public String agreeLeader(String senderIP){
        
        return "only agreeleader in rmiserver should be called";
        
    
    }
    
    private String getNextLeader(String currentLeader){
    
        int index = 0;
        double highest = 0;
        
        for(int i = 0; i < ipaddresses.length; i++){
        
            if(getDoubleIPAddress(ipaddresses[i]) > highest && !currentLeader.matches(ipaddresses[i])){
            
                highest = getDoubleIPAddress(ipaddresses[i]);
                index = i;
            
            }
        
        }
        return ipaddresses[index];
    
    }
    
    private String getFirstLeader() {
    
        int index = 0;
        double highest = 0;
        
        for(int i = 0; i < ipaddresses.length; i++){
        
            if(getDoubleIPAddress(ipaddresses[i]) > highest){
            
                highest = getDoubleIPAddress(ipaddresses[i]);
                index = i;
            
            }
        
        }
        
             
        return ipaddresses[index];
    
    }
    
    private void checkLeader() throws RemoteException, NotBoundException{
    
        System.out.println("conneting to - " + currentLeaderIp);
        connectServer(currentLeaderIp);
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                while(true){
                
                    try {
                        
                        if(rmi.isRunning()){
                    
                            System.out.println("Leader is running");
                    
                        }
                        
                        Thread.sleep(2000);
                        
                        } catch (RemoteException ex) {
                        try {                    
                            startElection();
                            System.out.println("Leader crashed");
                        } catch (RemoteException ex1) {
                            Logger.getLogger(ElectionManager.class.getName()).log(Level.SEVERE, null, ex1);
                        } catch (NotBoundException ex1) {
                            Logger.getLogger(ElectionManager.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    
                        } catch(InterruptedException ex){
                            
                        
                        }
                    
                
                }
                
                
            }

           
        });
        t.start();
    
    }
    
    private void startElection() throws RemoteException, NotBoundException {
                
                currentLeaderIp = getNextLeader(currentLeaderIp);
                connectServer(currentLeaderIp);
                if(rmi.isRunning()){
                    System.out.println("Connected to " + currentLeaderIp);
                    Logger.getLogger(ElectionManager.class.getName()).log(Level.FINE, null, "Connected to " + currentLeaderIp);
                
                }
                
    }
    
    private boolean connectServer(String ipaddress) throws RemoteException, NotBoundException {
        
        try {
            
            Registry reg = LocateRegistry.getRegistry(ipaddress, 1099);
            rmi = (RMI) reg.lookup("server");
            System.out.println("reg.lookup");
            return true;
        
        } catch(RemoteException | NotBoundException e){
        
            return false;
        
        }
    }
    
    public static String getMyIp() throws MalformedURLException, IOException{
    
        URL whatismyip = new URL("http://checkip.amazonaws.com/");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

        String ip = in.readLine(); //you get the IP as a String
        return ip;
    
    }
    //****************RMI******************
    @Override
    public String getData(String text) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<String> searchEvents(String query) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<String> getEvents() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<String> getServers() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ArrayList<String> getBookings(String event) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean book(String event, String customer, int amount) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addEvent(String name, String description) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void replicate(ArrayList<Event> latest) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRunning() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
