package eu.clarin.cmdi.virtualcollectionregistry.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jakarta.persistence.criteria.*;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User_;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection_;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class QueryOptions implements Serializable {
    public enum Property {
        VC_OWNER,
        VC_NAME,
        VC_TYPE,
        VC_STATE,
        VC_DESCRIPTION,
        VC_CREATION_DATE,
        VC_ORIGIN,
        VC_PARENT,
        VC_CHILD,
        VC_PUBLIC_LEAF
    } // enum QueryOptions.Property

    public enum Relation {
        EQ, NE, LT, LE, GT, GE, IN;

        private byte getCode() {
            switch (this) {
                case EQ:
                    return RELATION_EQ;
                case NE:
                    return RELATION_NE;
                case LT:
                    return RELATION_LT;
                case LE:
                    return RELATION_LE;
                case GT:
                    return RELATION_GT;
                case GE:
                    return RELATION_GE;
                case IN:
                    return RELATION_IN;
            }
            return -1;
        }
    } // enum QueryOptions.Relation

    private static final byte RELATION_NOOP = 0x00;   //00000000
    
    private static final byte RELATION_EQ = 0x01;   //00000001
    private static final byte RELATION_NE = 0x02;   //00000010
    private static final byte RELATION_LT = 0x04;   //00000100
    private static final byte RELATION_LE = 0x08;   //00001000
    private static final byte RELATION_GT = 0x10;   //00010000
    private static final byte RELATION_GE = 0x20;   //00100000
    private static final byte RELATION_IN = 0x40;   //01000000

    private static final byte RELATIONS_EQ_NE = RELATION_EQ | RELATION_NE;
    private static final byte RELATIONS_EQ_NE_IN = RELATION_EQ | RELATION_NE | RELATION_IN;
    private static final byte RELATIONS_ALL   = RELATION_EQ | RELATION_NE |
                                                RELATION_LT | RELATION_LE |
                                                RELATION_GT | RELATION_GE |
                                                RELATION_IN;
    public static final List<AbstractPropertyImpl> PROPERTIES =
        Arrays.asList(
                new PropertyImplOwner(),
                new PropertyImplName(),
                new PropertyImplType(),
                new PropertyImplState(),
                new PropertyImplDescription(),
                new PropertyImplCreationDate(),
                new PropertyImplOrigin(),
                new PropertyImplParent(),
                new PropertyImplChild(),
                new PropertyImplPublicLeaf()
        );
    
    
    private Filter filter;
    private List<OrderBy> orderByItems;

    public QueryOptions() {
        super();
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public boolean addSortProperty(Property property, boolean asc) {
        final AbstractPropertyImpl impl = getPropertyImpl(property);
        if (orderByItems == null) {
            orderByItems = new ArrayList<OrderBy>();
        } else {
            for (OrderBy item : orderByItems) {
                if (item.getProperty().equals(impl)) {
                    return false;
                }
            }
        }
        orderByItems.add(new OrderBy(impl, asc));
        return true;
    }

    public Filter and() {
        return new AndFilter();
    }

    public Filter or() {
        return new OrFilter();
    }

    public Filter expr(Property property, Relation relation, Object value) {
        return new ExpressionFilter(property, relation.getCode(), value);
    }

    @JsonIgnore
    public Predicate getWhere(CriteriaBuilder cb, AbstractQuery<?> cq,
            Root<VirtualCollection> root) {
        if (filter != null) {
            return filter.makePredicate(cb, cq, root);
        }
        return null;
    }

    public Order[] getOrderBy(CriteriaBuilder cb, Root<VirtualCollection> root) {
        Order[] order = null;
        if (orderByItems != null) {
            order = new Order[orderByItems.size()];
            int i = 0;
            for (OrderBy item : orderByItems) {
                order[i++] = item.makeOrder(cb, root);
            }
        }
        return order;
    }

    // Use this getter for JSON instead of the raw 'property' object
    @JsonProperty("property")
    public Property getPropertyName(Property property) {
        for (AbstractPropertyImpl predicate : PROPERTIES) {
            if (predicate.matchProperty(property)) {
                return predicate.getProperty();
            }
        }
        return null;
    }

    @JsonIgnore
    private static AbstractPropertyImpl getPropertyImpl(Property property) {
        for (AbstractPropertyImpl predicate : PROPERTIES) {
            if (predicate.matchProperty(property)) {
                return predicate;
            }
        }
        return null;
    }


    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, 
        include = JsonTypeInfo.As.PROPERTY, 
        property = "type"
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = QueryOptions.AndFilter.class, name = "and"),
        @JsonSubTypes.Type(value = QueryOptions.OrFilter.class, name = "or"),
        @JsonSubTypes.Type(value = QueryOptions.ExpressionFilter.class, name = "expr"),
        @JsonSubTypes.Type(value = QueryOptions.NullExpressionFilter.class, name = "isNull"),
        @JsonSubTypes.Type(value = QueryOptions.NotNullExpressionFilter.class, name = "isNotNull")
    })
    public static abstract class Filter implements Serializable {

        public final Filter add(Property property, Relation relation,
                Object value) {
            if (relation == null) {
                throw new IllegalArgumentException("relation == null");
            }
            doAdd(new ExpressionFilter(property, relation.getCode(), value));
            return this;
        }

        public final Filter addIsNull(Property property) {
            final AbstractPropertyImpl impl = getPropertyImpl(property);
            doAdd(new NullExpressionFilter(property));
            return this;
        }

        public final Filter addIsNotNull(Property property) {
            final AbstractPropertyImpl impl = getPropertyImpl(property);
            doAdd(new NotNullExpressionFilter(property));
            return this;
        }

        public abstract Filter add(Filter filter);

        protected abstract void doAdd(Filter filter);

        @JsonIgnore
        protected abstract Predicate makePredicate(CriteriaBuilder cb, AbstractQuery<?> cq, Root<VirtualCollection> root);

        public abstract List<Filter> getItems();
    } // QueryOptions.Filter


    public static class AndFilter extends Filter {
        private final List<Filter> items = new ArrayList<>();

        @Override
        public List<Filter> getItems() {
            return items;
        }

        public AndFilter() {}

        @Override
        public Filter add(Filter filter) {
            if (filter == null) {
                throw new IllegalArgumentException("filter == null");
            }
            if (filter == this) {
                throw new IllegalArgumentException("filter == this");
            }
            items.add(filter);
            return this;
        }

        @Override
        //protected void doAdd(ExpressionFilter filter) {
        protected void doAdd(Filter filter) {
            items.add(filter);
        }

        @JsonIgnore
        @Override
        protected Predicate makePredicate(CriteriaBuilder cb,
                                          AbstractQuery<?> cq, Root<VirtualCollection> root) {
            Predicate[] preds = new Predicate[items.size()];
            int i = 0;
            for (Filter filter : items) {
                preds[i++] = filter.makePredicate(cb, cq, root);
            }
            return cb.and(preds);
        }
    } // QueryOptions.AndFilter


    public static class OrFilter extends Filter {
        private final List<Filter> items = new ArrayList<>();

        public List<Filter> getItems() {
            return items;
        }

        public OrFilter() {}

        @Override
        public Filter add(Filter filter) {
            if (filter == null) {
                throw new IllegalArgumentException("filter == null");
            }
            if (filter == this) {
                throw new IllegalArgumentException("filter == this");
            }
            items.add(filter);
            return this;
        }

        @Override
        //protected void doAdd(ExpressionFilter filter) {
        protected void doAdd(Filter filter) {
            items.add(filter);
        }

        @JsonIgnore
        @Override
        protected Predicate makePredicate(CriteriaBuilder cb,
                                          AbstractQuery<?> cq, Root<VirtualCollection> root) {
            Predicate[] preds = new Predicate[items.size()];
            int i = 0;
            for (Filter filter : items) {
                preds[i++] = filter.makePredicate(cb, cq, root);
            }
            return cb.or(preds);
        }
    } // QueryOptions.OrFilter


    public static abstract class AbstractExpressionFilter extends Filter {
        protected final AbstractPropertyImpl property;
        protected final byte relation;
        protected final Object value;
        
        //TODO: Ideally this should perform the relation == null check and then call the other constructor with this(....). 
        //      This is not supported before java 25 (flexible constructors).
        public AbstractExpressionFilter(Property property, Relation relation, Object value) {
            if (relation == null) {
                throw new IllegalArgumentException("relation == null");
            }
            if(relation.getCode() != RELATION_NOOP) {
                if (property == null) {
                    throw new IllegalArgumentException("property == null");
                }
                if (value == null) {
                    throw new IllegalArgumentException("value == null");
                }
            }
            this.property = getPropertyImpl(property);
            this.relation = relation.getCode();
            this.value = value;
            
            if (relation.getCode() != RELATION_NOOP && !this.property.isValidRelation(relation)) {
                throw new IllegalArgumentException("invalid relation");
            }
        }
        
        public AbstractExpressionFilter(Property property, byte relation, Object value) {
            if(relation != RELATION_NOOP) {
                if (property == null) {
                    throw new IllegalArgumentException("property == null");
                }
                if (value == null) {
                    throw new IllegalArgumentException("value == null");
                }
            }
            this.property = getPropertyImpl(property);
            this.relation = relation;
            this.value = value;
            
            if (relation != RELATION_NOOP && !this.property.isValidRelation(relation)) {
                throw new IllegalArgumentException("invalid relation");
            }
        }

        /**
         * @return the property
         */
        @JsonIgnore
        public AbstractPropertyImpl getProperty() {
            return property;
        }

        @JsonProperty("property")
        public Property getPropertyEnum() {
            return property.getProperty();
        }
    
        /**
         * @return the relation
         */
        public byte getRelation() {
            return relation;
        }

        /**
         * @return the value
         */
        public Object getValue() {
            return value;
        }
        
        @Override
        public Filter add(Filter filter) {
            throw new RuntimeException("not permitted");
        }

        @Override
        protected void doAdd(Filter filter) {
            throw new RuntimeException("not permitted");
        }
        
        @Override
        public List<Filter> getItems() {
            return Arrays.asList(new Filter[] {});
        }
    }
    
    private static final class ExpressionFilter extends AbstractExpressionFilter {
        @JsonCreator
        public ExpressionFilter(
                @JsonProperty("property") Property property, 
                @JsonProperty("relation") byte relation, 
                @JsonProperty("value") Object value) {
            super(property, relation, value);
        }
        
        @JsonIgnore
        @Override
        public Predicate makePredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                Root<VirtualCollection> root) {
            return property.getPredicate(cb, cq, root, getRelation(), getValue());
        }
    } 
    
    private static final class NullExpressionFilter extends AbstractExpressionFilter {        
        @JsonCreator
        public NullExpressionFilter(@JsonProperty("property") Property property) {
            super(property, RELATION_NOOP, null);
        }

        @JsonIgnore
        @Override
        public Predicate makePredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                                       Root<VirtualCollection> root) {
            return property.getNullPredicate(cb, cq, root);
        }
    }

    private static final class NotNullExpressionFilter extends AbstractExpressionFilter {
        @JsonCreator
        public NotNullExpressionFilter(@JsonProperty("property") Property property) {
            super(property, RELATION_NOOP, null);
        }

        @JsonIgnore
        @Override
        public Predicate makePredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                                       Root<VirtualCollection> root) {
            return property.getNotNullPredicate(cb, cq, root);
        }
    }


    
    
    
    
    
    
    
    
    
    private static final class OrderBy implements Serializable {
        private final AbstractPropertyImpl property;
        private final boolean asc;

        @JsonCreator
        public OrderBy(@JsonProperty("property") Property property, @JsonProperty("asc") boolean asc) {
            this(getPropertyImpl(property), asc);
        }
        
        public OrderBy(AbstractPropertyImpl property, boolean asc) {
            this.property = property;
            this.asc = asc;
        }
        
        // Use this getter for JSON instead of the raw 'property' object
        @JsonProperty("property")
        public Property getPropertyName() {
            return property.getProperty();
        }

        @JsonIgnore
        public AbstractPropertyImpl getProperty() {
            return property;
        }

        public Order makeOrder(CriteriaBuilder cb, Root<VirtualCollection> root) {
            if (asc) {
                return cb.asc(property.getExpression(root));
            } else {
                return cb.desc(property.getExpression(root));
            }
        }
    } // class QueryOptions.OrderBy
    
    
    
    
    
    
    
    

    private static abstract class AbstractPropertyImpl {
        public abstract Property getProperty();

        public abstract byte getValidRelationMask();

        public abstract Expression<?> getExpression(
                Root<VirtualCollection> root);

        public abstract Predicate getPredicate(CriteriaBuilder cb,
                                               AbstractQuery<?> cq, Root<VirtualCollection> root,
                byte relation, Object value);

        public final boolean matchProperty(Property property) {
            return getProperty() == property;
        }

        public final boolean isValidRelation(byte relation) {
            return (getValidRelationMask() & relation) > 0;
        }
        
        public final boolean isValidRelation(Relation relation) {
            return (getValidRelationMask() & relation.getCode()) > 0;
        }

        public Predicate getNullPredicate(CriteriaBuilder cb, AbstractQuery<?> cq, Root<VirtualCollection> root) {
            throw new RuntimeException("not permitted");
        }

        public Predicate getNotNullPredicate(CriteriaBuilder cb, AbstractQuery<?> cq, Root<VirtualCollection> root) {
            throw new RuntimeException("not permitted");
        }

        //protected abstract Predicate makePredicate(CriteriaBuilder cb, Expression<?> expr, byte relation,
        //        Object value);
                /*
        protected final Predicate makePredicate(
                CriteriaBuilder cb, Expression<?> expr, byte relation,
                Object value) {
            switch (relation) {
            case RELATION_EQ:
                return cb.equal(expr, value);
            case RELATION_NE:
                return cb.notEqual(expr, value);
            case RELATION_IN:
                return expr.in(value);
            default:
                throw new InternalError("bad relation");
            } // switch
        }
*/
        protected static final Predicate makeDatePredicate(
                CriteriaBuilder cb, Expression<Date> expr, byte relation,
                Date value) {
            switch (relation) {
            case RELATION_EQ:
                return cb.equal(expr, value);
            case RELATION_NE:
                return cb.notEqual(expr, value);
            case RELATION_GT:
                return cb.greaterThan(expr, value);
            case RELATION_GE:
                return cb.greaterThanOrEqualTo(expr, value);
            case RELATION_LT:
                return cb.lessThan(expr, value);
            case RELATION_LE:
                return cb.lessThanOrEqualTo(expr, value);
            default:
                throw new InternalError("bad relation");
            } // switch
        }

        protected static final Predicate makeStringPredicate(
                CriteriaBuilder cb, Expression<String> expr, byte relation,
                String value) {
            // escape SQL specific stuff
            value = value.replace("\\", "\\\\");
            value = value.replace("%", "\\%");
            //value = value.replace("_", "\\_");
            // replace query language wildcards with add SQL wildcards
            value = value.replace("*", "%");

            switch (relation) {
            case RELATION_EQ:
                return cb.like(expr, value);
            case RELATION_NE:
                return cb.notLike(expr, value);
            default:
                throw new InternalError("bad relation");
            } // switch
        }
        
        protected static final Predicate makeBooleanPredicate(CriteriaBuilder cb, Expression<Boolean> expr, byte relation, Boolean value) {
            switch (relation) {
            case RELATION_EQ:
                return cb.equal(expr, value);
            case RELATION_NE:
                return cb.notEqual(expr, value);
            default:
                throw new InternalError("bad relation");
            } // switch
        }
        
        protected static final Predicate makeTypePredicate(CriteriaBuilder cb, Expression<VirtualCollection.Type> expr, byte relation, VirtualCollection.Type value) {
            switch (relation) {
            case RELATION_EQ:
                return cb.equal(expr, value);
            case RELATION_NE:
                return cb.notEqual(expr, value);
            default:
                throw new InternalError("bad relation");
            } // switch
        }
        
        protected static final Predicate makeStatePredicate(CriteriaBuilder cb, Expression<VirtualCollection.State> expr, byte relation, VirtualCollection.State value) {
            switch (relation) {
            case RELATION_EQ:
                return cb.equal(expr, value);
            case RELATION_NE:
                return cb.notEqual(expr, value);
            default:
                throw new InternalError("bad relation");
            } // switch
        }
        
        protected static final Predicate makeStateListPredicate(CriteriaBuilder cb, Expression<VirtualCollection.State> expr, byte relation, List value) {
            switch (relation) {
            case RELATION_IN:
                return expr.in(value);
            default:
                throw new InternalError("bad relation");
            } // switch
        }
                
        protected static final Predicate makeVirtualCollectionPredicate(
                CriteriaBuilder cb, Expression<VirtualCollection> expr, byte relation,
                VirtualCollection value) {
            switch (relation) {
            case RELATION_EQ:
                return cb.equal(expr, value);
            case RELATION_NE:
                return cb.notEqual(expr, value);
            case RELATION_IN:
                return expr.in(value);
            default:
                throw new InternalError("bad relation");
            } // switch
        }
    
    } // class QueryOptions.AbstractPropertyImpl


    private static final class PropertyImplOwner extends
            AbstractPropertyImpl {
        @Override
        public Property getProperty() {
            return Property.VC_OWNER;
        }

        @Override
        public byte getValidRelationMask() {
            return RELATIONS_EQ_NE;
        }

        @JsonIgnore
        @Override
        public Expression<String> getExpression(Root<VirtualCollection> root) {
            return root.get(VirtualCollection_.owner).get(User_.name);
        }

        @JsonIgnore
        @Override
        public Predicate getPredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                Root<VirtualCollection> root, byte relation, Object value) {
            final Expression<String> expr = getExpression(root);
            return makeStringPredicate(cb, expr, relation, (String) value);
        }
    } // class QueryOptions.PropertyImplOwner


    private static final class PropertyImplName extends
            AbstractPropertyImpl {
        @Override
        public Property getProperty() {
            return Property.VC_NAME;
        }

        @Override
        public byte getValidRelationMask() {
            return RELATIONS_EQ_NE;
        }

        @JsonIgnore
        @Override
        public Expression<String> getExpression(Root<VirtualCollection> root) {
            return root.get(VirtualCollection_.name);
        }

        @JsonIgnore
        @Override
        public Predicate getPredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                Root<VirtualCollection> root, byte relation, Object value) {
            final Expression<String> expr = getExpression(root);
            return makeStringPredicate(cb, expr, relation, (String) value);
        }
    } // class QueryOptions.PropertyImplName


    private static final class PropertyImplOrigin extends AbstractPropertyImpl {
        @Override
        public Property getProperty() { return Property.VC_ORIGIN; }

        @Override
        public byte getValidRelationMask() {
            return RELATIONS_EQ_NE;
        }

        @JsonIgnore
        @Override
        public Expression<String> getExpression(Root<VirtualCollection> root) {
            return root.get(VirtualCollection_.origin);
        }

        @JsonIgnore
        @Override
        public Predicate getPredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                                      Root<VirtualCollection> root, byte relation, Object value) {
            final Expression<String> expr = getExpression(root);
            return makeStringPredicate(cb, expr, relation, (String) value);
        }
    }

    private static final class PropertyImplParent extends AbstractPropertyImpl {
        @Override
        public Property getProperty() {
            return Property.VC_PARENT;
        }

        @Override
        public byte getValidRelationMask() {
            return 0x00;
        }

        @JsonIgnore
        @Override
        public Expression<VirtualCollection> getExpression(Root<VirtualCollection> root) {
            return root.get(VirtualCollection_.parent);
        }

        @JsonIgnore
        @Override
        public Predicate getPredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                                      Root<VirtualCollection> root, byte relation, Object value) {
            final Expression<VirtualCollection> expr = getExpression(root);
            return makeVirtualCollectionPredicate(cb, expr, relation, (VirtualCollection) value);
        }

        @JsonIgnore
        @Override
        public Predicate getNullPredicate(CriteriaBuilder cb, AbstractQuery<?> cq, Root<VirtualCollection> root) {
            final Expression<VirtualCollection> expr = getExpression(root);
            return cb.isNull(expr);
        }

        @JsonIgnore
        @Override
        public Predicate getNotNullPredicate(CriteriaBuilder cb, AbstractQuery<?> cq, Root<VirtualCollection> root) {
            final Expression<VirtualCollection> expr = getExpression(root);
            return cb.isNotNull(expr);
        }
    }

    private static final class PropertyImplChild extends AbstractPropertyImpl {
        @Override
        public Property getProperty() {
            return Property.VC_CHILD;
        }

        @Override
        public byte getValidRelationMask() {
            return 0x00;
        }

        @JsonIgnore
        @Override
        public Expression<VirtualCollection> getExpression(Root<VirtualCollection> root) {
            return root.get(VirtualCollection_.child);
        }

        @JsonIgnore
        @Override
        public Predicate getPredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                                      Root<VirtualCollection> root, byte relation, Object value) {
            final Expression<VirtualCollection> expr = getExpression(root);
            return makeVirtualCollectionPredicate(cb, expr, relation, (VirtualCollection) value);
        }
        
        @JsonIgnore
        @Override
        public Predicate getNullPredicate(CriteriaBuilder cb, AbstractQuery<?> cq, Root<VirtualCollection> root) {
            final Expression<VirtualCollection> expr = getExpression(root);
            return cb.isNull(expr);
        }

        @JsonIgnore
        @Override
        public Predicate getNotNullPredicate(CriteriaBuilder cb, AbstractQuery<?> cq, Root<VirtualCollection> root) {
            final Expression<VirtualCollection> expr = getExpression(root);
            return cb.isNotNull(expr);
        }
    }

    private static final class PropertyImplType extends AbstractPropertyImpl {
        @Override
        public Property getProperty() { return Property.VC_TYPE; }

        @Override
        public byte getValidRelationMask() {
            return RELATIONS_EQ_NE;
        }

        @JsonIgnore
        @Override
        public Expression<VirtualCollection.Type> getExpression(
                Root<VirtualCollection> root) {
            return root.get(VirtualCollection_.type);
        }

        @JsonIgnore
        @Override
        public Predicate getPredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                Root<VirtualCollection> root, byte relation, Object value) {
            final Expression<VirtualCollection.Type> expr =
                getExpression(root);
            return makeTypePredicate(cb, expr, relation, (VirtualCollection.Type) value);
        }
    } // class QueryOptions.PropertyImplName


    private static final class PropertyImplState extends AbstractPropertyImpl {
        @Override
        public Property getProperty() {
            return Property.VC_STATE;
        }

        @Override
        public byte getValidRelationMask() {
            return RELATIONS_EQ_NE_IN;
        }

        @JsonIgnore
        @Override
        public Expression<VirtualCollection.State> getExpression(
                Root<VirtualCollection> root) {
            return root.get(VirtualCollection_.state);
        }

        @JsonIgnore
        @Override
        public Predicate getPredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                Root<VirtualCollection> root, byte relation, Object value) {
            final Expression<VirtualCollection.State> expr =
                getExpression(root);
            //if(value instanceof LinkedList) {
            if(value instanceof List) {
                //While unmarshalling form JSON value is ceated as an ArrayList<String> but we want ArrayList<State>
                if(!((List) value).isEmpty() && ((List) value).get(0) instanceof String) {
                    ArrayList<VirtualCollection.State> _value = new ArrayList<>();
                    for(Object o : (List)value) {
                        VirtualCollection.State state = VirtualCollection.State.valueOf(o.toString().toUpperCase());
                        _value.add(state);
                    }
                    return makeStateListPredicate(cb, expr, relation, _value);
                }
                return makeStateListPredicate(cb, expr, relation, (List) value);
            } 
            return makeStatePredicate(cb, expr, relation, (VirtualCollection.State) value);
        }
    } // class QueryOptions.PropertyImplName

    private static final class PropertyImplDescription extends
            AbstractPropertyImpl {
        @Override
        public Property getProperty() {
            return Property.VC_DESCRIPTION;
        }

        @Override
        public byte getValidRelationMask() {
            return RELATIONS_EQ_NE;
        }

        @JsonIgnore
        @Override
        public Expression<String> getExpression(Root<VirtualCollection> root) {
            return root.get(VirtualCollection_.description);
        }

        @JsonIgnore
        @Override
        public Predicate getPredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                Root<VirtualCollection> root, byte relation, Object value) {
            final Expression<String> expr =
                getExpression(root);
            return makeStringPredicate(cb, expr, relation, (String) value);
        }
    }

    private static final class PropertyImplCreationDate extends
            AbstractPropertyImpl {
        @Override
        public Property getProperty() {
            return Property.VC_CREATION_DATE;
        }

        @Override
        public byte getValidRelationMask() {
            return RELATIONS_ALL;
        }

        @JsonIgnore
        @Override
        public Expression<Date> getExpression(Root<VirtualCollection> root) {
            return root.get(VirtualCollection_.creationDate);
        }

        @JsonIgnore
        @Override
        public Predicate getPredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                Root<VirtualCollection> root, byte relation, Object value) {
            final Expression<Date> expr = getExpression(root);
            return makeDatePredicate(cb, expr, relation, (Date) value);
        }
    }

    private static final class PropertyImplPublicLeaf extends AbstractPropertyImpl {
        @Override
        public Property getProperty() {
            return Property.VC_PUBLIC_LEAF;
        }

        @Override
        public byte getValidRelationMask() {
            return RELATIONS_EQ_NE;
        }

        @JsonIgnore
        @Override
        public Expression<Boolean> getExpression(Root<VirtualCollection> root) {
            return root.get(VirtualCollection_.publicLeaf);
        }

        @JsonIgnore
        @Override
        public Predicate getPredicate(CriteriaBuilder cb, AbstractQuery<?> cq,
                                      Root<VirtualCollection> root, byte relation, Object value) {
            final Expression expr = getExpression(root);
            return makeBooleanPredicate(cb, expr, relation, (Boolean) value);
        }
    }

} // class QueryOptions
