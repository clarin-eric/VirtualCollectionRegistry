package eu.clarin.cmdi.virtualcollectionregistry.query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Stack;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator_;
import eu.clarin.cmdi.virtualcollectionregistry.model.User_;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection_;

class WhereClauseBuilder implements QueryParserVisitor {
    static class Data {
        private CriteriaBuilder cb;
        private CriteriaQuery<?> cq;
        private Root<VirtualCollection> root;
        private Stack<Predicate> stack;
        
        public Data(CriteriaBuilder cb, CriteriaQuery<?> cq,
                Root<VirtualCollection> root) {
            this.cb = cb;
            this.cq = cq;
            this.root = root;
            this.stack = new Stack<Predicate>();
        }
        
        public CriteriaBuilder getBuilder() {
            return cb;
        }

        public CriteriaQuery<?> getQuery() {
            return cq;
        }
        
        public Root<VirtualCollection> getRoot() {
            return root;
        }
        
        public Stack<Predicate> getStack() {
            return stack;
        }
        
        public Predicate getWhere() {
            return stack.peek();
        }
    } // inner class Data
    
    @Override
    public Object visit(ASTStart node, Object d) {
        final Data data = (Data) d;
        node.childrenAccept(this, data);
        if (data.getStack().size() != 1) {
            throw new InternalError("expected only one element on stack");
        }
        return data;
    }

