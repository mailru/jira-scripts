import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption

def task = {
    updateAssignee(getIssue('KEY-1'), getUserByName('assigneeLogin'), getCurrentUser())
}

//convert to a pull
runMethodInNewThread(method)

def runMethodInNewThread(method){
    new Thread().start{
        method()
    }
}

def getIssue(key){
    ComponentAccessor.issueManager.getIssueObject(key)
}


def updateAssignee(issue, assignee, user){
    issue.assignee = assignee
    def im = ComponentAccessor.issueManager
    im.updateIssue(user, issue, EventDispatchOption.ISSUE_UPDATED, true)
}

def getCurrentUser(){
    ComponentAccessor.jiraAuthenticationContext?.user
}

def getUserByName(userName){
    ComponentAccessor.userManager.getUserByName(userName)
}