package kp.com.j8583;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.solab.iso8583.IsoMessage;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import kp.com.j8583.iso8583.ISOMsgUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            SocketTask socketTask = new SocketTask(Constants.DEV_IP_ADDRESS, Constants.DEV_PORT);
            socketTask.execute();

            Log.d(TAG, "******* time - " + ISOMsgUtils.getDateAs("yyyyMMddHHmmss"));

            /*new Thread(new Runnable() {
                @Override
                public void run() {
                    ISOMsgUtils.makeRequest(MainActivity.this);
                }
            }).start();*/

            //IsoMessage msg = ISOMsgUtils.createISOMsg(this);
            //ISOMsgUtils.print(msg);

            //ISOMsgUtils.readData(this, new InputStreamReader(getAssets().open("parse.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class SocketTask extends AsyncTask<Void, Void, Void> {
        String mAddress;
        int mPort;
        String response = "";

        SocketTask(String addr, int port) {
            mAddress = addr;
            mPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Socket client;

            try{
                IsoMessage message = ISOMsgUtils.createISOMsg(MainActivity.this);
                Log.d(TAG, "********* Request message *********");
                ISOMsgUtils.print(message);

                // write the request iso message to socket
                client = new Socket(mAddress, mPort);
                client.setSoTimeout(10000); // 10 secs
                message.write(client.getOutputStream(), 2);

                // read the response iso message from socket
                IsoMessage response = ISOMsgUtils.readSocketInputStream(MainActivity.this, client);
                Log.d(TAG, "********* Response message *********");
                ISOMsgUtils.print(response);

                // alternative method to read the stream
                //IsoMessage response = ISOMsgUtils.readData(MainActivity.this, new InputStreamReader(client.getInputStream()));

            } catch(UnknownHostException e) {
                System.out.println("****** UnknownHostException - " + e.getMessage());
            } catch(IOException e) {
                System.out.println("****** IOException - " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

    }
}
