# MiTimer
Timer Application for an Android Watch. It is designed like an IWatch Timer app. 

Set hours and minutes with your finger. Minutes are displayed in intervals of 5s (0,5,10,15,etc..), but if you do a long press on a number, when minutes is selected, it will allow you to drill down for the individual minute. If you hold even longer the application will prompt the user to exit. When user sets the a timer, it will vibrate when the timer is done, but make sure the watch is visible, otherwise it will wait to vibrate till the watch is displaying.

If you use the MiWatch with this application, the MiWatch Watchface will listen for a broadcast that MiTimer sends out, with the time left till Timer goes off. When the MiWatch Watchface receives the broadcast it actually sets an alarm that will vibrate the watch when the alarm has expired. 

Still some cleaning up that needs to be done, but it works well enough to use. Maybe change the timer to an Alarm, or give the user the feature of one or the other. When I integrate MiWatch and MiTimer I will display remaining time on alarm on the watchface.
