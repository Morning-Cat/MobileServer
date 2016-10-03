package server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ClientMap<K,V>
{
	//创建一个线程安全的HashMap，用来存储用户名和相对应的Socket
	public Map<K,V> map=Collections.synchronizedMap(new HashMap<K,V>());
	
	//通过value来删除相应的项
	public synchronized void removeByValue(Object value)
	{
		for(Object key:map.keySet())
		{
			if(map.get(key) == value)
			{
				map.remove(key);
				System.out.println("删除了客户端"+key);
				break;
			}
		}
	}
	
	
//获得HashMap中value组成的Set
	public synchronized Set<V> valueSet()
	{
		Set<V> result=new HashSet<V>();
		
		Collection values = map.values();
		
		for(Iterator iterator=values.iterator();iterator.hasNext();)
		{
			V value=(V)iterator.next();
			result.add(value);
		}
		
		return result;
	}
	
	//通过Value的值来获取Key的值，即通过用户名来获取相应的Socket
	public synchronized K getKeyByValue(V val)
	{
		for(K key:map.keySet())
		{
			if(map.get(key) == val||map.get(key).equals(val))
			{
				return key;
			}
		}
		return null;
	}
	
	//在HashMap中添加key,value对
	public synchronized V put(K key,V value)
	{
		for(V val:valueSet())
		{
			if(val.equals(value)&&val.hashCode() == value.hashCode())
			{
				//返还用户名重复信息
				System.out.println("value值重复！！");
			}
		}
		for(K keys:map.keySet())
		{
			if(key.equals(keys))
			{
				System.out.println("key值重复！！");
			}
		}
		return map.put(key, value);
	}
	
	
	
	
	
}
