package eu.clarin.cmdi.virtualcollectionregistry.vcrql;

import java.io.StringReader;
import java.util.ArrayList;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;


public class Search {

	private static void dumpAST(StringBuilder sb, CommonTree ast) {
		showAST(sb, ast, 0, new ArrayList<CommonTree>());
	}
	
	private static void showAST(StringBuilder sb, CommonTree ast, int pos,
			ArrayList<CommonTree> parents) {
		if (ast == null) {
			sb.append("AST is null");
			return;
		}
		for (CommonTree parent : parents) {
			if ((parent.getParent() != null)
					&& (parent.getParent().getChildCount() >
							parent.getChildIndex() + 1)) {
				sb.append(" | ");
			} else {
				sb.append("   ");
			}
		}
		if ((ast.getParent() != null)
				&& (ast.getParent().getChildCount() >
						ast.getChildIndex() + 1)) {
			sb.append(" +-");
		} else {
			sb.append(" \\-");
		}
		ArrayList<CommonTree> newParents = new ArrayList<CommonTree>(parents);
		newParents.add(ast);
		showNode(sb, ast);
		for (int i = 0; i < ast.getChildCount(); i++) {
			showAST(sb, (CommonTree) ast.getChild(i), i, newParents);

		}
	}
	
	private static void showNode(StringBuilder sb, CommonTree ast) {
		if (ast == null) {
			sb.append("{node:null}");
		} else {
			switch (ast.getType()) {
			case VCRQLParser.EQ:
			case VCRQLParser.GT:
			case VCRQLParser.LT:
			case VCRQLParser.AND:
			case VCRQLParser.OR:
				sb.append("[");
				break;
			default:
				sb.append(" ");
			}
			sb.append(ast.toString());
			switch (ast.getType()) {
			case VCRQLParser.EQ:
			case VCRQLParser.GT:
			case VCRQLParser.LT:
			case VCRQLParser.AND:
			case VCRQLParser.OR:
				sb.append("]");
			}
		}
		sb.append("\n");
	}

	public static void doSearch(String query) {
		try {
			System.err.println("---------------------------------------------");
			System.err.println("QUERY = >>>" + query + "<<<");
			VCRQLLexer lexer =
				new VCRQLLexer(new ANTLRReaderStream(new StringReader(query)));
			VCRQLParser parser =
				new VCRQLParser(new CommonTokenStream(lexer));
			VCRQLParser.query_return result =
				parser.query();
			CommonTree ast = (CommonTree) result.getTree();
			if (ast != null) {
				StringBuilder sb = new StringBuilder();
				dumpAST(sb, ast);
				System.err.print(sb.toString());
			} else {
				System.err.println("==> PARSE FAILED!");
			}
		} catch (Throwable e) {
			e.printStackTrace(System.err);
		}
	}
}
