package viso.sbeans.impl.store;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import viso.sbeans.impl.store.db.BDBDatabase;
import viso.sbeans.impl.store.db.DbCursor;
import viso.sbeans.impl.store.db.DbEnvironment;
import viso.sbeans.impl.store.db.DbTransaction;

public class DataUtility {

	private static final int SHASize = 20;
	private static final byte kDataClassInfoKey = 0;
	private static final byte kDataClassIdKey = 1;
	
	private static final ThreadLocal<MessageDigest> messageDigest =
		new ThreadLocal<MessageDigest>(){
			@Override
			protected MessageDigest initialValue(){
				try {
					return MessageDigest.getInstance("SHA-1");
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					throw new AssertionError(e);
				}
			}
		};
	
	private static byte[] getDataClassInfoKey(byte[] classinfo){
		MessageDigest digest = messageDigest.get();
		byte key[] = new byte[1+SHASize];
		key[0] = kDataClassInfoKey;
		try {
			digest.update(classinfo);
			int num = digest.digest(key, 1, SHASize);
			assert num == SHASize;
			return key;
		} catch (DigestException e) {
			// TODO Auto-generated catch block
			throw new AssertionError(e);
		}
	}
	
	private static byte[] getDataClassIdKey(int classId){
		byte[] key = DataEncoder.encodeInt(classId, 1);
		key[0] = kDataClassIdKey;
		return key;
	}
	
	/**
	 * 从类数据库中获取id对应的解析类ObjectStreamClass 的字节串
	 * */
	public static byte[] getClassInfo(BDBDatabase classDb, int classId, 
			DbEnvironment env, int timeout){
		DbTransaction dbTxn = env.beginTransaction(timeout);
		boolean done = false;
		try{
			byte[] result = classDb.get(getDataClassIdKey(classId), dbTxn, false);
			dbTxn.commit();
			done = true;
			return result;
		}finally{
			if(!done){
				dbTxn.abort();
			}
		}
	}
	
	/**
	 * 从类数据库中查找一个类的id,如果没有就生成一个并插入
	 * */
	public static int getClassId(BDBDatabase classDb, BDBDatabase classIdx, byte[] classInfo,
			DbEnvironment env, int timeout) {
		// TODO Auto-generated method stub
		byte[] classInfoKey = getDataClassInfoKey(classInfo);
		boolean done = false;
		DbTransaction dbTxn = env.beginTransaction(timeout, true);
		//这个地方会导致读事务生成一个 高隔离的 读锁，写锁的获取估计会等到所有读锁结束，这时如果
		//classIdx.get使用false，会在 classIdx.putNoOverWrite 时产生死锁
		try {
			int result;
			byte[] classIdKey = classIdx.get(classInfoKey, dbTxn, true);//不能使用false 否则在后面升写锁时 会造成死锁
			if (classIdKey != null) {
				result = DataEncoder.decodeInt(classIdKey, 1);
			} else {
//				classIdx.markForUpdate(classInfoKey, dbTxn); 
//				两把高隔离的读锁都想要升级为写锁时 会 嗝屁，因为他们都会等对方结束，我这里不想进行过多操作了 直接 get(true)
				DbCursor cursor = classDb.openCursor(dbTxn);
				try {
					result = cursor.findLast() ? DataEncoder.decodeInt(
							cursor.getKey(), 1) : 0;
					result += 1;
					classIdKey = getDataClassIdKey(result);
					if (!cursor.putNoOverWrite(classIdKey, classInfo)) {
						throw new IllegalStateException(
								"classIdKey already exist");
					}
				}finally{
					cursor.close();
				}
				if(!classIdx.putNoOverWrite(classInfoKey, classIdKey, dbTxn)){
					throw new IllegalStateException(
					"classInfoKey already exist");
				}
//				System.out.println("insert key:"+result);
			}
			dbTxn.commit();
			done = true;
			return result;
		} finally {
			if (!done) {
				dbTxn.abort();
			}
		}
	}

}
