import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;


/*
	File performs basic functionality of a server for file 
	transfers with many clients
	@authors: Andrew Olesak, Joseph Seder, Keith Rodgers
*/

// class allows the server to connect with on client
public class TcpServer {
	public static void main(String[] args) throws FileNotFoundException {
		try {
			// create a channel to send information
			ServerSocketChannel c = ServerSocketChannel.open();
			Console cons = System.console();

			// let the user enter in a port number
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
				// accept a socket channel in a new thread
				SocketChannel sc = c.accept();
				TcpServerThread t = new TcpServerThread(sc);
				t.start();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}

// class allows the server to connect to multiple clients using a thread
class TcpServerThread extends Thread {
	// create the socketchannel
	SocketChannel sc;

	// create a thread
	TcpServerThread(SocketChannel channel) {
		sc = channel;
	}

	public void run() {
		try {
			// create a string for the file name
			String fileName = "";
			
			// let the user know when the server connects
			System.out.println("Client connected"); // User notification

			// loop until the client wants to exit, 
			while (fileName.toLowerCase().equals("exit") == false) {
				ByteBuffer buffer = ByteBuffer.allocate(4096);
				sc.read(buffer);

				// obtain the name of the file in a string
				fileName = new String(buffer.array());
				fileName = fileName.trim();

				// list the files by sending them to the client
				if (fileName.toLowerCase().equals("files")) {
					// retrieve list of files
					File folder = new File(new File("").getAbsolutePath());
					File[] fileList = folder.listFiles();
					String commaSepFileList = "";

					// create comma separated list of file names
					for (int i = 0; i < fileList.length; i++) {
						if (fileList[i].isFile()) {
							commaSepFileList = commaSepFileList + fileList[i].getName() + ",";
						}
					}

					// send comma separated file list
					ByteBuffer buff = ByteBuffer.wrap(commaSepFileList.getBytes());
					sc.write(buff);

					// exit the connection with the client
				} else if (fileName.toLowerCase().equals("exit")) {
					System.out.println("");
					System.out.println("Client has disconnected");
					System.out.println("");
				} else {
					/* Check if file exists and print result */
					File file = new File(fileName);
					boolean fileFound = file.exists();
					if (fileFound) {
						System.out.println("File found!" + " " + fileName);
					} else {
						// send -1 to client if file is not found and continue to next iteration
						System.out.println("File not found!: " + fileName);
						String notFound = Integer.toString(-1);
						ByteBuffer notFoundBuf = ByteBuffer.wrap(notFound.getBytes());
						sc.write(notFoundBuf);
						continue;

						// System.out.println("File not found!: " + fileName);
						// throw new FileNotFoundException();
					}

					// send the number of bytes divided by the size of the buffer
					int iterations = (int)(file.length()/4096);
					String its = Integer.toString(iterations);
					ByteBuffer buf = ByteBuffer.wrap(its.getBytes());
					sc.write(buf);
					FileChannel fileChan = FileChannel.open(file.toPath());

					ByteBuffer buff = ByteBuffer.allocate(4096);
					int bytesread = fileChan.read(buff);
					// flip the bits accordingly and write them to the client
					while (bytesread != -1) {
						buff.flip();
						sc.write(buff);
						buff.compact();
						bytesread = fileChan.read(buff);
					}
				}
			}
			
			// close the connection
			sc.close();
		// catch any exceptions in the program
		} catch (IOException e) {
			System.out.println("IO Exception: " + e);
		}
	}
}
