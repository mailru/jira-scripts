import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.ofbiz.OfBizDelegator
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.ofbiz.core.entity.ConnectionFactory
import org.ofbiz.core.entity.DelegatorInterface

import java.sql.Connection

select("select ...")
//update("delete ...")
def select(String query){
    OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();
    DelegatorInterface delegatorInterface = delegator.getDelegatorInterface();
    String helperName = delegatorInterface.getGroupHelperName("default");
    Connection connection = ConnectionFactory.getConnection(helperName);
    Sql sql = new Sql(connection);
    
    List<GroovyRowResult> resultRows = []
    try{
        resultRows.addAll(sql.rows(query));
    } finally {
        //sql.close()
        connection.close()
    }

    return resultRows
}


def update(String query){
    OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();
    DelegatorInterface delegatorInterface = delegator.getDelegatorInterface();
    String helperName = delegatorInterface.getGroupHelperName("default");
    Connection connection = ConnectionFactory.getConnection(helperName);
    Sql sql = new Sql(connection);
    def i
    try{
        i = sql.executeUpdate(query);
    } finally {
        //sql.close()
        connection.close()
    }
    return i
}
