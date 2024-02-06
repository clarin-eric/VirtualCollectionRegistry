package eu.clarin.cmdi.virtualcollectionregistry.wicket.model;

import org.apache.wicket.model.IModel;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class JsonLdModel implements IModel<String> {

    private final static GsonBuilder GSON_BUILDER = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting();

    private final IModel<JsonLdObject> jsonLdObjectModel;

    public JsonLdModel(IModel<JsonLdObject> jsonLdObjectModel) {
        this.jsonLdObjectModel = jsonLdObjectModel;
    }

    @Override
    public String getObject() {
        return GSON_BUILDER.create().toJson(jsonLdObjectModel.getObject());
    }

    @Override
    public void setObject(String obj) {

    }

    @Override
    public void detach() {
        jsonLdObjectModel.detach();
    }

    public static class JsonLdObject implements Serializable {

        @SerializedName("@context")
        private final String context;

        @SerializedName("@type")
        private final String type;

        public JsonLdObject(String type) {
            this(null, type);
        }

        public JsonLdObject(String context, String type) {
            this.context = context;
            this.type = type;
        }

        public String getContext() {
            return context;
        }

        public String getType() {
            return type;
        }

    }
}
