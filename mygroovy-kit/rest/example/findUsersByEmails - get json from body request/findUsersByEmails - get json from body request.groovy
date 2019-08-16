import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import groovy.json.JsonSlurper
import java.util.concurrent.TimeUnit

import groovy.json.JsonBuilder
import groovy.transform.BaseScript

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response


queryParams = uriInfo.getQueryParameters(true)


def jsonSlurper = new JsonSlurper()
def json = jsonSlurper.parseText(body)
def emails = json.get('emails')
def usersByEmail = [:]
def allUsers = ComponentAccessor.userManager.getAllUsers()
emails.each{email->
    usersByEmail.put(email, allUsers.findAll{user-> user.emailAddress == email && user.isActive()}*.name)
}
return Response.ok(new JsonBuilder(['usersByEmail': usersByEmail]).toString()).build();



def getUserByEmail(String email){
    UserUtils.getUserByEmail(email)
}

def getUserByName(String name){
    ComponentAccessor.userManager.getUserByName(name)
}