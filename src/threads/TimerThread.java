package threads;

import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

import helper.Packet;

/**
 * @author M. van Helden, B. van 't Spijker, T. Sterrenburg, C. Visscher
 */
public class TimerThread extends Observable implements Runnable {

	ConcurrentHashMap<Integer, Packet> packetList = new ConcurrentHashMap<Integer, Packet>();
	public static final long PACKET_TIMEOUT = 4000;
	public boolean wait = true;

	/** 
	 * Checks every second whether packets have been expired
	 */
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
	
	/**
	 * Check whether a packet has been expired
	 */
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
	
	/**
	 * Put a new packet in the packet list
	 * Has to be called when a packet is sent
	 * @param index
	 * @param value
	 */
	public void put(Integer index, Packet value) {
		packetList.put(index, value);
	}
	
	/**
	 * Remove packet from the packet list 
	 * Has to be called after an ACK is received
	 * @param index
	 */
	public void remove(Integer index) {
		packetList.remove(index);
	}

}
