package CONVERTER;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.InetAddress;
import java.time.LocalDateTime;

import DATA.D_Attributes;
import DATA.D_Segments;
import GUI.G_Bottom;
import GUI.G_Center;
import GUI.G_Top;
import javafx.application.Platform;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import DATA.D_PortAttributes;
import DATA.D_StreamBlockAttributes;

public class C_Converter {

    private static D_Segments Segments = new D_Segments();
    private static D_Attributes Attributes;

    public void parse(boolean print2file) {

        try {
            Attributes = new D_Attributes();
            File fXmlFile = new File(G_Top.path_txtfld.getText());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document RootDocument = dBuilder.parse(fXmlFile);
            RootDocument.getDocumentElement().normalize();

            NodeList port_List = RootDocument.getElementsByTagName("Port");

            //==============Per Port Loop===============================================================================================================
            for (int port_index = 0; port_index < port_List.getLength(); port_index++) {
                print2dbglog("========================< Port:"+port_index+" >========================\n");
                //=====Creating a Document out of XML nodes so it can be search via Elements
                Document PortDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element root = PortDocument.createElement("root");
                PortDocument.appendChild(root);
                for (int port_childs_index = 0; port_childs_index < port_List.item(port_index).getChildNodes().getLength(); port_childs_index++) {
                    Node port_List_node = port_List.item(port_index).getChildNodes().item(port_childs_index);
                    Node copyNode = PortDocument.importNode(port_List_node, true);
                    root.appendChild(copyNode);
                }

                NodeList streamblock_List = PortDocument.getElementsByTagName("StreamBlock");

                Element genConfig_eElement=(Element) PortDocument.getElementsByTagName("Generator").item(0).getChildNodes().item(1);

                Attributes.addPortAttributes(genConfig_eElement);

                Element port_eElement = (Element) port_List.item(port_index);
                String log_portName=port_eElement.getAttribute("Name");
                int finalPort_index = port_index;
                print2xmllog("~~~~~~~~~~~~~[ Port #"+ finalPort_index +" ]~~~~~~~~~~~~~~~~~~~~~"+"\n");
                print2xpclog(";~~~~~~~~~~~~~[ Port #"+ finalPort_index +" ]~~~~~~~~~~~~~~~~~~~~~"+"\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");

                D_PortAttributes.analyzePortRate(Attributes.PortList.get(port_index),finalPort_index,print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");

                print2xpclog("P_COMMENT "+"\""+log_portName+"\"\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");;

                String indices="PS_INDICES";
                for (int stream_index = 0; stream_index < streamblock_List.getLength(); stream_index++) {indices+=" "+stream_index;}
                String finalIndices = indices;
                print2xpclog(finalIndices +"\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");


                //==============Per Stream under each port Loop===============================================================================================================
                for (int stream_index = 0; stream_index < streamblock_List.getLength(); stream_index++) {

                    String comment_out_prefix=new String("");

                    Node streamblock_List_Node = streamblock_List.item(stream_index);

                    if (streamblock_List_Node.getNodeType() == Node.ELEMENT_NODE) {

                        Element streamblock_eElement = (Element) streamblock_List_Node;
                        int i=stream_index;
                        Attributes.PortList.get(port_index).addStreamBlockAttributes(streamblock_eElement);
                        print2dbglog("Stream:"+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).name+"\n");

                        //<============Create a root node based on frame config string==========
                        DocumentBuilderFactory db2Factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder d2Builder = db2Factory.newDocumentBuilder();
                        InputSource is = new InputSource();
                        is.setCharacterStream(new StringReader(Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).frame_config));
                        Document frame_config_Document = d2Builder.parse(is);
                        frame_config_Document.getDocumentElement().normalize();
                        Node frame_config_root_node=frame_config_Document.getFirstChild();
                        /*/======================================================================>

                        if (!Segments.isSupportedSegments(frame_config_root_node)){
                            String log_stream_name=new String(Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).name);
                            print2dbglog(" ! [WARNING] Unsupported Stream Protocol Segments = ");
                            Segments.debug_print_pdus(frame_config_root_node);
                            comment_out_prefix=new String(";");
                        }*/

                        //==============Adding Modifiers Data==
                        D_StreamBlockAttributes.addModifiersAttributes(Attributes.PortList.get(port_index).StreamBlockList.get(stream_index),streamblock_eElement.getChildNodes());

                        //=========================================================================================================================================================================================================
                        //       Setting Values for Stream Parameters
                        //=========================================================================================================================================================================================================
                        Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.build_segments_strings(frame_config_root_node);
                        String log_comment=comment_out_prefix;
                        //=========================================================================================================================================================================================================
                        //       LOG OUTPUT
                        //============================================================================================= Stream INTRO LOG OUTPUT ====================================================================================
                        print2xmllog("[ Stream #"+i+" ]----------------------------------"+"\n");
                        print2xpclog(";[ Stream #"+i+" ]----------------------------------"+"\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");
                        //============================================================================================= Stream Misc LOG OUTPUT ====================================================================================
                        print2xpclog(log_comment+"PS_ENABLE ["+i+"] ON\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");;
                        print2xpclog(log_comment+"PS_PACKETLIMIT ["+i+"] -1\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");
                        print2xpclog(log_comment+"PS_BURST ["+i+"] -1 100\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");
                        print2xpclog(log_comment+"PS_INSERTFCS ["+i+"] ON\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");
                        //============================================================================================= Stream Name LOG OUTPUT ====================================================================================
                        print2xmllog("Name="+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).name+"\n");
                        print2xpclog(log_comment+"PS_COMMENT ["+i+"]"+" \""+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).name+"\""+"\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");
                        //============================================================================================= Headers Protocols LOG OUTPUT ====================================================================================
                        print2xmllog("HEADERS="+ Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.STC_headers +"\n");
                        print2xpclog(log_comment+"PS_HEADERPROTOCOL  ["+i+"]"+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.XENA_headers+"\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");;
                        print2xpclog(log_comment+"PS_PACKETHEADER  ["+i+"] "+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.XENA_headers_hex+"\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");

                        //==== Stream Modifiers Count LOG OUTPUT ====//
                        print2xmllog("#MODIFIERS="+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).ModifierList.size()+"\n");
                        print2xpclog(log_comment+"PS_MODIFIERCOUNT ["+i+"] "+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).ModifierList.size()+"\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");

