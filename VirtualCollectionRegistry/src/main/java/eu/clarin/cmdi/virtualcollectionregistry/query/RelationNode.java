package eu.clarin.cmdi.virtualcollectionregistry.query;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

class RelationNode extends CommonTree implements ParseTreeNode {
    public static enum Relation { EQ, NE };

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
        case VCRQLLexer.NE:
            return Relation.NE;
        default:
            throw new IllegalArgumentException("bad relation type: " + type);
        }
    }

} // class RelationNode