    @Override
    public Object visit(ASTOr node, Object d) {
        final Data data = (Data) d;
        node.childrenAccept(this, data);
        Predicate[] predicates = new Predicate[node.jjtGetNumChildren()];
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            predicates[i] = data.getStack().pop();
        }
        data.getStack().push(data.getBuilder().or(predicates));
        return data;
    }

    @Override
    public Object visit(ASTAnd node, Object d) {
        final Data data = (Data) d;
        node.childrenAccept(this, data);
        Predicate[] predicates = new Predicate[node.jjtGetNumChildren()];
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            predicates[i] = data.getStack().pop();
        }
        data.getStack().push(data.getBuilder().and(predicates));
        return data;
    }

    @Override
    public Object visit(ASTPredicate node, Object d) {
        final Data data = (Data) d;
        final Root<VirtualCollection> root = data.getRoot();
        
        Predicate predicate = null;
        switch (node.getAttribute()) {
        case QueryParserConstants.VC_NAME:
            predicate = makeStringPredicate(data,
                    root.get(VirtualCollection_.name),
                    node.getOperator(), node.getValue());
            break;
        case QueryParserConstants.VC_DESC:
            predicate = makeStringPredicate(data,
                    root.get(VirtualCollection_.description),
                    node.getOperator(), node.getValue());
            break;
        case QueryParserConstants.VC_STATE:
            predicate = makeStatePredicate(data,
                    node.getOperator(), node.getValue());
            break;
        case QueryParserConstants.VC_PURPOSE:
            predicate = makePurposePredicate(data,
                    node.getOperator(), node.getValue());
            break;
        case QueryParserConstants.VC_REPRODUCIBILITY:
            predicate = makeReproducibilityPredicate(data,
                    node.getOperator(), node.getValue());
            break;
        case QueryParserConstants.VC_CREATED:
            predicate = makeDatePredicate(data,
                    root.get(VirtualCollection_.dateCreated),
                    node.getOperator(), node.getValue());
            break;
        case QueryParserConstants.VC_MODIFIED:
            predicate = makeDatePredicate(data,
                    root.get(VirtualCollection_.dateModified),
                    node.getOperator(), node.getValue());
            break;
        case QueryParserConstants.VC_OWNER:
            predicate = makeStringPredicate(data,
                    root.get(VirtualCollection_.owner).get(User_.name),
                    node.getOperator(), node.getValue());
            break;
        case QueryParserConstants.CR_PERSON:
            predicate = makeStringPredicate(data,
                    root.join(VirtualCollection_.creators).get(Creator_.person),
                    node.getOperator(), node.getValue());
            break;
        case QueryParserConstants.CR_EMAIL:
            predicate = makeStringPredicate(data,
                    root.join(VirtualCollection_.creators).get(Creator_.email),
                    node.getOperator(), node.getValue());
            break;
        case QueryParserConstants.CR_ORGANIZATION:
            predicate = makeStringPredicate(data,
                    root.join(VirtualCollection_.creators)
                        .get(Creator_.organisation),
                    node.getOperator(), node.getValue());
            break;
        default:
            /* FALL-TROUGH */
        } // switch (node.getAttribute())

        if (predicate == null) {
            throw new InternalError("bad attribute");
        }
        data.getStack().push(predicate);
        return data;
    }

    @Override
    public Object visit(SimpleNode node, Object data) {
        throw new InternalError("unexpected node (" + node.value + ")");
    }

    private static Predicate makeStringPredicate(Data data,
            Expression<String> attribute, int operator, String value) {
        // escape SQL specific stuff
        value = value.replace("\\", "\\\\");
        value = value.replace("%", "\\%");
        value = value.replace("_", "\\_");
        // replace query language wildcards with add SQL wildcards
        value = value.replace("*", "%");

        // XXX: HSQLDB/Hibernate seems have trouble with escape char  
        switch (operator) {
        case QueryParserConstants.EQ:
            return data.getBuilder().like(attribute, value/*, '\\'*/);
        case QueryParserConstants.NE:
            return data.getBuilder().notLike(attribute, value/*, '\\'*/);
        default:
            throw new InternalError("bad operator");
        } // switch (operator)
    }

    private static Predicate makeStatePredicate(Data data, int operator,
            String value) {
        VirtualCollection.State state = null;
        if ("public".equalsIgnoreCase(value)) {
            state = VirtualCollection.State.PUBLIC;
        } else if ("private".equalsIgnoreCase(value)) {
            state = VirtualCollection.State.PRIVATE;
        } else if ("deleted".equalsIgnoreCase(value)) {
            state = VirtualCollection.State.DELETED;
        } else {
            throw new InternalError("bad value");
        }
        CriteriaBuilder cb = data.getBuilder();
        Expression<VirtualCollection.State> attribute =
            data.getRoot().get(VirtualCollection_.state);
        switch (operator) {
        case QueryParserConstants.EQ:
            // special case for public state
            if (state == VirtualCollection.State.PUBLIC) {
                return cb.or(
                        cb.equal(attribute,
                                 VirtualCollection.State.PUBLIC),
                        cb.equal(attribute,
                                 VirtualCollection.State.PUBLIC_PENDING)); 
            } else {
                return data.getBuilder().equal(attribute, state);
            }
        case QueryParserConstants.NE:
            // special case for public state
            if (state == VirtualCollection.State.PUBLIC) {
                return cb.and(
                        cb.notEqual(attribute,
                                    VirtualCollection.State.PUBLIC),
                        cb.notEqual(attribute,
                                    VirtualCollection.State.PUBLIC_PENDING)); 
                
            } else {
                return data.getBuilder().notEqual(attribute, state);
            }
        default:
            throw new InternalError("bad operator");
        } // switch (operator)
    }

    private static Predicate makePurposePredicate(Data data, int operator,
            String value) {
        VirtualCollection.Purpose purpose = null;
        if ("research".equalsIgnoreCase(value)) {
            purpose = VirtualCollection.Purpose.RESEARCH;
        } else if ("reference".equalsIgnoreCase(value)) {
            purpose = VirtualCollection.Purpose.REFERENCE;
        } else if ("sample".equalsIgnoreCase(value)) {
            purpose = VirtualCollection.Purpose.SAMPLE;
        } else if ("future-use".equalsIgnoreCase(value)) {
            purpose = VirtualCollection.Purpose.FUTURE_USE;
        } else {
            throw new InternalError("bad value");
        }
        Expression<VirtualCollection.Purpose> attribute =
            data.getRoot().get(VirtualCollection_.purpose);
        switch (operator) {
        case QueryParserConstants.EQ:
            return data.getBuilder().equal(attribute, purpose);
        case QueryParserConstants.NE:
            return data.getBuilder().notEqual(attribute, purpose);
        default:
            throw new InternalError("bad operator");
        } // switch (operator)
    }

    private static Predicate makeReproducibilityPredicate(Data data, 
            int operator, String value) {
        VirtualCollection.Reproducibility reproducability = null;
        if ("intended".equalsIgnoreCase(value)) {
            reproducability = VirtualCollection.Reproducibility.INTENDED;
        } else if ("fluctuating".equalsIgnoreCase(value)) {
            reproducability = VirtualCollection.Reproducibility.FLUCTUATING;
        } else if ("untended".equalsIgnoreCase(value)) {
            reproducability = VirtualCollection.Reproducibility.UNTENDED;
        } else {
            throw new InternalError("bad value");
        }
        Expression<VirtualCollection.Reproducibility> attribute =
            data.getRoot().get(VirtualCollection_.reproducibility);
        switch (operator) {
        case QueryParserConstants.EQ:
            return data.getBuilder().equal(attribute, reproducability);
        case QueryParserConstants.NE:
            return data.getBuilder().notEqual(attribute, reproducability);
        default:
            throw new InternalError("bad operator");
        } // switch (operator)
    }

    private static Predicate makeDatePredicate(Data data,
            Expression<Date> attribute, int operator, String value) {
        Date date = null;
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
            if (value.endsWith("Z")) {
                value = value.substring(0, value.length() - 1) + "GMT-00:00";
            } else {
                int inset = 6;
                String s0 =
                    value.substring(0, value.length() - inset);
                String s1 =
                    value.substring(value.length() - inset, value.length());
                value = s0 + "GMT" + s1;
            }
            date = df.parse(value);
        } catch (ParseException e) {
            throw new RuntimeException("invalid date", e);
        }
        
        CriteriaBuilder cb = data.getBuilder();
        switch (operator) {
        case QueryParserConstants.EQ:
            return cb.equal(attribute, date);
        case QueryParserConstants.NE:
            return cb.notEqual(attribute, date);
        case QueryParserConstants.GT:
            return cb.greaterThan(attribute, date);
        case QueryParserConstants.GE:
            return cb.greaterThanOrEqualTo(attribute, date);
        case QueryParserConstants.LT:
            return cb.lessThan(attribute, date);
        case QueryParserConstants.LE:
            return cb.lessThanOrEqualTo(attribute, date);
        default:
            throw new InternalError("bad operator");
        }
    }

} // class WhereClauseBuilder
