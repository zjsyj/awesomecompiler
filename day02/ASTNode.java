import java.util.List;

/**
 * abstract syntactic tree node
 * including: type, text value, child node, parent node
 */
public interface ASTNode {
	public ASTNode getParent();

	public List<ASTNode> getChildren();

	public ASTNodeType getType();

	public String getText();
}