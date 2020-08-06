# carbon-apimgt

## Prerequisite for building support branches

- Replace the ```<repositories>``` tag of the parent pom.xml file of the relevant support branch with the following before building the branch.
```
<repositories>
    <repository>
        <id>wso2-nexus</id>
        <name>WSO2 internal Repository</name>
        <url>https://support-maven.wso2.org/nexus/content/repositories/releases/</url>
        <releases>
            <enabled>true</enabled>
            <updatePolicy>daily</updatePolicy>
            <checksumPolicy>ignore</checksumPolicy>
        </releases>
    </repository>
</repositories>
```
- Make sure to be connected to the wso2 vpn when following this method.
- This method does not require to build the additional repos required by the corresponding support branch.
- As an alternative, build the additional repos mentioned under the relevant support branch before building the branch. (Do not need to be connected to the wso2 vpn when following this method)

## Corresponding APIM version of each support branch

- Support-1.2.0 - APIM 1.9.0
- Support-1.2.5 - APIM 1.9.1
- Support-5.0.3 - APIM 1.10.0
- Support-6.0.4 - APIM 2.0.0
- Support-6.1.66 - APIM 2.1.0

    We need to build below two additional Repos before building support-6.1.66 branch:

    1. carbon-governance : support-4.7.0
https://github.com/wso2-support/carbon-governance/tree/support-4.7.0

    2. wso2-wsdl4j : support-1.6.2-wso2v4
https://github.com/wso2-support/wso2-wsdl4j/tree/support-1.6.2-wso2v4

- Support-6.2.201 - APIM 2.2.0
- Support-6.3.95 - APIM 2.5.0

    We need to build following additional Repo before building support-6.3.95 branch:

    1. identity-inbound-auth-oauth - support-6.0.14
https://github.com/wso2-support/identity-inbound-auth-oauth/tree/support-6.0.14

- Support-6.4.50 - APIM 2.6.0

    We need to build following additional Repo before building support-6.4.50 branch:
    
    1. orbit - master
https://github.com/wso2-support/orbit
    2. carbon-kernel - support-4.4.35
https://github.com/wso2-support/carbon-kernel/tree/support-4.4.35
    3. carbon-consent-management - support-2.0.18
https://github.com/wso2-support/carbon-consent-management/tree/support-2.0.18
    4. carbon-identity-framework - support-5.12.153
https://github.com/wso2-support/carbon-identity-framework/tree/support-5.12.153
    5. wso2-axis2 - support-1.6.1-wso2v28
https://github.com/wso2-support/wso2-axis2/tree/support-1.6.1-wso2v28
    6. identity-inbound-auth-oauth - support-6.0.53
https://github.com/wso2-support/identity-inbound-auth-oauth/tree/support-6.0.53

- Support-6.5.349 - APIM 3.0.0
- [Support-6.6.163](https://github.com/wso2-support/carbon-apimgt/tree/support-6.6.163) - APIM 3.1.0

    We need to build following additional Repos before building support-6.6.163 branch:

    1. carbon4-kernel/core/javax.cache - support-4.6.0
https://github.com/wso2-support/carbon4-kernel/tree/support-4.6.0/core/javax.cache
    2. carbon-identity-framework - support-5.17.5
https://github.com/wso2-support/carbon-identity-framework/tree/support-5.17.5
    3. identity-inbound-auth-oauth - support-6.4.2
https://github.com/wso2-support/identity-inbound-auth-oauth/tree/support-6.4.2
