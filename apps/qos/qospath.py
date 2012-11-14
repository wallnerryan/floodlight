#! /usr/bin/python
"""
QoSPath.py ---------------------------------------------------------------------------------------------------
Developed By: Ryan Wallner (ryan.wallner1@marist.edu)
Add QoS to a specific path in the network. Utilized circuit pusher developed by KC Wang

 USAGE: 
		qospath.py <add|delete>	 <path>	<source-ip> <dest-ip> <type of service>
 		*note: This adds the Quality of Service to each switch along the path between hosts
-----------------------------------------------------------------------------------------------------------------------
"""

import os
import subprocess

##Get switches in a cirtcute using cirtcuit pusher (may need to modify to get all switches in path)
##Then use the add policy to a switch in QoSService to add a service along a path.
