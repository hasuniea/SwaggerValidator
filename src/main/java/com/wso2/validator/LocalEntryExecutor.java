
package com.wso2.validator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.executors.APIExecutor;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.localentry.stub.types.LocalEntryAdminException;
import org.wso2.carbon.localentry.stub.types.LocalEntryAdminServiceStub;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.rmi.RemoteException;
import java.util.Map;


public class LocalEntryExecutor extends APIExecutor {
    private static final Logger logger = LoggerFactory.getLogger(LocalEntryExecutor.class);
    API api;

    @Override
    public boolean execute(RequestContext context, String currentState, String targetState) {

        boolean executed;
        executed = super.execute(context, currentState, targetState);
        //create local entry admin service stub
        String domain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String user = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        String userWithDomain = user;
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(domain)) {
            userWithDomain = user + APIConstants.EMAIL_DOMAIN_SEPARATOR + domain;
        }
        userWithDomain = APIUtil.replaceEmailDomainBack(userWithDomain);
        try {
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(context.getSystemRegistry(), APIConstants.API_KEY);
            Resource apiResource = context.getResource();
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                return false;
            }
            //retrieve swagger
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            api = APIUtil.getAPI(apiArtifact);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(userWithDomain);
            String apiSwagger = apiProvider.getOpenAPIDefinition(api.getId());
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            Map<String, Environment> environments = config.getApiGatewayEnvironments();
            //add swagger as local entry all existing gateways
            for (String environmentName : api.getEnvironments()) {
                Environment environment = environments.get(environmentName);
                addLocalEntry(environment, api.getUUID(), apiSwagger);
            }
        } catch (AxisFault axisFault) {
            logger.error("Error while calling local entry admin service", axisFault);
            return false;
        } catch (RemoteException e) {
            logger.error("Error while contacting the authentication admin service", e);
            return false;
        } catch (LocalEntryAdminException e) {
            logger.error("Error while contacting the local entry admin service", e);
            return false;
        } catch (APIManagementException e) {
            logger.error("Failed to retrieve the API swagger definition", e);
            return false;
        } catch (RegistryException e) {
            logger.error("Failed to get the generic artifact while executing OBAPIPublisherExecutor", e);
            return false;
        }
        return executed;
    }

    private void setup(Stub stub, Environment environment) throws AxisFault {

        String cookie = loginToGateway(environment);
        ServiceClient serviceClient = stub._getServiceClient();
        Options options = serviceClient.getOptions();
        options.setTimeOutInMilliSeconds(15 * 60 * 1000);
        options.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);

    }

    private String loginToGateway(Environment environment) throws AxisFault {

        String userName = environment.getUserName();
        char[] password = null;
        if (environment.getPassword() != null) {
            password = environment.getPassword().toCharArray();
        }
        String serverURL = environment.getServerURL();

        if (serverURL == null || userName == null || password == null) {
            throw new AxisFault("Required API gateway admin configuration unspecified");
        }
        String host;

        host = new URL(serverURL).getHost();
        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null,
                serverURL + "AuthenticationAdmin");
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            authAdminStub.login(userName, String.valueOf(password), host);
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            clearPassword(password);
            return (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        } catch (RemoteException e) {
            throw new AxisFault("Error while contacting the authentication admin service", e);
        } catch (LoginAuthenticationExceptionException e) {
            throw new AxisFault("Error while authenticating against the API gateway admin", e);
        }
    }

    private static void clearPassword(char[] password) {

        for (int i = 0; i < password.length; i++) {
            password[i] = ' ';
        }
    }

    private void addLocalEntry(Environment environment, String localEntryKey, String apiSwagger)
            throws RemoteException, LocalEntryAdminException {

        String localEntryServiceURL = environment.getServerURL() + "LocalEntryAdmin";
        LocalEntryAdminServiceStub localEntryAdminServiceStub = new LocalEntryAdminServiceStub(localEntryServiceURL);
        setup(localEntryAdminServiceStub, environment);
        CarbonUtils.setBasicAccessSecurityHeaders(environment.getUserName(), environment.getPassword(),
                localEntryAdminServiceStub._getServiceClient());
        String[] entries = localEntryAdminServiceStub.getEntryNames();
        boolean isLocalEntryExist = false;
        String keyToDelete = "";
        if (entries != null) {
            for (String entry : entries) {
                if (entry.contains(localEntryKey)) {
                    isLocalEntryExist = true;
                    keyToDelete = entry;
                    break;
                }
            }
        }

        if (!isLocalEntryExist) {
            //add a new entry
            localEntryAdminServiceStub.addEntry("<localEntry key=\"" + api.getUUID() + "\">" +
                    apiSwagger.replaceAll("&(?!amp;)", "&amp;").
                            replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</localEntry>");
            //update entry
        } else {
            localEntryAdminServiceStub.deleteEntry(keyToDelete);
            localEntryAdminServiceStub.addEntry("<localEntry key=\"" + api.getUUID() + "\">" +
                    apiSwagger.replaceAll("&(?!amp;)", "&amp;").
                            replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</localEntry>");
        }
    }

}
