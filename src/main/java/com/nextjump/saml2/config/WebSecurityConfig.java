package com.nextjump.saml2.config;

import com.google.common.base.Strings;
import com.nextjump.saml2.AppSettings;
import com.nextjump.saml2.core.SAMLUserDetailsServiceImpl;
import com.nextjump.saml2.ext.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import com.nextjump.saml2.ext.authentication.SimpleUrlAuthenticationFailureHandler;
import com.nextjump.saml2.ext.context.SAMLContextProviderImpl;
import com.nextjump.saml2.ext.context.SAMLContextProviderLB;
import com.nextjump.saml2.ext.log.BizSAMLLogger;
import com.nextjump.saml2.ext.logout.SecurityContextLogoutHandler;
import com.nextjump.saml2.ext.logout.SimpleUrlLogoutSuccessHandler;
import com.nextjump.saml2.ext.storage.MemorySAMLStorageFactory;
import com.nextjump.saml2.service.SAMLIdentityProviderService;
import com.nextjump.saml2.service.request.DefaultSAMLIdentityProviderQueryRequest;
import com.nextjump.saml2.view.SAMLIdentityProviderView;
import com.nextjump.saml2.web.filter.ExceptionHandlerFilter;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.binding.encoding.HTTPPostEncoder;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml.*;
import org.springframework.security.saml.metadata.*;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.*;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.*;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.*;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter implements InitializingBean, DisposableBean {
    private static final Logger LOG = LoggerFactory
            .getLogger(WebSecurityConfig.class);
    @Autowired
    private AppSettings appSettings;
    private Timer backgroundTaskTimer;
    private MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager;
    @Autowired
    private SAMLUserDetailsServiceImpl samlUserDetailsServiceImpl;
    @Lazy
    @Autowired
    private SAMLIdentityProviderService samlIdentityProviderService;

    // Initialization of OpenSAML library
    @Bean
    public static SAMLBootstrap sAMLBootstrap() {
        return new SAMLBootstrap();
    }

    public void init() {
        backgroundTaskTimer();
        this.multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
    }

    public void shutdown() {
        this.backgroundTaskTimer.purge();
        this.backgroundTaskTimer.cancel();
        this.multiThreadedHttpConnectionManager.shutdown();
    }

    @Bean(name = "backgroundTaskTimer")
    public Timer backgroundTaskTimer() {
        this.backgroundTaskTimer = new Timer(true);
        return this.backgroundTaskTimer;
    }

    // Initialization of the velocity engine
    @Bean
    public VelocityEngine velocityEngine() {
        return VelocityFactory.getEngine();
    }

    // XML parser pool needed for OpenSAML parsing
    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        return new StaticBasicParserPool();
    }

    @Bean(name = "parserPoolHolder")
    public ParserPoolHolder parserPoolHolder() {
        return new ParserPoolHolder();
    }

    // Bindings, encoders and decoders used for creating and parsing messages
    @Bean
    public HttpClient httpClient() {
        return new HttpClient(this.multiThreadedHttpConnectionManager);
    }

    // SAML Authentication Provider responsible for validating of received SAML
    // messages
    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setUserDetails(samlUserDetailsServiceImpl);
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }

    // Provider of default SAML Context
    @Bean
    public SAMLContextProviderImpl contextProvider() {
        if (appSettings.isEnableLB()) {
            SAMLContextProviderLB result = new SAMLContextProviderLB(appSettings);
            result.setStorageFactory(new MemorySAMLStorageFactory());
            result.setScheme(appSettings.getLbSchema());
            result.setServerName(appSettings.getLbServerName());
            result.setIncludeServerPortInRequestURL(false);
            result.setContextPath("");
            return result;
        } else {
            SAMLContextProviderImpl result = new SAMLContextProviderImpl(appSettings);
            result.setStorageFactory(new MemorySAMLStorageFactory());
            return result;
        }
    }

    // Logger for SAML messages and events
    @Bean
    public BizSAMLLogger samlLogger() {
//        SAMLDefaultLogger samlLogger = new SAMLDefaultLogger();
//        samlLogger.setLogAllMessages(true);
//        samlLogger.setLogErrors(true);
//        samlLogger.setLogMessagesOnException(true);
        BizSAMLLogger samlLogger = new BizSAMLLogger();
        return samlLogger;
    }

    // SAML 2.0 WebSSO Assertion Consumer
    @Bean
    public WebSSOProfileConsumer webSSOprofileConsumer() {
        com.nextjump.saml2.ext.websso.WebSSOProfileConsumerImpl result =
                new com.nextjump.saml2.ext.websso.WebSSOProfileConsumerImpl();
        result.setAppSettings(appSettings);
//        result.setMaxAuthenticationAge(30 * 24 * 60 * 60);//30 days
        return result;
    }

    // SAML 2.0 Holder-of-Key WebSSO Assertion Consumer
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
        WebSSOProfileConsumerHoKImpl result = new WebSSOProfileConsumerHoKImpl();
        result.setMaxAuthenticationAge(30 * 24 * 60 * 60);//30 days
        return result;
    }

    // SAML 2.0 Web SSO profile
    @Bean
    public WebSSOProfile webSSOprofile() {
        return new WebSSOProfileImpl();
    }

    // SAML 2.0 Holder-of-Key Web SSO profile
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
        WebSSOProfileConsumerHoKImpl result = new WebSSOProfileConsumerHoKImpl();
        result.setMaxAuthenticationAge(30 * 24 * 60 * 60);//30 days
        return result;
    }

    // SAML 2.0 ECP profile
    @Bean
    public WebSSOProfileECPImpl ecpprofile() {
        return new WebSSOProfileECPImpl();
    }

    @Bean
    public SingleLogoutProfile logoutprofile() {
        return new SingleLogoutProfileImpl();
    }

    // Central storage of cryptographic keys
    @Bean
    public com.nextjump.saml2.ext.key.JKSKeyManager keyManager() {
        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource storeFile = loader
                .getResource(appSettings.getKeystorePath());
        String storePass = appSettings.getKeystorePass();
        Map<String, String> passwords = new HashMap<String, String>();
        passwords.put(appSettings.getKeystoreKey(), appSettings.getKeystoreKeyPass());
        if (!appSettings.getExtraSpKeystoreKeys().isEmpty()) {
            for (int i = 0; i < appSettings.getExtraSpKeystoreKeys().size(); i++) {
                String key = appSettings.getExtraSpKeystoreKeys().get(i);
                if (Strings.isNullOrEmpty(key)) {
                    continue;
                }
                String pass = appSettings.getExtraSpKeystoreKeyPasses().get(i);
                passwords.put(key, pass);
            }
        }
//        if (!Strings.isNullOrEmpty(appSettings.getSp2KeystoreKey())) {
//            passwords.put(appSettings.getSp2KeystoreKey(), appSettings.getSp2KeystoreKeyPass());
//        }
        String defaultKey = appSettings.getKeystoreKey();
        return new com.nextjump.saml2.ext.key.JKSKeyManager(storeFile, storePass, passwords, defaultKey);
    }

    @Bean
    public WebSSOProfileOptions defaultWebSSOProfileOptions() {
        WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
        webSSOProfileOptions.setIncludeScoping(false);
        return webSSOProfileOptions;
    }

    // Entry point to initialize authentication, default values taken from
    // properties file
    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        com.nextjump.saml2.ext.SAMLEntryPoint samlEntryPoint = new com.nextjump.saml2.ext.SAMLEntryPoint();
        samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
        samlEntryPoint.setAppSettings(appSettings);
        return samlEntryPoint;
    }

    // Setup advanced info about metadata
