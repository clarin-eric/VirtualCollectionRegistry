package eu.clarin.cmdi.virtualcollectionregistry.gui.dialog;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class ModalDialogBase extends ModalWindow {
    private final class Content extends Panel {
        public Content(String id) {
            super(id);
            setOutputMarkupId(true);
            Panel content = createContent("dialogContent");
            if (content == null) {
                throw new NullPointerException("createContent() == null");
            }
            add(content);
            Panel buttons = createButtonBar("dialogButtons");
            if (buttons == null) {
                throw new NullPointerException("createButtonBar() == null");
            }
            add(buttons);
        }
    } // class ModalDialogBase.Content

    public ModalDialogBase(String id, IModel<String> title) {
        super(id);
        if (title == null) {
            throw new NullPointerException("title == null");
        }
        setOutputMarkupId(true);
        setInitialWidth(350);
        setUseInitialHeight(false);
        setTitle(title);
        setContent(new Content(this.getContentId()));
    }
    
    protected abstract Panel createButtonBar(String id);

    protected abstract Panel createContent(String id);
    
} // class ModalDialogBase;
