import com.atlassian.jira.component.ComponentAccessor

def startWatching(user, issue) {
    ComponentAccessor.watcherManager.startWatching(user, issue)
}

def stopWatching(user, issue) {
    ComponentAccessor.watcherManager.stopWatching(user, issue)
}

def getAllWatchers(issue) {
    ComponentAccessor.watcherManager.getWatchers(issue, Locale.US)
}
