package eu.clarin.cmdi.wicket;

import org.apache.wicket.markup.html.WebComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Render a clipboard.js javascript snippet to the DOM.
 *
 * Reference:
 * - https://clipboardjs.com/
 */
public class ClipboardJs extends WebComponent {
    private final Logger logger = LoggerFactory.getLogger(ClipboardJs.class);

    private final String clipboardJsRef; //Dom selector to initialize clipboardjs on

    private int popoverFadeOutTimeout = 1000;

    private Properties i18n = new Properties();

    private final static String DEFAULT_MSG_COPIED = "Copied to clipboard!";

    public ClipboardJs(String id, String clipboardJsRef) {
        super(id);
        this.clipboardJsRef = clipboardJsRef;

        i18n.setProperty("msg_copied", "Copied to clipboard!");
    }

    public ClipboardJs setPopoverFadeOutTimeOut(int ms) {
        popoverFadeOutTimeout = ms;
        return this;
    }

    @Override
    protected void onRender() {
        getResponse().write("\n"
                + "<!-- ClipboardJs -->\n"
                + "<script type=\"text/javascript\">\n"
                + "  var clipboard = new ClipboardJS('"+clipboardJsRef+"');\n"
                + "\n"
                + "  clipboard.on('success', function(e) {\n"
//                + "    console.info('Copied to clipboard:', e.text);\n"
                + "    $('#'+e.trigger.id).popover({"
                +        "placement: 'bottom auto', "
                +        "content: '"+i18n.getProperty("msg_copied")+"', "
                +        "template: '<div class=\"popover\" role=\"tooltip\"><div class=\"arrow\"></div><div class=\"popover-content\"></div></div>'});\n"
                + "    $('#'+e.trigger.id).popover('show')\n"
                + "    setTimeout(function() {$('#'+e.trigger.id).popover('destroy');},"+popoverFadeOutTimeout+");"
                + "    e.clearSelection();\n"
                + "  });\n"
                + "\n"
                + "  clipboard.on('error', function(e) {\n"
                + "    console.error('Copy to clipboard failed:');\n"
                + "    console.errir(e);\n"
                + "  });\n"
                + "</script>\n"
                + "<!-- ClipboardJs -->");
    }
}
