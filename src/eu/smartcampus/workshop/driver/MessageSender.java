package eu.smartcampus.workshop.driver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Creates thread for sending messages to a given host
 * Initialize ArrayBlockingQueue to store strings and to manage the aceessto these strings
 */
public class MessageSender extends Thread{

    private final static int QUEUE_MAX_STORAGE = 1024;

    private final Socket listeningConnection;

    private final BlockingQueue<String> queue = new ArrayBlockingQueue<String>(QUEUE_MAX_STORAGE);

    /**
     * Initializes objects final variable listeningConnection
     * @param listeningConnection socket value
     */
    public MessageSender(final Socket listeningConnection) {
        this.listeningConnection = listeningConnection;
    }

    public void run() {
        String sentence;
        try {
        	
        	
            while (true) {
               /*PrintWriter toRouter = new PrintWriter(listeningConnection.getOutputStream(),true);
                sentence = queue.take();
                toRouter.println(sentence);
            	*/
            	DataOutputStream outToServer = new DataOutputStream(listeningConnection.getOutputStream());
                sentence = queue.take();
                //outToServer.writeChars(sentence);
                outToServer.writeBytes(sentence);
                System.out.println("WROTE: " + sentence);
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
/**
 * Method puts string to the ArrayBlockingQueue
 * @param message String message for sending to the connected host
 */
    public void submitMessage(String message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] arg){
    	
		try {
			Socket socket = new Socket("10.254.1.1",50000);
			MessageSender sender = new MessageSender(socket);
			sender.start();
			Scanner in = new Scanner(System.in);
			while(true){
				System.out.println("read line: ");
				String message = in.nextLine();
				sender.submitMessage(message);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

    }

}
