import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

public class TcpServer {
	public static void main(String[] args) throws FileNotFoundException {
		try {
			// create a channel to send information
			ServerSocketChannel c = ServerSocketChannel.open();
			Console cons = System.console();
			
			boolean validPort = false;
			while (!validPort) {
				String portNum = cons.readLine("Enter port number: ");

				/* Check for valid port */
				try {
					c.bind(new InetSocketAddress(Integer.parseInt(portNum)));
					validPort = true;
				} catch (Exception e) {
					System.out.println("Invalid port number!");
				}
			}

			// continually loop and search for connections
			while (true) {
				// accept a socket channel and create a buffer to read in to
				SocketChannel sc = c.accept();
				TcpServerThread t = new TcpServerThread(sc);
				t.start();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}

class TcpServerThread extends Thread {
	SocketChannel sc;

	TcpServerThread(SocketChannel channel) {
		sc = channel;
	}

	public void run() {
		try {
			ByteBuffer buffer = ByteBuffer.allocate(10000000);
			sc.read(buffer);
			System.out.println("Client connected"); // User notification

			// obtain the name of the file in a string
			String fileName = new String(buffer.array());
			fileName = fileName.trim();
			System.out.println("number of characters: " + fileName.size());

			if (fileName.toString() == "Files") {
				/* ToDo: Send list of files to client */
			} 
			else {
				/* Check if file exists and print result */
				File f = new File(fileName);
				boolean fileFound = f.exists();
				if (fileFound) {
					System.out.println("File found!" + " " + fileName);
				} else {
					System.out.println("File not found!: " + fileName);
					// throw new Exception();
				}

				// create a file object to start
				File file = new File(fileName);
				FileChannel fileChan = FileChannel.open(file.toPath());
				ByteBuffer buff = ByteBuffer.allocate(10000000);
				int bytesread = fileChan.read(buff);
				// flip the bits accordingly and write them to the client
				while (bytesread != -1) {
					buff.flip();
					sc.write(buff);
					buff.compact();
					bytesread = fileChan.read(buff);
				}
			}
			// close the connection
			sc.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
