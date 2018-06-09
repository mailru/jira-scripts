import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption

def task = {
    updateAssignee(getIssue('KEY-1'), getUserByName('assigneeLogin'), getCurrentUser())
}

//todo convert to a pull
runMethodInNewThread(task)

def runMethodInNewThread(method) {
    new Thread().start {
        method()
    }
}

def getIssue(key) {
    ComponentAccessor.issueManager.getIssueObject(key)
}


def updateAssignee(issue, assignee, user) {
    issue.assignee = assignee
    def issueManager = ComponentAccessor.issueManager
    issueManager.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, true)
}

def getCurrentUser() {
    ComponentAccessor.jiraAuthenticationContext?.user
}

def getUserByName(userName) {
    ComponentAccessor.userManager.getUserByName(userName)
}
