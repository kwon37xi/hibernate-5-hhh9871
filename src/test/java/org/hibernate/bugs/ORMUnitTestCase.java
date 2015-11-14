/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.bugs;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.test.User;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import static org.junit.Assert.*;

/**
 * https://hibernate.atlassian.net/browse/HHH-9871 Issue reproduce
 */
public class ORMUnitTestCase extends BaseCoreFunctionalTestCase {

    private Logger log = LoggerFactory.getLogger(ORMUnitTestCase.class);

    // Add your entities here.
    @Override
    protected Class[] getAnnotatedClasses() {
        return new Class[]{
                User.class
        };
    }

    // If you use *.hbm.xml mappings, instead of annotations, add the mappings here.
    @Override
    protected String[] getMappings() {
        return new String[]{
        };
    }

    // If those mappings reside somewhere other than resources/org/hibernate/test, change this.
    @Override
    protected String getBaseForMappings() {
        return "org/hibernate/test/";
    }

    // Add in any settings that are specific to your test.  See resources/hibernate.properties for the defaults.
    @Override
    protected void configure(Configuration configuration) {
        super.configure(configuration);
    }

    // Add your tests, using standard JUnit.
    @Test
    public void hhh9871Test() throws Exception {
        // BaseCoreFunctionalTestCase automatically creates the SessionFactory and provides the Session.
        Session s = openSession();
        Transaction tx = s.beginTransaction();

        // save a User object that has employ=true, male=true, old=false column data.
        persistFixture(s);

        // employ : @Type(type = "true_false")
        // male : @Type(type = "yes_no")
        Query query = s.createQuery("from User user where employee = ?1 and male = ?1 and old = ?2");
        // ## THERE ARE TWO ?1 POSITIONAL PARAMETERS ##
        query.setParameter("1", Boolean.TRUE); // parameter value for two ?1 positional parameters
        query.setParameter("2", Boolean.FALSE);

        List users = query.list();
        log.info("Result users : {}", users);

        assertTrue("Query result must have 1 item.", users.size() == 1);

        tx.commit();
        s.close();
    }

    private void persistFixture(Session s) {
        User user = new User();
        user.setUsername("hibernate");
        user.setEmployee(true);
        user.setMale(true);
        user.setOld(false);

        s.persist(user);
    }
}
