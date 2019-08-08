import com.atlassian.jira.JiraDataType
import com.atlassian.jira.JiraDataTypes
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.issue.search.SearchQuery
import com.atlassian.jira.jql.operand.QueryLiteral
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.jql.query.IssueIdCollector
import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.jira.jql.validator.NumberOfArgumentsValidator
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.MessageSet
import com.atlassian.jira.util.MessageSetImpl
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand
import ru.mail.jira.plugins.groovy.api.jql.ScriptedJqlValuesFunction

class DemoFunction implements ScriptedJqlValuesFunction {

    @Override
    public JiraDataType getDataType() {
        return JiraDataTypes.ISSUE;
    }

    @Override
    public MessageSet validate(ApplicationUser searcher, FunctionOperand operand, TerminalClause terminalClause) {
        def i18n = ComponentAccessor.getI18nHelperFactory().getInstance(searcher)
        def numberValidMessage = new NumberOfArgumentsValidator(1i, i18n).validate(operand);
        if (numberValidMessage.hasAnyErrors()) {
            return numberValidMessage
        }
        def messageSet = new MessageSetImpl();
        JqlQueryParser jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)
        try {
            def query = jqlQueryParser.parseQuery(operand.getArgs().get(0))
        } catch (any) {
            messageSet.addErrorMessage("not valid jql:${operand.getArgs().get(0)}");
            messageSet.addErrorMessage("${any}");
        }
        return messageSet
    }

    List<QueryLiteral> getValues(QueryCreationContext queryCreationContext, FunctionOperand functionOperand, TerminalClause terminalClause) {
        final List<QueryLiteral> literals = new LinkedList<>();
        String jql = functionOperand.getArgs().get(0)
        getIssuesByJQL(jql, queryCreationContext.getApplicationUser()).each { issue ->
            literals << new QueryLiteral(functionOperand, issue.key)
            issue.getSubTaskObjects().each { subTask ->
                literals << new QueryLiteral(functionOperand, subTask.key)
            }
        }

        return literals
    }

    private Collection<MutableIssue> getIssuesByJQL(String jql, ApplicationUser user) {
        def jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser)
        def searchProvider = ComponentAccessor.getComponent(SearchProvider)

        def query = jqlQueryParser.parseQuery(jql)
        SearchQuery searchQuery = SearchQuery.create(query, user)
        IssueIdCollector collector = new IssueIdCollector()
        searchProvider.search(searchQuery, collector)
        return collector.getIssueIds().collect { getIssue(it as Long) }
    }

    private MutableIssue getIssue(Long id) {
        ComponentAccessor.issueManager.getIssueObject(id)
    }
}