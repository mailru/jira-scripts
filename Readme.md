# Atlassian JIRA SriptRunner groovy templates/examples
Шаблон быстрого написания groovy скриптов для плагина [ScriptRunner for JIRA](https://marketplace.atlassian.com/plugins/com.onresolve.jira.groovy.groovyrunner/overview).
В шаблоне используюется обычный java-api в удобных для применения обертках.
Вам остается только скопировать код и применить его в нужном порядке.

[Документация по языку groovy](http://groovy-lang.org/documentation.html)

Дополнения и пожелания приветствуются. 

#### Пример 1
Получение issue
```groovy
	ComponentAccessor.issueManager.getIssueObject(key)
```

В шаблоне представлено методом, который досточно скопировать вместе с импортом.
```groovy
	def getIssue(String key){
		ComponentAccessor.issueManager.getIssueObject(key)
	}
```

#### Пример 2
Вызов перехода у issue. В файлe transition.groovy содержится метод doTransition(issue, actionId, user). 
В него передается issue у которой хотите вызвать переход, id перехода, user - пользователь от имени которого совершается переход
```groovy
	def doTransition(issue, actionId, user){
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

#### Пример 3
Для выполнения запроса в базу данных досточно вызвать метод select("select * from ...") из файла sql.groovy
```groovy
	select("select ...")

	def select(query){
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


