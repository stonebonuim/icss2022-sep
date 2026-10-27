grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';





//--- PARSER: ---
stylesheet: stylerule;
stylerule: (variableAssignment | cssname)*;

variableAssignment: CAPITAL_IDENT ASSIGNMENT_OPERATOR expression SEMICOLON;

cssname: selector OPEN_BRACE declaration* CLOSE_BRACE;

selector: LOWER_IDENT #tagSelector
        | ID_IDENT #idSelector
        | CLASS_IDENT #classSelector;

property: LOWER_IDENT;

declaration: property COLON expression SEMICOLON
           | ifcommand
           | variableAssignment;

mathvariable: PLUS | MIN ;

literal: PIXELSIZE #pixelLiteral
       | COLOR #colorLiteral
       | SCALAR #scalarLiteral
       | PERCENTAGE #percentageLiteral
       | (TRUE | FALSE) #boolLiteral;

expression: expression MUL expression
          | expression mathvariable expression
          | CAPITAL_IDENT
          | literal;

ifcommand: IF BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE OPEN_BRACE declaration* CLOSE_BRACE elsecommand?;

elsecommand: ELSE OPEN_BRACE declaration* CLOSE_BRACE;




