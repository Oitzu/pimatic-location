package blackoise.de.pimaticlocation;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ShowLogfile extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_logfile);
        TextView textLogfile = (TextView) findViewById(R.id.TextViewLogfile);
        textLogfile.setMovementMethod(new ScrollingMovementMethod());


        try {

            BufferedReader inputReader = new BufferedReader(new InputStreamReader(

                    openFileInput("logfile")));

            String inputString;

            StringBuffer stringBuffer = new StringBuffer();

            while ((inputString = inputReader.readLine()) != null) {

                stringBuffer.append(inputString + "\n");

            }

            textLogfile.setText(stringBuffer.toString());

        } catch (IOException e) {

            e.printStackTrace();

        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_logfile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
