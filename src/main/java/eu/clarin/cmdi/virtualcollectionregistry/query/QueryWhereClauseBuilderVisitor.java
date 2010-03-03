package eu.clarin.cmdi.virtualcollectionregistry.query;

import java.util.Stack;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator_;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection_;

class QueryWhereClauseBuilderVisitor<T> implements ParseTreeNodeVisitor {
	private CriteriaBuilder cb;
	private Root<VirtualCollection> vc;
	private Stack<Predicate> stack = new Stack<Predicate>();
	private Expression<?> property;
	private Object value;

	public QueryWhereClauseBuilderVisitor(CriteriaBuilder cb,
			CriteriaQuery<T> cq) {
		this.cb = cb;
		this.vc = cq.from(VirtualCollection.class);
	}

	public Root<VirtualCollection> getRoot() {
		return vc;
	}

	public Predicate getWhere() throws QueryException {
		if (stack.size() != 1) {
			throw new QueryException("[INTERNAL ERROR] " +
					"too many elements left on stack");
		}
		return stack.peek();
	}

	public void visit(QueryNode node) {
		if (node.getChildCount() != 1) {
			throw new RuntimeException("[INTERNAL ERROR] " +
					"QueryNode.childCount != 1");
		}
		// proceed ...
		((ParseTreeNode) node.getChild(0)).accept(this);
	}

	public void visit(BooleanNode node) {
		for (int i = 0; i < node.getChildCount(); i++) {
			ParseTreeNode child = (ParseTreeNode) node.getChild(i);
			child.accept(this);
		}
		Predicate pred = null;
		while (!stack.isEmpty()) {
			Predicate p = stack.pop();
			if (pred == null) {
				pred = p;
			} else {
				switch (node.getOperator()) {
				case AND:
					pred = cb.and(pred, p);
					break;
				case OR:
					pred = cb.or(pred, p);
					break;
				} // switch
			}
		}
		if (pred == null) {
			throw new RuntimeException("[INTERNAL ERROR] " +
					"unsupported boolean operator: " + node.getOperator());
		}
		stack.push(pred);
	}

	public void visit(RelationNode node) {
		if (node.getChildCount() != 2) {
			throw new RuntimeException("[INTERNAL ERROR] " +
					"RelationNode.childCount != 2");
		}
		/* clear property/value (sanity) and then visit children */
		property = null;
		value    = null;
		((ParseTreeNode) node.getChild(0)).accept(this);
		((ParseTreeNode) node.getChild(1)).accept(this);

		if ((property == null) || (value == null)) {
			throw new RuntimeException("[INTERNAL ERROR] no property or value");
		}

		Predicate predicate = null;
		switch (node.getRelation()) {
		case EQ:
			if (isStringProperty(property)) {
				predicate = cb.like(toStringExpression(property),
						toSQLString(value), '\\');
			} else {
				predicate = cb.equal(property, value);
			}
			break;
		case NE:
			if (isStringProperty(property)) {
				predicate = cb.notLike(toStringExpression(property),
						toSQLString(value), '\\');
			} else {
				predicate = cb.notEqual(property, value);
			}
		} // switch

		if (predicate == null) {
			throw new RuntimeException("[INTERNAL ERROR] no predicate");
		}
		stack.push(predicate);
	}

	public void visit(EntityNode node) {
		String        propertyName = node.getProperty();
		Expression<?> property     = null;

		switch (node.getEntity()) {
		case VC:
			if (propertyName.equals("name")) {
				property = vc.get(VirtualCollection_.name);
			} else if (propertyName.equals("description")) {
				property = vc.get(VirtualCollection_.description);
			}
			break;
		case CREATOR:
			if (propertyName.equals("name")) {
				property = vc.get(VirtualCollection_.creator)
					.get(Creator_.name);
			} else if (propertyName.equals("email")) {
				property = vc.get(VirtualCollection_.creator)
					.get(Creator_.email);
			} else if (propertyName.equals("organization")) {
				property = vc.get(VirtualCollection_.creator)
					.get(Creator_.organisation);
			}
			break;
		} // switch

		if (property == null) {
			throw new RuntimeException("[INTERNAL ERROR] unsupported " +
					"property: " + node.getEntity() + "." + propertyName );
		}
		this.property = property;
	}

	public void visit(ValueNode node) {
		this.value = node.getValue();
	}

	private boolean isStringProperty(Expression<?> exp) {
		return exp.getJavaType().isAssignableFrom(String.class);
	}

	@SuppressWarnings("unchecked")
	private static Expression<String> toStringExpression(Expression<?> exp) {
		try {
			return (Expression<String>) exp;
		} catch (ClassCastException e) {
			throw new RuntimeException("fail", e);
		}
	}

	private String toSQLString(Object o) {
		try {
			String s = (String) o;
			// escape SQL specific stuff
			s = s.replace("\\", "\\\\");
			s = s.replace("%", "\\%");
			s = s.replace("_", "\\_");
			// add SQL wildcards to VCRQL wildcards
			s = s.replace("*", "%");
			return s;
		} catch (ClassCastException e) {
			throw new RuntimeException("expected a java.lang.String instance");
		}
	}

} // class QueryBuilderVisitor
