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
	//����һ���̰߳�ȫ��HashMap�������洢�û��������Ӧ��Socket
	public Map<K,V> map=Collections.synchronizedMap(new HashMap<K,V>());
	
	//ͨ��value��ɾ����Ӧ����
	public synchronized void removeByValue(Object value)
	{
		for(Object key:map.keySet())
		{
			if(map.get(key) == value)
			{
				map.remove(key);
				System.out.println("ɾ���˿ͻ���"+key);
				break;
			}
		}
	}
	
	
//���HashMap��value��ɵ�Set
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
	
	//ͨ��Value��ֵ����ȡKey��ֵ����ͨ���û�������ȡ��Ӧ��Socket
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
	
	//��HashMap�����key,value��
	public synchronized V put(K key,V value)
	{
		for(V val:valueSet())
		{
			if(val.equals(value)&&val.hashCode() == value.hashCode())
			{
				//�����û����ظ���Ϣ
				System.out.println("valueֵ�ظ�����");
			}
		}
		for(K keys:map.keySet())
		{
			if(key.equals(keys))
			{
				System.out.println("keyֵ�ظ�����");
			}
		}
		return map.put(key, value);
	}
	
	
	
	
	
}
