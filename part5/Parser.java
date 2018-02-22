/* *** This file is given as part of the programming assignment. *** */
import java.util.*; 
public class Parser {


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private ArrayList<ArrayList<String>> stack;
    private void scan() {
	tok = scanner.scan();
    }

    private Scan scanner;
    Parser(Scan scanner) {
	this.scanner = scanner;
	this.stack = new ArrayList<ArrayList<String>>();
	scan();
	program();
	if( tok.kind != TK.EOF )
	    parse_error("junk after logical end of program");
    }
    
        //搜索当前的scope，检查变量是否找到
    private Boolean search(String varName){
        Boolean found=false;
        int length = this.stack.size()-1;
        ArrayList<String> scope = this.stack.get(length);
        for(int i=0;i<scope.size();i++){
                 if (scope.get(i).contains(tok.string)){
                     found=true;
                 }
        } 
        return found;
    }
    //
    private Boolean searchAt(int N,String varName){ //N the integer in ˜Nx
        Boolean found=false;
        int length; //length of stack
        //-1 现在表示的是第0层，也就是没有数字
        if (N==-1){
            length =0;
        }
        else{
            length = this.stack.size()-1-N;
        }
        ArrayList<String> scope = this.stack.get(length);
        for(int i=0;i<scope.size();i++){
                 if (scope.get(i).contains(tok.string)){
                     found=true;
                     break;
                 }
        } 
        return found;
    }
    
    private Boolean revSearch(String varName){
        Boolean found=false;
        for(int j = this.stack.size() - 1; j >= 0; j--) {
            if (this.stack.get(j).contains(tok.string)){
                found = true;
            }
        }
        return found;
    }
    
    private int findPos(String varName){
        Boolean found=false;
        int pos=-1;
        for(int j = this.stack.size() - 1; j >= 0; j--) {
            if (this.stack.get(j).contains(tok.string)){
                found = true;
                pos = j;
                break;
            }
        }
        return pos;
    }

    private void program() {
        
    System.out.print("#include <stdio.h>"
                                        +'\n'+"#include <stdlib.h>"
                                        +'\n'+"int main()"
                                        +'\n'+"{"
                                        +'\n');
	block();
	
	
    }

    private void block(){
    ArrayList<String> scope = new ArrayList<String>();
    this.stack.add(scope);
	declaration_list();
	statement_list();
	this.stack.remove(this.stack.size()-1);
	System.out.println('\n'+"}");
    }

    private void declaration_list() {
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.
	while( is(TK.DECLARE) ) {
	    declaration();
	}
    }

    private void declaration() {
    int scopeLevel = this.stack.size()-1;
    ArrayList<String> scope = this.stack.get(scopeLevel);
	mustbe(TK.DECLARE);
	Boolean found= false;
	Boolean isError=false;
	if(is(TK.ID)){
	    if(scope.isEmpty()){
	        scope.add(tok.string);
	    }else{
	         found=search(tok.string);
	         if(found){
	            redeclare_error();
	            found=false;
	            isError=true;
	         }else{
	           scope.add(tok.string);
	         }
	    }
	}
	if(!isError) System.out.print("int x_"+scopeLevel+tok.string);
	mustbe(TK.ID);
	while( is(TK.COMMA) ) {
	    scan();
	    if(is(TK.ID)){
	        if(scope.isEmpty()){
	            scope.add(tok.string);
	           // System.out.println(tok.string);
	        }else{
	         found=search(tok.string);
	         if(!found){
	            scope.add(tok.string);
	           // System.out.println(tok.string);
	         }else{
	            redeclare_error();
	            found=false;
	            isError=true;
	         }
	        }
	        if(!isError) System.out.print(",x_"+scopeLevel+tok.string);
	    }
	    mustbe(TK.ID);
        }
    //if (isError) System.exit(1);
     System.out.print(";"+"\n");
    }

    private void statement_list() {
        while (is(TK.TILDE) || is(TK.ID) || is(TK.PRINT) 
            || is(TK.DO) || is(TK.IF) || is(TK.FOR)){
            statement();
        }
    }
    
    
    private void statement() { //statement ::= assignment | print | do | if
        if (is(TK.TILDE) || is(TK.ID)) assignment();
        else if(is(TK.PRINT)) print();
        else if(is(TK.DO)) DO();
        else if(is(TK.IF)) IF();
        else if(is(TK.FOR)) FOR();
    }
    
    private void FOR(){
        mustbe(TK.FOR);
        mustbe(TK.LPAREN);
        System.out.print("for(");
        assignment();
        mustbe(TK.SEP);
        compare_statement();
        mustbe(TK.SEP);
        increment();
        mustbe(TK.RPAREN);
        System.out.print("){"+'\n');
        block();
        mustbe(TK.ENDFOR);
    }
    
    private void compare(){
        if(is(TK.ENDDO)){
            System.out.print(">");
            mustbe(TK.ENDDO);
            if(is(TK.ASSIGN)){
                System.out.print("=");
                mustbe(TK.ASSIGN);
            }
        }else if(is(TK.DO)){
            System.out.print("<");
            mustbe(TK.DO);
            if(is(TK.ASSIGN)){
                System.out.print("=");
                mustbe(TK.ASSIGN);
            }
        }
    }
    
