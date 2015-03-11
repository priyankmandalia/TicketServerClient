
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import javax.swing.JFrame;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author up662319
 */
public interface RMI extends Remote{
    
        public static final boolean REPLICA = true;
        public static final boolean PARTITION = false;
    
        public String getData(String text) throws RemoteException;
        
        public ArrayList<String> searchEvents(String query) throws RemoteException;
        
        public ArrayList<String> getEvents() throws RemoteException;
        
        public ArrayList<String> getServers() throws RemoteException;
        
        public ArrayList<String> getBookings(String event) throws RemoteException;
        
        public boolean book(String event, String customer, int amount) throws RemoteException;
        
        public boolean addEvent(String name, String description) throws RemoteException, NotBoundException;
        
        public void replicate(ArrayList<Event> latest) throws RemoteException;
        
        public boolean isRunning() throws RemoteException;
        
        public String agreeLeader(String senderIP, boolean replicaOrPartition) throws RemoteException;
        
        public String[] getIPaddresses() throws RemoteException;
        
        public int getNumberOfEvents() throws RemoteException;
        
         public int getNumberOfConnections() throws RemoteException;
        
        
}

