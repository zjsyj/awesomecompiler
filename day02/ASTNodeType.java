public enum ASTNodeType {
	Program,    // entry of program

	IntDeclaration,
	ExpressionStmt,  //expression sentence
	AssignmentStmt,  // assignment sentence

	Primary,
	Multiplicative,
	Additive,

	Identifier,
	IntLiteral
}