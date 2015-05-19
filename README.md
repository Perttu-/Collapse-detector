# Collapse Detector
Aware plugin which detects phone fall and provides information of nearby building collapses detected by the system and also provides information of safety and first aid.
Uses Aware framework http://www.awareframework.com/

Functionality in a nutshell: This application is crowd sourced building collapse warning system for high earthquake risk areas. Phones detects if it has fallen down and sends its location to server. The server then sees if there are enough fall events in certain area and interprets wether there is high probability of collapsed building. If the server detects building collapse it warns all phones.

This is the phone client implementation of the system.

Completed features:
- Detects phone fall from accelerometer values.
- Sends timestamp, device id and location (latitude and longitude) to server.
- Sends device id once in every minute to server
- Provides first aid and safety tips.
- Recieves collapsed buildings locations from server.
- Saves received collapse locations to SQLite database
- Gets the collapse locations from database and displays them on map.
- Debug panel where database can be cleared and filled with sample coordinates.
- Encrypting and decrypting with 128 bit AES
- Asks user question how the phone detected a fall

To do:
- Implement better way to get user location (getlastlocation returns null with some devices).
- Warn user when new collapses are received.
- More text and pictures to first aid and safety fragments.
- Change the map activity to fragment.
- Implement the news reporting functionality.



