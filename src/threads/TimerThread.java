package threads;

import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

import helper.Packet;

public class TimerThread extends Observable implements Runnable {

	ConcurrentHashMap<Integer, Packet> packetList = new ConcurrentHashMap<Integer, Packet>();
	public static final long PACKET_TIMEOUT = 4000;
	public boolean wait = true;
	public TimerThread() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		while(wait)
		{
			checkTimeouts();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {	}
		}
	

	}
	
	public void checkTimeouts() {
		for(Integer i : packetList.keySet()) {
			long now = System.currentTimeMillis();
			Packet packet = packetList.get(i);
			long timeSent = packet.getTimeStamp();
			if(now - timeSent > PACKET_TIMEOUT) {
				setChanged();
				notifyObservers(packet);
				clearChanged();
				remove(i);
			}
		}
	}
	
	public void put(Integer index, Packet value) {
		packetList.put(index, value);
	}
	
	public void remove(Integer index) {
		packetList.remove(index);
	}

}
