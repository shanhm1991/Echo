package io.github.shanhm1991.echo.lang.pool;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负责监听配置变化,更新和提高poolMap中的pool
 * 
 * @author shanhm1991@163.com
 *
 */
@Slf4j
public final class PoolManager {

	private static final ConcurrentMap<String,Pool<?>> poolMap = new ConcurrentHashMap<>();

	private static final List<Pool<?>> poolRemoved = new ArrayList<>();

	private static final AtomicInteger removeCount = new AtomicInteger(0);

	public static void listen(File poolXml){
		load(poolXml);//确保在加载启动任务前已经加载过pool
		new Listener(poolXml).start();
		new Monitor().start();
	}
	
	public static Pool<?> get(String poolName) {
		return poolMap.get(poolName);
	}
	
	private static void remove(String name){
		Pool<?> pool = poolMap.remove(name);
		if(pool != null){
			pool.name = "removed-" + removeCount.incrementAndGet() + "-" + name;
			log.warn("rename pool[" + name + "] -> pool[" + pool.name + "]"); 
			poolRemoved.add(pool);
		}
	}

	private static void remveAll(){
		for (String name : poolMap.keySet()) {
			remove(name);
		}
	}
	
	/**
	 * Listener单线程调用
	 * @param file
	 */
	@SuppressWarnings("rawtypes")
	private static void load(File file){
		SAXReader reader = new SAXReader();
		reader.setEncoding("UTF-8");
		log.info("load pool: " + file); 
		Document doc;
		try{
			doc = reader.read(new FileInputStream(file));
		}catch(Exception e){
			log.error("", e); 
			remveAll();
			return;
		}

		Element pools = doc.getRootElement();
		Iterator it = pools.elementIterator("pool");

		Set<String> nameSet = new HashSet<>();
		while (it.hasNext()) {
			Element ePool = (Element) it.next();
			String name = ePool.attributeValue("name");
			String clazz = ePool.attributeValue("class");
			if(name == null){
				log.warn("no name, pool init failed."); 
				continue;
			}
			if(nameSet.contains(name)){
				log.warn("pool[" + name + "] already exist, init canceled."); 
				continue;
			}
			if(clazz == null){
				log.warn("no class, pool[" + name + "] init failed.");
				remove(name);
				continue;
			}

			Pool pool = poolMap.get(name);
			if(pool != null){
				if(pool.getClass().getName().equals(clazz)){
					try {
						pool.load(ePool);
						nameSet.add(name);
					} catch (Exception e) {
						log.error("pool[" + name + "] init failed.", e); 
						remove(name);
					}
					continue;
				}else{
					remove(name);
				}
			}

			try {
				Class<?> poolClass = Class.forName(clazz);
				Constructor ct = poolClass.getDeclaredConstructor(String.class);
				ct.setAccessible(true);
				pool = (Pool)ct.newInstance(name);
				pool.load(ePool);
				nameSet.add(name);
				poolMap.put(name, pool);
			} catch (Exception e) {
				log.error("pool[" + name + "] init failed.", e); 
			}
		}
		log.info("loaded pools=" + poolMap.keySet());
	}

	private static class Listener extends Thread {

		private final File poolXml;

		public Listener(File poolXml) {
			this.setName("pool-listener");
			this.setDaemon(true); 
			this.poolXml = poolXml;
		}

		@Override
		public void run() {
			String parentPath = poolXml.getParent();
			String name = poolXml.getName();

			WatchService watch;
			try {
				watch = FileSystems.getDefault().newWatchService();
				Paths.get(parentPath).register(watch, StandardWatchEventKinds.ENTRY_MODIFY); 
			} catch (IOException e) {
				log.error("pool listen failed", e); 
				return;
			}

			WatchKey key;
			while(true){
				try {
					key = watch.take();
				} catch (InterruptedException e) {
					return;
				}
				for(WatchEvent<?> event : key.pollEvents()){
					if(StandardWatchEventKinds.ENTRY_MODIFY == event.kind()){ 
						String eventName = event.context().toString();
						if(name.equals(eventName)){ 
							load(poolXml);
						}
					}
				}
				key.reset();
			}
		}
	}

	private static class Monitor extends Thread {
		
		private static final long DELAY = 15 * 1000;
		
		private static final int UNIT = 100;
		
		private int cleanTimes = 0;

		public Monitor() {
			this.setName("pool-monitor");
			this.setDaemon(true); 
		}

		@Override
		public void run() {
			while(true){
				try {
					sleep(DELAY);
				} catch (InterruptedException e) {
					//should never happened
				}

				Iterator<Pool<?>> it = poolRemoved.iterator();
				while(it.hasNext()){
					Pool<?> pool = it.next();
					pool.clean();
					if(pool.getLocalAlives() == 0){
						it.remove();
					}
				}

				for(Pool<?> pool : poolMap.values()){
					if(pool.aliveTimeOut == 0){
						continue;
					}
					pool.clean();
				}

				StringBuilder builder = new StringBuilder(", Details=[");
				for(Pool<?> pool : poolMap.values()){
					builder.append(pool.name).append(":").append(pool.getLocalAlives()).append("; ");
				}
				for(Pool<?> pool : poolRemoved){
					builder.append(pool.name).append("(removed):").append(pool.getLocalAlives()).append("; ");
				}
				String detail = builder.append("]").toString();
				if(cleanTimes++ % UNIT == 0){
					log.info("Total=" + Pool.getAlives() + detail); 
				}
			}
		}
	}
	
}
