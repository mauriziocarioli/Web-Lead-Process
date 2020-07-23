package com.home_security.web_lead;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Persistence;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkItemHandler;

public class ReadWebLeadSplitWorkItemHandler implements WorkItemHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ReadWebLeadSplitWorkItemHandler.class);
    private EntityManagerFactory emf;

    public ReadWebLeadSplitWorkItemHandler() {
        LOG.info("Registered ReadWebLeadSplitWorkItemHandler");
        emf = Persistence.createEntityManagerFactory("WEBLEADSPLIT_PERSISTENCE_UNIT");
    }

    @Override
    public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
        LOG.info("Executing Read WebLeadSplit Work Item with id '"+wi.getId()+
        "' on process instance: "+wi.getProcessInstanceId());
        EntityManager em = emf.createEntityManager();
        String s = "SELECT * FROM WEBLEAD.WEBLEADSPLIT WHERE NAME='"+(String)wi.getParameter("Name")+"'";
        Query q = em.createQuery(s);
        WebLeadSplit wls = new WebLeadSplit();
        try {
            em.joinTransaction();
            wls = (WebLeadSplit)q.getSingleResult();
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
        Map<String, Object> r = new HashMap<>();
        r.put("Result",wls);
        wim.completeWorkItem(wi.getId(), r);
    }

    @Override
    public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
    }

}