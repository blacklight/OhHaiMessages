#!/usr/bin/python

#
# Client for the OhHaiMessages service
# It allows you to send text messages through your Android device from your computer,
# using a simple command line syntax:
# echo "text message" | python thisscript.py -n phone_number [options]
#
# Check it out at https://github.com/BlackLight/OhHaiMessages
# by Fabio "BlackLight" Manganiello, <blacklight@autistici.org>, 2011
#

import bluetooth
import getopt
import hashlib
import os
import socket
import sys
import time

addr = None
port = 4444
connType = None
service = "OhHaiMessages"
uuid = "828b721e-8e88-276b-6c29-0987f79bdc21"
logfile = os.getenv("HOME") + "/.ohhaimessages"
number = None
text = None
password = None
newpassword = None

def usage() :
	global port, service, uuid

	print >> sys.stderr, "Usage: echo \"text message\" | %s\n\t<-n|--number destination_number>\n\t[-h|--host wifi_host_address] \
\n\t[-S|--set-password <new_password>] [-P|--password <password>] \
\n\t[-p|--port wifi_host_port (default:%d)]\n\t[-s|--service bluetooth_service_name (default:%s)]\n\t[-u|--uuid bluetooth_uuid (default:%s)] \
\n\t[-l|--logfile logfile_path (default: $HOME/.ohhaimessages)]\n\t[-w|--wifi]\n\t[-b|--bluetooth]\n" % (sys.argv[0], port, service, uuid)

try :
	optlist, args = getopt.getopt (sys.argv[1:], "n:h:p:s:u:l:S:P:wb", ["number=", "host=", "port=", "service=", "uuid=", \
		"logfile=", "set-passord", "password", "wifi", "bluetooth"])
except getopt.GetoptError, err :
	print str(err)
	usage()
	sys.exit(1)

for o, a in optlist :
	if o in ("-n", "--number") :
		number = a
	elif o in ("-h", "--host") :
		addr = a
	elif o in ("-p", "--port") :
		port = a
	elif o in ("-s", "--service") :
		service = a
	elif o in ("-u", "--uuid") :
		uuid = a
	elif o in ("-l", "--logfile") :
		logfile = a
	elif o in ("-w", "--wifi") :
		if connType == "bt" :
			print >> sys.stderr, "You cannot specify a wifi and a bluetooth connection at the same time"
			usage()
			sys.exit(1)
		else :
			connType = "wifi"
	elif o in ("-b", "--bluetooth") :
		if connType == "wifi" :
			print >> sys.stderr, "You cannot specify a wifi and a bluetooth connection at the same time"
			usage()
			sys.exit(1)
		else :
			connType = "bt"
	elif o in ("-S", "--set-password") :
		newpassword = a
	elif o in ("-P", "--password") :
		password = a
	else :
		assert False, "Unhandled option"

if connType == "wifi" :
	if not addr :
		print >> sys.stderr, "WiFi connection specified but no server address was provided"
		usage()
		sys.exit(1)
elif not connType :
	print "No connection type was specified - choose between --wifi and --bluetooth"
	usage()
	sys.exit(1)

if password :
	password = hashlib.sha1(password).hexdigest()

if newpassword :
	# Password change
	newpassword = hashlib.sha1(newpassword).hexdigest()
	xmlRequest = '<?xml version="1.0" encoding="UTF-8"?>\n\n<ohhairequest>\n\t<setpassword>%s</setpassword>\n' % (newpassword)

	if password :
		xmlRequest += '\t<password>' + password + '</password>\n'

	xmlRequest += '</ohhairequest>\n'
elif not number :
	# Not a password change, not a valid text
	print >> sys.stderr, "No destination number specified"
	usage()
	sys.exit(1)
else :
	# Not a password change but a text sending
	text = ""

	while True :
		line = sys.stdin.readline().strip()
		text += line + "\n"

		if line == "" :
			break

	text = text.strip()

	if len (text) > 160 :
		print >> sys.stderr, "The text you provided is %d characters long, that's more than the sms limit of 160 characters. \
	It will be splitted in more messages" % (len(text))

	xmlRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<ohhairequest>\n\t<number>%s</number>\n\t<text><![CDATA[%s]]></text>\n" % (number, text)

	if password :
		xmlRequest += '\t<password>' + password + '</password>\n'

	xmlRequest += '</ohhairequest>\n'

request = "Content-Length: %d\n\n%s" % (len(xmlRequest), xmlRequest)

if connType == "wifi" :
	sock = socket.socket (socket.AF_INET, socket.SOCK_STREAM)
else :
	print "Scanning for the service %s having UUID %s..." % (service, uuid)
	services = bluetooth.find_service (name=service, uuid=uuid)
	port = None
	addr = None

	for i in range(len(services)) :
		match = services[i]
		print match["host"]

		if match["name"] == "OhHaiMessages" :
			port = match["port"]
			addr = match["host"]

	if not addr and not port :
		print >> sys.stderr, "Service not found"
		sys.exit(1)
	else :
		print "Service found on the device %s on port %d" % (addr, port)

	sock = bluetooth.BluetoothSocket (bluetooth.RFCOMM)

sock.connect ((addr, port))
sock.send (request)

while True :
	try :
		buf = sock.recv (1024).strip()
		print buf

		if buf == "" :
			break

	except :
		break

sock.close()
ltime = time.localtime()
strtime = "%.2d/%.2d/%.4d, %.2d:%.2d:%.2d" % (ltime.tm_mday, ltime.tm_mon, ltime.tm_year, ltime.tm_hour, ltime.tm_min, ltime.tm_sec)

if text and number :
	try :
		f = open (logfile, "a")
		f.write ("[%s]\nTo: %s\n\t%s\n\n" % (strtime, number, text))
		f.close()
	except :
		print >> sys.stderr, "Warning: could not write to the log file ", logfile

