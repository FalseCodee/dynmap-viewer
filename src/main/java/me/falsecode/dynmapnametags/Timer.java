package me.falsecode.dynmapnametags;

public class Timer {
    public long lastMS = System.currentTimeMillis();

    public void reset(){
        lastMS = System.currentTimeMillis();
    }
    public boolean hasTimeElapsed(long time, boolean reset){
        if(System.currentTimeMillis()-lastMS > time){
            if(reset){
                reset();
            }
            return true;
        }
        return false;
    }

    public long timeElapsed(boolean reset) {
        long time = System.currentTimeMillis()-lastMS;
        if(reset){
            reset();
        }
        return time;
    }
}