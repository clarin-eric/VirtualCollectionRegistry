package eu.clarin.cmdi.virtualcollectionregistry.query;

interface ParseTreeNodeVisitor {

	public void visit(QueryNode node);

	public void visit(BooleanNode node);

	public void visit(RelationNode node);

	public void visit(EntityNode node);

	public void visit(ValueNode node);

} // interface ParseTreeNodeVisitor
