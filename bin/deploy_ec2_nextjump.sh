#/bin/bash
scp restart.sh ubuntu@34.228.186.209:/home/ubuntu/saml2_sp
scp target/saml2-sp-1.0.0.jar ubuntu@34.228.186.209:/home/ubuntu/saml2_sp
scp src/main/resources/application-ec2-nextjump.properties ubuntu@34.228.186.209:/home/ubuntu/saml2_sp
ssh ubuntu@34.228.186.209 'cd /home/ubuntu/saml2_sp && ./restart.sh ec2-nextjump'