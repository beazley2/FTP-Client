# Basic FTP Client
A progressively implemented FTP client for the CS410/510 Agile and Extreme Programming Methodologies course at Portland State University.

# How To Run
Compile source to a .jar file, invoke the .jar from the command line, and interact with the resulting command line prompt.

# Running demo
Requires two command line arguments, your MCECS login and your password. The first time it runs, it will create two directories;
one local and one remote. To see the upload/download in action, create a file called download.txt in your newly created
remote directory and one called upload.txt in your new local directory. Then run the program again, you should end up
with an upload.txt and download.txt in both directories.

# Getting JSch
Download the .jar here: http://www.jcraft.com/jsch/
To add the library in Intellij,
 - CTRL+ALT+SHIFT+S
 - Select libraries, click the green plus, find the .jar file and add it
