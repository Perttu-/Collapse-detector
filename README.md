# Collapse Detector
Aware plugin which detects phone fall and provides information of nearby building collapses detected by the system and also provides information of safety and first aid.
Uses Aware framework http://www.awareframework.com/

Functionality in a nutshell: This application is crowd sourced building collapse warning system for high earthquake risk areas. Phones detects if it has fallen down and sends its location to server. The server then sees if there are enough fall events in certain area and interprets wether there is high probability of collapsed building. If the server detects building collapse it warns all phones.

This is just the phone client implementation.

Completed features:
- Detects phone fall from accelerometer values.
- Sends timestamp, device id and location (latitude and longitude) to server.
- Provides first aid and safety tips.

To do:
- Recieve collapsed buildings locations from server.
- Change the map activity to fragment.
- Warn user and display the collapse location on map.
- More text and pictures to first aid and safety fragments.
- Implement the news reporting functionality.


socket info sended to server: 
"device id"
"phone ip"
"phone port"
