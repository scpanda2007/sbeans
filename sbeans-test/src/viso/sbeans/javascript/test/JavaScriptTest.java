package viso.sbeans.javascript.test;

import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

public class JavaScriptTest {
	@Test
	public void test(){
		ScriptEngineManager semgr = new ScriptEngineManager();
		ScriptEngine engine = semgr.getEngineByName("JavaScript");
		try {
			engine.eval(new FileReader("test.js"));
			System.out.println(String.valueOf(((Invocable)engine).invokeFunction("test")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
