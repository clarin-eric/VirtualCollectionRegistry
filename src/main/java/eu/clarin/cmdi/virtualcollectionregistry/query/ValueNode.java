package eu.clarin.cmdi.virtualcollectionregistry.query;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

class ValueNode extends CommonTree implements ParseTreeNode {
	private String value;
	
	ValueNode(Token token) {
		super(token);
		String s = token.getText();
		int spos = s.indexOf('"');
		int epos = s.lastIndexOf('"');
		if ((spos != -1) && (epos != -1)) {
			s = s.substring(spos + 1, epos);
		}
		value = s;
	}

	public String getValue() {
		return value;
	}

	public void accept(ParseTreeNodeVisitor visitor) {
		visitor.visit(this);
	}

} // class StringNode
