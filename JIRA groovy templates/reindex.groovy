import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.index.IssueIndexManager

def issueIndexManager = ComponentAccessor.getComponent(IssueIndexManager)
issueIndexManager.reIndexIssueObjects(issues)
