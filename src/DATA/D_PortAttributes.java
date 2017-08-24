package DATA;

import CONVERTER.C_Converter;
import org.w3c.dom.Element;

import java.util.ArrayList;

public class D_PortAttributes {

    public String gen_schedulingMode;
    public String gen_loadMode;
    public String gen_loadUnit;
    public String gen_fixedLoad;

    public ArrayList<D_StreamBlockAttributes> StreamBlockList;

    public D_PortAttributes() {
        gen_schedulingMode=  new String("");
        gen_loadMode=  new String("");
        gen_loadUnit=  new String("");
        gen_fixedLoad=  new String("");
        StreamBlockList=new ArrayList<D_StreamBlockAttributes>();
    }

    public void addStreamBlockAttributes(Element eElement){
        D_StreamBlockAttributes tempSBA=new D_StreamBlockAttributes();
        tempSBA.frame_config = new String(eElement.getAttribute("FrameConfig"));
        tempSBA.frame_length_mode=new String(eElement.getAttribute("FrameLengthMode"));
        tempSBA.frame_length_fixed=new String( eElement.getAttribute("FixedFrameLength"));
        tempSBA.frame_length_min=new String( eElement.getAttribute("MinFrameLength"));
        tempSBA.frame_length_max=new String( eElement.getAttribute("MaxFrameLength"));
        tempSBA.stream_sig=new String( eElement.getAttribute("InsertSig"));
        tempSBA.frame_payload_type=new String( eElement.getAttribute("FillType"));
        tempSBA.frame_payload_pattern=new String( eElement.getAttribute("ConstantFillPattern"));
        tempSBA.name=new String(eElement.getAttribute("Name"));
        StreamBlockList.add(tempSBA);
    }



    public static void analyzePortRate(D_PortAttributes port,int port_index,boolean print2file,String filename){
        if (port.gen_schedulingMode.equals("PORT_BASED")) {
            C_Converter.print2xpclog("P_TXMODE SEQUENTIAL\n",print2file,filename);
            C_Converter.print2xmllog("Port Based Rate");
            switch (port.gen_loadUnit){
                case "FRAMES_PER_SECOND":
                    C_Converter.print2xpclog("P_RATEPPS "+port.gen_fixedLoad+"\n",print2file,filename);
                    break;
                case "PERCENT_LINE_RATE":
                    C_Converter.print2xpclog("P_RATEPPS "+Integer.valueOf(port.gen_fixedLoad)*10000+"\n",print2file,filename);
                    break;
                case "BITS_PER_SECOND":
                    C_Converter.print2xpclog("P_RATEL2BPS "+Integer.valueOf(port.gen_fixedLoad)+"\n",print2file,filename);
                    break;
                case "KILOBITS_PER_SECOND":
                    C_Converter.print2xpclog("P_RATEL2BPS "+Integer.valueOf(port.gen_fixedLoad)*1000+"\n",print2file,filename);
                    break;
                case "MEGABITS_PER_SECOND":
                    C_Converter.print2xpclog("P_RATEL2BPS "+Integer.valueOf(port.gen_fixedLoad)*1000000+"\n",print2file,filename);
                    break;
                default:
                    C_Converter.print2dbglog(" ! [WARNING]Unsupported Port LoadUnit <Port,Unit> = <"+ port_index+","+port.gen_loadUnit+"\n");
                    break;
            }
        } else  C_Converter.print2dbglog(" ! [WARNING]Unsupported Port Scheduling Mode <Port,Mode> = <"+ port_index+","+port.gen_loadMode+"\n");
    }
}
