package de.blackoise.pimaticlocation;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class ShowLogfile extends Activity {

    TextView textLogfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_logfile);
        textLogfile = (TextView) findViewById(R.id.TextViewLogfile);
        textLogfile.setMovementMethod(new ScrollingMovementMethod());
        loadLogfile();
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

    public void loadLogfile() {
        try {

            BufferedReader inputReader = new BufferedReader(new InputStreamReader(

                    openFileInput("logfile")));

            String inputString;

            StringBuffer stringBuffer = new StringBuffer();

            while ((inputString = inputReader.readLine()) != null) {

                stringBuffer.append(inputString + "\n");

            }

            textLogfile.setText(stringBuffer.toString());
           // int scrollAmount = textLogfile.getLayout().getLineTop(textLogfile.getLineCount()) - textLogfile.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
           // if (scrollAmount > 0)
             //   textLogfile.scrollTo(0, scrollAmount);

            textLogfile.post(new Runnable()
            {
                public void run()
                {
                    int scrollAmount = textLogfile.getLayout().getLineTop(textLogfile.getLineCount()) - textLogfile.getHeight();
                    if (scrollAmount > 0)
                        textLogfile.scrollTo(0, scrollAmount);
                }
            });

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    public void OnClickClear(View v) {
        try {
            FileOutputStream fos = openFileOutput("logfile", Context.MODE_PRIVATE);

            fos.write("".getBytes());

            fos.close();

        } catch (Exception e) {

            e.printStackTrace();

        }
        loadLogfile();
    }

    public void OnClickRefresh(View v) {
        loadLogfile();
    }

}
