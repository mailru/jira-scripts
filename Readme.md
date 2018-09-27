# Atlassian JIRA MyGroovy templates / examples
Template of quick writing groovy scripts for the plugin [Groovy Amigo] (https://marketplace.atlassian.com/1218755).
The template uses the usual java-api in easy-to-use wrappers.
You can just to copy the code and apply it in the right order.

[Groovy language docs is here] (http://groovy-lang.org/documentation.html)

Additions and wishes are welcome.


#### Example 1
Receiving issue
```groovy
	ComponentAccessor.issueManager.getIssueObject(key)
```

The template is represented by a method that is copied with the import.
```groovy
	def getIssue(String key){
		ComponentAccessor.issueManager.getIssueObject(key)
	}
```

#### Example 2
Calling a transition at issue. The transition.groovy file contains the doTransition (issue, actionId, user) method.
It passes the issue with which you want to call the transition, the transition id, user - the user on whose behalf the transition is made
```groovy
	def doTransition(issue, int actionId, user){
	    def issueService = ComponentAccessor.getIssueService()
	    def issueInputParameters = issueService.newIssueInputParameters();
	    def transitionValidationResult = issueService.validateTransition(user, issue.id, actionId, issueInputParameters);
	    if (transitionValidationResult.isValid()){
	       issueService.transition(user, transitionValidationResult);
	        return true
	    } else {
	        return false
	    }
	}
```

#### Example 3
To execute a query in the database, call the select ("select * from ...") method from the sql.groovy file

```groovy
	select("select ...")

	def select(String query){
	    OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();
	    DelegatorInterface delegatorInterface = delegator.getDelegatorInterface();
	    String helperName = delegatorInterface.getGroupHelperName("default");
	    Connection connection = ConnectionFactory.getConnection(helperName);
	    Sql sql = new Sql(connection);
	    
	    List<GroovyRowResult> resultRows = []
	    try{
	        resultRows.addAll(sql.rows(query));
	    } finally {
	        connection.close()
	    }
	    return resultRows
	}
```


