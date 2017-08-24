package DATA;

import org.w3c.dom.*;
import DATA.D_PortAttributes;

import java.util.ArrayList;



public class D_Attributes {

    public ArrayList<D_PortAttributes> PortList;

    public D_Attributes() {
        PortList=new ArrayList<D_PortAttributes>();
    }

    public void addPortAttributes(Element eElement){
        D_PortAttributes tempPA=new D_PortAttributes();
        tempPA.gen_schedulingMode = new String(eElement.getAttribute("SchedulingMode"));
        tempPA.gen_loadMode=new String(eElement.getAttribute("LoadMode"));
        tempPA.gen_loadUnit=new String( eElement.getAttribute("LoadUnit"));
        tempPA.gen_fixedLoad=new String( eElement.getAttribute("FixedLoad"));
        PortList.add(tempPA);
    }


}
