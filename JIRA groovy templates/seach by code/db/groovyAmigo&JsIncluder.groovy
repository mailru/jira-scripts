import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.ofbiz.OfBizDelegator
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.ofbiz.core.entity.ConnectionFactory
import org.ofbiz.core.entity.DelegatorInterface

import java.sql.Connection

def findText = "test"
[
        groovyAmigo: new FinderGroovyAmigo().find(findText, true),
        JsIncluder : new FinderJsIncluder().find(findText, true)
]


class FinderJsIncluder {
    def find(String findText, boolean ignoreCase) {
        def pluginTableInfo = new TableManager().pluginTablesMap.find { it.pluginKey == 'jsincluder' }//or just set plugin table prefix
        def tableName = "${pluginTableInfo.tablePrefix}_SCRIPT"
        if(ignoreCase){
            return new DBService().select("select * from ${tableName} Where LOWER(code) like LOWER('%${findText}%') ")?.name
        }
        return new DBService().select("select * from ${tableName} Where code like '%${findText}%' ")?.name
    }
}


class FinderGroovyAmigo {
    def find(String findText, boolean ignoreCase) {
        def pluginTableInfo = new TableManager().pluginTablesMap.find { it.pluginKey == 'Groovy Amigo' }

        def whereScriptBodyLikeText = "SCRIPT_BODY like '%${findText}%'"
        if(ignoreCase){
            whereScriptBodyLikeText = " LOWER(SCRIPT_BODY) like  LOWER('%${findText}%')"
        }
        def tableNameScript = "${pluginTableInfo.tablePrefix}_SCRIPT"
        def tableDirectory = "${pluginTableInfo.tablePrefix}_SCRIPT_DIRECTORY"
        def scripts = new DBService().select("select ${tableDirectory}.NAME as DIRECTORY_NAME, ${tableNameScript}.NAME from ${tableNameScript} join ${tableDirectory} on ${tableDirectory}.ID = ${tableNameScript}.DIRECTORY_ID where ${tableNameScript}.DELETED = false and ${whereScriptBodyLikeText}")

        def tableNameRestScript = "${pluginTableInfo.tablePrefix}_REST_SCRIPT"
        def restScripts = new DBService().select("select NAME from ${tableNameRestScript} Where DELETED = false and ${whereScriptBodyLikeText} ")

        def tableNameField = "${pluginTableInfo.tablePrefix}_FIELD_CONFIG"
        def fieldScrips = new DBService().select("select FIELD_CONFIG_ID, ID from ${tableNameField} Where ${whereScriptBodyLikeText} ")

        def tableNameListener = "${pluginTableInfo.tablePrefix}_LISTENER"
        def listenerScripts = new DBService().select("select NAME from ${tableNameListener} Where DELETED = false and ${whereScriptBodyLikeText} ")

        return [
                scripts        : scripts,
                restScripts    : restScripts,
                fieldScrips    : fieldScrips,
                listenerScripts: listenerScripts
        ]
    }
}

class DBService {
    def select(String query) {
        OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();
        DelegatorInterface delegatorInterface = delegator.getDelegatorInterface();
        String helperName = delegatorInterface.getGroupHelperName("default");
        Connection connection = ConnectionFactory.getConnection(helperName);
        Sql sql = new Sql(connection);

        List<GroovyRowResult> resultRows = []
        try {
            resultRows.addAll(sql.rows(query));
        } finally {
            //sql.close()
            connection.close()
        }

        return resultRows
    }
}

class TableManager {
    def pluginTablesMap = [[pluginKey  : 'jsincluder',
                            tablePrefix: getTablePrefixForPlugin(['SCRIPT', 'BINDING'])
                           ],
                           [pluginKey  : 'Groovy Amigo',
                            tablePrefix: getTablePrefixForPlugin(['EVENT_LISTENER',
                                                                  'CHANGELOG',
                                                                  'FIELD_CONFIG',
                                                                  'REST_SCRIPT',
                                                                  'SCRIPT',
                                                                  'LISTENER_CHANGELOG',
                                                                  'SCRIPT_EXECUTION',
                                                                  'S_TASK_CHANGELOG',
                                                                  'SCRIPT_DIRECTORY',
                                                                  'SCHEDULED_TASK',
                                                                  'LISTENER',
                                                                  'AUDIT_LOG_ENTRY',
                                                                  'FIELD_CHANGELOG',
                                                                  'REST_CHANGELOG'])
                           ]
    ]

    def getTablePrefixForPlugin(Collection<String> tableNames) {
        def allTableNames = getAllTableNames().findAll { tableName ->
            return tableName.findAll { it == "_" }.size() >= 2 && tableName.startsWith('AO')
        }
        def tableCodeNameMap = allTableNames.groupBy {
            getPrefixFromTableName(it)
        }.collectEntries { key, value ->
            [(key): value.collect { return getTableName(it) }]
        }

        return tableCodeNameMap.find {
            it.value.intersect(tableNames) == tableNames
        }?.key
    }

    def getTableName(tableName) {
        return tableName.substring(tableName.indexOf('_', 3) + 1, tableName.length())
    }

    def getPrefixFromTableName(tableName) {
        return tableName.substring(0, tableName.indexOf('_', 3))
    }

    def getAllTableNames() {
        select('show tables').Tables_in_jiradb
    }

    def select(String query) {
        OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();
        DelegatorInterface delegatorInterface = delegator.getDelegatorInterface();
        String helperName = delegatorInterface.getGroupHelperName("default");
        Connection connection = ConnectionFactory.getConnection(helperName);
        Sql sql = new Sql(connection);

        List<GroovyRowResult> resultRows = []
        try {
            resultRows.addAll(sql.rows(query));
        } finally {
            //sql.close()
            connection.close()
        }

        return resultRows
    }

}
