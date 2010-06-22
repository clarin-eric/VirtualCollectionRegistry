package eu.clarin.cmdi.virtualcollectionregistry.query;

public class ASTPredicate extends SimpleNode {
    private int attribute;
    private int operator;
    private Object value;

    public ASTPredicate(int id) {
        super(id);
    }

    public ASTPredicate(QueryParser p, int id) {
        super(p, id);
    }

    public Object jjtAccept(QueryParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
    
    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }
    
    public int getAttribute() {
        return attribute;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }
    
    public int getOperator() {
        return operator;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
    public Object getValue() {
        return value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Predicate(");
        sb.append(attribute);
        sb.append(",");
        sb.append(operator);
        sb.append(",");
        sb.append(value);
        sb.append(")");
        return sb.toString();
    }
} // class ASTPredicate
