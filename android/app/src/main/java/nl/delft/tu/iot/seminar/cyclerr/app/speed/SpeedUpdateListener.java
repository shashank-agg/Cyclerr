package nl.delft.tu.iot.seminar.cyclerr.app.speed;

import java.time.Instant;

public interface SpeedUpdateListener {
    void onSpeedUpdateListener(Instant time, Float speed, double altitude);
}
