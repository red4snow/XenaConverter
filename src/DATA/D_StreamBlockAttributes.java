package DATA;

import CONVERTER.C_Converter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

import static DATA.D_Segments.double_octets_to_int;

public class D_StreamBlockAttributes {

    public String frame_config;
    public String frame_length_mode;
    public String frame_length_fixed;
    public String frame_length_min;
    public String frame_length_max;
    public String stream_sig;
    public String frame_payload_type;
    public String frame_payload_pattern;
    public String name;

    public D_Segments StreamBlockSegments;

    public ArrayList<D_ModifierAttributes> ModifierList;

    public D_StreamBlockAttributes() {
        frame_config= new String("");
        frame_length_mode= new String("");
        frame_length_fixed= new String("");
        stream_sig= new String("");
        frame_payload_type= new String("");
        frame_payload_pattern= new String("");
        name= new String("");
        ModifierList= new ArrayList<D_ModifierAttributes>();
        StreamBlockSegments=new D_Segments();
    }

    private void addModifierAttributes(Element eElement){
        D_ModifierAttributes tempMA = new D_ModifierAttributes();
        tempMA.mode= new String(eElement.getAttribute("ModifierMode"));
        tempMA.step= new String(eElement.getAttribute("StepValue"));
        tempMA.values_amount=new String(eElement.getAttribute("RecycleCount"));
        tempMA.min_value=new String( eElement.getAttribute("Data"));
        tempMA.repeat=new String(eElement.getAttribute("RepeatCount"));
        tempMA.offset=new String( eElement.getAttribute("Offset"));
        tempMA.offset_ref=new String(eElement.getAttribute("OffsetReference"));
        tempMA.mask=new String(eElement.getAttribute("Mask"));
        ModifierList.add(tempMA);
    }
    
    public static void addModifiersAttributes(D_StreamBlockAttributes stream,NodeList StreamBlockChildren_List) {
        for (int stream_child_index = 0; stream_child_index < StreamBlockChildren_List.getLength(); stream_child_index++) {
            Node StreamChildNode = StreamBlockChildren_List.item(stream_child_index);
            if (StreamChildNode.getNodeType() == Node.ELEMENT_NODE) {
                if (StreamChildNode.getNodeName().equals("RangeModifier")) {
                    stream.addModifierAttributes((Element) StreamChildNode);
                }
                if (StreamChildNode.getNodeName().equals("TableModifier")) {
                    C_Converter.print2xmllog(" ! [WARNING] Unsupported SHUFFLE Modifier @Stream = " + stream.name + "\n");
                }
                if (StreamChildNode.getNodeName().equals("RandomModifier")) {
                    stream.addModifierAttributes((Element) StreamChildNode);
                    stream.ModifierList.get(stream.ModifierList.size() - 1).mode = "RAND";
                }
            }
        }
    }
    public static void analyzeStreamPayload(D_StreamBlockAttributes stream,String log_comment,int stream_index,boolean print2file,String filename){
        String FourDigitHex=new String(Integer.toHexString(Integer.parseInt(stream.frame_payload_pattern)).toUpperCase());
        if (stream.frame_payload_type.equals("CONSTANT")){
            if (stream.frame_payload_pattern.equals("0")){ FourDigitHex=new String("0000");}
            stream.frame_payload_pattern=new String(FourDigitHex+FourDigitHex+FourDigitHex+FourDigitHex+FourDigitHex+FourDigitHex+FourDigitHex+FourDigitHex);
            String finalFourDigitHex = FourDigitHex;
            C_Converter.print2xmllog("PayloadPattern=<"+stream.frame_payload_type+","+ finalFourDigitHex +">\n");
            C_Converter.print2xpclog(log_comment+"PS_PAYLOAD ["+stream_index+"] PATTERN 0x"+ stream.frame_payload_pattern +"\n",print2file,filename);
        } else {
            C_Converter.print2xmllog(" ! [WARNING] Unknown PAYLOAD.TYPE=<"+stream.frame_payload_type+">\n");
        }
        
    }

