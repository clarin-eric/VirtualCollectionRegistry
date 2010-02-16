package eu.clarin.cmdi.virtualcollectionregistry.query;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;

class QueryNode extends CommonTree implements ParseTreeNode {

	public QueryNode(int type) {
		super(new CommonToken(type, "QUERY"));
	}
	
	public void accept(ParseTreeNodeVisitor visitor) {
		visitor.visit(this);
	}

} // class QueryNode
