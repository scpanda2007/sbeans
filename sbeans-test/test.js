importPackage(Packages.viso.sbeans.javascript.test);

function test(){
	return "This is a test in java.";
}

function test2(){
	CallJava.createInstance().test();
}