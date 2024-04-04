package eu.clarin.cmdi.virtualcollectionregistry.query.ast;

public class ASTPredicate extends SimpleNode {
    private int attribute;
    private int operator;
    private String value;

    public ASTPredicate(int id) {
        super(id);
    }

    public ASTPredicate(QueryParser p, int id) {
        super(p, id);
    }

    @Override
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

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Predicate[");
        switch (attribute) {
        case QueryParserConstants.VC_NAME:
            sb.append("vc_name");
            break;
        case QueryParserConstants.VC_DESC:
            sb.append("vc_desc");
            break;
        case QueryParserConstants.VC_STATE:
            sb.append("vc_state");
            break;
        case QueryParserConstants.VC_PURPOSE:
            sb.append("vc_purpose");
            break;
        case QueryParserConstants.VC_REPRODUCIBILITY:
            sb.append("vc_reproducibility");
            break;
        case QueryParserConstants.VC_MODIFIED:
            sb.append("vc_modified");
            break;
        case QueryParserConstants.VC_CREATED:
            sb.append("vc_created");
            break;
        case QueryParserConstants.VC_OWNER:
            sb.append("vc_owner");
            break;
        case QueryParserConstants.CR_PERSON:
            sb.append("cr_name");
            break;
        case QueryParserConstants.CR_EMAIL:
            sb.append("cr_email");
            break;
        case QueryParserConstants.CR_ORGANIZATION:
            sb.append("cr_organization");
            break;
        default:
            sb.append("0x");
            sb.append(Integer.toHexString(attribute));
        } // switch (attribute)
        sb.append(", ");
        switch (operator) {
        case QueryParserConstants.EQ:
            sb.append("eq");
            break;
        case QueryParserConstants.NE:
            sb.append("ne");
            break;
        case QueryParserConstants.GT:
            sb.append("gt");
            break;
        case QueryParserConstants.GE:
            sb.append("ge");
            break;
        case QueryParserConstants.LT:
            sb.append("lt");
            break;
        case QueryParserConstants.LE:
            sb.append("le");
            break;
         default:
             sb.append("0x");
             sb.append(Integer.toHexString(operator));
        } // switch (operator)
        sb.append(", ");
        sb.append(value);
        sb.append("]");
        return sb.toString();
    }
} // class ASTPredicate
