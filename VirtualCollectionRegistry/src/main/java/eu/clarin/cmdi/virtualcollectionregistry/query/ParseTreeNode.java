package eu.clarin.cmdi.virtualcollectionregistry.query;

interface ParseTreeNode {

    public void accept(ParseTreeNodeVisitor visitor);

} // interface ParseTreeNode
