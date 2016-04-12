package helper;

import java.net.InetAddress;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher 
 */
public class RoutingTable extends Observable {
	//The routingtable contains the DistanceVector for every received IP
	private ConcurrentHashMap<InetAddress, DistanceVectorEntry> routingTable = new ConcurrentHashMap<InetAddress, DistanceVectorEntry>();

	/**
	 * Puts new distance vectors to IP's in the routingTable
	 * @param key Ip-address of neighbor
	 * @param value Distance Vector to key
	 */
	public void put(InetAddress key, DistanceVectorEntry value) {
			routingTable.put(key, value);
			setChanged();
			notifyObservers();
			clearChanged();
	}

	public DistanceVectorEntry get(InetAddress key) {
		return routingTable.get(key);
	}

	public KeySetView<InetAddress, DistanceVectorEntry> keySet() {
		return routingTable.keySet();
	}

	public int size() {
		return routingTable.size();
	}

	/**
	 * Removes distance vector in the routingTable
	 * @param key IP-address of neighbor
	 * @return removed key
	 */
	public DistanceVectorEntry remove(InetAddress key) {
		DistanceVectorEntry result = routingTable.remove(key);
		setChanged();
		notifyObservers();
		clearChanged();
		return result;
	}
}