//    @Bean
//    public ExtendedMetadata extendedMetadata() {
//        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
//        extendedMetadata.setIdpDiscoveryEnabled(true);
//        extendedMetadata.setSigningAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
//        extendedMetadata.setSignMetadata(true);
//        extendedMetadata.setEcpEnabled(true);
//        return extendedMetadata;
//    }

    // IDP Discovery Service
    @Bean
    public SAMLDiscovery samlIDPDiscovery() {
        SAMLDiscovery idpDiscovery = new SAMLDiscovery();
        idpDiscovery.setIdpSelectionPath("/saml/discovery");
        return idpDiscovery;
    }

//    @Bean
//    @Qualifier("idp-ssocircle")
//    public ExtendedMetadataDelegate ssoCircleExtendedMetadataProvider()
//            throws MetadataProviderException {
//        String idpSSOCircleMetadataURL = "https://idp.ssocircle.com/meta-idp.xml";
//        HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
//                this.backgroundTaskTimer, httpClient(), idpSSOCircleMetadataURL);
//        httpMetadataProvider.setParserPool(parserPool());
//        ExtendedMetadataDelegate extendedMetadataDelegate =
//                new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
//        extendedMetadataDelegate.setMetadataTrustCheck(true);
//        extendedMetadataDelegate.setMetadataRequireSignature(false);
//        backgroundTaskTimer.purge();
//        return extendedMetadataDelegate;
//    }

    // IDP Metadata configuration - paths to metadata of IDPs in circle of trust
    @Bean
    @Qualifier("metadata")
    public CachingMetadataManager metadata() throws MetadataProviderException {
        List<MetadataProvider> providers = new ArrayList<MetadataProvider>();
        DefaultSAMLIdentityProviderQueryRequest queryParam = new DefaultSAMLIdentityProviderQueryRequest();
        boolean lastPage = false;
        while (!lastPage) {
            Page<SAMLIdentityProviderView> samlIdentityProviders =
                    samlIdentityProviderService.queryPage(queryParam);
            for (SAMLIdentityProviderView samlIdentityProvider : samlIdentityProviders.getContent()) {
                providers.add(samlIdentityProviderService.generateMetadata(samlIdentityProviderService.getOne(samlIdentityProvider.getId())));
            }
            lastPage = samlIdentityProviders.isLast();
            queryParam.setPage(queryParam.getPage() + 1);
        }
//        providers.add(ssoCircleExtendedMetadataProvider());
        return new CachingMetadataManager(providers);
    }

    // Filter automatically generates default SP metadata
    @Bean
    public MetadataGenerator metadataGenerator() {
        com.nextjump.saml2.ext.metadata.MetadataGenerator metadataGenerator =
                new com.nextjump.saml2.ext.metadata.MetadataGenerator(appSettings);
        metadataGenerator.setEntityId(appSettings.getSpEntityId());
        if (!Strings.isNullOrEmpty(appSettings.getSpEntityBaseUrl())) {
            metadataGenerator.setEntityBaseURL(appSettings.getSpEntityBaseUrl());
        }

        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setIdpDiscoveryEnabled(true);
        extendedMetadata.setSigningAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        extendedMetadata.setSignMetadata(false);
        extendedMetadata.setEcpEnabled(true);
        extendedMetadata.setAlias(appSettings.getSpAlias());

        metadataGenerator.setExtendedMetadata(extendedMetadata);
        metadataGenerator.setIncludeDiscoveryExtension(false);
        metadataGenerator.setKeyManager(keyManager());
        return metadataGenerator;
    }

    @Bean(name = "extraSpMetadataGenerator")
    @ConditionalOnProperty(prefix = "com.nextjump.saml2", value = "extraSpEntityIds")
    public com.nextjump.saml2.ext.metadata.MetadataGenerator extraSpMetadataGenerator() {
        com.nextjump.saml2.ext.metadata.MetadataGenerator metadataGenerator =
                new com.nextjump.saml2.ext.metadata.MetadataGenerator(appSettings);
//        metadataGenerator.setEntityId(appSettings.getSp2EntityId());
//        if (!Strings.isNullOrEmpty(appSettings.getSp2EntityBaseUrl())) {
//            metadataGenerator.setEntityBaseURL(appSettings.getSp2EntityBaseUrl());
//        }

        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setIdpDiscoveryEnabled(true);
        extendedMetadata.setSigningAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        extendedMetadata.setSignMetadata(false);
        extendedMetadata.setEcpEnabled(true);
//        extendedMetadata.setAlias(appSettings.getSp2Alias());
//        extendedMetadata.setSigningKey(appSettings.getSp2KeystoreKey());

        metadataGenerator.setExtendedMetadata(extendedMetadata);
        metadataGenerator.setIncludeDiscoveryExtension(false);
        metadataGenerator.setKeyManager(keyManager());
        return metadataGenerator;
    }

    // The filter is waiting for connections on URL suffixed with filterSuffix
    // and presents SP metadata there
    @Bean
    public MetadataDisplayFilter metadataDisplayFilter() {
        com.nextjump.saml2.ext.metadata.MetadataDisplayFilter result =
                new com.nextjump.saml2.ext.metadata.MetadataDisplayFilter();
        return result;
    }

    // Handler deciding where to redirect user after successful login
    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
        SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        if (appSettings.isEnableLB()) {
            String url = String.format("%s://%s%s/landing", appSettings.getLbSchema(),
                    appSettings.getLbServerName(),
                    appSettings.getLbServerContextPath());
            successRedirectHandler.setAlwaysUseDefaultTargetUrl(true);
            successRedirectHandler.setDefaultTargetUrl(url);
            successRedirectHandler.setAppSettings(appSettings);
        } else {
            successRedirectHandler.setDefaultTargetUrl("/landing");
        }
        return successRedirectHandler;
    }

    // Handler deciding where to redirect user after failed login
    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler failureHandler =
                new SimpleUrlAuthenticationFailureHandler();
        if (appSettings.isEnableLB()) {
            String url = String.format("%s://%s%s/error", appSettings.getLbSchema(),
                    appSettings.getLbServerName(),
                    appSettings.getLbServerContextPath());
            failureHandler.setDefaultFailureUrl(url);
            failureHandler.setAppSettings(appSettings);
        } else {
            failureHandler.setUseForward(true);
            failureHandler.setDefaultFailureUrl("/error");
        }

        return failureHandler;
    }

    @Bean
    public SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter() throws Exception {
        SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter = new SAMLWebSSOHoKProcessingFilter();
        samlWebSSOHoKProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOHoKProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOHoKProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlWebSSOHoKProcessingFilter;
    }

    // Processing filter for WebSSO profile messages
    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
        SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlWebSSOProcessingFilter;
    }

    public SAMLProcessingFilter samlWebSSOProcessingFilter(String processingUrl) throws Exception {
        SAMLProcessingFilter samlWebSSOProcessingFilter =
                new SAMLProcessingFilter();
        samlWebSSOProcessingFilter.setFilterProcessesUrl(processingUrl);
        samlWebSSOProcessingFilter.setContextProvider(contextProvider());
        samlWebSSOProcessingFilter.setSAMLProcessor(processor());
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlWebSSOProcessingFilter;
    }

    @Bean
    public MetadataGeneratorFilter metadataGeneratorFilter() {
        return new MetadataGeneratorFilter(metadataGenerator());
    }

    @Bean
    @ConditionalOnProperty(prefix = "com.nextjump.saml2", value = "extraSpEntityIds")
    public MetadataGeneratorFilter extraSpsMetadataGeneratorFilter() {
        return new com.nextjump.saml2.ext.metadata.MetadataGeneratorFilter(extraSpMetadataGenerator(), appSettings);
    }

    // Handler for successful logout
    @Bean
    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler successLogoutHandler = new SimpleUrlLogoutSuccessHandler();
        if (appSettings.isEnableLB()) {
            String url = String.format("%s://%s%s/landing", appSettings.getLbSchema(),
                    appSettings.getLbServerName(),
                    appSettings.getLbServerContextPath());
            successLogoutHandler.setDefaultTargetUrl(url);
            successLogoutHandler.setAppSettings(appSettings);
        } else {
            successLogoutHandler.setDefaultTargetUrl("/");
        }
        return successLogoutHandler;
    }

    // Logout handler terminating local session
    @Bean
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler =
                new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        return logoutHandler;
    }

    // Filter processing incoming logout messages
    // First argument determines URL user will be redirected to after successful
    // global logout
    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        return new SAMLLogoutProcessingFilter(successLogoutHandler(),
                logoutHandler());
    }

    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter(String processUrl) {
        SAMLLogoutProcessingFilter result = new SAMLLogoutProcessingFilter(successLogoutHandler(),
                logoutHandler());
        result.setFilterProcessesUrl(processUrl);
        result.setSAMLProcessor(processor());
        result.setContextProvider(contextProvider());
        result.setSamlLogger(samlLogger());
        result.setLogoutProfile(logoutprofile());
        return result;
    }

    // Overrides default logout processing filter with the one processing SAML
    // messages
    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        return new SAMLLogoutFilter(successLogoutHandler(),
                new LogoutHandler[]{logoutHandler()},
                new LogoutHandler[]{logoutHandler()});
    }

    // Bindings
    private ArtifactResolutionProfile artifactResolutionProfile() {
        final ArtifactResolutionProfileImpl artifactResolutionProfile =
                new ArtifactResolutionProfileImpl(httpClient());
        artifactResolutionProfile.setProcessor(new SAMLProcessorImpl(soapBinding()));
        return artifactResolutionProfile;
    }

    @Bean
    public HTTPArtifactBinding artifactBinding(ParserPool parserPool, VelocityEngine velocityEngine) {
        return new HTTPArtifactBinding(parserPool, velocityEngine, artifactResolutionProfile());
    }

    @Bean
    public HTTPSOAP11Binding soapBinding() {
        return new HTTPSOAP11Binding(parserPool());
    }

    @Bean
    public HTTPPostBinding httpPostBinding() {
        com.nextjump.saml2.ext.decoding.HTTPPostDecoder httpPostDecoder =
                new com.nextjump.saml2.ext.decoding.HTTPPostDecoder(parserPool());
        return new HTTPPostBinding(parserPool(), httpPostDecoder,
                new HTTPPostEncoder(velocityEngine(), "/templates/saml2-post-binding.vm"));
//        return new HTTPPostBinding(parserPool(), velocityEngine());
    }

    @Bean
    public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
        return new HTTPRedirectDeflateBinding(parserPool());
    }

    @Bean
    public HTTPSOAP11Binding httpSOAP11Binding() {
        return new HTTPSOAP11Binding(parserPool());
    }

    @Bean
    public HTTPPAOS11Binding httpPAOS11Binding() {
        return new HTTPPAOS11Binding(parserPool());
    }

    // Processor
    @Bean
    public SAMLProcessorImpl processor() {
        Collection<SAMLBinding> bindings = new ArrayList<SAMLBinding>();
        bindings.add(httpRedirectDeflateBinding());
        bindings.add(httpPostBinding());
        bindings.add(artifactBinding(parserPool(), velocityEngine()));
        bindings.add(httpSOAP11Binding());
        bindings.add(httpPAOS11Binding());
        return new SAMLProcessorImpl(bindings);
    }

    /**
     * Define the security filter chain in order to support SSO Auth by using SAML 2.0
     *
     * @return Filter chain proxy
     * @throws Exception
     */
    @Bean
    public FilterChainProxy samlFilter() throws Exception {
        List<SecurityFilterChain> chains = new ArrayList<SecurityFilterChain>();
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"),
                samlEntryPoint()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"),
                samlLogoutFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/metadata/**"),
                metadataDisplayFilter()));

        SAMLProcessingFilter samlProcessingFilter = samlWebSSOProcessingFilter();
        // default sign on
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"),
                samlProcessingFilter));
        for (String extraSpSsoPath : appSettings.getExtraSpSsoPaths()) {
            if (!Strings.isNullOrEmpty(extraSpSsoPath)) {
                LOG.info(String.format("Added %s as SSO path", extraSpSsoPath));
                chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(extraSpSsoPath),
                        samlWebSSOProcessingFilter(extraSpSsoPath)));
            }
        }


        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSOHoK/**"),
                samlWebSSOHoKProcessingFilter()));
        // default logout
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"),
                samlLogoutProcessingFilter()));
        for (String extraSpSloPath : appSettings.getExtraSpSloPaths()) {
            if (!Strings.isNullOrEmpty(extraSpSloPath)) {
                LOG.info(String.format("Added %s as logout path", extraSpSloPath));
                chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(extraSpSloPath),
                        samlLogoutProcessingFilter(extraSpSloPath)));
            }
        }

        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/discovery/**"),
                samlIDPDiscovery()));
        return new FilterChainProxy(chains);
    }

    /**
     * Returns the authentication manager currently used by Spring.
     * It represents a bean definition with the aim allow wiring from
     * other classes performing the Inversion of Control (IoC).
     *
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public ExceptionHandlerFilter exceptionHandlerFilter() {
        return new ExceptionHandlerFilter();
    }

    /**
     * Defines the web based security configuration.
     *
     * @param http It allows configuring web based security for specific http requests.
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic()
                .authenticationEntryPoint(samlEntryPoint());
        http.csrf().disable();
        http
                .addFilterBefore(exceptionHandlerFilter(), ChannelProcessingFilter.class)
                .addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class);
        if (!appSettings.getExtraSpEntityIds().isEmpty()) {
            http.addFilterBefore(extraSpsMetadataGeneratorFilter(), ChannelProcessingFilter.class);
        }
        http.addFilterAfter(samlFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(samlFilter(), CsrfFilter.class);

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry config = http
                .authorizeRequests();
        for (String extraSpSsoPath : appSettings.getExtraSpSsoPaths()) {
            if (!Strings.isNullOrEmpty(extraSpSsoPath)) {
                config.antMatchers(extraSpSsoPath).permitAll();
            }
        }
        for (String extraSpSloPath : appSettings.getExtraSpSloPaths()) {
            if (!Strings.isNullOrEmpty(extraSpSloPath)) {
                config.antMatchers(extraSpSloPath).permitAll();
            }
        }

        config
                .antMatchers("/mgr/**", "/swagger-ui.html", "/swagger-ui/**").hasRole("MGR")
                .antMatchers("/").permitAll()
                .antMatchers("/saml/**").permitAll()
                .antMatchers("/css/**").permitAll()
                .antMatchers("/img/**").permitAll()
                .antMatchers("/h2-console").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/js/**").permitAll()
                .anyRequest().authenticated();
        http
                .logout()
                .disable();    // The logout procedure is already handled by SAML filters.
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Sets a custom authentication provider.
     *
     * @param auth SecurityBuilder used to create an AuthenticationManager.
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(appSettings.getBasicAuthnUser()).password(passwordEncoder().encode(appSettings.getBasicAuthnPass()))
                .authorities("ROLE_MGR");
        auth
                .authenticationProvider(samlAuthenticationProvider());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/h2-console/**",
//                "/swagger-ui.html",
//                "/swagger-ui/**",
                "/v3/**",
                "/error",
                "/static/**"
        );
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Override
    public void destroy() throws Exception {
        shutdown();
    }


//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication()
//                .withUser(appSettings.getBasicAuthnUser()).password(appSettings.getBasicAuthnPass())
//                .authorities("ROLE_MGR");
//        auth.authenticationProvider()
//    }
}
