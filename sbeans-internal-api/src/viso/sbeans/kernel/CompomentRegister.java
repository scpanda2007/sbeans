package viso.sbeans.kernel;

public interface CompomentRegister {
	public void  register(Class<?> klazz,Object object);
	public <T> T getCompoment(Class<T> klazz);
}
