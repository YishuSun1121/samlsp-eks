#!/bin/bash
profile=$1
kill `ps -ef|grep 'java -jar saml2-sp-1.0.0-runtime.jar'|grep -v grep|awk '{print $2}'`
sleep 5
cp saml2-sp-1.0.0.jar saml2-sp-1.0.0-runtime.jar
#nohup java -jar saml2-sp-1.0.0-runtime.jar --spring.profiles.active="$profile" &
nohup java -jar saml2-sp-1.0.0-runtime.jar --spring.config.location=application-"$profile".properties &
tail -f nohup.out