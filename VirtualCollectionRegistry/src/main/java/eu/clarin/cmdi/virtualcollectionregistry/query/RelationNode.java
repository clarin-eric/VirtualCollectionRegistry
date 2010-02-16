package eu.clarin.cmdi.virtualcollectionregistry.query;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

import eu.clarin.cmdi.virtualcollectionregistry.query.VCRQLLexer;

class RelationNode extends CommonTree implements ParseTreeNode {
	public static enum Relation { EQ, LT, GT };
	private Relation relation;
	
	public RelationNode(Token token) {
		super(token);
		this.relation = fromType(token.getType());
	}

	public Relation getRelation() {
		return this.relation;
	}

	public void accept(ParseTreeNodeVisitor visitor) {
		visitor.visit(this);
	}

	private static Relation fromType(int type) {
		switch (type) {
		case VCRQLLexer.EQ:
			return Relation.EQ;
		case VCRQLLexer.LT:
			return Relation.LT;			
		case VCRQLLexer.GT:
			return Relation.GT;
		default:
			throw new IllegalArgumentException("inavlid relation type");
		}
	}

} // class RelationNode
