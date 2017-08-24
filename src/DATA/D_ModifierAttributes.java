package DATA;

import java.util.ArrayList;

class D_ModifierAttributes{
    public String mode;
    public String step;
    public String values_amount;
    public String min_value;
    public String repeat;
    public String offset;
    public String offset_ref;
    public String mask;

    public boolean XENA_modifier_supported;
    public String XENA_modifier_offset;
    public String XENA_modifier_mask;
    public String XENA_modifier_type;
    public String XENA_modifier_step;
    public String XENA_modifier_repeat;
    public String XENA_modifier_min;
    public String XENA_modifier_max;




    public D_ModifierAttributes() {
        mode=  new String("");
        step=  new String("");
        values_amount= new String("");
        min_value= new String("");
        repeat= new String("");
        offset= new String("");
        offset_ref= new String("");


        XENA_modifier_supported=true;
        XENA_modifier_offset =new String("");;
        XENA_modifier_mask =new String("");
        XENA_modifier_type =new String("");
        XENA_modifier_step= new String("");
        XENA_modifier_repeat= new String("");
        XENA_modifier_min= new String("");
        XENA_modifier_max= new String("");
    }


}