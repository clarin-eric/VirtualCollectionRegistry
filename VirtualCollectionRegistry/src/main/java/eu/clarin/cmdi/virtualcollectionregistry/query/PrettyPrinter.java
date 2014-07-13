package eu.clarin.cmdi.virtualcollectionregistry.query;

import java.util.Stack;

public class PrettyPrinter implements QueryParserVisitor {
    private StringBuilder out;
    private Stack<Node> parents = new Stack<Node>();

    public PrettyPrinter(StringBuilder out) {
        if (out == null) {
            throw new NullPointerException("out == null");
        }
        this.out = out;
    }

    @Override
    public Object visit(ASTStart node, Object data) {
        out.append(node);
        out.append("\n");
        data = node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(ASTOr node, Object data) {
        printNode(node);
        parents.push(node);
        data = node.childrenAccept(this, data);
        parents.pop();
        return data;
    }

    @Override
    public Object visit(ASTAnd node, Object data) {
        printNode(node);
        parents.push(node);
        data = node.childrenAccept(this, data);
        parents.pop();
        return data;
    }

    @Override
    public Object visit(ASTPredicate node, Object data) {
        printNode(node);
        return data;
    }

    @Override
    public Object visit(SimpleNode node, Object data) {
        System.err.println("simpleNode");
        parents.push(node);
        data = node.childrenAccept(this, data);
        parents.pop();
        return data;
    }

    private void printNode(Node node) {
        for (Node parent : parents) {
            if (isLastSibling(parent)) {
                out.append("   ");
            } else {
                out.append(" | ");
            }
        }
        if (isLastSibling(node)) {
            out.append(" \\-");
        } else {
            out.append(" +-");
        }
        if (node instanceof ASTPredicate) {
            out.append(" ");
            out.append(node.toString());
            out.append("\n");
        } else {
            out.append("[");
            out.append(node.toString());
            out.append("]");
            out.append("\n");
        }
    }

    private boolean isLastSibling(Node node) {
        Node parent = node.jjtGetParent();
        if (parent == null) {
            return true;
        } else {
            int idx = parent.jjtGetNumChildren() -1;
            Node n = parent.jjtGetChild(idx);
            return node.equals(n);
        }
    }
} // class PrettyPrinter
