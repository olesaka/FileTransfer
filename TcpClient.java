import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

/*
	File performs basic client operations for file transfers with a server
	@Authors: Andrew Olesak, Joseph Seder, Keith Rodgers
*/

// class allows a connection to a single server
public class TcpClient {
	public static void main(String[] args) {
		try {
			// open up the console to read in information
			Console cons = System.console();
			SocketChannel sc = SocketChannel.open();

			// ask for IP address and port number
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

			// create a string for the file name
			String fileName = "";

			// continue loooping until the user wants to exit the current client
			while (fileName.toLowerCase().equals("exit") == false) {
				
				// Display options and get user input
				System.out.println("");
				System.out.println("Enter \"Files\" to view available files.");
				System.out.println("Enter \"Exit\" to close connection.");
				fileName = cons.readLine("Or enter desired file name : ");

				// Send desired filename to server using a bytebuffer
				ByteBuffer buf = ByteBuffer.wrap(fileName.getBytes());
				sc.write(buf);

				// allocate memory for a new buffer
				buf = ByteBuffer.allocate(4096); 

				/* Obtain list of files in server director */
				if (fileName.toLowerCase().equals("files")) {
					// read in the bytes for the list of files
					sc.read(buf);

					// obtain the comma separated list of files
					String commaSepFileList = new String(buf.array());
					commaSepFileList = commaSepFileList.trim();

					// print list of files to console
					for (String serverFileName : commaSepFileList.split(",")) {
						System.out.println(serverFileName);
					}
				}
				/* send message to server to disconnect */
				else if (fileName.toLowerCase().equals("exit")) {
					buf = ByteBuffer.wrap("exit".getBytes());
					sc.write(buf);
				}
				/* Obtain copy of desired file from server */
				else {

					// read the number of iterations the file will take
					ByteBuffer buffer = ByteBuffer.allocate(4096);
					sc.read(buffer);
					String its = new String(buffer.array());
					its = its.trim();	
					int iterations = Integer.parseInt(its);	
					
					// receive -1 if file is not found and continue to next iteration
					if (its.equals("-1")) {
						System.out.println("\r" + "File not found!: " + fileName + "\r");
						continue;
					}				

					// create a new file object to write to
					File file = new File(fileName);
					
					// read in the bytes for the file with a file output stream
					// and file channel
					int bytesRead = sc.read(buf);

					// prepare file streams
					FileOutputStream fos = new FileOutputStream(file);
					FileChannel fileChan = fos.getChannel();

					// flip the bits accordingly and write them to the file
					// bytesRead != -1					while (true) {
					for(int i=0; i<iterations; i++){
						buf.flip();
						fileChan.write(buf);
						buf.compact();
						bytesRead = sc.read(buf);
					}
					// complete the last iteration without reading another buffer
					buf.flip();
					fileChan.write(buf);
					buf.compact();
					fos.close();
					fileChan.close();
				}
			}

			// close the connections and streams
			sc.close();
		} catch (Exception e) {
			System.out.println("IO Exception: " + e);
		}
	}
}
