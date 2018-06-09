import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.StatusManager

def getPreviousAssignee(issue) {
    ComponentAccessor.changeHistoryManager.getChangeItemsForField(issue, "Assignee").max { it.created }?.from
}

def getPreviousValue(customField, issue) {
    ComponentAccessor.changeHistoryManager.getChangeItemsForField(issue, customField.name).max { it.created }?.from
}

def getPreviousStatus(issue) {
    def id = ComponentAccessor.changeHistoryManager.getChangeItemsForField(issue, "status").max { it.created }.from
    def statusManager = ComponentAccessor.getComponentOfType(StatusManager)
    statusManager.getStatus(id)
}

def Long getTimeInStatus(String STATUS_ID, issue) {
    if (!getAllStatusIdsFor(issue).find { it == STATUS_ID }) {
        return 0
    }
    def list = ComponentAccessor.changeHistoryManager.getChangeItemsForField(issue, "status").findAll {
        it.to == STATUS_ID || it.from == STATUS_ID
    }.sort { it.created }
    if (list.size() == 0) {
        return new Date().time - issue.created.time
    }
    def timeInStatus = 0;
    def isLastItem = false
    while (!isLastItem) {
        def itemTo = list.find { it.to == STATUS_ID && it.to != it.from }
        if (itemTo) {
            list = list - itemTo
            def itemFrom = list.find { it.from == STATUS_ID && it.to != it.from }
            if (itemFrom) {
                list = list - itemFrom
                timeInStatus += itemFrom.created.time - itemTo.created.time
            } else {
                isLastItem = true
                timeInStatus += new Date().time - itemTo.created.time
                return timeInStatus
            }
        } else {
            isLastItem = true
            return timeInStatus
        }
    }
    return timeInStatus;
}
