package viso.sbeans.impl.store.db;

import java.io.File;
import java.io.FileNotFoundException;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.LockDetectMode;
import com.sleepycat.db.TransactionConfig;

public class DbEnvironment {
	Environment env;
	private static long kDefaultCacheSize = 128000000L;

	private static TransactionConfig fullIsolation = new TransactionConfig();
	static{
		fullIsolation.setReadCommitted(false);
		fullIsolation.setReadUncommitted(false);
	}
	private TransactionConfig defaultIsolation;
	private EnvironmentConfig config;
	
	private DbEnvironment(File dir) {
		config = new EnvironmentConfig();
		config.setTransactional(true);
		config.setAllowCreate(true);
		config.setCacheSize(kDefaultCacheSize);
		config.setInitializeCache(true);
		config.setInitializeLocking(true);
		config.setInitializeLogging(true);
		config.setLogAutoRemove(true);
		config.setRunRecovery(true);
		config.setLockDetectMode(LockDetectMode.YOUNGEST);
		config.setLockTimeout(10000000);
		config.setTxnWriteNoSync(true);//ª∫¥Ê–¥
		try {
			env = new Environment(dir, config);
			defaultIsolation = new TransactionConfig();
			defaultIsolation.setReadUncommitted(true);
			defaultIsolation.setReadCommitted(false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static DbEnvironment environment(String base, String dir) {
		String basedir = base;
		if (basedir == null) {
			basedir = new File("").getAbsolutePath();
		}
		File directory = new File(basedir + File.separator + dir);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				throw new IllegalArgumentException(
						"Failed to create directory:[ " + base
								+ File.pathSeparator + dir + " ].");
			}
		} else if (!directory.isDirectory()) {
			throw new IllegalArgumentException("The path:[ " + base
					+ File.pathSeparator + dir + " ] is not directory.");
		}
		return new DbEnvironment(directory);
	}

	public void close() {
		try {
			env.close();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public DbTransaction beginTransaction(int timeout){
		return beginTransaction(timeout,false);
	}
	
	public DbTransaction beginTransaction(long timeout,boolean allIsolation){
		return new DbTransaction(env, timeout, allIsolation?fullIsolation : defaultIsolation );
	}

	public BDBDatabase open(DbTransaction transaction, String fileName,
			boolean create) {
		return new BDBDatabase(env, transaction.getTransaction(), fileName,
				create);
	}
}
