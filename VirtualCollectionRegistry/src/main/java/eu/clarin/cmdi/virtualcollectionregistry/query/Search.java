package eu.clarin.cmdi.virtualcollectionregistry.query;

import java.io.StringReader;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;


public class Search {

	public static String doSearch(String query) {
		try {
			System.err.println("---------------------------------------------");
			System.err.println("QUERY = >>>" + query + "<<<");
			VCRQLLexer lexer =
				new VCRQLLexer(new ANTLRReaderStream(new StringReader(query)));
			VCRQLParser parser =
				new VCRQLParser(new CommonTokenStream(lexer));
			VCRQLParser.query_return result =
				parser.query();
			ParseTreeNode node = null;
			try {
				node = (ParseTreeNode) result.getTree();
			} catch (ClassCastException e) {
				// should not happen
			}
			if (node != null) {
				node.accept(new PrettyPrinter(System.out));
			} else {
				System.err.println("==> PARSE FAILED!");
			}
		} catch (Throwable e) {
			e.printStackTrace(System.err);
		}
		return null;
	}
}
