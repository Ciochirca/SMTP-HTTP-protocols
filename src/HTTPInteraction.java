/*************************************
 * Filename:  HTTPInteraction.java
 * Student-ID: 201360672
 * Date: 04.11.2020
 *************************************/

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Class for downloading one object from http server.
 *
 */
public class HTTPInteraction {
    private String host;
    private String path;
    private String requestMessage;


    private static final int HTTP_PORT = 80;
    private static final String CRLF = "\r\n";
    private static final int BUF_SIZE = 4096;
    private static final int MAX_OBJECT_SIZE = 102400;

    /* Create HTTPInteraction object. */
    public HTTPInteraction(String url) {

        /* Split "URL" into "host name" and "path name", and
         * set host and path class variables.
         * if the URL is only a host name, use "/" as path
         */
        String[] arrOfStr = url.split("/", 2);
        host = arrOfStr[0];
        path ="/" + arrOfStr[1];
        System.out.println("PATH: "+path+" "+"HOST: "+host);
        /* Fill in */


        /* Construct requestMessage, add a header line so that
         * server closes connection after one response. */
        requestMessage = "GET "+path+" HTTP/1.1"+CRLF+"Host: "+host+CRLF+CRLF;
        /* Fill in */

        return;
    }


    /* Send Http request, parse response and return requested object
     * as a String (if no errors),
     * otherwise return meaningful error message.
     * Don't catch Exceptions. EmailClient will handle them. */
    public String send() throws IOException {

        /* buffer to read object in 4kB chunks */
        char[] buf = new char[BUF_SIZE];

        /* Maximum size of object is 100kB, which should be enough for most objects.
         * Change constant if you need more. */
        char[] body = new char[MAX_OBJECT_SIZE];

        String statusLine="";	// status line
        int status;		// status code
        String headers="";	// headers
        int bodyLength=-1;	// lenghth of body

        String[] tmp;

        /* The socket to the server */
        Socket connection;

        /* Streams for reading from and writing to socket */
        BufferedReader fromServer;
        DataOutputStream toServer;

        System.out.println("Connecting server: " + host+CRLF);

        /* Connect to http server on port 80.
         * Assign input and output streams to connection. */
        connection = new Socket(host,  HTTP_PORT);
        InputStream is = connection.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        fromServer = new BufferedReader(isr);
        toServer = new DataOutputStream(connection.getOutputStream());

        System.out.println("Send request:\n" + requestMessage);

        /* Send requestMessage to http server */
        toServer.writeBytes(requestMessage);

        /* Read the status line from response message */
        statusLine= fromServer.readLine();
        System.out.println("Status Line:\n"+statusLine+CRLF);

        /* Extract status code from status line. If status code is not 200,
         * close connection and return an error message.
         * Do NOT throw an exception */
        tmp = statusLine.split(" ", 3);
        status = Integer.parseInt(tmp[1]);
        if (status != 200) {
            toServer.close();
            return  statusLine;
        }

        /* Read header lines from response message, convert to a string,
         * and assign to "headers" variable.
         * Recall that an empty line indicates end of headers.
         * Extract length  from "Content-Length:" (or "Content-length:")
         * header line, if present, and assign to "bodyLength" variable.
         */
        boolean gotStatusLine = false;
        while(statusLine.length() != 0) {
            statusLine = fromServer.readLine();
            if(!gotStatusLine) {
                gotStatusLine = true;
            } else {
                headers += statusLine + CRLF;
            }
            if (statusLine.startsWith("Content-Length:") || statusLine.startsWith("Content-length")) {
                tmp = statusLine.split(" ", 2);
                bodyLength = Integer.parseInt(tmp[1]);
            }
        }

        System.out.println("Headers:\n"+headers+CRLF);


        /* If object is larger than MAX_OBJECT_SIZE, close the connection and
         * return meaningful message. */
        if (bodyLength > MAX_OBJECT_SIZE) {
            toServer.close();
            return("The value " +bodyLength + " is too big, it shouldn't exceed " +MAX_OBJECT_SIZE+ ".");
        }

        /* Read the body in chunks of BUF_SIZE using buf[] and copy the chunk
         * into body[]. Stop when either we have
         * read Content-Length bytes or when the connection is
         * closed (when there is no Content-Length in the response).
         * Use one of the read() methods of BufferedReader here, NOT readLine().
         * Make sure not to read more than MAX_OBJECT_SIZE characters.
         */
        int bytesRead = 0;
        boolean connClose = false;

        if(bodyLength == -1) {
            connClose = true;
        }
        while(bytesRead < bodyLength || connClose) {
            int res = fromServer.read(buf, 0, BUF_SIZE);
            if (res == -1) {
                break;
            }
            for (int i = 0; i < res && (i + bytesRead) < MAX_OBJECT_SIZE; i++) {
                body[bytesRead + i] = buf[i];
            }
            bytesRead += res;
        }

        /* At this points body[] should hold to body of the downloaded object and
         * bytesRead should hold the number of bytes read from the BufferedReader
         */

        /* Close connection and return object as String. */
        System.out.println("Done reading file. Closing connection.");
        connection.close();
        return(new String(body, 0, bytesRead));
    }
}
