/*
 * Copyright 2014 Unicon, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.unicon.cas.support.wsfederation.authentication.principal;

import net.unicon.cas.support.wsfederation.WsFederationUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml1.core.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * @author John Gasper
 * @since 3.5.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/applicationContext.xml")
public class WsFederationCredentialTests {

    @Autowired
    HashMap<String, String> testTokens;
    
    WsFederationCredential standardCred;

    /**
     *
     */
    @Before
    public void setUp() {
        standardCred = new WsFederationCredential();
        standardCred.setNotBefore(new DateTime().withZone(DateTimeZone.UTC));
        standardCred.setNotOnOrAfter(new DateTime().withZone(DateTimeZone.UTC).plusHours(1));
        standardCred.setIssuedOn(new DateTime().withZone(DateTimeZone.UTC));
        standardCred.setIssuer("http://adfs.example.com/adfs/services/trust");
        standardCred.setAudience("urn:federation:cas");
        standardCred.setId("_6257b2bf-7361-4081-ae1f-ec58d4310f61");
        standardCred.setRetrievedOn(new DateTime().withZone(DateTimeZone.UTC).plusSeconds(1));
    }

    /**
     *
     */
    @Test
    public void testToString() {
        final String wresult = testTokens.get("goodToken");
        final Assertion assertion = WsFederationUtils.parseTokenFromString(wresult);
        final WsFederationCredential instance = WsFederationUtils.createCredentialFromToken(assertion);
        final String expResult =
        "[ID=_6257b2bf-7361-4081-ae1f-ec58d4310f61,Issuer=http://adfs.example.com/adfs/services/trust,"
        + "Audience=urn:federation:cas,Authentication Method=urn:federation:authentication:windows,"
        + "Issued On=2014-02-26T22:51:16.504Z,Valid After=2014-02-26T22:51:16.474Z,Valid Before=2014-02-26T23:51:16.474Z,"
        + "Attributes={Group=example.com\\Domain Users, upn=jgasper@example.com, surname=Gasper, givenname=John, emailaddress=jgasper@example.com}]";
        final String result = instance.toString();
        assertEquals("toString() not equal", expResult, result);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testIsValidAllGood() throws Exception {
        final boolean result = standardCred.isValid("urn:federation:cas", "http://adfs.example.com/adfs/services/trust", 2000);
        assertTrue("testIsValidAllGood() - True", result);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testIsValidBadAudience() throws Exception {
        standardCred.setAudience("urn:NotUs");
        final boolean result = standardCred.isValid("urn:federation:cas", "http://adfs.example.com/adfs/services/trust", 2000);
        assertFalse("testIsValidBadAudeience() - False", result);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testIsValidBadIssuer() throws Exception {
        standardCred.setIssuer("urn:NotThem");
        final boolean result = standardCred.isValid("urn:federation:cas", "http://adfs.example.com/adfs/services/trust", 2000);
        assertFalse("testIsValidBadIssuer() - False", result);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testIsValidEarlyToken() throws Exception {
        standardCred.setNotBefore(new DateTime().withZone(DateTimeZone.UTC).plusDays(1));
        standardCred.setNotOnOrAfter(new DateTime().withZone(DateTimeZone.UTC).plusHours(1).plusDays(1));
        standardCred.setIssuedOn(new DateTime().withZone(DateTimeZone.UTC).plusDays(1));
        
        final boolean result = standardCred.isValid("urn:federation:cas", "http://adfs.example.com/adfs/services/trust", 2000);
        assertFalse("testIsValidEarlyToken() - False", result);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testIsValidOldToken() throws Exception {
        standardCred.setNotBefore(new DateTime().withZone(DateTimeZone.UTC).minusDays(1));
        standardCred.setNotOnOrAfter(new DateTime().withZone(DateTimeZone.UTC).plusHours(1).minusDays(1));
        standardCred.setIssuedOn(new DateTime().withZone(DateTimeZone.UTC).minusDays(1));
        
        final boolean result = standardCred.isValid("urn:federation:cas", "http://adfs.example.com/adfs/services/trust", 2000);
        assertFalse("testIsValidOldToken() - False", result);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testIsValidExpiredIssuedOn() throws Exception {
        standardCred.setIssuedOn(new DateTime().withZone(DateTimeZone.UTC).minusSeconds(3));
        
        final boolean result = standardCred.isValid("urn:federation:cas", "http://adfs.example.com/adfs/services/trust", 2000);
        assertFalse("testIsValidOldToken() - False", result);
    }

    /**
     * sets the Token
     * @param testTokens the test tokens
     */
    public void setTestTokens(final HashMap<String, String> testTokens) {
        this.testTokens = testTokens;
    }
}
