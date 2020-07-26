package com.home_security.web_lead;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;
import javax.persistence.TransactionRequiredException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.NoResultException;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkItemHandler;

public class WebLeadSplitWorkItemHandler implements WorkItemHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WebLeadSplitWorkItemHandler.class);
    private EntityManagerFactory emf;

    public WebLeadSplitWorkItemHandler(ClassLoader cl) {
        LOG.info("Registered UpdateWebLeadSplitWorkItemHandler");
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            LOG.info("Creating Entity Manager Factory");
            emf = Persistence.createEntityManagerFactory("WEBLEADSPLIT_PERSISTENCE_UNIT");
        } finally {
            Thread.currentThread().setContextClassLoader(ccl);
        }
    }

    @Override
    public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
        LOG.info("Executing WebLeadSplit Work Item with id '"+wi.getId() + 
                "' on process instance: "+wi.getProcessInstanceId());
        EntityManager em = emf.createEntityManager();
        switch ((String)wi.getParameter("Action")) {
            case "READ":
                LOG.info("Executing READ action");
                //JPQL 
                /*String s =
                "select w from WebLeadSplit w where w.name=:name";
                Query q = em.createQuery(s).setParameter("name", (String)wi.getParameter("Name"));*/
                //SQL
                String rs = "SELECT * FROM WEBLEADSPLIT W WHERE W.NAME='"+(String)wi.getParameter("Name")+"'";
                Query rq = em.createNativeQuery(rs,WebLeadSplit.class);
                Object wls = new Object();
                try {
                    em.joinTransaction();
                    LOG.info("Joined transaction");
                    wls = rq.getSingleResult();
                    if (wls == null) {LOG.error("wls is null");}
                    LOG.info("Result returned");
                } catch (NoResultException e) {
                    LOG.error("Read WebLeadSplit WIH: No result.");
                } catch (NonUniqueResultException e) {
                    LOG.error("Read WebLeadSplit WIH: No unique result.");
                } catch (QueryTimeoutException e) {
                    LOG.error("Read WebLeadSplit WIH: Query timeout.");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    em.close();
                }
                Map<String, Object> rr = new HashMap<>();
                rr.put("Result",wls);
                wim.completeWorkItem(wi.getId(), rr);
                LOG.info("Completed WebLeadSplit Work Item");
            break;
            case "UPDATE":
            LOG.info("Executing UPDATE action");
                //SQL
                String us = 
                    "UPDATE WEBLEADSPLIT SET "+
                        "LIVE_TO_DATE="+wi.getParameter("LiveToDate")+", "+
                        "SPLIT_RATIO="+wi.getParameter("SplitRatio")+", "+
                        "SPLIT_COUNT="+wi.getParameter("SplitCount")+", "+
                        "LOCAL_COUNT="+wi.getParameter("LocalCount")+" "+
                    "WHERE NAME='"+(String)wi.getParameter("Name")+"'";
                Query uq = em.createNativeQuery(us);
                try {
                    em.joinTransaction();
                    uq.executeUpdate();
                } catch (TransactionRequiredException e) {
                    LOG.error("Update WebLeadSplit WIH: No transaction has been joined to the persistence context.");
                }  catch (QueryTimeoutException e) {
                    LOG.error("Update WebLeadSplit WIH: Statement timed out and was rolled back.");
                } catch (PersistenceException e) {
                    LOG.error("Update WebLeadSplit WIH: Query timed out and was rolled back.");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    em.close();
                }
                Map<String, Object> ur = new HashMap<>();
                wim.completeWorkItem(wi.getId(), ur);
                LOG.info("Completed WebLeadSplit Work Item");
                break; 
        }
    }

    @Override
    public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
    }

}