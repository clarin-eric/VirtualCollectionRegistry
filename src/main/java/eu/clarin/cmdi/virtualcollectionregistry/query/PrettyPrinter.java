package eu.clarin.cmdi.virtualcollectionregistry.query;

import java.io.PrintStream;
import java.util.Stack;

public class PrettyPrinter implements QueryParserVisitor {
    private PrintStream out;
    private Stack<Node> parents = new Stack<Node>();

    public PrettyPrinter(PrintStream out) {
        if (out == null) {
            throw new NullPointerException("out == null");
        }
        this.out = out;
    }

    @Override
    public Object visit(ASTStart node, Object data) {
        out.println(node);
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
                out.print("   ");
            } else {
                out.print(" | ");
            }
        }
        if (isLastSibling(node)) {
            out.append(" \\-");
        } else {
            out.append(" +-");
        }
        if (node instanceof ASTPredicate) {
            out.print(" ");
            out.println(node.toString());
        } else {
            out.print("[");
            out.print(node.toString());
            out.println("]");
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
