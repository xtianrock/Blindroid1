package com.example.blindroid;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

 
public class AccelerometerManager {
  
    private static Context aContext=null;
     
     /**Sensibilidad*/ 
    public static int threshold  = 30; 
    private static long interval     = 900000000;
  
    private static Sensor sensor;
    private static SensorManager sensorManager;
    static AccelerometerListener listener;
  
    /** indica si el telefono tiene acelerometro */
    private static Boolean supported;
    
    /** indica si el acelerometro esta activo */
    private static boolean running = false;
  
    /** devuelve true si el aceleromtro esta activo*/    
    public static boolean isListening() {
        return running;
    }
  
    /**desactiva el listener */
    public static void stopListening() {
        running = false;
        try {
            if (sensorManager != null && sensorEventListener != null) {            	
                sensorManager.unregisterListener(sensorEventListener);
            }
        } catch (Exception e) {}
        Log.i("xtian", "Acelerometro desactivado");
    }
  
    /**
     * Recibe un contexto y determina si el servicio es soportado en dicho contexto.
     */
    public static boolean isSupported(Context context) {
        aContext = context;
        if (supported == null) {
            if (aContext != null) {                 
                 
                sensorManager = (SensorManager) aContext.getSystemService(Context.SENSOR_SERVICE);
                 
                // guardamos en una lista todos los sensores disponibles del tipo acelerometro
                List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);                 
                supported = Boolean.valueOf(sensors.size() > 0);                 
                 
                    
            } else {
                supported = Boolean.FALSE;
            }
        }
        return supported;
    }
  
    /**
     * Configure the listener for shaking
     * @param threshold
     *             minimum acceleration variation for considering shaking
     * @param interval
     *             minimum interval between to shake events
     */
    public static void configure(int threshold) {
        AccelerometerManager.threshold = threshold;
      
    }
  
    /**
     * Registers a listener and start listening
     * @param accelerometerListener
     *             callback for accelerometer events
     */
    public static void startListening( AccelerometerListener accelerometerListener ) 
    {
       
        sensorManager = (SensorManager) aContext.getSystemService(Context.SENSOR_SERVICE);
         
        // Take all sensors in device
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
         
        if (sensors.size() > 0) {
             
            sensor = sensors.get(0);             
            // Register Accelerometer Listener
            running = sensorManager.registerListener(sensorEventListener, sensor,SensorManager.SENSOR_DELAY_GAME);             
            listener = accelerometerListener;
            if (BuildConfig.DEBUG) {
            Log.i("xtian", "Acelerometro activado");
            }
        }
         
         
    }
  
    /**
     * Configures threshold and interval
     * And registers a listener and start listening
     * @param accelerometerListener
     *             callback for accelerometer events
     * @param threshold
     *             minimum acceleration variation for considering shaking
     * @param interval
     *             minimum interval between to shake events
     */
    public static void startListening(
            AccelerometerListener accelerometerListener, 
            int threshold) {
        configure(threshold);
        startListening(accelerometerListener);
    }
  
    /**
     * The listener that listen to events from the accelerometer listener
     */
    private static SensorEventListener sensorEventListener = 
        new SensorEventListener() {
  
        private long now = 0;
        private long timeDiff = 0;
        private long lastUpdate = 0;
        private long lastShake = 0;
  
        private float x = 0;
        private float y = 0;
        private float z = 0;
        private float lastX = 0;
        private float lastY = 0;
       // private float lastZ = 0;
        private float force = 0;
  
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
  
        public void onSensorChanged(SensorEvent event) {
        	 synchronized(this){
            // use the event timestamp as reference
            // so the manager precision won't depends 
            // on the AccelerometerListener implementation
            // processing time
            now = event.timestamp;
  
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
  
            // if not interesting in shake events
            // just remove the whole if then else block
            if (lastUpdate == 0) {
                lastUpdate = now;
                lastShake = now;
                lastX = x;
                lastY = y;
                //lastZ = z;
              
                 
            } else {
                timeDiff = now - lastUpdate;
                
                if (timeDiff > 0) { 
                     
                    /* Si pongo el eje z da problemas al dejar el movil en la mesa.
                     * force = Math.abs(x + y + z - lastX - lastY - lastZ);*/
                    force = Math.abs(x + y - lastX - lastY);
                     
                    if (Float.compare(force, threshold) >0 ) {                      
                         
                        if (now - lastShake >= interval) { 
                             
                            // trigger shake event
                            listener.onShake(force);
                        }
                    
                        lastShake = now;
                    }
                    lastX = x;
                    lastY = y;
                    //lastZ = z;
                    lastUpdate = now; 
                }
              
                
            }
            // trigger change event
           
            listener.onAccelerationChanged(x, y, z);
        }}
  
    };
  
}