package eu.clarin.cmdi.wicket;

import org.apache.wicket.markup.html.WebComponent;

public class ScrollTracker extends WebComponent {
    public ScrollTracker(String id) {
        super(id);
    }

    @Override
    protected void onRender() {
        getResponse().write("\n"
                + " <!-- Scroll Tracker -->\n"
                + " <script type=\"text/javascript\">\n"
                + " let el = document.querySelector('#crud-action-buttons');\n"
                + " if(el) {\n"
                + " console.debug('Enabling scrolling');\n"
                + " let elFooter = document.querySelector('#footer');\n"
                + " //Check if the top of the footer is visible from the bottom of the viewport\n"
                + " function isFooterVisibile() {\n"
                + "     const rect = elFooter.getBoundingClientRect();\n"
                + "     return rect.top >= 0 && rect.top <= (window.innerHeight || document.documentElement.clientHeight);\n"
                + " }\n"
                + " let footer_was_visible = isFooterVisibile();\n"
                + " function checkFooterVisibility() {\n"
                + "     const footerVisible = isFooterVisibile();\n"
                + "     //console.log('Rect.top=:'+rect.top+', window.innerHeight='+window.innerHeight+',document.documentElement.clientHeight='+document.documentElement.clientHeight+', footerVisible='+footerVisible)\n"
/*
                + "     if (footerVisible) {\n"
                + "         let viewPortHeight = 0;\n"
                + "         if (window.innerHeight) {\n"
                + "             viewPortHeight = window.innerHeight;\n"
                + "         } else if (document.documentElement.clientHeight) {\n"
                + "             viewPortHeight = document.documentElement.clientHeight;\n"
                + "         }\n"
                + "        const bottom = viewPortHeight - rect.top;\n"
                + "         el.style.bottom = bottom + 'px';\n"
                + "         console.log('Bottom='+bottom+'px');\n"
                + "     }\n"
  */
                + "     if (!footerVisible && footer_was_visible) {\n"
                + "         el.className = 'row crud-action-buttons-hover'"
/*                + "         el.style.position = 'fixed';"
                + "         el.style.bottom = '0px';"
                + "         el.style.background-color = 'white';"
                + "         el.style.left = '50%';"
                + "         el.style.transform = 'translate(-50%)';"
                + "         el.style.padding-top = '10px';"
                + "         el.style.padding-bottom = '10px';"
                + "         el.style.width = '400px';"*/
                + "     } else if(footerVisible && !footer_was_visible) {\n"
                + "         el.className = 'row crud-action-buttons'"
//                + "         el.style.position = 'relative';"
                + "     }\n"
                + "     footer_was_visible = footerVisible;\n"
                + " }\n"
                + "\n"
                + " const config = { attributes: true, childList: true, subtree: true };"
                + " const callback = function(mutationsList, observer) {\n"
                + "     for(const mutation of mutationsList) {"
                + "         if (mutation.type === 'childList') {"
                + "             console.log('A child node has been added or removed.');"
                + "         }"
                + "         else if (mutation.type === 'attributes') {"
                + "             console.log('The ' + mutation.attributeName + ' attribute was modified.');"
                + "         }"
                + "     }"
                + "     checkFooterVisibility();\n"
                + " };"
                + "\n"
                + "//Register event listeners\n"
                + "window.addEventListener('resize', function (e) { checkFooterVisibility(); });\n"
                + "document.addEventListener('scroll', function (e) { checkFooterVisibility(); });\n"
                + "//Register dom change listeners\n"
//                + "const observer = new MutationObserver(callback); \n"
//                + "observer.observe(document.querySelector('body'), config);\n"
                + "checkFooterVisibility(); //Run once on page loaded\n"
                + "}\n"
                + "</script>\n"
                + "<!-- End Scroll Tracker -->");
    }
}
