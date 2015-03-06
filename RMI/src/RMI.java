
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
    
        public String getData(String text) throws RemoteException;
        
        public ArrayList<String> searchEvents(String query) throws RemoteException;
        
        public ArrayList<String> getEvents() throws RemoteException;
        
        public ArrayList<String> getServers() throws RemoteException;
        
        public ArrayList<String> getBookings(String event) throws RemoteException;
        
        public boolean book(String event, String customer, int amount) throws RemoteException;
        
        public boolean addEvent(String name, String description) throws RemoteException;
        
        public void replicate(ArrayList<Event> latest) throws RemoteException;
        
        public boolean isRunning() throws RemoteException;
        
        public String backonline(String s) throws RemoteException;
        
        
}

