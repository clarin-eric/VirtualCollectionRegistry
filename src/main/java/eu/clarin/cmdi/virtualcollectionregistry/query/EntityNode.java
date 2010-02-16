package eu.clarin.cmdi.virtualcollectionregistry.query;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

class EntityNode extends CommonTree implements ParseTreeNode {
	private String entity;
	private String property;
	
	public EntityNode(int type, Token entity, Token property) {
		super(new CommonToken(type, "ENTITY"));
		// check values for sanity!
		this.entity   = entity.getText();
		this.property = property.getText();
	}

	public String getEntity() {
		return entity;
	}
	
	public String getProperty() {
		return property;
	}

	public void accept(ParseTreeNodeVisitor visitor) {
		visitor.visit(this);
	}

} // class EntityNode
