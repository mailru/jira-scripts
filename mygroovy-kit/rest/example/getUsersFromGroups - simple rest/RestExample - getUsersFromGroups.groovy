import com.atlassian.jira.component.ComponentAccessor
import groovy.json.JsonOutput
import javax.ws.rs.core.Response

def groups = ["group1","group2"]
def users = [] as Set
groups.each{group->
    users.addAll(getUsersInGroup(group))
}
return Response.ok(toJson(users)).build();

def getUsersInGroup(String groupName){
    ComponentAccessor.getGroupManager().with{
        getUsersInGroup(getGroup(groupName))
    }.collect{
        return [name:it.name, displayName:it.displayName]
    }
}


def toJson(obj){
    JsonOutput.toJson(obj)
}
