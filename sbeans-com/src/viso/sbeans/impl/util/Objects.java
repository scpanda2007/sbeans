package viso.sbeans.impl.util;

public class Objects {
	
	private Objects(){}
	
	public static void checkNull(String name, Object object){
		if(object==null){
			throw new NullPointerException(" ����:"+name+" ����Ϊ��");
		}
	}
}
