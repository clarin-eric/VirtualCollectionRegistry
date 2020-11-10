 package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.authors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.ActionablePanel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmAction;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.AbstractEvent;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Event;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.EventType;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Listener;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.AbstractField;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.ComposedField;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.FieldComposition;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.VcrTextField;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 /**
  *
  * @author wilelb
  */
 public class AuthorsEditor extends ComposedField {

     private final static Logger logger = LoggerFactory.getLogger(AuthorsEditor.class);

     private final List<Editable<Creator>> authors = new ArrayList<>();

     @Override
     public boolean completeSubmit(AjaxRequestTarget target) {
         return false;
     }

     public class Editable<T> implements Serializable {
         private final T data;

         private boolean editing = false;

         public Editable(T data) {
             this.data = data;
         }

         public void setEditing(boolean editing) {
             this.editing = editing;
         }

         public boolean isEditing() {
             return editing;
         }

         public T getData() {
             return data;
         }
     }

     public class AuthorPanel extends ActionablePanel {

         public AuthorPanel(String id, final Editable<Creator> editableAuthor, final Component componentToUpdate) {
             super(id);
             setOutputMarkupId(true);

             final Creator a = editableAuthor.getData();
             add(new Label("name", a.getPerson()));
             add(new Label("email", a.getEMail()));
             add(new Label("affiliation", a.getOrganisation()));

             AjaxFallbackLink editButton = new AjaxFallbackLink("btn_edit") {
                 @Override
                 public void onClick(AjaxRequestTarget target) {
                     for(int i = 0; i < authors.size(); i++) {
                         if(authors.get(i).getData().getId() == a.getId()) {
                             authors.get(i).setEditing(true);
                         }
                     }

                     noAuthors.setVisible(authors.size() <= 0);
                     ajaxWrapper.setVisible(authors.size() > 0);

                     if (target != null) {
                         target.add(componentToUpdate);
                     }
                 }
             };
             add(editButton);

             AjaxFallbackLink removeButton = new AjaxFallbackLink("btn_remove") {
                 @Override
                 public void onClick(AjaxRequestTarget target) {
                     fireEvent(new AbstractEvent<>(
                         EventType.DELETE,
                         a,
                         target));
                 }
             };
             add(removeButton);
         }

     }

     public class AuthorEditPanel extends Panel implements FieldComposition {

         private final IModel<String> mdlName = new Model<>();
         private final IModel<String> mdlEmail = new Model<>();
         private final IModel<String> mdlAffiliation = new Model<>();

         private final List<AbstractField> fields = new ArrayList<>();

         private final Editable<Creator> editableAuthor;
         private final Component componentToUpdate;

         private int focusCount = 0;

         /**
          * Editor to create a new author
          *
          * @param id
          * @param componentToUpdate
          */
         public AuthorEditPanel(String id, final Component componentToUpdate) {
             this(id, null, componentToUpdate);
         }

         /**
          * If editableAuthor is not null edit the values, otherwise create
          *
          * @param id
          * @param editableAuthor
          * @param componentToUpdate
          */
         public AuthorEditPanel(String id, final Editable<Creator> editableAuthor, final Component componentToUpdate) {
             super(id);
             this.editableAuthor = editableAuthor;
             this.componentToUpdate = componentToUpdate;

             setOutputMarkupId(true);

             if(editableAuthor != null) {
                 Creator a = editableAuthor.getData();
                 mdlName.setObject(a.getPerson());
                 mdlEmail.setObject(a.getEMail());
                 mdlAffiliation.setObject(a.getOrganisation());
             }

             AbstractField f1 = new VcrTextField("author_name", "Name", "New author name", mdlName, this);
             f1.setRequired(true);
             AbstractField f2 = new VcrTextField("author_email", "Email", "New author email", mdlEmail, this);
             f2.setRequired(true);
             AbstractField f3 = new VcrTextField("author_affiliation", "Affiliation", "New author affiliation (optional)", mdlAffiliation, this);
             f3.setCompleteSubmitOnUpdate(true);

             fields.add(f1);
             fields.add(f2);
             fields.add(f3);

             add(f1);
             add(f2);
             add(f3);
         }

         public boolean validate() {
             boolean valid = true;
             for (AbstractField f : fields) {
                 if (!f.validate()) {
                     valid = false;
                 }
             }
             return valid;
         }

         @Override
         public boolean completeSubmit(AjaxRequestTarget target) {
             logger.info("completeSubmit :: focuscount = {}", this.focusCount);
             if(focusCount > 0) {
                 //One component of this composition still has focus, dont submit
                 logger.info("focusCount > 0, in composition focus, no submit");
             } else if (focusCount < 0) {
                 //Should not happen, log to detect any issues
                 logger.info("focusCount < 0, this is an invalid composition focus state");
             } else {
                 //focusCount = 0, so all components in the composition lost focus. Submit now.
                 logger.info("Completing author submit: name=" + mdlName.getObject() + ", email=" + mdlEmail.getObject() + ", affiliation=" + mdlAffiliation.getObject());

                 boolean valid = validate();
                 logger.info("Valid = " + valid);
                 if (valid) {
                     String name = mdlName.getObject();
                     String email = mdlEmail.getObject();
                     String affiliation = mdlAffiliation.getObject() == null ? null : mdlAffiliation.getObject();

                     //Create or edit
                     if(editableAuthor == null) {
                         Creator creator = new Creator(name);
                         creator.setEMail(email);
                         creator.setOrganisation(affiliation);
                         authors.add(new Editable(creator));
                     } else {
                         editableAuthor.getData().setPerson(name);
                         editableAuthor.getData().setEMail(email);
                         editableAuthor.getData().setOrganisation(affiliation);
                         editableAuthor.setEditing(false);
                     }

                     mdlName.setObject("");
                     mdlEmail.setObject("");
                     mdlAffiliation.setObject("");

                     noAuthors.setVisible(authors.size() <= 0);
                     ajaxWrapper.setVisible(authors.size() > 0);

                     if (target != null) {
                         target.add(componentToUpdate);
                     }

                     this.focusCount = 0;
                 }
             }
             return false;
         }

         @Override
         public void increaseFocusCount() {
             //this.focusCount++;
             //logger.info("increaseFocusCount :: focuscount = {}", this.focusCount);
         }

         @Override
         public void decreaseFocusCount() {
             //this.focusCount--;
             //logger.info("decreaseFocusCount :: focuscount = {}", this.focusCount);
         }

     }

     private Label noAuthors;
     private WebMarkupContainer ajaxWrapper;
     private final ModalConfirmDialog localDialog;

     public AuthorsEditor(String id, String label) {
         super(id, label, null);
         setOutputMarkupId(true);

         final Component componentToUpdate = this;
         noAuthors = new Label("lbl_no_authors", "No authors");

         ajaxWrapper = new WebMarkupContainer("ajaxwrapper");
         ajaxWrapper.setOutputMarkupId(true);

         localDialog = new ModalConfirmDialog("modal");
         localDialog.addListener(new Listener() {
             @Override
             public void handleEvent(final Event event) {
                 switch(event.getType()) {
                     case OK:
                             logger.info("Default confirm");
                             event.updateTarget(ajaxWrapper);
                         break;
                     case CONFIRMED_DELETE:
                             if(event.getData() == null) {
                                 logger.info("No author found for removal");
                             } else {
                                 Creator a = (Creator)event.getData();
                                 logger.info("Removing author with id = {}", a.getId());
                                 int idx = -1;
                                 for(int i = 0; i < authors.size(); i++) {
                                     if(authors.get(i).data.getId() == a.getId()) {
                                         idx = i;
                                     }
                                 }
                                 if(idx >= 0) {
                                     authors.remove(idx);
                                 }
                                 noAuthors.setVisible(authors.isEmpty());
                             }
                             event.updateTarget(ajaxWrapper);
                         break;
                     case CANCEL:
                             event.updateTarget();
                         break;
                 }
             }
         });
         add(localDialog);

         ListView listview = new ListView("listview", authors) {
             @Override
             protected void populateItem(ListItem item) {
                 Editable<Creator> object = (Editable<Creator>) item.getModel().getObject();
                 if(object.isEditing()) {
                     item.add(new AuthorEditPanel("pnl_author_details", object, componentToUpdate));
                 } else {
                     ActionablePanel pnl = new AuthorPanel("pnl_author_details", object, componentToUpdate);
                     pnl.addListener(new Listener<Creator>() {
                         @Override
                         public void handleEvent(Event<Creator> event) {
                             switch(event.getType()) {
                             case DELETE:
                                 String title = "Confirm removal";
                                 String body = "Confirm removal of author: "+event.getData().getPerson();
                                 localDialog.update(title, body);
                                 localDialog.setModalConfirmAction(
                                     new ModalConfirmAction<>(
                                         EventType.CONFIRMED_DELETE,
                                         event.getData()));
                                 event.getAjaxRequestTarget().add(localDialog);
                                 localDialog.show(event.getAjaxRequestTarget());
                                 break;
                             default:
                                 throw new RuntimeException("Unhandled event. type = "+event.getType().toString());
                         }
                         }
                     });
                     item.add(pnl);
                 }
             }
         };
         ajaxWrapper.add(listview);

         ajaxWrapper.add(noAuthors);
         add(ajaxWrapper);
         add(new AuthorEditPanel("pnl_create_author", null, componentToUpdate));

         noAuthors.setVisible(authors.size() <= 0);
         ajaxWrapper.setVisible(authors.size() > 0);
     }

     /**
      * @return the authors
      */
     public List<Creator> getData() {
         List<Creator> result = new ArrayList<>();
         for(Editable<Creator> ea : authors) {
             result.add(ea.getData());
         }
         return result;
     }

      /**
      * @param authors
      */
     public void setData(List<Creator> authors) {
         logger.info("Set author data: {} authors", authors.size());
         for(Creator a : authors) {
             this.authors.add(new Editable<>(a));
         }
     }

     public void reset() {
         authors.clear();
     }
 }
