#!/bin/bash

curl -X POST -d '{"cmd":"ADD","dst-port":"0","dst-switch":"00:00:00:00:00:00:00:01","src-switch":"00:00:00:00:00:00:00:01","src-port":"0","requirement":"6.0"}' http://localhost:8080/wm/fdm/config/json

curl -X POST -d '{"cmd":"ADD","dst-port":"0","dst-switch":"00:00:00:00:00:00:00:02","src-switch":"00:00:00:00:00:00:00:02","src-port":"0","requirement":"6.0"}' http://localhost:8080/wm/fdm/config/json

curl -X POST -d '{"cmd":"ADD","dst-port":"0","dst-switch":"00:00:00:00:00:00:00:03","src-switch":"00:00:00:00:00:00:00:03","src-port":"0","requirement":"6.0"}' http://localhost:8080/wm/fdm/config/json

# curl -X POST -d '{"cmd":"ADD","dst-port":"1","dst-switch":"00:00:00:00:00:00:00:06","src-switch":"00:00:00:00:00:00:00:06","src-port":"1","requirement":"6.0"}' http://localhost:8080/wm/fdm/config/json

curl -X POST -d '{"cmd":"ADD","dst-port":"1","dst-switch":"00:00:00:00:00:00:00:07","src-switch":"00:00:00:00:00:00:00:04","src-port":"4","capacity":"4.0"}' http://localhost:8080/wm/fdm/config/json

curl -X POST -d '{"cmd":"ADD","dst-port":"2","dst-switch":"00:00:00:00:00:00:00:07","src-switch":"00:00:00:00:00:00:00:05","src-port":"4","capacity":"4.0"}' http://localhost:8080/wm/fdm/config/json

curl -X POST -d '{"cmd":"ADD","dst-port":"3","dst-switch":"00:00:00:00:00:00:00:07","src-switch":"00:00:00:00:00:00:00:06","src-port":"3","capacity":"12.0"}' http://localhost:8080/wm/fdm/config/json
