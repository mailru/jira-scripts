import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.greenhopper.service.sprint.Sprint
import com.atlassian.greenhopper.service.sprint.SprintManager
import com.atlassian.greenhopper.service.sprint.SprintIssueService
import com.atlassian.greenhopper.service.rapid.view.RapidViewService
import groovy.transform.BaseScript

import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response


def pluginKey = "com.pyxis.greenhopper.jira"
def moduleKey = "greenhopper-sprint-service"
def sprintService = ComponentAccessor.getPluginAccessor().getEnabledPlugin(pluginKey).getModuleDescriptor(moduleKey).getModule()
SprintManager sprintManager = sprintService.sprintManager

@WithPlugin("com.pyxis.greenhopper.jira")
@PluginModule
RapidViewService rapidViewService

def isUserInGroup(user, String groupName) {
    ComponentAccessor.getGroupManager().with {
        isUserInGroup(user, getGroup(groupName))
    }
}

def groups = ['jira-users']
if(!(groups.find{isUserInGroup(currentUser, it)})){
    return Response.status(404).entity(["exception": 'no access']).build()
}

long boardId = 1696L;

def createModel = {Sprint sprint ->
    [
            id: sprint.getId(),
            name: sprint.getName(),
            boardName: rapidViewService.getRapidView(currentUser, boardId).get().name,
            stateKey: sprint.getState().toString().toUpperCase(),
            date: sprint.getStartDate() != null?sprint.getStartDate().toString("YYYY-MM-dd\'T\'HH:mm:s\'Z\'"):""
    ]
}

Collection<Sprint> boardSprints = sprintManager.getSprintsForView(boardId, EnumSet.allOf(Sprint.State.class)).get()

Sprint lastSprint = boardSprints
        .findAll{ it.state == Sprint.State.CLOSED }
        .sort {it.id}
        .last()
Sprint currentSprint = boardSprints
        .find{ it.state == Sprint.State.ACTIVE }
Sprint futureSprint = boardSprints
        .find{ it.state == Sprint.State.FUTURE }

def model = [
        previous: lastSprint ? createModel(lastSprint) : null,
        current: currentSprint ? createModel(currentSprint) : null,
        future: futureSprint ? createModel(futureSprint) : null
]

return Response.status(200).entity(model).build()

