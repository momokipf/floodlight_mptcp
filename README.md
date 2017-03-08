# floodlight_mptcp with FDM bandwidth allocation algorithm 

After you have cloned whole dir from here, use `ant eclipse` to generate eclipse project file or use `ant` to compile all source code and genrate executable .jar file.

##FloodLight
If you want to know how to run floodlight, follow the tutorial in this [link](https://floodlight.atlassian.net/wiki/spaces/floodlightcontroller/pages/1343544/Installation+Guide)

Before you generate networks traffic in mininet, you should specify the capacity and requirement on links or host using floodlight rest service under this url `http://(controller's IP)):8080/wm/fdm/config/json`. Do get method on `http://localhost:8080/wm/fdm/links/json` to get all links in current topology.
###how to set capacity
	Customized capacity is realted to real pyhsical links. So when you want set capacity on a link, you should first make sure that link is exist in topology.
	`eg."cmd":"ADD","dst-port":"2","dst-switch":"00:00:00:00:00:00:00:04","src-switch":"00:00:00:00:00:00:00:03","src-port":"2","capacity":"5.0"}'`

###How to set requirement 
 	For conveninece, we assume application on each host could only request for one bandwidth requirement. Thus, the format of requirment is letting source swith's id and port number be identical to destination switch's id and port number.
	`Eg. {"cmd":"ADD","dst-port":"1","dst-switch":"00:00:00:00:00:00:00:01","src-switch":"00:00:00:00:00:00:00:01","src-port":"1","requirement":"6.0"}'`


##Mininet

Basic eperation: following this [link](http://mininet.org/walkthrough/) 

Use `dpctl unix:/tmp/(switch name) states-flow` to check the switch's flow table in networks, 
Use `dpctl unix:/tmp/(switch name) meter-config` to check meter table.


This project is collaborated with UCLA Phd Pengyuan Du 



Current bugs:
	Can only run once.