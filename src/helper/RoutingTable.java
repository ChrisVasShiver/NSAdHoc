package helper;

import java.net.InetAddress;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

public class RoutingTable extends Observable {

	private ConcurrentHashMap<InetAddress, DistanceVectorEntry> routingTable = new ConcurrentHashMap<InetAddress, DistanceVectorEntry>();

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

	public DistanceVectorEntry remove(InetAddress key) {
		DistanceVectorEntry result = routingTable.remove(key);
		setChanged();
		notifyObservers();
		clearChanged();
		return result;
	}
}
