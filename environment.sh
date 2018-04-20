#!/bin/bash
sudo apt-get update
sudo apt-get install python3-venv
python3 -m venv env
. env/bin/activate

pip3 install -r requirements.txt
