

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author up705759
 */
public class RMIClient extends JFrame {
    
    JList listbox;
    JList addresslist;
    static RMI rmi;
    static ArrayList<String> listData;
    static ArrayList<String> bookingData;
    private String chosenIpAddress;
    public static String[] leadServerIPs = {"109.157.191.99", "127.0.0.1"};
    public static String NewServer;
    private static String writeServer;
    private static String readServer;
     private static int i = -1;

    private static Map<String, Integer> map = new HashMap<>();

    public static void main(String args[]) throws RemoteException, NotBoundException {

        connectLowestLoadServer();
        rmi.notifyConnected();
        leadServerIPs = rmi.getIPaddresses();
        RMIClient obj = new RMIClient();

    }

    private static void connectLowestLoadServer() throws RemoteException, NotBoundException {

        int load = 0;
        int lowestLoad = -1;
        String lowestLoadServer = null;
        
        for (String leadServerIP : leadServerIPs) {
            
            try {
                
                connectServer(leadServerIP);
                
                load = rmi.getNumberOfConnections();
                
                if(load < lowestLoad || lowestLoad < 0){
            
                lowestLoad = load;
                lowestLoadServer = leadServerIP;
            
            }
                
            }catch (RemoteException ex) {
                
                System.out.println("Failed to connect to - " + leadServerIP);
                
            }
        }
        
        connectServer(lowestLoadServer);
        writeServer = lowestLoadServer;
        readServer = rmi.getReadServer();

    }
    
    private static void connectServer(String ip) throws RemoteException, NotBoundException {

//        Registry reg = LocateRegistry.getRegistry(ip, 1099);
//        rmi = (RMI) reg.lookup("server");
//        if (rmi.isRunning()) {
//
//            System.out.println("Connected to - " + ip);
//
//        }
        
        try {
                            System.out.println("Inside connectServer");
                            Registry reg = LocateRegistry.getRegistry(ip, 1099);
                            rmi = (RMI) reg.lookup("server");

                            if (rmi.isRunning()) {
                                System.out.println("Connected to - " + ip);
                               
                            }

                        } catch (RemoteException ex) {
                            try {
                                getRunnerUp();
                                System.out.println("Leader crashed");
                            } catch (RemoteException ex1) {
                                Logger.getLogger(RMIClient.class.getName()).log(Level.SEVERE, null, ex1);
                            } catch (NotBoundException ex1) {
                                Logger.getLogger(RMIClient.class.getName()).log(Level.SEVERE, null, ex1);
                            }

                        } 

    }
    
    private static void getRunnerUp() throws RemoteException, NotBoundException {
                
                NewServer = getNextServer();
                connectServer(NewServer);
                System.out.println("NewServer IP: "+NewServer);
                
    }

   private static String getNextServer(){
         
         
         if(i < leadServerIPs.length-1){
         i++;
         }
         else{
         i=0;
         }
         System.out.println("List length: "+leadServerIPs.length);
         System.out.println("Index: "+i);
         String s = leadServerIPs[i];
         
         
         
     
     
     return s;
     }

    //Adds amount of loads of all the server in hashmap
    private static void getnumberOfClientsConn() throws RemoteException, NotBoundException {
        int count;
        for (int i = 0; i < leadServerIPs.length - 1; i++) {
            String s = leadServerIPs[i];
            connectServer(s);
            count = rmi.getNumberOfConnections();
            System.out.println("Server " + i + ": " + s);
            System.out.println("Number of Connections: " + rmi.getNumberOfConnections());

            map.put(s, count);

        }

    }

    //Get Server IP with least amount of load

    private static String getServerWithLeastLoad() {

        String ServerIP = "";
        Integer min = 0;
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (min.compareTo(e.getValue()) > 0) {
                ServerIP = e.getKey();
                min = e.getValue();
            }
        }
        System.out.println("Server with least load: " + ServerIP);

