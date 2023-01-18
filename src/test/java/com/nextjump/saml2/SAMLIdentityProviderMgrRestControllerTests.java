package com.nextjump.saml2;

import com.nextjump.saml2.enums.MetadataSource;
import com.nextjump.saml2.model.SAMLIdentityProvider;
import com.nextjump.saml2.repository.SAMLIdentityProviderRepository;
import com.nextjump.saml2.service.SAMLIdentityProviderService;
import com.nextjump.saml2.view.SAMLIdentityProviderView;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class})
public class SAMLIdentityProviderMgrRestControllerTests {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AppSettings appSettings;

    @Autowired
    private SAMLIdentityProviderRepository samlIdentityProviderRepository;


    String testalias = "ssoCircle";
    MetadataSource testmetadataSource = MetadataSource.HTTP;
    String testmetadataUrl = "https://idp.ssocircle.com/meta-idp.xml";
    Boolean testdebugging = true;
    // username:password
    String basicauth = "nextjump:nextjump@123456";

    String plainCreds = basicauth;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    String base64Creds = new String(base64CredsBytes);

    @Test
    public void testGetAllIdp() throws JSONException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<String>(headers);

        SAMLIdentityProvider body = new SAMLIdentityProvider();
        body.setAlias(testalias);
        body.setMetadataSource(MetadataSource.HTTP);
        body.setMetadataUrl(testmetadataUrl);
        body = samlIdentityProviderRepository.save(body);
        String IdpID = body.getId();

        ResponseEntity<String> resp = restTemplate.exchange("/mgr/saml-identity-providers",HttpMethod.GET, request, String.class);
        Assert.assertEquals(HttpStatus.OK, resp.getStatusCode());

        JSONObject respbody = new JSONObject(resp.getBody());
        JSONArray respcontent = respbody.getJSONArray("content");
        JSONObject respidp = respcontent.getJSONObject(0);

        String respalias = respidp.getString("alias");
        String respmetadataSource = respidp.getString("metadataSource");
        String respmetadataUrl = respidp.getString("metadataUrl");


        Assert.assertEquals(respalias,testalias);
        Assert.assertEquals(respmetadataSource,testmetadataSource.toString());
        Assert.assertEquals(respmetadataUrl,testmetadataUrl);