                        //==== Stream Payload LOG OUTPUT ====//
                        D_StreamBlockAttributes.analyzeStreamPayload(Attributes.PortList.get(port_index).StreamBlockList.get(stream_index),log_comment,stream_index,print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");

                        //==== Stream Sig LOG OUTPUT ====//
                        D_StreamBlockAttributes.analyzeStreamSignature(Attributes.PortList.get(port_index).StreamBlockList.get(stream_index),log_comment,stream_index,print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");

                        //===== Stream FRAME SIZE LOG OUTPUT =====//
                        D_StreamBlockAttributes.analyzeStreamFrameSize(Attributes.PortList.get(port_index).StreamBlockList.get(stream_index),log_comment,stream_index,print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");
                        //===== SRC.DST MAC LOG OUTPUT =====//
                        print2xmllog("SRC.MAC="+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.src_mac_l2+"\nDST.MAC="+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.dst_mac_l2+"\n");

                        //============================================================================================= VLANS (1,2) LOG OUTPUT ====================================================================================
                        if (Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.number_of_vlans(frame_config_root_node)==2){print2xmllog("VLAN1="+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.vlan1_id_l2+"\nVLAN2="+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.vlan2_id_l2+"\n");}
                        if (Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.number_of_vlans(frame_config_root_node)==1){print2xmllog("VLAN1="+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.vlan1_id_l2+"\n");}

                        //============================================================================================= IPv4/6 Gateway LOG OUTPUT ===================================================================================
                        String log_gw_ipv6_l3_formatted = new String("0x"+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.IPv6_2Hex(InetAddress.getByName(Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.gw_ipv6_l3).toString()));
                        print2xpclog(log_comment+"PS_IPV4GATEWAY ["+i+"] "+Attributes.PortList.get(port_index).StreamBlockList.get(stream_index).StreamBlockSegments.gw_ipv4_l3+"\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");
                        print2xpclog(log_comment+"PS_IPV6GATEWAY ["+i+"] "+log_gw_ipv6_l3_formatted+"\n",print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");

                        D_StreamBlockAttributes.analyzeModifierAttributes(Attributes.PortList.get(port_index).StreamBlockList.get(stream_index),stream_index,print2file,G_Top.path_txtfld.getText()+"_"+port_index+".xpc");
                    }
                }
            }
        } catch (Exception e) {
            print2dbglog("-----------------Parse Exception @ "+ LocalDateTime.now()+"-----------------\n");
            print2dbglog(e.getMessage()+"\n");
            print2dbglog("--------------------------------------------------------------------\n");
        }
    }

    public static void print2file(String message,String filePath){
        File file = new File(filePath);
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
                fw = new FileWriter(file.getAbsoluteFile(), true);
                bw = new BufferedWriter(fw);
                bw.write("; ------------------Created @"+LocalDateTime.now()+"-----------------\n");

            }
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);

            bw.write(message);
        }
        catch (IOException e2) {
                print2dbglog("-----------------IO Exception @ "+ LocalDateTime.now()+"-----------------\n");
                print2dbglog(e2.getMessage()+"\n");
                print2dbglog("--------------------------------------------------------------------\n");
        }
        finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException ex) {
                print2dbglog("-----------------IO Exception @ " + LocalDateTime.now() + "-----------------\n");
                print2dbglog(ex.getMessage() + "\n");
                print2dbglog("--------------------------------------------------------------------\n");
            }
        }
    }

    public static  void print2xmllog(String message){
        Platform.runLater(new Runnable() { @Override public void run() { G_Center.xmllog_txt.appendText(message);}});
    }
    public static  void print2xpclog(String message,boolean print2file,String filePath){
        Platform.runLater(new Runnable() { @Override public void run() { G_Center.xpclog_txt.appendText(message);}});
        if (print2file) print2file(message,filePath);
    }
    public static void print2dbglog(String message){
        Platform.runLater(new Runnable() { @Override public void run() { G_Bottom.debug_log_txt.appendText(message);}});
    }
}
