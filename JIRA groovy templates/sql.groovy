import groovy.sql.Sql;
import groovy.sql.GroovyRowResult;
import java.sql.SQLException;
import java.sql.Connection;
import org.ofbiz.core.entity.ConnectionFactory;
import org.ofbiz.core.entity.DelegatorInterface;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.component.ComponentAccessor;

select("select ...")
//update("delete ...")
def select(query){
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


def update(query){
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
