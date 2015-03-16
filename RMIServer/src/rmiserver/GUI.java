package rmiserver;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
 
public class GUI extends JFrame
{
    static RMIServer server;
    
    Container container;
    String frameTitle;
    
    TextArea logText;
    
    
    Color purple = new Color(78,49,104);
    Font font = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    
    
    
    public GUI(String title){
        
        
        this.setTitle(title);
        
        
        initComponents();
        
       
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(600, 500));
        this.pack();
        this.setVisible(true);
  
        addStringAndUpdate("RMI Server and GUI Started");
        
    }
    
    public void addStringAndUpdate(String input){
    
        logText.setForeground(Color.white);
        logText.setFont(font);
    	logText.setText(logText.getText() + getTimeStamp() + input + "\n" );
        logText.setCaretPosition( logText.getText().length());
    
    }
    
    private String getTimeStamp(){
        Calendar cal = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        cal.getTime();
        return "<" + sdf.format(cal.getTime()) + "> ";
        
    }

    private void initComponents() {
        
        this.container = this.getContentPane();
        
        logText = new TextArea(5, 20);
        logText.setBackground(purple);
        logText.setEditable(false);
        
        this.container.add(logText, BorderLayout.CENTER);
        
         
    }
}


