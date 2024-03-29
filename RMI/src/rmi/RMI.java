package rmi;

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
        
        public ArrayList<String> getEventTitles() throws RemoteException;
        
        public ArrayList<String> serverGetEventTitles() throws RemoteException;
        
        public ArrayList<Event> getEvents() throws RemoteException;
        
        public ArrayList<Event> serverGetEvents() throws RemoteException;
        
        public ArrayList<String> getServers() throws RemoteException;
        
        public ArrayList<String> getBookings(String event) throws RemoteException;
        
        public boolean book(String event, String customer, int amount) throws RemoteException, NotBoundException;
        
        public boolean addEvent(String name, String description) throws RemoteException, NotBoundException;
        
        public void replicate(ArrayList<Event> latest) throws RemoteException;
        
        public boolean isRunning() throws RemoteException;
        
        public String agreeLeader(String senderIP) throws RemoteException;
        
        public String[] getIPaddresses() throws RemoteException;
        
        public int getNumberOfEvents() throws RemoteException;
        
        public int getNumberOfConnections() throws RemoteException;
        
        public void tellConnected(String whosconnected) throws RemoteException;
        
        public void notifyDisconnected() throws RemoteException;
        
        public String getReadServer() throws RemoteException;
        
        public String getWriteServer() throws RemoteException;
        
        public boolean claimAsReplica(String ip) throws RemoteException;
        
        public Event getEventByExactName(String event) throws RemoteException;
        
        public ArrayList<String> getActiveReplicas() throws RemoteException;
        
        
}

