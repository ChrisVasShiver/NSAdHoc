package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import helper.DistanceVectorEntry;
import helper.Packet;
import main.Client;
import threads.UniListeningThread;

public class MultiConnection implements Observer {

	private Client client;
	public static final InetAddress GROUP = getGroup();

	public MultiConnection(Client client) {
		this.client = client;
		client.ulRunnable.addObserver(this);
	}

	private static InetAddress getGroup() {
		InetAddress result = null;
		try {
			result = InetAddress.getByName("228.0.0.8");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public void sendMessage(String message) {
		Packet packet = new Packet(client.getLocalAddress(), GROUP, 0, 0, (byte) 0, System.currentTimeMillis(), 0, 0,
				Packet.dataToByteArray(message));
		for (InetAddress address : client.routingTable.keySet())
			sendPacket(packet, address);
	}

	private void sendPacket(Packet packet, InetAddress other) {
		DistanceVectorEntry dve = client.routingTable.get(other);
		if (dve == null) {
			System.out.println("Address " + other.getHostName() + " is not in your routing table.");
			return;
		}
		DatagramPacket dpack = new DatagramPacket(packet.getBytes(), packet.getBytes().length, dve.nextHop,
				client.uniPort);
		try {
			client.uniSocket.send(dpack);
		} catch (IOException e) {
			// TODO remove stack trace
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg0 instanceof UniListeningThread) {
			Packet packet = (Packet) arg1;
			if (packet.getDest().equals(GROUP)) {
				String msg = packet.getSrc().getHostName() + " (" + new Date(packet.getTimeStamp()) + "):"
						+ System.lineSeparator() + " ";

				msg += Packet.dataToString(packet.getData()) + System.lineSeparator();
				client.getGUI().setText(msg);
			}
		}

	}
}
