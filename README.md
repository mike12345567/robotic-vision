# Vision System

This is the computer vision system for my final year project. Its quite simple, currently it will return the location of robots (determined based on colour patterns) via a web socket or HTTP POST requests.

To use this software please install IntelliJ and and OpenCV for your current platform, both will function correctly on Linux, Windows and OSX. Module linking must then be performed, this [guide](https://medium.com/@aadimator/how-to-set-up-opencv-in-intellij-idea-6eb103c1d45c#.fig722yl7 "Guide") shows the easiest method to complete this task.

Once the OpenCV library has been linked the software can then be ran, once a Cloud instance is running locally it will start sending any location data available.

Please note there is a testing application found in the *'testing'* package, this can be used to check the vision system can transmit data correctly to the API.