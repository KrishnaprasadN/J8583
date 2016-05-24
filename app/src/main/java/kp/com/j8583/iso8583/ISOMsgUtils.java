package kp.com.j8583.iso8583;

import android.content.Context;
import android.util.Log;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.impl.SimpleTraceGenerator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Krishnaprasad.n on 4/19/2016.
 */
public class ISOMsgUtils {

    public static IsoMessage readData(Context context, InputStreamReader inputStreamReader) {
        MessageFactory messageFactory = null;
        IsoMessage isoMessageResponse = null;
        try {
           /* messageFactory = ConfigParserMod.createFromInputStream(context.getAssets().open("config.xml"));
            messageFactory.setAssignDate(true);
            messageFactory.setTraceNumberGenerator(new SimpleTraceGenerator((int) (System
                    .currentTimeMillis() % 100000)));*/

            messageFactory = getMessageFactory(context);

            // this should be the actual server response
            LineNumberReader reader = new LineNumberReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null && line.length() > 0) {
                isoMessageResponse = messageFactory.parseMessage(line.getBytes(), 12);
                print(isoMessageResponse);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return isoMessageResponse;
    }

    public static IsoMessage createISOMsg(Context context) throws IOException {
        MessageFactory messageFactory = ConfigParserMod.createFromInputStream(context.getAssets().open("config.xml"));

        messageFactory.setAssignDate(true);
        messageFactory.setTraceNumberGenerator(new SimpleTraceGenerator((int) (System
                .currentTimeMillis() % 100000)));

        // Create a new message
        IsoMessage isoMessage = messageFactory.newMessage(0x1200);
        isoMessage.setBinary(true);

        // Set the required data
        /*isoMessage.setValue(4, new BigDecimal("501.25"), IsoType.AMOUNT, 0);
        isoMessage.setValue(12, new Date(), IsoType.TIME, 0);
        isoMessage.setValue(15, new Date(), IsoType.DATE4, 0);
        isoMessage.setValue(17, new Date(), IsoType.DATE_EXP, 0);
        isoMessage.setValue(37, 12345678, IsoType.NUMERIC, 12);
        isoMessage.setValue(41, "TEST-TERMINAL", IsoType.ALPHA, 16);*/

        return isoMessage;
    }

    public static void print(IsoMessage isoMessage) {
        System.out.printf("TYPE: %04x\n", isoMessage.getType());
        for (int i = 2; i < 128; i++) {
            if (isoMessage.hasField(i)) {
                System.out.printf("F %3d(%s): %s -> '%s'\n", i, isoMessage.getField(i)
                        .getType(), isoMessage.getObjectValue(i), isoMessage.getField(i)
                        .toString());
            }
        }
    }

    public static void makeRequest(Context context) {
        try {
            IsoMessage message = createISOMsg(context);

            Socket socket = new Socket("172.25.3.130", 23750);
            Client client = new Client(socket);

            Thread reader = new Thread(client, "j8583-client");
            reader.start();

            message.write(socket.getOutputStream(), 2);
            Log.d("Kp", "Waiting for responses");

            try {
                Thread.sleep(200);
                /*BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                InputStreamReader inputStreamReader = new InputStreamReader(bis, "US-ASCII");

                readData(context, inputStreamReader);*/
            } catch (Exception e) {
                e.printStackTrace();
            }

            client.stop();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static IsoMessage readSocketInputStream(Context context, Socket socket) throws IOException {
        MessageFactory messageFactory = getMessageFactory(context);
        IsoMessage isoMessageResponse = null;
        byte[] lengthBuffer = new byte[2];
        try {
            while (socket != null && socket.isConnected()) {
                socket.getInputStream().read(lengthBuffer);
                int size = ((lengthBuffer[0] & 0xff) << 8) | (lengthBuffer[1] & 0xff);

                byte[] buf = new byte[size];
                // We're not expecting ETX in this case
                if (socket.getInputStream().read(buf) == size) {
                    try {
                        // We'll use this header length as a reference.
                        // In practice, ISO headers for any message type are the
                        // same length.
                        String respHeader = messageFactory.getIsoHeader(0x200);
                        isoMessageResponse = messageFactory.parseMessage(buf, respHeader == null ? 12 : respHeader.length());

                        Log.d("Kp", String.format("Read response %s conf %s: %s",
                                isoMessageResponse.getField(11), isoMessageResponse.getField(38), new String(buf)));
                    } catch (ParseException ex) {
                        Log.d("Kp", "Parsing response" + ex);
                    }
                } else {
                    return null;
                }
            }
        } catch (IOException ex) {
            Log.d("Kp", "Parsing response" + ex);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                }

            }
        }

        return isoMessageResponse;
    }

    private static MessageFactory getMessageFactory(Context context) throws IOException {
        MessageFactory messageFactory = ConfigParserMod.createFromInputStream(context.getAssets().open("config.xml"));
        messageFactory.setAssignDate(true);
        messageFactory.setTraceNumberGenerator(new SimpleTraceGenerator((int) (System
                .currentTimeMillis() % 100000)));

        return messageFactory;
    }

    public static String getDateAs(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date(System.currentTimeMillis()));
    }


}
