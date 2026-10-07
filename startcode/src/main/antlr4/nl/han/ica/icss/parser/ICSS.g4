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

term:CAPITAL_IDENT| PIXELSIZE| SCALAR;
mathvariable:  PLUS| MIN| MUL ;
assigment: ASSIGNMENT_OPERATOR (COLOR| PIXELSIZE| TRUE| FALSE) SEMICOLON;

color: LOWER_IDENT  COLON  (COLOR| CAPITAL_IDENT)  SEMICOLON;
width: LOWER_IDENT  COLON  (PIXELSIZE| CAPITAL_IDENT)  SEMICOLON;
sumcommand:  LOWER_IDENT  COLON  sum SEMICOLON;

sum:  sum mathvariable sum | term ;
command: OPEN_BRACE  (color|width| sumcommand| ifcommand)*  CLOSE_BRACE;


variable: CAPITAL_IDENT assigment*;
cssname: LOWER_IDENT  command;
cssownname: (ID_IDENT|CLASS_IDENT) command ifcommand*;
ifcommand: ((IF|ELSE) (BOX_BRACKET_OPEN variable BOX_BRACKET_CLOSE)*) command;

cssstyle: variable* cssname* cssownname* ifcommand*;



//--- PARSER: ---
stylesheet:  cssstyle EOF;

