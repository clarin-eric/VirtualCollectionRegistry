package eu.clarin.cmdi.wicket;

import org.apache.wicket.markup.html.WebComponent;

public class ScrollTracker extends WebComponent {
    public ScrollTracker(String id) {
        super(id);
    }

    @Override
    protected void onRender() {
        getResponse().write("\n"
                + "<!-- Scroll Tracker -->\n"
                + "<script type=\"text/javascript\">\n"
                + "let el = document.querySelector('#crud-action-buttons');\n"
                + "if(el) {\n"
                + "console.debug('Enabling scrolling');\n"
                + "let elFooter = document.querySelector('#footer');\n"
                + "function checkFooterVisibility() {\n"
                + "const rect = elFooter.getBoundingClientRect();\n"
                + "//Check if the top of the footer is visible from the bottom of the viewport\n"
                + "    const footerVisible = rect.top >= 0 && rect.top <= (window.innerHeight || document.documentElement.clientHeight);\n"
                + "if (footerVisible) {\n"
                + "    let viewPortHeight = 0;\n"
                + "    if (window.innerHeight) {\n"
                + "        viewPortHeight = window.innerHeight;\n"
                + "    } else if (document.documentElement.clientHeight) {\n"
                + "        viewPortHeight = document.documentElement.clientHeight;\n"
                + "    }\n"
                + "        const bottom = viewPortHeight - rect.top;\n"
                + "    el.style.bottom = bottom + 'px';\n"
                + "}\n"
                + "}\n"
                + "\n"
                + "//Register event listeners\n"
                + "window.addEventListener('resize', function (e) { checkFooterVisibility(); });\n"
                + "document.addEventListener('scroll', function (e) { checkFooterVisibility(); });\n"
                + "checkFooterVisibility(); //Run once on page loaded\n"
                + "}\n"
                + "</script>\n"
                + "<!-- End Scroll Tracker -->");
    }
}
