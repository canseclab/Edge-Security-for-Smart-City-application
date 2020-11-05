# NDN-Project  

## Edge Server & User Side
There are KGC, EC, and ED on the edge server. 
<!-- Because we wanted to test easily, we did not separate the user from the edge.  
Please refer to the folder "GUI" if you need to separate them.  
-->
### Before Start
You should set some parameters as follow:  
On the edge server:  
1.ED_ccloud:  
On lines 131 and 132, you should set the NDN IP and the port.  
On line 134, the parameter is the path of files that are the edge server keeps when the user downloads a file.  
On line 135, the edge server will keep 3 files which are the top 3 times of download.  
You can just set the number that you want to keep the number of files.  
2.GUI:  
On lines 63, you need to set the IP of the edge.  
3.All files on the Edge server:  
OPTIONAL:  
The default port of KGC is 5487, the EC is 5278, and the ED is 5457.  

### How to Run the Edge Server  
1.Run the KGC_server  
2.Run the EC_client  
3.Run the ED_ccloud if you want to connect with NDN.  
Otherwise, you should run the ED_client. (For a test)  
If you want to test, remember to change the ED's IP in the GUI.  
But there are some differences between the file ED_ccloud and ED_client.  
In the test, the edge server searches the file and downloads it for you.  

### How to Share Files with Users  
1.Run the edge server  
2.Run the GUI(Maybe run two of them, one for file provider, another for file consumer.)  
3.Enter the user ID except for space  
4.File provider uploads a file that can choose three type of the file and two clouds.  
5.After uploading the file, the provider should share his file by entering the consumer's ID and sent the token to the consumer.  
6.The consumer needs to enter the provider's id and the token to authenticate.  
Note that the consumer should enter the id and the token success for the first time, or you should relaunch the GUI and enter the same id of the consumer.  
7.After verifying the token, the consumer can download the file by entering the id and the file name.  
The NDN will send the string like this"/home/folder_name/filename" to the edge.  
After receiving the path, the edge server downloads the file for the user if the file did not be kept in the "edgefile" folder.   
The edge server calculates the download times and choose the keeping file.  

Remember that you should choose the same type in each process 4 to 7 or you will fail.  

## NDN Searching Side  
NDN Searching Side is running under Ubuntu 18.04.
Due to the file size is too large to upload, please check the code [here](https://drive.google.com/file/d/1g3wLJ_hWOI_gD4_9B_rQWp71ChVBMlg0/view?usp=sharing).  
### Before Start
Searching Side is based on [ndnSIM](https://ndnsim.net/current/) and [amus ndnSIM](https://github.com/ChristianKreuzberger/amus-ndnSIM).  
Set up the system by the links above.
Searching side also use [grive](https://github.com/vitalif/grive2) to search in the Cloud.
Remember to bind to your target cloud.

In file "\ndnSIM\ns-3\src\ndnSIM\examples\ndn-file-thread-kac.cpp",   
line 38, make sure you setting the same port as edge user side. (Defalt port as 2323.)  
Set your searching path in line 55, and line 136.  
(Defalt path as: "/home/user/drive/Dropbox")

### How to run NDN Searching Side
1. In amus ndnSIM, the system uses ns-3 compliation procedure. Make configure and python binding enable with following commands, 
```
cd <ns-3 folder>  
./waf configure --enable-examples
./waf
```
  Due to python is not very stable for ns-3 stimulator, you can disable python with following commands, 
```
cd <ns-3 folder>
./waf configure --disable-python --enable examples
./waf
```
2. Execute searching side with command
```
cd <ns-3 folder>
./waf --run=ndn-file-thread-kac
```
If the server is running, it will show "Server listening...".  
If edge side successfully connects to NDN searching side, it will show "Connection accepted."  

(Sometimes it will failed to connect with edge side and NDN searching side, please try to restart both of them.)
As "Connection accepted." shows, you can do the experiment as the following procedure. 


## Experiment  
### Beginning
![image](https://i.imgur.com/h9t3nJZ.jpg)  
You need to enter a name as a user.  
We assumed that we had two users (user1 & user2).  
![image](https://i.imgur.com/Rxemtar.jpg)  
User1 uploaded a document file to the cloud.  
![image](https://i.imgur.com/ac3FeOr.jpg)  
We can see the file was uploaded and its' name had encrypted.  
![image](https://i.imgur.com/iGQKFoz.jpg)  
User1 wanted to share the file with the user2, so user1 gave the token to user2 by a secure channel.  
![image](https://i.imgur.com/nTWYPVM.jpg)  
After that, user2 could download the file through the edge server.
The edge server would ask the NDN server where the file was.
![image](https://i.imgur.com/6UovcZh.jpg)  
You can see the download time in the figure.
