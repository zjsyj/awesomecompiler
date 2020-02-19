package play;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class PlayScript {

	public static void main(String[] args) {
		//String script = "int age = 44; { int i = 10; age+i;println(age+i);}";
		//String script = "int fun1(int a){return a+1;} println(fun1(2)); function int(int) fun2=fun1; fun2(3);";
		//String script = "class myclass{int a=2; int b; myclass(){ b = 3;} }  myclass c = myclass(); c.b;";
		String script = "int a=0; function int() fun1(){int b=0; int inner(){a=a+1; b=b+1; return b;} return inner;} function int() fun2 = fun1(); println(fun2()); println(fun2());";
		boolean genAsm = false;
		Map params = null;

		try {
			params = parseParams(args);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
		
		boolean help = params.containsKey("help") ? (Boolean)params.get("help") : false;
		if (help) {
			showHelp();
			return;
		}
		
		String scriptFile = params.containsKey("scriptFile") ? (String)params.get("scriptFile") : null;
		if (scriptFile != null) {
			try {
				script = readTextFile(scriptFile);
			}
			catch (Exception e ) {
				System.out.println("unable to read file from : " + scriptFile);
				return;
			}
		}
		
		boolean verbose = params.containsKey("verbose") ? (Boolean)params.get("verbose") : false;
		boolean ast_dump = params.containsKey("ast_dump") ? (Boolean)params.get("ast_dump") : false;
		
		verbose = true;

		if (script == null) {
			REPL(verbose, ast_dump);
		}
		else if (genAsm) {
			
		}
		else {
			PlayScriptCompiler compiler = new PlayScriptCompiler();
			AnnotatedTree at = compiler.compile(script, verbose, ast_dump);
			
			if (!at.hasCompilationError()) {
				Object result = compiler.Execute(at);
				System.out.println(result);
			}
		}
		
		
	}
	
	private static void showHelp() {
        System.out.println("usage: java play.PlayScript [-h | --help | -o outputfile | -S | -v | -ast-dump] [scriptfile]");

        System.out.println("\t-h or --help : print this help information");
        System.out.println("\t-v verbose mode : dump AST and symbols");
        System.out.println("\t-ast-dump : dump AST in lisp style");
        System.out.println("\t-o outputfile : file pathname used to save generated code, eg. assembly code");
        System.out.println("\t-S : compile to assembly code");
        System.out.println("\tscriptfile : file contains playscript code");

        System.out.println("\nexamples:");
        System.out.println("\tjava play.PlayScript");
        System.out.println("\t>>interactive REPL mode");
        System.out.println();

        System.out.println("\tjava play.PlayScript -v");
        System.out.println("\t>>enter REPL with verbose mode, dump ast and symbols");
        System.out.println();

        System.out.println("\tjava play.PlayScript scratch.play");
        System.out.println("\t>>compile and execute scratch.play");
        System.out.println();

        System.out.println("\tjava play.PlayScript -v scratch.play");
        System.out.println("\t>>compile and execute scratch.play in verbose mode, dump ast and symbols");
        System.out.println();
		return;
	}
	
	private static Map parseParams(String args[]) throws Exception {
		Map<String, Object> params= new HashMap<>();
		
		for (int i = 0; i < args.length; i++) {
			if (args[i].contentEquals("-S")) {
				params.put("genAsm", true);
			}
			else if (args[i].equals("-h") || args[i].equals("-H")) {
				params.put("help", true);
			}
			else if (args[i].equals("-v")) {
				params.put("verbos", true);
			}
			else if (args[i].equals("-ast-dump")) {
				params.put("ast_dump", true);
			}
			else if (args[i].equals("-o")) {
				if (i + 1 < args.length) {
					params.put("outputFile", args[++i]);
				}
				else {
					throw new Exception("Expecting a file path after -o");
				}
			}
			else if (args[i].startsWith("-")) {
				throw new Exception("Unknown parameter : " + args[i]);
			}
			else {
				params.put("scriptFile", args[i]);
			}
		}
		
		return params;
		
	}
	
	private static String readTextFile(String pathName) throws IOException {
		StringBuffer buffer = new StringBuffer();
		
		try (FileReader reader = new FileReader(pathName)) {
			BufferedReader br = new BufferedReader(reader);
			String line;
			
			while ((line = br.readLine()) != null) {
				buffer.append(line).append('\n');
			}
		}
		
		return buffer.toString();
	}
	
	public static void writeTextFile(String pathName, String text) throws IOException {
		File file = new File(pathName);
		
		file.createNewFile();
		try (FileWriter writer = new FileWriter(file)) {
			BufferedWriter out = new BufferedWriter(writer);
			StringReader reader = new StringReader(text);
			BufferedReader br = new BufferedReader(reader);
			
			String line = null;
			while ((line = br.readLine()) != null) {
				out.write(line);
			}
			out.flush();
		}
	}
	
	private static void REPL(boolean verbose, boolean ast_dump) {
		System.out.println("Enjoy PlayScript!");
		
		PlayScriptCompiler compiler = new PlayScriptCompiler();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String script = "";
		String scriptLet = "";
		System.out.println("\n>");
		
		while (true) {
			try {
				String line = reader.readLine().trim();
				if (line.equals("exit();")) {
					System.out.println("good bye");
					break;
				}
				
				scriptLet += line + "\n";
				if (line.endsWith(";")) {
					AnnotatedTree at = compiler.compile(script + scriptLet, verbose, ast_dump);
					
					if (!at.hasCompilationError()) {
						Object result = compiler.Execute(at);
						System.out.println(result);
						script = script + scriptLet;
					}
					
					System.out.print("\n>");
					scriptLet = "";
				}
			}
			catch (Exception e) {
				System.out.println(e.getLocalizedMessage());
				System.out.print("\n");
				scriptLet = "";
			}
		}
	}

}
