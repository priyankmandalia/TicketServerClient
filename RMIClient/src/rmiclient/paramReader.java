package rmiclient;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author up662319
 */
public class paramReader {

    Document servers;
    
    public paramReader(String nameOfServersFile) throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
        
        File file = new File(nameOfServersFile);
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	
        
        
	servers = dBuilder.parse(file);
        
    }
    
 
    
    public String[] getServers(){
        
        Node nNode;
        Element eElement;
        ArrayList<String> result = new ArrayList<>();
        NodeList nList = servers.getElementsByTagName("server");
        for(int i = 0; i < nList.getLength(); i++){
        
            nNode = nList.item(i);
            eElement = (Element) nNode;
            result.add(eElement.getElementsByTagName("ip").item(0).getTextContent());
//            System.out.println(" id : " + eElement.getAttribute("id"));
//            System.out.println("ip : " + eElement.getElementsByTagName("ip").item(0).getTextContent());
        
        }
        
        String[] temp = new String[result.size()];
        temp = result.toArray(temp);
        for(int i=0; i<result.size(); i++){
        
            temp[i] = result.get(i);
        
        }
            
        
        return temp;
        
    }
    
    
    
}
