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

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkItemHandler;

public class UpdateWebLeadSplitWorkItemHandler implements WorkItemHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateWebLeadSplitWorkItemHandler.class);
    private EntityManagerFactory emf;

    public UpdateWebLeadSplitWorkItemHandler(ClassLoader cl) {
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
        LOG.info("!Executing Update WebLeadSplit Work Item with id '"+wi.getId() + 
                "' on process instance: "+wi.getProcessInstanceId());
        EntityManager em = emf.createEntityManager();
        Object wls = wi.getParameter("WebLeadSplit");
        //SQL
        /*String s = 
            "UPDATE WEBLEADSPLIT SET "+
                "LIVE_TO_DATE="+wls.getLiveToDate()+", "+
                "SPLIT_RATIO="+wls.getSplitRatio()+", "+
                "SPLIT_COUNT="+wls.getSplitCount()+", "+
                "LOCAL_COUNT="+wls.getLocalCount()+" "+
            "WHERE NAME='"+(String)wi.getParameter("Name")+"'";
        Query q = em.createNativeQuery(s);*/

        try {
            em.joinTransaction();
            wls = em.merge(wls);
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
        Map<String, Object> r = new HashMap<>();
        LOG.info("Update WebLeadSplit WIH about to complete");
        r.put("Result",wls);
        wim.completeWorkItem(wi.getId(), r);
        LOG.info("Update WebLeadSplit WIH completed");
    }

    @Override
    public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
    }

}