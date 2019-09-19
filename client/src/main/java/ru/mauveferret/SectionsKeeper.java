package ru.mauveferret;

import java.util.HashMap;

public class SectionsKeeper {

     private HashMap<String, SocketCryptedCommunicator>  sections = new HashMap<>();
     public static final String VACUUM = "vacuum";
     public static final String DISCHARGE = "discharge";
     public static final String DIAGNOSTICS = "diagnostics";

     public void addSection(String sectionType, String host, String port ) throws Exception
     {
         int port1 = Integer.parseInt(port);
         sections.put(sectionType, new SocketCryptedCommunicator(host,port1));
     }

     public SocketCryptedCommunicator getCommunicator (String sectionType)
     {
         return sections.get(sectionType);
     }
}
