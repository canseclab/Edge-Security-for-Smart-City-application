# NDN-Project

## Edge side
There are KGC, EC, and ED on the edge server, and file a.properties ,AES.java ,credentials.json , GUI.java at the user side.  
Because we wanted to test easily, We did not separate the user from the edge.  
Please refer to the folder "GUI" if you need to separate them.  

### Before start
You should set some parameters as follow:  
On the edge server:  
1.ED_ccloud:  
On lines 131 and 132, you should set the NDN IP and the port.  
On line 134, the parameter is the path of files that are the edge server keeps when the user downloads a file.  
On line 135, the edge server will keep 3 files which are the top 3 times of download.  
You can just set the number that you want to keep the number of files.  
2.GUI:  
On lines 63, you need to set the IP of the edge.  
OPTIONAL:  
On line 1617, this is the path that the user download a file.(Default is desktop)  
3.All files on the Edge server  
OPTIONAL:  
The default port of KGC is 5487, the EC is 5278, and the ED is 5457.  

### How to start the edge server  
1.Run the KGC_server  
2.Run the EC_client  
3.Run the ED_ccloud if you want to connect with NDN.  
Otherwise, you should run the ED_client. (For a test)  
If you want to test, remember to change the ED's IP in the GUI.  
But there are some differences between the file ED_ccloud and ED_client.  
In the test, the edge server searches the file and downloads it for you.  

### How to start the user  
1.Run the edge server  
2.Run the GUI(Maybe run two of them, one for file provider, another for file downloader.)  
3.Enter the user ID except for space  
4.File provider uploads a file that can choose three type of the file and two clouds.  
5.After uploading the file, the provider should share his file by entering the downloader's ID and sent the token to the downloader.  
6.The downloader needs to enter the provider's id and the token to authenticate.  
Note that the downloader should enter the id and the token success for the first time, or you should relaunch the GUI and enter the same id of the downloader.  
7.After verifying the token, the downloader can download the file by entering the id and the file name.  
The NDN will send the string like this"/home/folder_name/filename" to the edge.  
After receiving the path, the edge server downloads the file if the file did not be kept in the "edgefile" folder.   
The edge server calculates the download times and choose the keeping file.  

Remember that you should choose the same type in each process 4 to 7 or you will fail.
