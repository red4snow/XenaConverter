package DATA;

import CONVERTER.C_Converter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class D_Segments {
    public static String src_mac_l2; //
    public static String dst_mac_l2;

    public static String vlan1_id_l2; //decimal
    public static String vlan1_pri_l2; //bits
    public static String vlan1_cfi_l2; //bit

    public static String vlan2_id_l2; //decimal
    public static String vlan2_pri_l2; //bits
    public static String vlan2_cfi_l2; //bit

    public static String src_ipv4_l3;
    public static String dst_ipv4_l3 ;
    public static String gw_ipv4_l3;
    public static String ttl_ipv4_l3;


    public static String src_ipv6_l3;
    public static String dst_ipv6_l3;
    public static String gw_ipv6_l3;

    public static String srcport_l4;
    public static String dstport_l4;

    public static String gtpv1_type;
    public static String gtpv1_length;
    public static String gtpv1_teid;

    public static String STC_headers;
    public static String XENA_headers;
    public static String XENA_headers_hex;

    public static ArrayList<D_Segment_Node> Segments_list;

    public D_Segments() {
        reset_eth_attributes();
        reset_ipv4_attributes();
        reset_ipv6_attributes();
        reset_udptcp_attributes();

        STC_headers = new String("");
        XENA_headers = new String("");
        XENA_headers_hex = new String("0x");

        gtpv1_type = new String("0");
        gtpv1_length = new String("8");
        gtpv1_teid = new String("0");
        Segments_list = new ArrayList<>();

    }

    public void addSegmentNode(String segment_name,int segment_length){
        D_Segment_Node tempSegment_node=new D_Segment_Node();
        tempSegment_node.length=segment_length;
        tempSegment_node.name=segment_name;
        Segments_list.add(tempSegment_node);

    }

    private void reset_eth_attributes(){
        src_mac_l2 = new String("00:10:94:00:00:02"); //
        dst_mac_l2 = new String("00:00:01:00:00:01");

        vlan1_id_l2 = new String("100"); //decimal
        vlan1_pri_l2 = new String("000"); //bits
        vlan1_cfi_l2 = new String("0"); //bit

        vlan2_id_l2 = new String("100"); //decimal
        vlan2_pri_l2 = new String("000"); //bits
        vlan2_cfi_l2 = new String("0"); //bit
    }
    private void reset_ipv4_attributes(){
        src_ipv4_l3 = new String("192.85.1.2");
        dst_ipv4_l3 = new String("192.0.0.1");
        gw_ipv4_l3 = new String("192.85.1.1");
        ttl_ipv4_l3 = new String("255");
    }
    private void reset_ipv6_attributes(){
        src_ipv6_l3 = new String("2000::2");
        dst_ipv6_l3 = new String("2000::1");
        gw_ipv6_l3 = new String("::");
    }
    private void reset_udptcp_attributes() {
        srcport_l4 = new String("1024");
        dstport_l4 = new String("1024");
    }
    /*
    public void debug_print_pdus(Node root_node) {
        int number_of_pdus=root_node.getFirstChild().getFirstChild().getChildNodes().getLength();
        for (int i = 0; i < number_of_pdus; i++) {
            Node PduNode = root_node.getFirstChild().getFirstChild().getChildNodes().item(i);
            String formatted_header_name = format_headersegment(PduNode.getAttributes().item(1).toString());
            C_Converter.print2dbglog(formatted_header_name+" ");
        }
        C_Converter.print2dbglog("\n");
    }*/

    public void build_segments_strings(Node root_node) {
        int number_of_pdus=root_node.getFirstChild().getFirstChild().getChildNodes().getLength();

        for (int i = 0; i < number_of_pdus; i++) {
            Node PduNode = root_node.getFirstChild().getFirstChild().getChildNodes().item(i);
            String formatted_header_name= new String(format_headersegment(PduNode.getAttributes().item(1).toString()));
            switch (formatted_header_name){
                case "EthernetII":
                    analyze_l2_pdu(root_node,i);
                    addSegmentNode(format_headersegment_prefix(PduNode.getAttributes().item(0).toString()),14);
                    if (number_of_vlans(root_node)==1) { addSegmentNode("Vlan" ,4); }
                    if (number_of_vlans(root_node)==2) {
                        addSegmentNode("Vlan" ,4);
                        addSegmentNode("Vlan1" ,4);
                    }
                    break;
                case "IPv4":
                    analyze_l3v4_pdu(root_node,i);
                    addSegmentNode(format_headersegment_prefix(PduNode.getAttributes().item(0).toString()),20);
                    break;
                case "IPv6":
                    analyze_l3v6_pdu(root_node,i);
                    addSegmentNode(format_headersegment_prefix(PduNode.getAttributes().item(0).toString()),40);
                    break;
                case "Udp":
                    analyze_l4_pdu(root_node,i);
                    addSegmentNode(format_headersegment_prefix(PduNode.getAttributes().item(0).toString()),8);
                    break;
                case "Tcp":
                    analyze_l4_pdu(root_node,i);
                    addSegmentNode(format_headersegment_prefix(PduNode.getAttributes().item(0).toString()),20);
                    break;
                case "Custom":
                    analyze_custom_pdu(root_node,i);
                    addSegmentNode(format_headersegment_prefix(PduNode.getAttributes().item(0).toString()),PduNode.getFirstChild().getFirstChild().getNodeValue().length());
                    break;
                case "GTPv1":
                    analyze_gtpv1_pdu(root_node,i);
                    addSegmentNode(format_headersegment_prefix(PduNode.getAttributes().item(0).toString()),12);
                    break;
                default:
                    C_Converter.print2dbglog(" ! [WARNING] Unsupported Stream Protocol Segment = "+formatted_header_name+"\n");
                    break;
            }
        }
    }


    public void analyze_l2_pdu(Node root_node,int current_inspected_index) {
        this.XENA_headers+=" ETHERNET";
        this.STC_headers+="ETHERNET";
        Node PduNode = root_node.getFirstChild().getFirstChild().getChildNodes().item(current_inspected_index);
        if(!(findValueRecursively(PduNode,"srcMac").isEmpty())){this.src_mac_l2=new String(findValueRecursively(PduNode,"srcMac"));}
        if(!(findValueRecursively(PduNode,"dstMac").isEmpty())){this.dst_mac_l2=new String(findValueRecursively(PduNode,"dstMac"));}
        XENA_headers_hex+=src_mac_l2.replace(":", "")+dst_mac_l2.replace(":", "");

        switch (number_of_vlans(root_node)){
            case 0: {
                if (isNextHeaderIPv4(root_node,current_inspected_index))  XENA_headers_hex+="0800";
                else if (isNextHeaderIPv6(root_node,current_inspected_index))  XENA_headers_hex+="08DD";
                else XENA_headers_hex+="FFFF";

                break;
            }
            case 1: {
                if(!(findValueRecursively(PduNode,"vlan_id1").isEmpty())){this.vlan1_id_l2=new String(findValueRecursively(PduNode,"vlan_id1"));}
                if(!(findValueRecursively(PduNode,"vlan_id1").isEmpty())){this.vlan1_pri_l2=new String(findValueRecursively(PduNode,"vlan_pri1"));}
                if(!(findValueRecursively(PduNode,"vlan_id1").isEmpty())){this.vlan1_cfi_l2=new String(findValueRecursively(PduNode,"vlan_cfi1"));}
                this.XENA_headers+=" VLAN";
                this.STC_headers+=" VLAN";
                String cfipri=String.format("%x",Integer.parseInt(vlan1_cfi_l2+vlan1_pri_l2,2)).toUpperCase();

                XENA_headers_hex+="8100"+cfipri+String.format("%03x",Integer.valueOf(vlan1_id_l2)).toUpperCase();
                if (isNextHeaderIPv4(root_node,current_inspected_index))  XENA_headers_hex+="0800";
                else if (isNextHeaderIPv6(root_node,current_inspected_index))  XENA_headers_hex+="08DD";
                else XENA_headers_hex+="FFFF";

                break;
            }
            case 2:  {
                if(!(findValueRecursively(PduNode,"vlan_id1").isEmpty())){this.vlan1_id_l2=new String(findValueRecursively(PduNode,"vlan_id1"));}
                if(!(findValueRecursively(PduNode,"vlan_id1").isEmpty())){this.vlan1_pri_l2=new String(findValueRecursively(PduNode,"vlan_pri1"));}
                if(!(findValueRecursively(PduNode,"vlan_id1").isEmpty())){this.vlan1_cfi_l2=new String(findValueRecursively(PduNode,"vlan_cfi1"));}
                if(!(findValueRecursively(PduNode,"vlan_id2").isEmpty())){this.vlan2_id_l2=new String(findValueRecursively(PduNode,"vlan_id2"));}
                if(!(findValueRecursively(PduNode,"vlan_id2").isEmpty())){this.vlan2_pri_l2=new String(findValueRecursively(PduNode,"vlan_pri2"));}
                if(!(findValueRecursively(PduNode,"vlan_id2").isEmpty())){this.vlan2_cfi_l2=new String(findValueRecursively(PduNode,"vlan_cfi2"));}
                this.XENA_headers+=" VLAN VLAN";
                this.STC_headers+=" VLAN VLAN";
                String cfipri1=String.format("%x",Integer.parseInt(vlan1_cfi_l2+vlan1_pri_l2,2)).toUpperCase();
                String cfipri2=String.format("%x",Integer.parseInt(vlan2_cfi_l2+vlan2_pri_l2,2)).toUpperCase();
                XENA_headers_hex+="88A8"+cfipri1+String.format("%03x",Integer.valueOf(vlan1_id_l2)).toUpperCase()+"8100"+cfipri2+String.format("%03x",Integer.valueOf(vlan2_id_l2)).toUpperCase();
                if (isNextHeaderIPv4(root_node,current_inspected_index))  XENA_headers_hex+="0800";
                else if (isNextHeaderIPv6(root_node,current_inspected_index))  XENA_headers_hex+="08DD";
                else XENA_headers_hex+="FFFF";
                break;
            }
        }
        // Resetting L2 attributes to default
        reset_eth_attributes();
    }
    public void analyze_l3v4_pdu(Node root_node,int current_inspected_index) {
        this.XENA_headers+=" IP";
        this.STC_headers+=" IPv4";
        Node PduNode = root_node.getFirstChild().getFirstChild().getChildNodes().item(current_inspected_index);
        if(!(findValueRecursively(PduNode,"sourceAddr").isEmpty())){this.src_ipv4_l3=new String(findValueRecursively(PduNode,"sourceAddr"));}
        if(!(findValueRecursively(PduNode,"destAddr").isEmpty())){this.dst_ipv4_l3=new String(findValueRecursively(PduNode,"destAddr"));}
        if(!(findValueRecursively(PduNode,"gateway").isEmpty())){this.gw_ipv4_l3=new String(findValueRecursively(PduNode,"gateway"));}
        if(!(findValueRecursively(PduNode,"ttl").isEmpty())){this.ttl_ipv4_l3=new String(findValueRecursively(PduNode,"ttl"));}
        XENA_headers_hex+="4500001400000000"+String.format("%02x",Integer.valueOf(ttl_ipv4_l3.toString())).toUpperCase();

        if (isNextHeaderUDP(root_node,current_inspected_index)) XENA_headers_hex+="110000";
        else if (isNextHeaderTCP(root_node,current_inspected_index)) XENA_headers_hex+="060000";
        else XENA_headers_hex+="FD0000";

        XENA_headers_hex+=IPv4_2Hex(src_ipv4_l3)+IPv4_2Hex(dst_ipv4_l3);
        reset_ipv4_attributes();
    }
    public void analyze_l3v6_pdu(Node root_node,int current_inspected_index) {
        this.XENA_headers+=" IPV6";
        this.STC_headers+=" IPv6";
        Node PduNode = root_node.getFirstChild().getFirstChild().getChildNodes().item(current_inspected_index);
        if(!(findValueRecursively(PduNode,"sourceAddr").isEmpty())){this.src_ipv6_l3=new String(findValueRecursively(PduNode,"sourceAddr"));}
        if(!(findValueRecursively(PduNode,"destAddr").isEmpty())){this.dst_ipv6_l3=new String(findValueRecursively(PduNode,"destAddr"));}
        if(!(findValueRecursively(PduNode,"gateway").isEmpty())){this.gw_ipv6_l3=new String(findValueRecursively(PduNode,"gateway"));}
        XENA_headers_hex+="600000000000";

        if (isNextHeaderUDP(root_node,current_inspected_index)) XENA_headers_hex+="11FF";
        else if (isNextHeaderTCP(root_node,current_inspected_index)) XENA_headers_hex+="06FF";
        else XENA_headers_hex+="3BFF";

        XENA_headers_hex+=IPv6_2Hex(src_ipv6_l3)+IPv6_2Hex(dst_ipv6_l3);
        reset_ipv6_attributes();
    }
    public void analyze_l4_pdu(Node root_node,int current_inspected_index) {
        Node PduNode = root_node.getFirstChild().getFirstChild().getChildNodes().item(current_inspected_index);
        if(!(findValueRecursively(PduNode,"sourcePort").isEmpty())){this.srcport_l4=new String(findValueRecursively(PduNode,"sourcePort"));}
        if(!(findValueRecursively(PduNode,"destPort").isEmpty())){this.dstport_l4=new String(findValueRecursively(PduNode,"destPort"));}
        XENA_headers_hex+=String.format("%04x",Integer.valueOf(srcport_l4)).toUpperCase()+String.format("%04x",Integer.valueOf(dstport_l4)).toUpperCase()+"00000000";
        if (isNextHeaderUDP(root_node,current_inspected_index-1)) {
            this.XENA_headers+=" UDPCHECK";
            this.STC_headers+=" Udp";
        }
        if (isNextHeaderTCP(root_node,current_inspected_index-1)) {
            this.XENA_headers+=" TCP";
            this.STC_headers+=" Tcp";
        }
        reset_udptcp_attributes();
    }
    public void analyze_gtpv1_pdu(Node root_node,int current_inspected_index) {
        Node PduNode = root_node.getFirstChild().getFirstChild().getChildNodes().item(current_inspected_index);
        if(!(findValueRecursively(PduNode,"msgType").isEmpty())){this.gtpv1_type=new String(findValueRecursively(PduNode,"msgType"));}
        if(!(findValueRecursively(PduNode,"mlength").isEmpty())){this.gtpv1_length=new String(findValueRecursively(PduNode,"mlength"));}
        if(!(findValueRecursively(PduNode,"teid").isEmpty())){this.gtpv1_teid=new String(findValueRecursively(PduNode,"teid"));}

        XENA_headers_hex+="30"+String.format("%02x",Integer.valueOf(gtpv1_type)).toUpperCase()+String.format("%04x",Integer.valueOf(gtpv1_length)).toUpperCase()+String.format("%08x",Integer.valueOf(gtpv1_teid)).toUpperCase();
        this.XENA_headers+=" GTPV1L0";
        this.STC_headers+=" GTPv1";
    }
    public void analyze_custom_pdu(Node root_node,int current_inspected_index) {

        Node PduNode = root_node.getFirstChild().getFirstChild().getChildNodes().item(current_inspected_index);
        String raw_hex_pattern=PduNode.getFirstChild().getFirstChild().getNodeValue();
        int new_segment_name_based_on_size=256-raw_hex_pattern.length()/2;
        this.XENA_headers+=" "+Integer.toString(new_segment_name_based_on_size);
        this.XENA_headers_hex+=raw_hex_pattern;
        this.STC_headers+=" Custom";
    }

    private static String findValueRecursively(Node node,String wanted) {
        // get all child nodes
        String returnstring="";
        NodeList list = node.getChildNodes();
        for (int i=0; i<list.getLength(); i++) {
            // get child node
            Node childNode = list.item(i);
            if (wanted.startsWith("vlan_id")) {
                if ((childNode.getNodeName().startsWith("#text")) && (childNode.getParentNode().getNodeName().equalsIgnoreCase("id"))) {
                    if ((wanted.equalsIgnoreCase("vlan_id1")) && (childNode.getParentNode().getParentNode().getAttributes().item(0).toString().equalsIgnoreCase("name=\"Vlan\""))){return childNode.getNodeValue();}
                    if ((wanted.equalsIgnoreCase("vlan_id2")) && (childNode.getParentNode().getParentNode().getAttributes().item(0).toString().equalsIgnoreCase("name=\"Vlan_1\""))){return childNode.getNodeValue();}
                }
            }
            if ((childNode.getNodeName().startsWith("#text"))&& (childNode.getParentNode().getNodeName().equalsIgnoreCase("pri"))) {
                if ((wanted.equalsIgnoreCase("vlan_pri1")) && (childNode.getParentNode().getParentNode().getAttributes().item(0).toString().equalsIgnoreCase("name=\"Vlan\""))){return childNode.getNodeValue();}
                if ((wanted.equalsIgnoreCase("vlan_pri2")) && (childNode.getParentNode().getParentNode().getAttributes().item(0).toString().equalsIgnoreCase("name=\"Vlan_1\""))){return childNode.getNodeValue();}
            }
            if ((childNode.getNodeName().startsWith("#text"))&& (childNode.getParentNode().getNodeName().equalsIgnoreCase("cfi"))) {
                if ((wanted.equalsIgnoreCase("vlan_cfi1")) && (childNode.getParentNode().getParentNode().getAttributes().item(0).toString().equalsIgnoreCase("name=\"Vlan\""))){return childNode.getNodeValue();}
                if ((wanted.equalsIgnoreCase("vlan_cfi2")) && (childNode.getParentNode().getParentNode().getAttributes().item(0).toString().equalsIgnoreCase("name=\"Vlan_1\""))) {return childNode.getNodeValue();}
            }
            else if ((childNode.getNodeName().startsWith("#text"))&& (childNode.getParentNode().getNodeName().equalsIgnoreCase(wanted))){return  childNode.getNodeValue();}
            returnstring+=findValueRecursively(childNode,wanted);
        }
        return returnstring;
    }

    public static int number_of_vlans(Node node) {
        // get all child nodes
        int return_amount=0;
        NodeList list = node.getChildNodes();
        for (int i=0; i<list.getLength(); i++) {
            // get child node
            Node childNode = list.item(i);
            if (childNode.getParentNode().getNodeName()=="vlans"){
                if (childNode.getParentNode().getNodeName().equalsIgnoreCase("vlans")) {
                    return childNode.getParentNode().getChildNodes().getLength();
                }
            }
            return_amount+=number_of_vlans(childNode);
        }
        return return_amount;
    }

    public static String IPv4_2Hex(String reqIpAddr) {
        String hex = "";
        String[] part = reqIpAddr.split("[\\.,]");
        if (part.length < 4) {
            return "00000000";
        }
        for (int i = 0; i < 4; i++) {
            int decimal = Integer.parseInt(part[i]);
            if (decimal < 16) // Append a 0 to maintian 2 digits for every
            // number
            {
                hex += "0" + String.format("%01X", decimal);
            } else {
                hex += String.format("%01X", decimal);
            }
        }
        return hex;
    }
    public static String IPv6_2Hex(String reqIpAddr) {
        String hex = "";
        reqIpAddr = reqIpAddr.replace("/","");
        String[] part = reqIpAddr.split("[\\:,]");
        if (part.length < 8) {
            return "00000000000000000000000000000000";
        }
        for (int i = 0; i < 8; i++) {
            hex += String.format("%04X", Integer.parseInt(part[i].trim(), 16 ));
        }
        return hex;
    }


    private String format_headersegment(String headersegment) {
        if (!headersegment.isEmpty()) {
            headersegment = headersegment.replaceFirst("^pdu=\"", "");
            headersegment = headersegment.replaceFirst("^ethernet:", "");
            headersegment = headersegment.replaceFirst("^ipv4:", "");
            headersegment = headersegment.replaceFirst("^ipv6:", "");
            headersegment = headersegment.replaceFirst("^udp:", "");
            headersegment = headersegment.replaceFirst("^tcp:", "");
            headersegment = headersegment.replaceFirst("^lldp:", "");
            headersegment = headersegment.replaceFirst("^dhcp:", "");
            headersegment = headersegment.replaceFirst("^custom:", "");
            headersegment = headersegment.replaceFirst("^gre:", "");
            headersegment = headersegment.replaceFirst("^gtpv1:", "");
            headersegment = headersegment.replaceFirst("^icmp:", "");
            headersegment = headersegment.replace("\"","");
        }
        return headersegment;
    }
    private String format_headersegment_prefix(String headersegment) {
        if (!headersegment.isEmpty()) {
            headersegment = headersegment.replaceFirst("^name=\"", "");
            headersegment = headersegment.replace("\"","");
        }
        return headersegment;
    }

    public static boolean isNextHeaderIPv4(Node node,int current_inspected_index) {

        boolean return_flag=false;
        int number_of_pdus=node.getFirstChild().getFirstChild().getChildNodes().getLength();
        if (!(number_of_pdus>current_inspected_index+1)) return false;
        Node next_PduNode = node.getFirstChild().getFirstChild().getChildNodes().item(current_inspected_index+1);
        if (next_PduNode.getAttributes().item(1).toString().equalsIgnoreCase("pdu=\"ipv4:IPv4\"")) return true;
        return return_flag;
    }
    public static boolean isNextHeaderIPv6(Node node,int current_inspected_index) {

        boolean return_flag=false;
        int number_of_pdus=node.getFirstChild().getFirstChild().getChildNodes().getLength();
        if (!(number_of_pdus>current_inspected_index+1)) return false;
        Node next_PduNode = node.getFirstChild().getFirstChild().getChildNodes().item(current_inspected_index+1);
        if (next_PduNode.getAttributes().item(1).toString().equalsIgnoreCase("pdu=\"ipv6:IPv6\"")) return true;
        return return_flag;
    }
    public static boolean isNextHeaderUDP(Node node,int current_inspected_index) {
        boolean return_flag=false;
        int number_of_pdus=node.getFirstChild().getFirstChild().getChildNodes().getLength();
        if (!(number_of_pdus>current_inspected_index+1)) return false;
        Node next_PduNode = node.getFirstChild().getFirstChild().getChildNodes().item(current_inspected_index+1);
        if (next_PduNode.getAttributes().item(1).toString().equalsIgnoreCase("pdu=\"udp:Udp\"")) return true;
        return return_flag;
    }
    public static boolean isNextHeaderTCP(Node node,int current_inspected_index) {

        boolean return_flag=false;
        int number_of_pdus=node.getFirstChild().getFirstChild().getChildNodes().getLength();
        if (!(number_of_pdus>current_inspected_index+1)) return false;
        Node next_PduNode = node.getFirstChild().getFirstChild().getChildNodes().item(current_inspected_index+1);
        if (next_PduNode.getAttributes().item(1).toString().equalsIgnoreCase("pdu=\"tcp:Tcp\"")) return true;
        return return_flag;
    }

    public static int segment_offset(String segment_name){
        int base_offset=0;
        boolean stop_loop=false;
        for (int i=0;i<Segments_list.size() && !stop_loop;i++){
            if (Segments_list.get(i).name.equalsIgnoreCase(segment_name)) stop_loop=true;
            if (!stop_loop) base_offset+=Segments_list.get(i).length;
        }
        return base_offset;
    }
    public static int double_octets_to_int(String four_octets){

        String[] ipAddressInArray = four_octets.split("\\.");

        int result = 0;
        for (int i = 0; i < ipAddressInArray.length; i++) {

            int power = 1 - i;
            int ip = Integer.parseInt(ipAddressInArray[i]);
            result += ip * Math.pow(256, power);
        }
        return result;
    }


}
