package threads;

import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

import helper.Packet;

public class TimerThread extends Observable implements Runnable {

	ConcurrentHashMap<Integer, Packet> packetList = new ConcurrentHashMap<Integer, Packet>();
	private int lastIndex = 0;
	public static final long PACKET_TIMEOUT = 2000;
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
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	

	}
	
	public void checkTimeouts() {
		for(Integer i : packetList.keySet()) {
			long now = System.currentTimeMillis();
			Packet packet = packetList.get(i);
			long timeSent = packet.getTimeStamp();
			if(now - timeSent > PACKET_TIMEOUT) {
				System.out.println("Timer elapsed!");
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
