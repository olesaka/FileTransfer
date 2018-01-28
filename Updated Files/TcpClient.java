import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

public class TcpClient {
	public static void main(String[] args) {
		try {
			// open up the console to read in information
			Console cons = System.console();
			SocketChannel sc = SocketChannel.open();
			
			boolean validPortAndIp = false;
			while (!validPortAndIp) {
				String ipAdd = cons.readLine("Enter IP address: ");
				String portNum = cons.readLine("Enter port number: ");
				/* Check for valid ip and port */
				try {
					sc.connect(new InetSocketAddress(ipAdd, Integer.parseInt(portNum)));
					validPortAndIp = true;
				} catch (Exception e) {
					System.out.println("Invalid IP or port");
				}
			}
			
			// Send desired filename to server using a bytebuffer
			String fileName = cons.readLine("Enter file name or \"Files\" to view available files: ");
			ByteBuffer buf = ByteBuffer.wrap(fileName.getBytes());
			sc.write(buf);

			// create a new file object to use and 
			// allocate memory for a new buffer
			File file = new File(fileName);
			buf = ByteBuffer.allocate(10000000); // not sufficient for pictures
			
			if (fileName.toString() == "Files") {
				// read in the bytes for the list of files
				int bytesRead = sc.read(buf);
				/* ToDo: Get list of files from server */
			}
			else {
				// read in the bytes for the file with a file output stream
				// and file channel
				int bytesRead = sc.read(buf);
				FileOutputStream fos = new FileOutputStream(file);
				FileChannel fileChan = fos.getChannel();

				// flip the bits accordingly and write them to the file
				while (bytesRead != -1) {
					buf.flip();
					fileChan.write(buf);
					buf.compact();
					bytesRead = sc.read(buf);
				}
				// close the connections and streams
				sc.close();
				fos.close();
			}
			
		} catch (Exception e) {
			System.out.println("There was an IO Exception");
		}
	}
}
