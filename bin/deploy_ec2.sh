#/bin/bash
scp restart.sh ubuntu@ec2-3-144-104-2.us-east-2.compute.amazonaws.com:/home/ubuntu/saml2_sp
scp target/saml2-sp-1.0.0.jar ubuntu@ec2-3-144-104-2.us-east-2.compute.amazonaws.com:/home/ubuntu/saml2_sp
scp src/main/resources/application-ec2.properties ubuntu@ec2-3-144-104-2.us-east-2.compute.amazonaws.com:/home/ubuntu/saml2_sp
ssh ubuntu@ec2-3-144-104-2.us-east-2.compute.amazonaws.com 'cd /home/ubuntu/saml2_sp && ./restart.sh ec2'