    private void assignment(){ //assignment ::= ref_id ’=’ expr
        ref_id();
        if(is(TK.ASSIGN)){
            System.out.print("=");
        }
        mustbe(TK.ASSIGN); 
        expr();
        System.out.print(";");
    }
    
    private void increment(){ //assignment ::= ref_id ’=’ expr
        ref_id();
        if(is(TK.ASSIGN)){
            System.out.print("=");
        }
        mustbe(TK.ASSIGN); 
        expr();
    }
    
    private void compare_statement(){ //guarded_command ::= expr ':' block
        factor();
        compare();
        factor();
        System.out.print(";");
    }
    
    private void print(){ //print ::= ’!’ expr
        mustbe(TK.PRINT); 
        System.out.print("printf(\"%d\\n\",");
        expr();
        System.out.print(");"+'\n');
    }
    private void DO(){ //do ::= ’<’ guarded_command ’>’
        mustbe(TK.DO); 
        System.out.print("while(0>=(");
        guarded_command();
        mustbe(TK.ENDDO); 
        
    }
    private void IF(){ //if ::= '[' guarded_command { '|' guarded_command } [ '%' block ] ']'
        mustbe(TK.IF);
        System.out.print("if(0>=(");
        guarded_command();
        while(is(TK.ELSEIF)){
            System.out.print("else if(0>=(");
            scan();
            guarded_command();
        }
        if(is(TK.ELSE)){
                scan();
                System.out.print("else{"+"\n");
                block();
            }
        mustbe(TK.ENDIF);
    }
    private void ref_id(){ //ref_id ::= [ ’˜’ [ number ] ] id
        int gobackNUM=-1; //-1 stands for no number after ˜ e.g ˜x
        int scopeLevel=this.stack.size()-1;
        Boolean found=false;
        //Boolean outOfscope=false;
        ArrayList<String> scope;
        if(is(TK.TILDE)){
            scan();
            if(is(TK.NUM)){
                gobackNUM = Integer.parseInt(tok.string);
                mustbe(TK.NUM);
                if(this.stack.size()-1-gobackNUM<0){
                 tilde_error(gobackNUM);
                }else{
                    found=searchAt(gobackNUM,tok.string);
                if (!found){
                    tilde_error(gobackNUM);
                }else{
                    int targetLevel=scopeLevel-gobackNUM;
                    System.out.print("x_"+targetLevel+tok.string);
                }
                }
            }else if(is(TK.ID)){
                found=searchAt(gobackNUM,tok.string);
                if (!found){
                    tilde_error(gobackNUM);
                }else{
                    System.out.print("x_"+0+tok.string);
                }
            }
            mustbe(TK.ID);
        }else{ //no tilde just id
            if(is(TK.ID)){
                found=revSearch(tok.string);
                if (!found){
                    undeclare_error();
                }else{
                    int pos=findPos(tok.string);
                    if (pos != -1){
                        System.out.print("x_"+pos+tok.string);
                    }else{
                        undeclare_error();
                    }
                    
                }
                
            }
            mustbe(TK.ID);
        }
    }
    
    private void guarded_command(){ //guarded_command ::= expr ':' block
        expr();
        System.out.print("))");
        mustbe(TK.THEN);
        System.out.println("{");
        block();
    }
    
    private void expr(){ //expr ::= term { addop term }
        term();
        while(is(TK.PLUS) || is(TK.MINUS)){
            addop();
            term();
        }
        
    }

    private void term(){ //term ::= factor { multop factor }
        factor();
        while(is(TK.TIMES) || is(TK.DIVIDE)){
            multop();
            factor();
        }
    }
    
    private void factor(){ //factor ::= ’(’ expr ’)’ | ref_id | number
        if(is(TK.LPAREN)){
            System.out.print("(");
            mustbe(TK.LPAREN);
            expr();
            mustbe(TK.RPAREN);
            System.out.print(")");
        }else if(is(TK.TILDE) || is(TK.ID)){
            ref_id();
        }else if(is(TK.NUM)){
            System.out.print(tok.string);
            mustbe(TK.NUM);
        }
        
    }
    
    private void addop(){ //addop ::= ’+’ | ’-’
        if(is(TK.PLUS)){
            System.out.print("+");
             mustbe(TK.PLUS);
        }else if(is(TK.MINUS)){
            System.out.print("-");
            mustbe(TK.MINUS);
        }
    }
    
    private void multop(){
        if(is(TK.TIMES)){
            System.out.print("*");
             mustbe(TK.TIMES);
        }else if(is(TK.DIVIDE)){
            System.out.print("/");
            mustbe(TK.DIVIDE);
        }
    }
    
    
    
    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
	if( tok.kind != tk ) {
	    System.err.println( "mustbe: want " + tk + ", got " +
				    tok);
	    parse_error( "missing token (mustbe)" );
	}

	scan();
    }

    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	System.exit(1);
    }
    
    private void undeclare_error() {
	System.err.println( tok.string + " is an undeclared variable on line "
			    + tok.lineNumber);
	System.exit(1);
    }
    
    private void tilde_error(int N) {
        if(N==-1){
            System.err.println("no such variable ~" + tok.string 
            + " on line " + tok.lineNumber);
        }else{
            System.err.println("no such variable ~" + N + tok.string 
            + " on line " + tok.lineNumber);
        }
        System.exit(1);
    }
    
    private void redeclare_error() {
	System.err.println( "redeclaration of variable "+tok.string);
    }
    
}