        return ServerIP;//returns server IP with least load

    }

    public RMIClient() throws RemoteException, NotBoundException {

        initTabs();

        this.setSize(1000, 500);
        this.setVisible(true);
        //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
    
    public JPanel clientUI() throws RemoteException, NotBoundException {

        //   JPanel emptypanel;
        Color purple = new Color(78, 49, 104);
        Color green = new Color(85, 222, 127);
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 15);

        //     Container pane;
        final JTextField tickets;
        final JTextField customer;
        final JTextField searchfield;
        final JList bookings;
        JButton search;
        JButton book;
        JButton showall;

        book = new JButton();
        book.setText("Book");
        book.setForeground(Color.white);
        book.setBackground(green);
        book.setFont(font);
        book.setPreferredSize(new Dimension(130, 40));

        search = new JButton();
        search.setText("Search");
        search.setForeground(Color.white);
        search.setBackground(green);
        search.setFont(font);
        search.setPreferredSize(new Dimension(100, 40));

        showall = new JButton();
        showall.setText("Show All");
        showall.setForeground(Color.white);
        showall.setBackground(green);
        showall.setFont(font);
        showall.setPreferredSize(new Dimension(130, 40));

        JPanel topLevelPanel = new JPanel();
        topLevelPanel.setBackground(purple);
        topLevelPanel.setLayout(new BoxLayout(topLevelPanel, BoxLayout.PAGE_AXIS));

        bookings = new JList();
        //     searchfield.setInputPrompt("Hint"); 
        bookings.setFont(font);
        //   bookings.setUI(new JTextFieldHintUI("Bookings", Color.lightGray));
        bookings.setPreferredSize(new Dimension(200, 200));

        searchfield = new JTextField();
        //     searchfield.setInputPrompt("Hint"); 
        searchfield.setFont(font);
        searchfield.setUI(new JTextFieldHintUI("Event name", Color.lightGray));
        searchfield.setPreferredSize(new Dimension(400, 40));

        tickets = new JTextField();
        tickets.setFont(font);
        tickets.setUI(new JTextFieldHintUI("no", Color.lightGray));
        tickets.setPreferredSize(new Dimension(100, 40));
        tickets.setColumns(3);

        customer = new JTextField();
        customer.setFont(font);
        customer.setUI(new JTextFieldHintUI("Customer name", Color.lightGray));
        customer.setPreferredSize(new Dimension(350, 40));

        JPanel searcheventpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searcheventpanel.setBackground(purple);
        searcheventpanel.setSize(300, 100);

        JPanel buttonpanle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonpanle.setBackground(purple);
        buttonpanle.setSize(100, 100);

        JPanel customerpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        customerpanel.setBackground(purple);
        customerpanel.setSize(500, 100);

        final JPanel textpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        textpanel.setBackground(purple);
        textpanel.setSize(200, 380);
        connectServer(readServer);
        listData = rmi.getEvents();

//        String listData[] = {
//                "Football Match",
//                "Rock Concert ",
//                "Movie Preview",
//                "Cricket Match"
//            };
        listbox = new JList(listData.toArray());
        //     listbox = new JList(listData);
        listbox.setBackground(Color.white);
        listbox.setFont(font);
        listbox.setPreferredSize(new Dimension(200, 200));
        listbox.setFixedCellWidth(400);

        ListSelectionListener listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {

                JList list = (JList) listSelectionEvent.getSource();

                bookings.setListData(new Object[0]);
                try {
                    connectServer(readServer);
                    bookingData = rmi.getBookings(list.getSelectedValue().toString());
                } catch (RemoteException e1) {
                    // TODO Auto-generated catch block
                    System.out.println(e1.getMessage());
                } catch (NotBoundException ex) {
                    Logger.getLogger(RMIClient.class.getName()).log(Level.SEVERE, null, ex);
                }

                DefaultListModel model = new DefaultListModel();

                for (String bookingdata : bookingData) {
                    model.addElement(bookingdata); // <-- Add item to model
                }
                bookings.setModel(model);

            }

        };
        listbox.addListSelectionListener(listSelectionListener);

        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = searchfield.getText();

                try {
                    connectServer(readServer);
                    ArrayList<String> events = rmi.searchEvents(s);
                    System.out.println(events);

                    listbox.setListData(new Object[0]);

                    DefaultListModel model = new DefaultListModel();

                    for (String event : events) {
                        model.addElement(event); // <-- Add item to model
                    }
                    listbox.setModel(model);

                } catch (RemoteException e1) {
                    System.out.println(e1.getMessage());
                    
                } catch (NotBoundException ex) {
                    Logger.getLogger(RMIClient.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        showall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                listbox.setListData(new Object[0]);
                try {
                    connectServer(readServer);
                    listData = rmi.getEvents();
                } catch (RemoteException e1) {
                    // TODO Auto-generated catch block
                    System.out.println(e1.getMessage());
                } catch (NotBoundException ex) {
                    Logger.getLogger(RMIClient.class.getName()).log(Level.SEVERE, null, ex);
                }

                DefaultListModel model = new DefaultListModel();

                for (String listdata : listData) {
                    model.addElement(listdata); // <-- Add item to model
                }
                listbox.setModel(model);

            }
        });

        book.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    connectServer(writeServer);
                    rmi.book(listbox.getSelectedValue().toString(), customer.getText(), Integer.parseInt(tickets.getText()));
                } catch (RemoteException e1) {
                    // TODO Auto-generated catch block
                    System.out.println(e1.getMessage());
                } catch (NotBoundException ex) {
                    Logger.getLogger(RMIClient.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        searcheventpanel.add(searchfield);
        searcheventpanel.add(search);

        textpanel.add(listbox);
        textpanel.add(bookings);
        buttonpanle.add(showall);
        buttonpanle.add(book);
        customerpanel.add(customer);
        customerpanel.add(tickets);

        topLevelPanel.add(searcheventpanel);
        topLevelPanel.add(textpanel);
        topLevelPanel.add(customerpanel);
        topLevelPanel.add(buttonpanle);

        return topLevelPanel;
    }

    public JPanel clientUIBussiness() throws RemoteException {

        //   JPanel emptypanel;
        Color purple = new Color(78, 49, 104);
        Color orange = new Color(222, 146, 85);
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 15);

        //   JList listbox;
        //   Container pane;
        final JTextField eventname;
        final JTextField description;
        JButton addevent;

        addevent = new JButton();
        addevent.setText("Add Event");
        addevent.setBackground(orange);
        addevent.setForeground(Color.white);
        addevent.setFont(font);
        addevent.setPreferredSize(new Dimension(130, 40));
        JPanel topLevelPanel = new JPanel();
        topLevelPanel.setBackground(purple);
        topLevelPanel.setLayout(new BoxLayout(topLevelPanel, BoxLayout.PAGE_AXIS));

        eventname = new JTextField();
        eventname.setFont(font);
        eventname.setUI(new JTextFieldHintUI("Event name", Color.lightGray));
        eventname.setPreferredSize(new Dimension(400, 40));

        description = new JTextField();
        description.setFont(font);
        description.setUI(new JTextFieldHintUI("Description", Color.lightGray));
        description.setPreferredSize(new Dimension(500, 250));
        //  tickets.setColumns(3);
        JPanel buttonpanle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonpanle.setBackground(purple);
        buttonpanle.setSize(100, 100);

        JPanel textpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        textpanel.setBackground(purple);
        textpanel.setSize(600, 380);

        JPanel descriptionpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        descriptionpanel.setBackground(purple);
        descriptionpanel.setSize(600, 380);
           //ArrayList<String> listData = rmi.getEvents();

        addevent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                  //  getnumberOfClientsConn();
                  //  String IP = getServerWithLeastLoad();
                  //  connectServer(IP);                   //Connects with server with least amount of load
                    connectServer(writeServer);
                    rmi.addEvent(eventname.getText(), description.getText());
                } catch (RemoteException e1) {
                    // TODO Auto-generated catch block
                    System.out.println(e1.getMessage());
                } catch (NotBoundException ex) {
                    Logger.getLogger(RMIClient.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        textpanel.add(eventname);
        textpanel.add(addevent);
        descriptionpanel.add(description);
        //      buttonpanle.add(book);

        topLevelPanel.add(textpanel);
        topLevelPanel.add(descriptionpanel);
    //       topLevelPanel.add(buttonpanle);

        return topLevelPanel;
    }

    public JPanel configureUI() throws RemoteException {

        //   JPanel emptypanel;
        Color purple = new Color(78, 49, 104);
        Color green = new Color(85, 222, 127);
        Color orange = new Color(222, 146, 85);
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 15);

        //     Container pane;
        JButton connect;
        final JList bookings;

        JPanel topLevelPanel = new JPanel();
        topLevelPanel.setBackground(purple);
        topLevelPanel.setLayout(new BoxLayout(topLevelPanel, BoxLayout.PAGE_AXIS));

        JPanel buttonpanle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonpanle.setBackground(purple);
        buttonpanle.setSize(100, 100);

        connect = new JButton();
        connect.setText("Connect");
        connect.setBackground(orange);
        connect.setForeground(Color.white);
        connect.setFont(font);
        connect.setPreferredSize(new Dimension(130, 40));

        connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //To Do
                    connectServer(chosenIpAddress);
                    listData = rmi.getEvents();
                } catch (RemoteException | NotBoundException ex) {
                    Logger.getLogger(RMIClient.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        bookings = new JList();
        //     searchfield.setInputPrompt("Hint"); 
        bookings.setFont(font);
        //   bookings.setUI(new JTextFieldHintUI("Bookings", Color.lightGray));
        bookings.setPreferredSize(new Dimension(400, 200));

        final JPanel textpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        textpanel.setBackground(purple);
        textpanel.setSize(200, 380);
   //   listData = rmi.getServers();//use this in future

//        String listIP[] = {
//                "127.0.0.1",
//                "148.197.27.153",
//                "148.197.27.154",
//                "148.197.27.155",
//                "148.197.27.156"
//            };
        addresslist = new JList(leadServerIPs);
        //     listbox = new JList(listData);
        addresslist.setBackground(Color.white);
        addresslist.setFont(font);
        addresslist.setPreferredSize(new Dimension(400, 200));
        addresslist.setFixedCellWidth(400);

        ListSelectionListener listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {

                JList list = (JList) listSelectionEvent.getSource();
                chosenIpAddress = list.getSelectedValue().toString();

            }

        };
        addresslist.addListSelectionListener(listSelectionListener);

        textpanel.add(addresslist);
        buttonpanle.add(connect);

        topLevelPanel.add(textpanel);
        topLevelPanel.add(buttonpanle);

        return topLevelPanel;
    }

    private void initTabs() throws RemoteException, NotBoundException {
        Color green = new Color(85, 222, 127);
        Color orange = new Color(222, 146, 85);
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Configure", null, configureUI(), "Settings");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        tabbedPane.addTab("Client", null, clientUI(), "Book Events");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        tabbedPane.addTab("Business", null, clientUIBussiness(), "Add Events");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        tabbedPane.setBackground(Color.white);

        getContentPane().add(tabbedPane);

    }

}
