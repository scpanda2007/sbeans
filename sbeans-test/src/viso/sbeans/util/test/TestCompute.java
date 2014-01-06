package viso.sbeans.util.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.junit.Test;

public class TestCompute {
	Stack<String> opStacks = new Stack<String>();
	Stack<Double> numberStacks = new Stack<Double>();
	static Map<String,Integer> optokens = new HashMap<String,Integer>();
	static {
		optokens.put("(",5);
		optokens.put(")",5);
		optokens.put("+",3);
		optokens.put("-",3);
		optokens.put("*",1);
		optokens.put("/",1);
		optokens.put("=",10);
		optokens.put("#",10);
	}
	
	Double opValue(Double left,Double right,String op){
		if(op.equals("-")){
			return left - right;
		}
		if(op.equals("+")){
			return left + right;
		}
		if(op.equals("*")){
			return left * right;
		}
		if(op.equals("/")){
			return left / right;
		}
		throw new IllegalArgumentException("´íÎóµÄÔËËã·ûºÅ:"+op);
	}
	
	
	public Double compute(String value){
		char values[] = value.trim().toCharArray();
		int index = 0;
		String number = "";
		List<String> input = new ArrayList<String>();
		
		while(index < values.length){
			String tmp = new String(""+values[index]);
			if(optokens.containsKey(tmp)){
				if(!number.isEmpty())input.add(number);
				input.add(tmp);
				number = "";
			}else{
				number += tmp;
			}
			index++;
		}
		if(!number.isEmpty())input.add(number);
		input.add("=");
		opStacks.push("#");
		
		System.out.println("input:"+input.toString());
		
		int max = 100;//·ÀÖ¹ËÀÑ­»·
		int counter = 0;
		String tmp = null;
		while(counter++ < max){
			if(tmp!=null){}
			else if(!input.isEmpty()){
				tmp = input.remove(0);
			}else{
				tmp = opStacks.pop();
			}
			if(optokens.containsKey(tmp)){
				String preToken = opStacks.peek();
				if(tmp.equals("=") && preToken.equals("#")){
					opStacks.pop();
					return numberStacks.pop();
				}
				System.out.println("pre:   "+preToken);
				System.out.println("tmp:   "+tmp);
				if(preToken.equals("(") && tmp.equals(")")){
					opStacks.pop();//µ¯³ö "("
					tmp = null;
				}
				else{
					if (!tmp.equals("(") && optokens.get(preToken) < optokens.get(tmp)) {
						opStacks.pop();
						numberStacks.push(opValue(numberStacks.pop(),
								numberStacks.pop(), preToken));
					}
					if(!tmp.equals(")")){
						opStacks.push(tmp);
						tmp = null;
					}
				}
			}else{
				numberStacks.push(Double.parseDouble(tmp));
				tmp = null;
			}
			System.out.println("op:"+opStacks.toString());
			System.out.println("number:"+numberStacks.toString());
			System.out.println("====================================");
		}
		return null;
	}
	@Test
	public void test(){
		String function_ = "(6+5)*3+(3+6)*4";
		System.out.println(function_+"="+compute(function_));
		function_ = "3+4*(3+6)*4";
		System.out.println(function_+"="+compute(function_));
	}
}
