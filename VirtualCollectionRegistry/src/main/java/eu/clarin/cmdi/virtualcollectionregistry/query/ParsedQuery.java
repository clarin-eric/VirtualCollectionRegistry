package eu.clarin.cmdi.virtualcollectionregistry.query;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;

import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection_;

public class ParsedQuery {
    private EntityManager em = null;
    private String query = null;
    private ParseTreeNode parseTree = null;

    private ParsedQuery(EntityManager em, String query,
                        ParseTreeNode parseTree) {
        if (em == null) {
            throw new NullPointerException("em == null");
        }
        this.em = em;
        if (query == null) {
            throw new NullPointerException("query == null");
        }
        this.query = query;
        if (parseTree == null) {
            throw new NullPointerException("parseTree == null");
        }
        this.parseTree = parseTree;
    }

    public void prettyPrint() {
        System.err.println("QUERY: " + query);
        parseTree.accept(new PrettyPrinter(System.err));
    }

    public TypedQuery<Long> getCountQuery(User owner,
            VirtualCollection.State state) throws QueryException {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            QueryWhereClauseBuilderVisitor<Long> visitor =
                new QueryWhereClauseBuilderVisitor<Long>(cb, cq);
            parseTree.accept(visitor);
            Predicate where = visitor.getWhere();
            if (owner != null) {
                where = cb.and(where, cb.equal(visitor.getRoot()
                        .get(VirtualCollection_.owner), owner));
            }
            if (state != null) {
                where = cb.and(where, cb.equal(visitor.getRoot()
                        .get(VirtualCollection_.state), state));
            }
            cq.where(where);
            return em.createQuery(cq.select(cb.count(visitor.getRoot())));
        } catch (Exception e) {
            throw new QueryException("error generating query", e);
        }
    }

    public TypedQuery<VirtualCollection> getQuery(User owner,
            VirtualCollection.State state) throws QueryException {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<VirtualCollection> cq = cb
                    .createQuery(VirtualCollection.class);
            QueryWhereClauseBuilderVisitor<VirtualCollection> visitor =
                new QueryWhereClauseBuilderVisitor<VirtualCollection>(cb, cq);
            parseTree.accept(visitor);
            Predicate where = visitor.getWhere();
            if (owner != null) {
                where = cb.and(where, cb.equal(visitor.getRoot()
                        .get(VirtualCollection_.owner), owner));
            }
            if (state != null) {
                where = cb.and(where, cb.equal(visitor.getRoot()
                        .get(VirtualCollection_.state), state));
            }
            cq.where(where);
            return em.createQuery(cq.select(visitor.getRoot()));
        } catch (Exception e) {
            throw new QueryException("error generating query", e);
        }
    }

    public static ParsedQuery parseQuery(EntityManager em, String q)
            throws QueryException {
        return new ParsedQuery(em, q, parseQuery(q));
    }

    private static ParseTreeNode parseQuery(String q) throws QueryException {
        try {
            VCRQLLexer lexer = new VCRQLLexer(new ANTLRStringStream(q));
            // dirty hack to let ANTLR fail early and pass on error message
            VCRQLParser parser = new VCRQLParser(new CommonTokenStream(lexer)) {
                @Override
                public void emitErrorMessage(String msg) {
                    if (msg.startsWith("line")) {
                        int pos1 = msg.indexOf(' ');
                        if (pos1 != -1) {
                            int pos2 = msg.indexOf(' ', pos1 + 1);
                            if (pos2 != -1) {
                                msg = msg.substring(pos2 + 1) + " [" +
                                        msg.substring(0, pos2) + "]";
                            }
                        }
                    }
                    throw new RuntimeException(msg);
                }
            };
            VCRQLParser.query_return result = parser.query();
            return (ParseTreeNode) result.getTree();
        } catch (RuntimeException e) {
            throw new QueryException("cannot parse query: " + e.getMessage());
        } catch (Exception e) {
            throw new QueryException("cannot parse query", e);
        }
    }

} // class ParsedQuery
