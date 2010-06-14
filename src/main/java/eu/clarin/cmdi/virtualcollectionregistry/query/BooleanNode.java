package eu.clarin.cmdi.virtualcollectionregistry.query;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

import eu.clarin.cmdi.virtualcollectionregistry.query.VCRQLLexer;

class BooleanNode extends CommonTree implements ParseTreeNode {
    public static enum Operator { AND, OR };

    private Operator operator;

    public BooleanNode(Token token) {
        super(token);
        this.operator = fromType(token.getType());
    }

    public Operator getOperator() {
        return operator;
    }

    public void accept(ParseTreeNodeVisitor visitor) {
        visitor.visit(this);
    }

    private static Operator fromType(int type) {
        switch (type) {
        case VCRQLLexer.AND:
            return Operator.AND;
        case VCRQLLexer.OR:
            return Operator.OR;
        default:
            throw new IllegalArgumentException("bad type: " + type);
        }
    }

} // class BooleanNode