    public static void analyzeStreamFrameSize(D_StreamBlockAttributes stream,String log_comment,int stream_index,boolean print2file,String filename){
        switch (stream.frame_length_mode) {
            case "FIXED":
                C_Converter.print2xpclog(log_comment+"PS_PACKETLENGTH ["+stream_index+"] FIXED "+stream.frame_length_fixed+" 16383\n",print2file,filename);
                C_Converter.print2xmllog("F.SIZE=<"+stream.frame_length_mode+","+stream.frame_length_fixed+">\n");
                break;
            case "INCR":
                C_Converter.print2xpclog(log_comment+"PS_PACKETLENGTH ["+stream_index+"] INCREMENTING "+stream.frame_length_min+" "+stream.frame_length_max+"\n",print2file,filename);
                C_Converter.print2xmllog("F.SIZE=<"+stream.frame_length_mode+","+stream.frame_length_min+","+stream.frame_length_max+">\n");
                break;
            case "RANDOM":
                C_Converter.print2xpclog(log_comment+"PS_PACKETLENGTH ["+stream_index+"] RANDOM "+stream.frame_length_min+" "+stream.frame_length_max+"\n",print2file,filename);
                C_Converter.print2xmllog("F.SIZE=<"+stream.frame_length_mode+","+stream.frame_length_min+","+stream.frame_length_max+">\n");
                break;
            default:
                C_Converter.print2xmllog(" ! [WARNING] Unsupported Frame length mode=<"+stream.frame_length_mode+">\n");
        }
    }

    public static void analyzeStreamSignature(D_StreamBlockAttributes stream,String log_comment,int stream_index,boolean print2file,String filename){
        if (stream.stream_sig.equals("TRUE")){
            C_Converter.print2xmllog("SIG?=TRUE\n");
            C_Converter.print2xpclog(log_comment+"PS_TPLD ["+stream_index+"] "+stream_index+"\n",print2file,filename);
        } else {
            C_Converter.print2xmllog("SIG?=FALSE\n");
            C_Converter.print2xpclog(log_comment+"PS_TPLD ["+stream_index+"] -1\n",print2file,filename);
        }

    }
    