        samlIdentityProviderRepository.deleteById(IdpID);

    }

    @Test
    public void testGetAllIdpbyID() throws JSONException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<String>(headers);

        SAMLIdentityProvider body = new SAMLIdentityProvider();
        body.setAlias(testalias);
        body.setMetadataSource(MetadataSource.HTTP);
        body.setMetadataUrl(testmetadataUrl);
        body = samlIdentityProviderRepository.save(body);
        String IdpID = body.getId();

        ResponseEntity<String> resp = restTemplate.exchange("/mgr/saml-identity-providers/"+IdpID,HttpMethod.GET, request, String.class);
        Assert.assertEquals(HttpStatus.OK, resp.getStatusCode());

        JSONObject respbody = new JSONObject(resp.getBody());
        String respalias = respbody.getString("alias");
        String respmetadataSource = respbody.getString("metadataSource");
        String respmetadataUrl = respbody.getString("metadataUrl");


        Assert.assertEquals(respalias,testalias);
        Assert.assertEquals(respmetadataSource,testmetadataSource.toString());
        Assert.assertEquals(respmetadataUrl,testmetadataUrl);

        samlIdentityProviderRepository.deleteById(IdpID);

    }


    @Test
    public void testCreateIdpHttp() throws JSONException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("alias", testalias);
        body.put("metadataSource", testmetadataSource.toString());
        body.put("metadataUrl", testmetadataUrl);

        HttpEntity<String> requestEntity = new HttpEntity<String>(
                body.toString(), headers
        );

        ResponseEntity<String> resp = restTemplate
                .exchange("/mgr/saml-identity-providers", HttpMethod.POST, requestEntity, String.class);
        Assert.assertTrue(resp.getStatusCode() == HttpStatus.OK);
        JSONObject respbody = new JSONObject(resp.getBody());
        String respalias = respbody.getString("alias");
        String respmetadataSource = respbody.getString("metadataSource");
        String respmetadataUrl = respbody.getString("metadataUrl");
        String respIdpId = respbody.getString("id");
        Assert.assertEquals(respalias,testalias);
        Assert.assertEquals(respmetadataSource,testmetadataSource.toString());
        Assert.assertEquals(respmetadataUrl,testmetadataUrl);

        samlIdentityProviderRepository.deleteById(respIdpId);

    }

    @Test
    public void testCreateIdpHTTPwithEmptyUrl() throws JSONException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("alias", testalias);
        body.put("metadataSource", testmetadataSource.toString());

        HttpEntity<String> requestEntity = new HttpEntity<String>(
                body.toString(), headers
        );

        ResponseEntity<String> resp = restTemplate
                .exchange("/mgr/saml-identity-providers", HttpMethod.POST, requestEntity, String.class);
        Assert.assertTrue(resp.getStatusCode() == HttpStatus.BAD_REQUEST);
        JSONObject respbody = new JSONObject(resp.getBody());
        String respmessage = respbody.getString("message");
        Assert.assertEquals(respmessage,"empty metadataUrl for HTTP not allowed");
    }


    @Test
    public void testCreateIdpHTTPwithEmptyAlias() throws JSONException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("metadataSource", testmetadataSource.toString());
        body.put("metadataUrl", testmetadataUrl);

        HttpEntity<String> requestEntity = new HttpEntity<String>(
                body.toString(), headers
        );

        ResponseEntity<String> resp = restTemplate
                .exchange("/mgr/saml-identity-providers", HttpMethod.POST, requestEntity, String.class);
        Assert.assertTrue(resp.getStatusCode() == HttpStatus.BAD_REQUEST);
        JSONObject respbody = new JSONObject(resp.getBody());
        String respmessage = respbody.getString("message");
        Assert.assertEquals(respmessage,"empty alias not allowed");
    }

    @Test
    public void testCreateIdpwithEmptySource() throws JSONException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject body = new JSONObject();
        body.put("alias", testalias);
        body.put("metadataUrl", testmetadataUrl);

        HttpEntity<String> requestEntity = new HttpEntity<String>(
                body.toString(), headers
        );

        ResponseEntity<String> resp = restTemplate
                .exchange("/mgr/saml-identity-providers", HttpMethod.POST, requestEntity, String.class);
        Assert.assertTrue(resp.getStatusCode() == HttpStatus.BAD_REQUEST);
        JSONObject respbody = new JSONObject(resp.getBody());
        String respmessage = respbody.getString("message");
        Assert.assertEquals(respmessage,"null metadataSource not allowed");
    }

    @Test
    public void testUpdateIdp() throws JSONException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        headers.setContentType(MediaType.APPLICATION_JSON);

        SAMLIdentityProvider body = new SAMLIdentityProvider();
        body.setAlias(testalias);
        body.setMetadataSource(MetadataSource.HTTP);
        body.setMetadataUrl(testmetadataUrl);
        body = samlIdentityProviderRepository.save(body);
        String IdpID = body.getId();

        JSONObject updatebody = new JSONObject();
        updatebody.put("alias", testalias);
        updatebody.put("metadataSource", testmetadataSource.toString());
        updatebody.put("metadataUrl", testmetadataUrl);
        updatebody.put("debugging", testdebugging);

        HttpEntity<String> requestEntity = new HttpEntity<String>(
                updatebody.toString(), headers
        );

        ResponseEntity<String> resp = restTemplate
                .exchange("/mgr/saml-identity-providers/"+IdpID, HttpMethod.POST, requestEntity, String.class);

        Assert.assertTrue(resp.getStatusCode() == HttpStatus.OK);

        JSONObject respbody = new JSONObject(resp.getBody());
        String respalias = respbody.getString("alias");
        String respmetadataSource = respbody.getString("metadataSource");
        String respmetadataUrl = respbody.getString("metadataUrl");
        Boolean respdebugging = respbody.getBoolean("debugging");

        Assert.assertEquals(respalias,testalias);
        Assert.assertEquals(respmetadataSource,testmetadataSource.toString());
        Assert.assertEquals(respmetadataUrl,testmetadataUrl);
        Assert.assertEquals(respdebugging,testdebugging);

        samlIdentityProviderRepository.deleteById(IdpID);

    }

    @Test
    public void testDeleteIdp() throws JSONException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);
        HttpEntity<String> request = new HttpEntity<String>(headers);


        SAMLIdentityProvider body = new SAMLIdentityProvider();
        body.setAlias(testalias);
        body.setMetadataSource(MetadataSource.HTTP);
        body.setMetadataUrl(testmetadataUrl);
        body = samlIdentityProviderRepository.save(body);
        String IdpID = body.getId();

        ResponseEntity<String> resp = restTemplate
                .exchange("/mgr/saml-identity-providers/"+IdpID, HttpMethod.DELETE, request, String.class);
        Assert.assertTrue(resp.getStatusCode() == HttpStatus.OK);

        SAMLIdentityProvider DeletedIdp = new SAMLIdentityProvider();
        DeletedIdp = samlIdentityProviderRepository.findByAlias(testalias);

        Assert.assertTrue(DeletedIdp == null);

    }
}
