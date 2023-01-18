<?php
// $data is assumed to contain the data to be signed

// fetch private key from file and ready it
$pubkeyid = openssl_pkey_get_public("file://./apollo.pub");
// $data = 'orgdir=nextjump;ecid=whu';
// $signature = base64_decode('UMHQB+oC1OdF5X7lRzITiiQu7ynu1hbaBEezd1R/QfELzt6StnCPfsrIxx4AaGgCGy7h39ZTswEYJmHxM86xvUozUSwsQgBErIMnFrSS1mZ6E+VvSC+IM3MF1iALVQhBAVOOoDFzc+amVa16bFP94GTUnlWacnawH8oo/gqoMQtXqdD5idJj3B+FDjcyAD47znQKOMuJPRw/bi3W2trjtUJo1FqEUVrICHXjdxJgI+NX68aZmXn5h0hTIhmAaZXBSEo0f4vQO5RAS+nASPSs1lOaEARtvbnX/+IlpxE3zB6gX6GvqEGgF2DtGmCkaeYe4LSgb8HC+e8/Nd0JI8I6Jw==');

// $ok = openssl_verify($data, $signature, $pubkeyid);
// if ($ok == 1) {
//     echo "good";
// } elseif ($ok == 0) {
//     echo "bad";
// } else {
//     echo "ugly, error checking signature";
// }
// // free the key from memory
// openssl_free_key($pubkeyid);

$samlresponse = "b3JnZGlyPW5leHRqdW1wO2VjaWQ9d2h1O3NpZ25hdHVyZT1VTUhRQitvQzFPZEY1WDdsUnpJVGlpUXU3eW51MWhiYUJFZXpkMVIvUWZFTHp0NlN0bkNQZnNySXh4NEFhR2dDR3k3aDM5WlRzd0VZSm1IeE04Nnh2VW96VVN3c1FnQkVySU1uRnJTUzFtWjZFK1Z2U0MrSU0zTUYxaUFMVlFoQkFWT09vREZ6YythbVZhMTZiRlA5NEdUVW5sV2FjbmF3SDhvby9ncW9NUXRYcWRENWlkSmozQitGRGpjeUFENDd6blFLT011SlBSdy9iaTNXMnRyanRVSm8xRnFFVVZySUNIWGpkeEpnSStOWDY4YVptWG41aDBoVElobUFhWlhCU0VvMGY0dlFPNVJBUytuQVNQU3MxbE9hRUFSdHZiblgvK0lscHhFM3pCNmdYNkd2cUVHZ0YyRHRHbUNrYWVZZTRMU2diOEhDK2U4L05kMEpJOEk2Snc9PQ==";
$samlresponse = base64_decode($samlresponse);
echo $samlresponse;
print "\n";
// $saml_array = explode(';', $samlresponse);
$keys = array("signature");
$values = array("UMHQB+oC1OdF5X7lRzITiiQu7ynu1hbaBEezd1R/QfELzt6StnCPfsrIxx4AaGgCGy7h39ZTswEYJmHxM86xvUozUSwsQgBErIMnFrSS1mZ6E+VvSC+IM3MF1iALVQhBAVOOoDFzc+amVa16bFP94GTUnlWacnawH8oo/gqoMQtXqdD5idJj3B+FDjcyAD47znQKOMuJPRw/bi3W2trjtUJo1FqEUVrICHXjdxJgI+NX68aZmXn5h0hTIhmAaZXBSEo0f4vQO5RAS+nASPSs1lOaEARtvbnX/+IlpxE3zB6gX6GvqEGgF2DtGmCkaeYe4LSgb8HC+e8/Nd0JI8I6Jw==");
$saml_array = array_combine($keys, $values);
// var_dump($saml_array);
$pos = strrpos($samlresponse, ';');
// echo $pos;
// print "\n";
$samlresponseorigin=substr($samlresponse,0,$pos);
echo $samlresponseorigin;
print "\n";
// echo $saml_array["signature"];
// print "\n";
$signatureValid = array_key_exists('signature', $saml_array) && $saml_array["signature"]!='' && openssl_verify($samlresponseorigin, base64_decode($saml_array["signature"]), $pubkeyid);
echo $signatureValid;
print "\n";
?>