    public static void analyzeModifierAttributes(D_StreamBlockAttributes stream,int sid,boolean print2file,String filename) {
        for (int modifiers_index = 0; modifiers_index < stream.ModifierList.size(); modifiers_index++) {

            stream.ModifierList.get(modifiers_index).XENA_modifier_repeat=stream.ModifierList.get(modifiers_index).repeat;
            stream.ModifierList.get(modifiers_index).XENA_modifier_min=stream.ModifierList.get(modifiers_index).min_value;
            int min_data=0;
            if (stream.ModifierList.get(modifiers_index).min_value.contains(".")){
                String[] ipAddressInArray = stream.ModifierList.get(modifiers_index).min_value.split("\\.");
                switch (stream.ModifierList.get(modifiers_index).mask) {
                    case "0.0.255.255": case "0.0.255.0": case "0.0.0.255":
                        min_data = double_octets_to_int(ipAddressInArray[2]+"."+ipAddressInArray[3]);
                        stream.ModifierList.get(modifiers_index).offset="2";
                        break;
                    case "0.255.255.0":
                        min_data = double_octets_to_int(ipAddressInArray[1]+"."+ipAddressInArray[2]);
                        stream.ModifierList.get(modifiers_index).offset="1";
                        break;
                    case "255.255.0.0": case "0.255.0.0": case "255.0.0.0":
                        min_data = double_octets_to_int(ipAddressInArray[0]+"."+ipAddressInArray[1]);
                        break;
                }
            }
            else if (stream.ModifierList.get(modifiers_index).min_value.contains(":")){ min_data=0;}//IPv6 not supported for modifiers yet
            else min_data=Integer.valueOf(stream.ModifierList.get(modifiers_index).min_value);
            stream.ModifierList.get(modifiers_index).XENA_modifier_max=Integer.toString(min_data+Integer.valueOf(stream.ModifierList.get(modifiers_index).values_amount)-1);
            //=========================================================================================[ OffsetReference ]==========================================================================
            switch ( (stream.ModifierList.get(modifiers_index).offset_ref)){
                case "eth1.srcMac":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_offset=Integer.toString(D_Segments.segment_offset("eth1")+6+Integer.valueOf(stream.ModifierList.get(modifiers_index).offset));
                    break;
                case "eth1.dstMac":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_offset=Integer.toString(D_Segments.segment_offset("eth1")+Integer.valueOf(stream.ModifierList.get(modifiers_index).offset));
                    break;
                case "eth1.vlans.Vlan.id":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_offset=Integer.toString(D_Segments.segment_offset("Vlan")+Integer.valueOf(stream.ModifierList.get(modifiers_index).offset));
                    break;
                case "ip_1.sourceAddr":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_offset=Integer.toString(D_Segments.segment_offset("ip_1")+12+Integer.valueOf(stream.ModifierList.get(modifiers_index).offset));
                    break;
                case "ip_1.destAddr":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_offset=Integer.toString(D_Segments.segment_offset("ip_1")+16+Integer.valueOf(stream.ModifierList.get(modifiers_index).offset));
                    break;
                case "ip_1.ttl":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_offset=Integer.toString(D_Segments.segment_offset("ip_1")+8+Integer.valueOf(stream.ModifierList.get(modifiers_index).offset));
                    break;
                case "proto1.sourcePort":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_offset=Integer.toString(D_Segments.segment_offset("proto1")+Integer.valueOf(stream.ModifierList.get(modifiers_index).offset));
                    break;
                case "proto1.destPort":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_offset=Integer.toString(D_Segments.segment_offset("proto1")+2+Integer.valueOf(stream.ModifierList.get(modifiers_index).offset));
                    break;
                case "proto1.sourceAddr":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_offset=Integer.toString(D_Segments.segment_offset("proto1")+8+Integer.valueOf(stream.ModifierList.get(modifiers_index).offset));
                    break;
                case "proto1.destAddr":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_offset=Integer.toString(D_Segments.segment_offset("proto1")+24+Integer.valueOf(stream.ModifierList.get(modifiers_index).offset));
                    break;
                case "proto1.hopLimit":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_offset=Integer.toString(D_Segments.segment_offset("proto1")+7+Integer.valueOf(stream.ModifierList.get(modifiers_index).offset));
                    break;
                default:
                    C_Converter.print2dbglog(" ! [WARNING] Unknown Modifier Offset Reference ["+modifiers_index+"] = "+stream.ModifierList.get(modifiers_index).offset_ref+"\n");
                    stream.ModifierList.get(modifiers_index).XENA_modifier_supported=false;
                    break;
            }


            /*
            switch (stream.ModifierList.get(modifiers_index).offset) {
                case "0":
                    System.out.println("Stream= "+stream.name+" Offset= "+stream.ModifierList.get(modifiers_index).offset+" "+stream.ModifierList.get(modifiers_index).offset_ref+" "+stream.ModifierList.get(modifiers_index).mask+" "+stream.ModifierList.get(modifiers_index).step);
                    break;
                default:
                    System.out.println("Stream= "+stream.name+" Offset= "+stream.ModifierList.get(modifiers_index).offset+" "+stream.ModifierList.get(modifiers_index).offset_ref+" "+stream.ModifierList.get(modifiers_index).mask+" "+stream.ModifierList.get(modifiers_index).step);
                    C_Converter.print2dbglog(" ! [WARNING] Unknown Modifier["+modifiers_index+"] Offset  = "+stream.ModifierList.get(modifiers_index).offset+"\n");
                    stream.ModifierList.get(modifiers_index).XENA_modifier_supported=false;
                    break;
            }*/
            //=========================================================================================[ MODE ]=================================================================================
            switch (stream.ModifierList.get(modifiers_index).mode) {
                case "INCR":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_type="INC";
                    break;
                case "DECR":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_type="DEC";
                    break;
                case "RAND":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_type="RANDOM";
                    break;
                default:
                    C_Converter.print2dbglog(" ! [WARNING] Unknown Modifier["+modifiers_index+"] Mode  = "+stream.ModifierList.get(modifiers_index).mode+"\n");
                    stream.ModifierList.get(modifiers_index).XENA_modifier_supported=false;
                    break;
            }
            //=========================================================================================[ MASK ]=================================================================================
            switch (stream.ModifierList.get(modifiers_index).mask) {
                case "000F::": case "0:000F::0": case "0000:000F::":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_mask="0x000F0000";
                    break;

                case "255":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_mask="0x00FF0000";
                    break;
                case "4095":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_mask="0x0FFF0000";
                    break;
                case "65535":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_mask="0xFFFF0000";
                    break;
                case "00:00:FF:FF:FF:FF": case "::FFFF:FFFF": case "255.255.255.255":
                    C_Converter.print2dbglog(" ! [WARNING] Unsupported Modifier["+modifiers_index+"] Mask  = "+stream.ModifierList.get(modifiers_index).mask+"\n");
                    stream.ModifierList.get(modifiers_index).XENA_modifier_supported=false;
                    break;
                default:
                    C_Converter.print2dbglog(" ! [WARNING] Unknown Modifier["+modifiers_index+"] Mask  = "+stream.ModifierList.get(modifiers_index).mask+"\n");
                    stream.ModifierList.get(modifiers_index).XENA_modifier_supported=false;
                    break;
            }
            //=========================================================================================[ STEP ]=================================================================================
            switch (stream.ModifierList.get(modifiers_index).step) {
                case "1":
                case "0.0.0.1":
                case "::1":
                case "::01":
                case "00:00:00:00:00:01":
                case "0001::":
                case "0:0001::0":
                case "0000:0001::":
                    stream.ModifierList.get(modifiers_index).XENA_modifier_step="1";
                    break;
                case "::10": case "::010":
                    C_Converter.print2dbglog(" ! [WARNING] Unsupported Modifier["+modifiers_index+"] Step  = "+stream.ModifierList.get(modifiers_index).step+"\n");
                    stream.ModifierList.get(modifiers_index).XENA_modifier_supported=false;
                    break;
                default:
                    C_Converter.print2dbglog(" ! [WARNING] Unknown Modifier["+modifiers_index+"] Step  = "+stream.ModifierList.get(modifiers_index).step+"\n");
                    stream.ModifierList.get(modifiers_index).XENA_modifier_supported=false;
                    break;
            }

        }
        for (int i=0;i<stream.ModifierList.size();i++) {
            C_Converter.print2xpclog("PS_MODIFIER [" + sid + "," + i + "] " + stream.ModifierList.get(i).XENA_modifier_offset + " " + stream.ModifierList.get(i).XENA_modifier_mask + " " + stream.ModifierList.get(i).XENA_modifier_type + " " + stream.ModifierList.get(i).XENA_modifier_repeat+"\n", print2file, filename);
            C_Converter.print2xpclog("PS_MODIFIERRANGE ["+sid+","+i+"] "+stream.ModifierList.get(i).XENA_modifier_min+" "+stream.ModifierList.get(i).XENA_modifier_step+" "+stream.ModifierList.get(i).XENA_modifier_max+"\n", print2file, filename);
        }
    }
    
    /*static public void print_xena_modifier(D_StreamBlockAttributes stream,int sid){
        for (int i=0;i<stream.ModifierList.size();i++){
            if (stream.ModifierList.get(i).XENA_modifier_supported){
                System.out.println("---<"+stream.name+">---");
                System.out.println("PS_MODIFIER ["+sid+","+i+"] "+stream.ModifierList.get(i).XENA_modifier_offset+" "+stream.ModifierList.get(i).XENA_modifier_mask+" "+stream.ModifierList.get(i).XENA_modifier_type+" "+stream.ModifierList.get(i).XENA_modifier_repeat);
                System.out.println("PS_MODIFIERRANGE ["+sid+","+i+"] "+stream.ModifierList.get(i).XENA_modifier_min+" "+stream.ModifierList.get(i).XENA_modifier_step+" "+stream.ModifierList.get(i).XENA_modifier_max);
            }
        }
    }*/

}