/*
 * This file is part of meteoLCD-Android.
 *
 * Copyright (c) 2017 Benoît FRISCH
 *
 * meteoLCD-Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * meteoLCD-Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with meteoLCD-Android If not, see <http://www.gnu.org/licenses/>.
 */

package lu.lcd.meteolcd;

import android.graphics.Color;

/**
 * Current Weather Class
 */
public class CurrentWeather {
    private String temperature;
    private String icon;
    private String pression;
    private String lastUpdate;
    private int color;

    /**
     * Current Weather Constructor
     *
     * @param temperature the temperature
     * @param icon        the icon
     * @param pression    the pression
     * @param lastUpdate  last updated
     */
    public CurrentWeather(String temperature, String icon, String pression, String lastUpdate) {
        this.temperature = temperature;
        this.icon = icon;
        this.pression = pression;
        this.lastUpdate = lastUpdate;

        if (getIcon().equals("sun")) {
            this.color = Color.argb(190, 226, 170, 51);
        } else if (getIcon().equals("clouds")) {
            this.color = Color.argb(190, 69, 84, 97);
        } else if (getIcon().equals("partly_cloudy_day")) {
            this.color = Color.argb(190, 130, 165, 186);
        } else if (getIcon().equals("rain")) {
            this.color = Color.argb(190, 112, 114, 115);
        } else if (getIcon().equals("moon")) {
            this.color = Color.argb(190, 38, 40, 40);
        }

    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPression() {
        return pression;
    }

    public void setPression(String pression) {
        this.pression = pression;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
