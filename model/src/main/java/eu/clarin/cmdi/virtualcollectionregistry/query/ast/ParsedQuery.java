package eu.clarin.cmdi.virtualcollectionregistry.query.ast;

import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryUsageException;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection_;

public class ParsedQuery {
    private static final WhereClauseBuilder visitor = new WhereClauseBuilder();
    private final String query;
    private final ASTStart start;

    private ParsedQuery(String query, ASTStart start) {
        this.query = query;
        this.start = start;
    }

    public TypedQuery<Long> getCountQuery(EntityManager em, User owner,
            VirtualCollection.State state)
            throws VirtualCollectionRegistryException {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<VirtualCollection> root = cq.from(VirtualCollection.class);
            WhereClauseBuilder.Data data =
                new WhereClauseBuilder.Data(cb, cq, root);
            visitor.visit(start, data);

            Predicate where = data.getWhere();
            if (owner != null) {
                where = cb.and(where, cb.equal(
                            root.get(VirtualCollection_.owner), owner));
            }
            if (state != null) {
                where = cb.and(where, cb.equal(
                            root.get(VirtualCollection_.state), state));
            }
            cq.where(where);
            return em.createQuery(cq.select(cb.count(root)));
        } catch (Throwable e) {
            throw new VirtualCollectionRegistryException(
                    "error building query", e);
        }
    }

    public TypedQuery<VirtualCollection> getQuery(EntityManager em, User owner,
            VirtualCollection.State state)
            throws VirtualCollectionRegistryException {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<VirtualCollection> cq =
                cb.createQuery(VirtualCollection.class);
            Root<VirtualCollection> root = cq.from(VirtualCollection.class);
            WhereClauseBuilder.Data data =
                new WhereClauseBuilder.Data(cb, cq, root);
            visitor.visit(start, data);

            Predicate where = data.getWhere();
            if (owner != null) {
                where = cb.and(where, cb.equal(
                            root.get(VirtualCollection_.owner), owner));
            }
            if (state != null) {
                where = cb.and(where, cb.equal(
                            root.get(VirtualCollection_.state), state));
            }
            cq.where(where);
            return em.createQuery(cq.select(root));
        } catch (Throwable e) {
            throw new VirtualCollectionRegistryException(
                    "error building query", e);
        }
    }

    public String getPrettyPrinted() {
        StringBuilder sb = new StringBuilder();
        sb.append("query: ");
        sb.append(query);
        sb.append("\n");
        PrettyPrinter pp = new PrettyPrinter(sb);
        pp.visit(start, null);
        return sb.toString();
    }

    public static ParsedQuery parseQuery(String query)
        throws VirtualCollectionRegistryUsageException {
        try {
            QueryParser parser = new QueryParser(query);
            return new ParsedQuery(query, parser.start());
        } catch (Throwable e) {
            throw new VirtualCollectionRegistryUsageException(
                        "invalid query", e);
        }
    }

} // class ParsedQuery
