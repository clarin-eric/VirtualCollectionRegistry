package eu.clarin.cmdi.virtualcollectionregistry.query;

import java.io.PrintStream;
import java.util.Stack;

import org.antlr.runtime.tree.CommonTree;

class PrettyPrinter implements ParseTreeNodeVisitor {
	private PrintStream out;
	private Stack<CommonTree> parents = new Stack<CommonTree>();

	public PrettyPrinter(PrintStream out) {
		if (out == null) {
			throw new NullPointerException("out == null");
		}
		this.out = out;
	}

	public void visit(QueryNode node) {
		for (int i = 0; i < node.getChildCount(); i++) {
			ParseTreeNode child = (ParseTreeNode) node.getChild(i);
			child.accept(this);
		}
	}

	public void visit(RelationNode node) {
		doIndent(node);
		out.print("[");
		out.print(node.getRelation());
		out.println("]");
		parents.push(node);
		for (int i = 0; i < node.getChildCount(); i++) {
			ParseTreeNode child = (ParseTreeNode) node.getChild(i);
			child.accept(this);
		}
		parents.pop();
	}

	public void visit(BooleanNode node) {
		doIndent(node);
		out.print("[");
		out.print(node.getOperator());
		out.println("]");
		parents.push(node);
		for (int i = 0; i < node.getChildCount(); i++) {
			ParseTreeNode child = (ParseTreeNode) node.getChild(i);
			child.accept(this);
		}
		parents.pop();
	}

	public void visit(EntityNode node) {
		doIndent(node);
		out.print(node.getEntity());
		out.print(".");
		out.println(node.getProperty());
	}

	public void visit(StringNode node) {
		doIndent(node);
		out.println(node.getValue());
	}

	private void doIndent(CommonTree node) {
		for (CommonTree parent : parents) {
			if ((parent.getParent() != null)
					&& (parent.getParent().getChildCount() >
							parent.getChildIndex() + 1)) {
				out.print(" | ");
			} else {
				out.print("   ");
			}
		}
		if ((node.getParent() != null)
				&& (node.getParent().getChildCount() >
						node.getChildIndex() + 1)) {
			out.append(" +-");
		} else {
			out.append(" \\-");
		}
	}

} // class PrettyPrinter
