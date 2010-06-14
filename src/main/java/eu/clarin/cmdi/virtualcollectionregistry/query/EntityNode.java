package eu.clarin.cmdi.virtualcollectionregistry.query;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

class EntityNode extends CommonTree implements ParseTreeNode {
    public static enum Entity { VC, CREATOR };
    private Entity entity;
    private String property;

    public EntityNode(int type, Token entity, Token property) {
        super(new CommonToken(type, "ENTITY"));
        this.entity = entityFromString(entity.getText());
        this.property = propertyFromString(property.getText());
    }

    public Entity getEntity() {
        return entity;
    }

    public String getProperty() {
        return property;
    }

    public void accept(ParseTreeNodeVisitor visitor) {
        visitor.visit(this);
    }

    private static Entity entityFromString(String s) {
        if (s == null) {
            throw new NullPointerException("s == null");
        }
        s = s.trim();
        if (s.equalsIgnoreCase("vc")) {
            return Entity.VC;
        } else if (s.equalsIgnoreCase("creator")) {
            return Entity.CREATOR;
        } else {
            throw new IllegalArgumentException("unknown entity: " + s);
        }
    }

    private static String propertyFromString(String s) {
        if (s != null) {
            s = s.trim();
        }
        if ((s == null) || (s.length() < 1)) {
            throw new IllegalArgumentException("property is null or empty");
        }
        return s.toLowerCase();
    }

} // class EntityNode
