<?php
// $data is assumed to contain the data to be signed

// fetch private key from file and ready it
$pkeyid = openssl_pkey_get_private("file://./apollo.pkcs8");
$data = 'orgdir=nextjump;ecid=whu';
// compute signature
openssl_sign($data, $signature, $pkeyid);

// free the key from memory
openssl_free_key($pkeyid);
echo $signature;
print "\n";
echo base64_encode($signature);
print "\n";
$encode_data  = base64_encode($data.';signature='.$signature);
echo $encode_data;
print "\n";
echo base64_decode($encode_data);
print "\n";
echo base64_decode("b3JnZGlyPW5leHRqdW1wO2VjaWQ9d2h1O3NpZ25hdHVyZT1QwdAH6gLU50XlfuVHMhOKJC7vKe7WFtoER7N3VH9B8QvO3pK2cI9+ysjHHgBoaAIbLuHf1lOzARgmYfEzzrG9SjNRLCxCAESsgycWtJLWZnoT5W9IL4gzcwXWIAtVCEEBU46gMXNz5qZVrXpsU/3gZNSeVZpydrAfyij+CqgxC1ep0PmJ0mPcH4UONzIAPjvOdAo4y4k9HD9uLdba2uO1QmjUWoRRWsgIdeN3EmAj41frxpmZefmHSFMiGYBplcFISjR/i9A7lEBL6cBI9KzWU5oQBG29udf/4iWnETfMHqBfoa+oQaAXYO0aYKRp5h7gtKBvwcL57z813Qkjwjon");
?>