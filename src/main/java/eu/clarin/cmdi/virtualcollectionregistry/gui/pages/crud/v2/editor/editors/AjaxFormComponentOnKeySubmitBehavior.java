package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * //Reference on getting the pressed key
            //https://stackoverflow.com/a/14468972
 * @author wilelb
 */
public abstract class AjaxFormComponentOnKeySubmitBehavior extends AjaxFormComponentUpdatingBehavior {
    final static String KEYPRESS_PARAM = "keycode";

    public AjaxFormComponentOnKeySubmitBehavior() {
        super("keypress");
    } 
    
    @Override
    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
        super.updateAjaxAttributes(attributes);

        final int enter_keycode = 13;
        final int escape_keycode = 27;

        IAjaxCallListener listener = new AjaxCallListener() {
            @Override
            public CharSequence getPrecondition(Component component) {
                //this javascript code evaluates wether an ajaxcall is necessary.
                //Here only by keyocdes for F9 and F10 
                return "var keycode = Wicket.Event.keyCode(attrs.event);"
                        + "if ((keycode == "+enter_keycode+") || (keycode == "+escape_keycode+"))"
                        + "    return true;"
                        + "else"
                        + "    return false;";
            }
        };
        attributes.getAjaxCallListeners().add(listener);

        //Append the pressed keycode to the ajaxrequest 
        attributes.getDynamicExtraParameters()
                .add("var eventKeycode = Wicket.Event.keyCode(attrs.event);"
                        + "return {keycode: eventKeycode};");

        //whithout setting, no keyboard events will reach any inputfield
        attributes.setPreventDefault(false);// setAllowDefault(true);
    }

    protected int getPressedKeyCode() {
        final Request request = RequestCycle.get().getRequest();
        final String jsKeycode = request.getRequestParameters()
                .getParameterValue(KEYPRESS_PARAM).toString("");
        if(jsKeycode == null || jsKeycode.isEmpty()) {
            return -1;
        }
        return Integer.valueOf(jsKeycode);
    }
    
    protected boolean pressedReturn() {
        return getPressedKeyCode() == 13;
    }
    
    protected boolean pressedEscape() {
        return getPressedKeyCode() == 27;
    }
}
