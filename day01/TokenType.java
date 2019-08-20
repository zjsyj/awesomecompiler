/**
 * Token types
 */
public enum TokenType {
	Plus, // +
	Minus, // -
	Star, // *
	Slash, // /

	GE, // >=
	GT, // >
	EQ, // =
	LE, // <=
	LT, // <

	SemiColon,  // ;
	LeftParen,  // (
	RightParen, // )

	Assignment, // =
	If,
	Else,

	Int,

	Identifier,

	IntLiteral,      // int
	StringLiteral,   // String
}