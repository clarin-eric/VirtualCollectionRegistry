 package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.authors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.ActionablePanel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmAction;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.*;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.*;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Orderable;
import eu.clarin.cmdi.virtualcollectionregistry.model.OrderableComparator;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 /**
  *
  * @author wilelb
  */
 public class AuthorsEditor extends ComposedField {

     private final static Logger logger = LoggerFactory.getLogger(AuthorsEditor.class);

     private final List<Editable<Creator>> authors = new ArrayList<>();

     private AuthorEditPanel pnl;

     private final ListView listview;

     @Override
     public boolean completeSubmit(AjaxRequestTarget target) {
         return false;
     }

     public class Editable<T extends Orderable> implements Serializable, Comparable {
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

         @Override
         public int compareTo(@NotNull Object o) {
             if(o instanceof Editable) {
                 return OrderableComparator.compare(
                     getData(),
                     ((Editable<T>) o).getData());
             }
             return 0;
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

             AjaxFallbackLink orderTopButton = new AjaxFallbackLink("btn_order_top") {
                 @Override
                 public void onClick(AjaxRequestTarget target) {
                     move(0, a.getId());
                     if (target != null) {
                         target.add(componentToUpdate);
                     }
                 }
             };
             orderTopButton.setEnabled(authors.size() > 1);
             add(orderTopButton);
             AjaxFallbackLink orderUpButton = new AjaxFallbackLink("btn_order_up") {
                 @Override
                 public void onClick(AjaxRequestTarget target) {
                     move(-1, a.getId());
                     if (target != null) {
                         target.add(componentToUpdate);
                     }
                 }
             };
             orderUpButton.setEnabled(authors.size() > 1);
             add(orderUpButton);
             AjaxFallbackLink orderDownButton = new AjaxFallbackLink("btn_order_down") {
                 @Override
                 public void onClick(AjaxRequestTarget target) {
                     move(1, a.getId());
                     if (target != null) {
                         target.add(componentToUpdate);
                     }
                 }
             };
             orderDownButton.setEnabled(authors.size() > 1);
             add(orderDownButton);
             AjaxFallbackLink orderBottomButton = new AjaxFallbackLink("btn_order_bottom") {
                 @Override
                 public void onClick(AjaxRequestTarget target) {
                     move(authors.size()-1, a.getId());
                     if (target != null) {
                         target.add(componentToUpdate);
                     }
                 }
             };
             orderBottomButton.setEnabled(authors.size() > 1);
             add(orderBottomButton);
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
                     pnl.setVisible(false);

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

         private final IModel<String> mdlFamilyName = new Model<>();
         private final IModel<String> mdlGivenName = new Model<>();
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
                 mdlFamilyName.setObject(a.getFamilyName());
                 mdlGivenName.setObject(a.getGivenName());
                 mdlEmail.setObject(a.getEMail());
                 mdlAffiliation.setObject(a.getOrganisation());
             }

             AbstractField f1 = new VcrTextField("author_family_name", "Family Name", "New author family name", mdlFamilyName, this,null);
             f1.setRequired(true);
             AbstractField f2 = new VcrTextField("author_given_name", "Given Name", "New author given name", mdlGivenName, this,null);
             f2.setRequired(true);
             AbstractField f3 = new VcrTextField("author_email", "Email", "New author email", mdlEmail, this,null);
             f3.setRequired(true);
             AbstractField f4 = new VcrTextField("author_affiliation", "Affiliation", "New author affiliation (optional)", mdlAffiliation, this,null);
             f4.setCompleteSubmitOnUpdate(true);

             fields.add(f1);
             fields.add(f2);
             fields.add(f3);
             fields.add(f4);

             add(f1);
             add(f2);
             add(f3);
             add(f4);
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
             logger.trace("completeSubmit :: focuscount = {}", this.focusCount);
             if(focusCount > 0) {
                 //One component of this composition still has focus, dont submit
                 logger.trace("focusCount > 0, in composition focus, no submit");
             } else if (focusCount < 0) {
                 //Should not happen, log to detect any issues
                 logger.trace("focusCount < 0, this is an invalid composition focus state");
             } else {
                 //focusCount = 0, so all components in the composition lost focus. Submit now.
                 logger.trace("Completing author submit: name="+mdlFamilyName.getObject()+
                         ", "+mdlGivenName.getObject()+", email=" + mdlEmail.getObject() +
                         ", affiliation=" + mdlAffiliation.getObject());
                 boolean valid = validate();
                 if (valid) {
                     String familyName = mdlFamilyName.getObject();
                     String givenName = mdlGivenName.getObject();
                     String email = mdlEmail.getObject();
                     String affiliation = mdlAffiliation.getObject() == null ? null : mdlAffiliation.getObject();

                     //Create or edit
                     if(editableAuthor == null) {
                         Creator creator = new Creator(mdlFamilyName.getObject(), mdlGivenName.getObject());
                         creator.setEMail(email);
                         creator.setOrganisation(affiliation);
                         authors.add(new Editable(creator));
                     } else {
                         editableAuthor.getData().setFamilyName(mdlFamilyName.getObject());
                         editableAuthor.getData().setGivenName(mdlGivenName.getObject());
                         editableAuthor.getData().setEMail(email);
                         editableAuthor.getData().setOrganisation(affiliation);
                         editableAuthor.setEditing(false);
                     }

                     mdlFamilyName.setObject("");
                     mdlGivenName.setObject("");
                     mdlEmail.setObject("");
                     mdlAffiliation.setObject("");

                     noAuthors.setVisible(authors.size() <= 0);
                     ajaxWrapper.setVisible(authors.size() > 0);
                     pnl.setVisible(true);

                     fireEvent(new DataUpdatedEvent(target));

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
         this(id, label, null);
     }

     public AuthorsEditor(String id, String label, VisabilityUpdater updater) {
         super(id, label, null, updater);
         setOutputMarkupId(true);

         final WebMarkupContainer editorwrapper = new WebMarkupContainer("editorwrapper");
         editorwrapper.setOutputMarkupId(true);

         final Component componentToUpdate = this;
         noAuthors = new Label("lbl_no_authors", "No authors found.<br />Please add one or more persons responsible for creation of this virtual collection.");
         noAuthors.setEscapeModelStrings(false);

         ajaxWrapper = new WebMarkupContainer("ajaxwrapper");
         ajaxWrapper.setOutputMarkupId(true);

         localDialog = new ModalConfirmDialog("authors_modal");
         localDialog.addListener(new Listener() {
             @Override
             public void handleEvent(final Event event) {
                 switch(event.getType()) {
                     case OK: event.updateTarget(ajaxWrapper); break;
                     case CONFIRMED_DELETE:
                             if(event.getData() == null) {
                                 logger.trace("No author found for removal");
                             } else {
                                 Creator a = (Creator)event.getData();
                                 logger.trace("Removing author with id = {}", a.getId());
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
                     case CANCEL: event.updateTarget(); break;
                 }
             }
         });
         add(localDialog);

         listview = new ListView("listview", authors) {
             @Override
             protected void populateItem(ListItem item) {
                 Editable<Creator> object = (Editable<Creator>) item.getModel().getObject();
                 logger.info("Authors ListView: {}, id={}, display order={}",
                         object.getData().getPerson(),
                         object.getData().getId(),
                         object.getData().getDisplayOrder());

                 if(!object.isEditing()) {
                     ActionablePanel _pnl = new AuthorPanel("pnl_author_details", object, componentToUpdate);
                     _pnl.addListener(new Listener<Creator>() {
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
                     _pnl.setVisible(pnl.isVisible());  //Hide panel after adding if we are currently editing
                     item.add(_pnl);
                 } else if(object.isEditing()) {
                     item.add(new AuthorEditPanel("pnl_author_details", object, componentToUpdate));
                 }
             }
         };
         ajaxWrapper.add(listview);

         noAuthors.setVisible(authors.isEmpty());
         ajaxWrapper.add(noAuthors);
         add(ajaxWrapper);

         pnl = new AuthorEditPanel("pnl_create_author", null, componentToUpdate);
         editorwrapper.add(pnl);
         add(editorwrapper);
     }

     /**
      * @return the authors
      */
     public List<Creator> getData() {
         List<Creator> result = new ArrayList<>();
         for(Editable<Creator> ea : authors) {
             Creator c = ea.getData();
             result.add(c);
         }
         return result;
     }

      /**
      * @param authors
      */
     public void setData(List<Creator> authors) {
         logger.trace("Set author data: {} authors", authors.size());
         this.authors.clear();
         for(Creator a : authors) {
             this.authors.add(new Editable<>(a));
         }
         noAuthors.setVisible(authors.isEmpty());
     }

     public void reset() {
         authors.clear();
         noAuthors.setVisible(authors.isEmpty());
     }

     @Override
     public boolean validate() {
         //Check for value if required == true
         if(required && authors.isEmpty()) {
             return setError("Required field.");
         }
         //All validators passed, reset error message and return true
         return setError(null);
     }

     protected void move(int direction, Long id) {
         //Abort on invalid direction
         if(direction < -1 || direction >= authors.size()) {
             logger.warn("Author list move: invalid direction={}, authors size={}.", direction, authors.size());
             return;
         }

         //Find index of specified (by id) collection
         int idx = -1;
         for(int i = 0; i < authors.size() && idx == -1; i++) {
             if(authors.get(i).getData().getId() == id) {
                 idx = i;
             }
         }

         //Abort if the collection was not found
         if(idx == -1) {
             logger.warn("Author list move: author with id = {} not found.", id);
             return;
         }

         //Swap the collection with the collection at the specified destination (up=1, down=-1, beginning=0 or end=i)
         if (direction == -1 && idx > 0) {
             authors.get(idx).getData().setDisplayOrder(new Long(idx-1));
             authors.get(idx-1).getData().setDisplayOrder(new Long(idx));
         } else if(direction == 1 && idx < authors.size()-1) {
             authors.get(idx).getData().setDisplayOrder(new Long(idx+1));
             authors.get(idx+1).getData().setDisplayOrder(new Long(idx));
         } else {
             authors.get(idx).getData().setDisplayOrder(new Long(direction));
             authors.get(direction).getData().setDisplayOrder(new Long(idx));
         }

         //Resort list based on new sort order
         Collections.sort(authors);
    }
}
