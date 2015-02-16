# pimatic-location

To use this app you need to add 3 variables to your Pimatic.

1. longitude - longitude of your home
2. latitude - latitude of your home
3. distance - You can choose the name of this variable freely and change it in the app. In this variable the distance betweend your home and your phone will be saved.

The distance will be reported in meters and as direct line between your smartphone and the given location.

You can use this website to get your longitude and latitude.
http://www.mapcoordinates.net/en

# Tasker integration

You can use the Android App "Tasker" to send an intent to application to set a new interval.
Tasker example intent config:
```
Send Intent [ 
 Action:android.intent.action.SEND 
 Cat:Default 
 Mime Type:text/plain 
 Data: 
 Extra:android.intent.extra.INTERVAL: 1000
 Extra: 
 Package:de.blackoise.pimaticlocation
 Class: 
 Target:Service ]
```

# Screenshots
![Settings Window](https://raw.githubusercontent.com/Oitzu/pimatic-location/master/Android/screenshots/2015-02-06%2012.05.59.png)