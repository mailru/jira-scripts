from com.atlassian.jira.component import ComponentAccessor
userManager = ComponentAccessor.getUserManager()
avatar = userManager.getUserByKey('v.pupkin')
if avatar:
  classLoader = ComponentAccessor.getClassLoader()
  request = classLoader.loadClass('com.atlassian.jira.web.ExecutingHttpRequest').get()
  loggedInKey = classLoader.loadClass('com.atlassian.seraph.auth.DefaultAuthenticator').LOGGED_IN_KEY
  request.getSession().setAttribute(loggedInKey, avatar)