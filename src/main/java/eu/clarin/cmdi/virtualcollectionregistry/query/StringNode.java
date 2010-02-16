package eu.clarin.cmdi.virtualcollectionregistry.query;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

class StringNode extends CommonTree implements ParseTreeNode {
	private String value;
	
	StringNode(Token token) {
		super(token);
		String s = token.getText();
		int spos = s.indexOf('"');
		int epos = s.lastIndexOf('"');
		if ((spos != -1) && (epos != -1)) {
			value = s.substring(spos + 1, epos);
		} else {
			value = s;
		}
		// TODO: check escaping
	}

	public String getValue() {
		return value;
	}

	public void accept(ParseTreeNodeVisitor visitor) {
		visitor.visit(this);
	}

} // class StringNode
