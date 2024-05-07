package bku.iot.demoiot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {
    MQTTHelper mqttHelper;
    TextView txtTemp, txtDistance, txtLight;
    LabeledSwitch btnled1, btnled2, btnled3, btnled4, btndoor, btnauto;
    SeekBar fanslider;
    int auto = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //câu lệnh phải đứng sau setContentView
        txtTemp = findViewById(R.id.txtTemperature);
        txtDistance = findViewById(R.id.txtDistance);
        txtLight = findViewById(R.id.txtLight);
        btnled1 = findViewById(R.id.btnLed1);
        btnled2 = findViewById(R.id.btnLed2);
        btnled3 = findViewById(R.id.btnLed3);
        btnled3 = findViewById(R.id.btnLed3);
        btnled4 = findViewById(R.id.btnLed4);
        btndoor = findViewById(R.id.btndoor);
        btnauto = findViewById(R.id.btnauto);
        fanslider = findViewById(R.id.fanslider);

        btnled1.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn == true){
                    sendDataMQTT("tdp2309/feeds/led1","1");
                }else{
                    sendDataMQTT("tdp2309/feeds/led1","0");
                }
            }
        });
        btnled2.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn == true){
                    sendDataMQTT("tdp2309/feeds/led2","1");
                }else{
                    sendDataMQTT("tdp2309/feeds/led2","0");
                }
            }
        });
        btnled3.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn == true){
                    sendDataMQTT("tdp2309/feeds/led3","1");
                }else{
                    sendDataMQTT("tdp2309/feeds/led3","0");
                }
            }
        });
        btnled4.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn == true){
                    sendDataMQTT("tdp2309/feeds/led4","1");
                }else{
                    sendDataMQTT("tdp2309/feeds/led4","0");
                }
            }
        });
        btndoor.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn == true){
                    sendDataMQTT("tdp2309/feeds/door","1");
                }else{
                    sendDataMQTT("tdp2309/feeds/door","0");
                }
            }
        });
        btnauto.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                if(isOn == true) {
                    sendDataMQTT("tdp2309/feeds/auto", "1");
                    auto = 1;
                }
                else {
                    sendDataMQTT("tdp2309/feeds/auto", "0");
                    auto = 0;
                }
            }
        });
        fanslider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String payload = String.valueOf(seekBar.getProgress());
                sendDataMQTT("tdp2309/feeds/fan", payload);
            }
        });
        startMQTT();
    }

    public void sendDataMQTT(String topic, String value){
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        }catch (MqttException e){

        }
    }
    public void startMQTT(){
        mqttHelper = new MQTTHelper(this);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("TEST", topic + "***" + message.toString());
                if(topic.contains("nhietdo")){
                    txtTemp.setText(message.toString() + " °C");
                    int sensorvalue1 = Integer.parseInt(message.toString());
                    if (auto == 1) {
                        if (sensorvalue1 < 20) {
                            fanslider.setProgress(0);
                        } else if (sensorvalue1 >= 20 && sensorvalue1 < 25) {
                            fanslider.setProgress(25);
                        } else if (sensorvalue1 >= 25 && sensorvalue1 < 30) {
                            fanslider.setProgress(30);
                        } else if (sensorvalue1 >= 30 && sensorvalue1 < 35) {
                            fanslider.setProgress(35);
                        } else if (sensorvalue1 >= 35) {
                            fanslider.setProgress(100);
                        }
                    }
                }else if(topic.contains("khoangcach")){
                    txtDistance.setText(message.toString() + " cm");
                    int distancevalue = Integer.parseInt(message.toString());
                    if (auto == 1) {
                        if (distancevalue <= 80) {
                            btndoor.setOn(true);
                            btnled4.setOn(true);
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    btndoor.setOn(false);
                                    btnled4.setOn(false);
                                }
                            }, 5000);  // Tạm dừng trong 5 giây trên luồng chính mà không làm đứng UI
                        } else {
                            btndoor.setOn(false);
                        }
                    }
                }else if(topic.contains("anhsang")) {
                    txtLight.setText(message.toString() + " %");
                    int sensorvalue = Integer.parseInt(message.toString());
                    if (auto == 1) {
                        if (sensorvalue >= 0 && sensorvalue < 25) {
                            btnled1.setOn(true);
                            btnled2.setOn(true);
                            btnled4.setOn(true);
                        } else if (sensorvalue >= 25 && sensorvalue < 50) {
                            btnled1.setOn(true);
                            btnled2.setOn(true);
                            btnled4.setOn(false);
                        } else if (sensorvalue >= 50 && sensorvalue < 75) {
                            btnled1.setOn(true);
                            btnled2.setOn(true);
                            btnled4.setOn(false);
                        } else if (sensorvalue >= 75 && sensorvalue < 90) {
                            btnled1.setOn(true);
                            btnled2.setOn(false);
                            btnled4.setOn(false);
                        } else {
                            btnled1.setOn(false);
                            btnled2.setOn(false);
                            btnled4.setOn(false);
                        }
                    }
                }else if(topic.contains("led1")){
                    if(message.toString().equals("1")){
                        btnled1.setOn(true);
                    }else{
                        btnled1.setOn(false);
                    }
                }else if(topic.contains("led2")){
                    if(message.toString().equals("1")){
                        btnled2.setOn(true);
                    }else{
                        btnled2.setOn(false);
                    }
                }else if(topic.contains("led3")){
                    if(message.toString().equals("1")){
                        btnled3.setOn(true);
                    }else{
                        btnled3.setOn(false);
                    }
                }else if(topic.contains("led4")){
                    if(message.toString().equals("1")){
                        btnled4.setOn(true);
                    }else{
                        btnled4.setOn(false);
                    }
                }else if(topic.contains("fan")){
                    int fanSpeed = Integer.parseInt(message.toString());
                    fanslider.setProgress(fanSpeed);
                }else if(topic.contains("door")){
                    if(message.toString().equals("1")){
                        btndoor.setOn(true);
                    }else{
                        btndoor.setOn(false);
                    }
                }
                else if(topic.contains("auto")){
                    if(message.toString().equals("1")){
                        btnauto.setOn(true);
//                        auto = 1;
                    }else{
                        btnauto.setOn(false);
//                        auto = 0;
                    }
                }
            }


            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

}