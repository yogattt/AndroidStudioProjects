package  com.xmobileapp.android.step;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import static java.lang.Math.min;
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.max;

public class MainActivity extends Activity implements SensorEventListener,OnClickListener {
    private Button mWriteButton, mStopButton;
    private StepView cc;
    private int Stepcount=0;
    private boolean doWrite = false;
    private SensorManager sm;
    private DrawPic picx,picy,picz;
    private float lowX = 0, lowY = 0, lowZ = 0;
    private float FILTERING_VALAUE = 0.1f;
    private TextView AT,ACT;
    private List []mList = {new ArrayList(),new ArrayList(),new ArrayList()};
    private List []avgList={new ArrayList(),new ArrayList(),new ArrayList()};
    private class info{
        public float x=0,y=0,z=0;
    }
    info MAX,MIN,old_sample,new_sample;
    public static double SENSITIVITY =5; // SENSITIVITY灵敏度
    private long time=0;
    /*
    方法二要用到的数据成员。
    private float LastV =0;
    private float []mScale = new float[2];
    private float mYOffset;
    private  long end = 0;
    private  long start = 0;

    private float LastDirections = 0;
    private float [] LastExtremes = new float[2];
    private float mLastDiff =0;
    private int mLastMatch = -1;
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        AT = (TextView)findViewById(R.id.AT);
        ACT = (TextView)findViewById(R.id.onAccuracyChanged);
        sm =(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(this,
                sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        mWriteButton = (Button) findViewById(R.id.Button_Write);
        mWriteButton.setOnClickListener(this);
        mStopButton = (Button) findViewById(R.id.Button_Stop);
        mStopButton.setOnClickListener(this);
        cc=(StepView) findViewById(R.id.cc);
        picx=findViewById(R.id.X);
        picy=findViewById(R.id.Y);
        picy.mPoint.setColor(Color.RED);
        picz=findViewById(R.id.Z);
        picz.mPoint.setColor(Color.BLACK);
        MIN=new info();
        MAX=new info();
        old_sample=new info();
        new_sample=new info();
        MAX.x=-100;MAX.y=-100;MAX.z=-100;
        MIN.x=100;MIN.y=100;MIN.z=100;
        /*
        方法二
        int h = 480;
        mYOffset = h * 0.5f;
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
        */
    }
    public void onPause(){
        super.onPause();
    }
    public void onClick(View v) {
        if (v.getId() == R.id.Button_Write)
            doWrite = true;
        if (v.getId() == R.id.Button_Stop)
            doWrite = false;
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        ACT.setText("onAccuracyChanged is detonated");
    }
    public void addStep()
    {
        long cur=System.currentTimeMillis();
        if(cur-time>200) {
            time=cur;
            Stepcount++;
            cc.setTextSize(Stepcount);
            cc.setCurrentCount(7000, Stepcount);
        }
    }
    public void onSensorChanged(SensorEvent event) {
        String message = new String();
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            float X = event.values[0];
            float Y = event.values[1];
            float Z = event.values[2];
            //消除重力影响，否则会有重力加速度影响
            lowX = X * FILTERING_VALAUE + lowX * (1.0f - FILTERING_VALAUE);
            lowY = Y * FILTERING_VALAUE + lowY * (1.0f - FILTERING_VALAUE);
            lowZ = Z * FILTERING_VALAUE + lowZ * (1.0f - FILTERING_VALAUE);
            //High-pass filter 重力属于低频成分，可以被过滤掉
            X = X - lowX;
            Y = Y - lowY;
            Z = Z - lowZ;

            //以下是使用近四次采样的平均结果作为当前加速度，使加速度更加平滑。
            if(avgList[0].size()>=4)
            {
                avgList[0].remove(0);
                avgList[1].remove(0);
                avgList[2].remove(0);
            }
            avgList[0].add(X);avgList[1].add(Y);avgList[2].add(Z);
            X=0;Y=0;Z=0;
            for(int i=0;i<avgList[0].size();i++)
            {
                X+=(float)avgList[0].get(i);
                Y+=(float)avgList[1].get(i);
                Z+=(float)avgList[2].get(i);
            }
            X=X/avgList[0].size();Y=Y/avgList[0].size();Z=Z/avgList[0].size();

            if(mList[0].size()>=500)
            {
                mList[0].remove(0);
                mList[1].remove(0);
                mList[2].remove(0);
            }
            //计算500组数据中的最大和最小值
            mList[0].add(X);mList[1].add(Y);mList[2].add(Z);
            MAX.x=-1000;MAX.y=-1000;MAX.z=-1000;
            MIN.x=1000;MIN.y=1000;MIN.z=1000;
            for(int i=0;i<mList[0].size();i++)
            {
                MAX.x=max(MAX.x,(float)mList[0].get(i));
                MAX.y=max(MAX.y,(float)mList[1].get(i));
                MAX.z=max(MAX.z,(float)mList[2].get(i));

                MIN.x=min(MIN.x,(float)mList[0].get(i));
                MIN.y=min(MIN.y,(float)mList[1].get(i));
                MIN.z=min(MIN.z,(float)mList[2].get(i));
            }

            old_sample.x=new_sample.x;old_sample.y=new_sample.y;old_sample.z=new_sample.z;
            if(abs(new_sample.x-X)>SENSITIVITY)
                new_sample.x=X;
            if(abs(new_sample.y-Y)>SENSITIVITY)
                new_sample.y=Y;
            if(abs(new_sample.z-Z)>SENSITIVITY)
                new_sample.z=Z;
            float x_change=MAX.x-MIN.x;
            float y_change=MAX.y-MIN.y;
            float z_change=MAX.z-MIN.z;

            int active=0;
            if(x_change>y_change&&x_change>z_change&&x_change>=15)
                active=1;
            if(y_change>x_change&&y_change>z_change&&y_change>=15)
                active=2;
            if(z_change>y_change&&z_change>x_change&&z_change>=15)
                active=3;
            switch (active)
            {
                case 0:break;
                case 1:
                    float avg=(MAX.x+MIN.x)/2;
                    if(old_sample.x>avg && new_sample.x<avg)
                        addStep();
                    break;
                case 2:
                    avg=(MAX.y+MIN.y)/2;
                    if(old_sample.y>avg && new_sample.y<avg)
                        addStep();
                    break;
                case 3:
                    avg=(MAX.z+MIN.z)/2;
                    if(old_sample.z>avg && new_sample.z<avg)
                        addStep();
                    break;
            }
            picx.AddPointToList(X);
            picy.AddPointToList(Y);
            picz.AddPointToList(Z);
            DecimalFormat df = new DecimalFormat("#,##0.000");
            message = df.format(X) + " ";
            message += df.format(Y) + " ";
            message += df.format(Z) + "\n";
            AT.setText(message+"\n");
            if (doWrite) {
                write2file(message);
            }
        }
        /*
        方法二判断方法
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float vSum = 0;
            for (int i = 0; i < 3; i++) {
                final float v = event.values[i] * mScale[1];
                vSum += v;
            }
            float v = vSum / 3+mYOffset;

            float direction = v > LastV ? 1 : (v < LastV ? -1 : 0);
            if (direction == -LastDirections) {
                int extType = (direction > 0 ? 0 : 1);
                LastExtremes[extType] = LastV;
                float diff = Math.abs(LastExtremes[extType] - LastExtremes[1 - extType]);
                if (diff > SENSITIVITY) {
                    boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff * 2 / 3);
                    boolean isPreviousLargeEnough = mLastDiff > (diff / 3);
                    boolean isNotContra = (mLastMatch != 1 - extType);
                    if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                        end = System.currentTimeMillis();
                        if (end - start > 500) {
                            Stepcount++;
                            cc.setTextSize(Stepcount);
                            cc.setCurrentCount(7000, Stepcount);
                            mLastMatch = extType;
                            start = end;
                        }
                    } else {
                        mLastMatch = -1;
                    }
                }
                mLastDiff = diff;
            }
            LastDirections = direction;
            LastV = v;
            pic.AddPointToList(v);
            DecimalFormat df = new DecimalFormat("#,##000.000");
            message = df.format(v) + "\n";
            AT.setText(message);
            if (doWrite) {
                write2file(message);
            }
        }*/

    }
    private void write2file(String a){
        try {
            File file = new File("/sdcard/acc.txt");
            if (!file.exists()){
                file.createNewFile();}
            RandomAccessFile randomFile = new
                    RandomAccessFile("/sdcard/acc.txt", "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);
            randomFile.writeBytes(a);
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
