package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.wizard.CancelButton;
import org.apache.wicket.extensions.wizard.FinishButton;
import org.apache.wicket.extensions.wizard.IWizard;
import org.apache.wicket.extensions.wizard.LastButton;
import org.apache.wicket.extensions.wizard.NextButton;
import org.apache.wicket.extensions.wizard.PreviousButton;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

@SuppressWarnings("serial")
public class WizardBase extends Wizard {

    private static final class ButtonBarPanel extends Panel {

        private ButtonBarPanel(String id, final IWizard wizard) {
            super(id);
            add(new PreviousButton("previous", wizard));
            add(new NextButton("next", wizard));
            add(new LastButton("last", wizard));
            add(new CancelButton("cancel", wizard));
            add(new FinishButton("finish", wizard)
                    // add an onclick confirmation event in the case a public
                    // collection is being edited
                    .add(new AttributeModifier("onclick", true, new AbstractReadOnlyModel<String>() {

                        @Override
                        public String getObject() {
                            if (wizard instanceof CreateVirtualCollectionWizard) {
                                final CreateVirtualCollectionWizard vcWizard = (CreateVirtualCollectionWizard) wizard;
                                if (vcWizard.getVirtualCollectionModel().getObject().isPublic()) {
                                    return "return confirm('Are you sure you want to update this published collection?');";
                                }
                            }
                            return null;
                        }
                    })));
        }
    } // class WizardBase.ButtonBarPanel

    public WizardBase(String id) {
        super(id);
    }

    @Override
    protected Component newButtonBar(String id) {
        return new ButtonBarPanel(id, this);
    }

} // class WizardBase
