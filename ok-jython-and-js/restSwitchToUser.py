# -*- coding: UTF-8 -*-
from com.atlassian.jira.component import ComponentAccessor
from com.atlassian.jira.permission import ProjectPermissions

def switchToUser(args):
  actor = checkUser()
  if 'user' in args:
    try:
      userManager = ComponentAccessor.getUserManager()
      groupManager = ComponentAccessor.getGroupManager()

      suGroup = groupManager.getGroup('jira-administrators')
      avatar = userManager.getUserByKey(args['user'])

      if not avatar:
        return {'result': False, 'error': 'Unknown user %s' % args['user']}
      elif not groupManager.isUserInGroup(actor, suGroup):
        return {'result': False, 'error': 'Insufficient privileges'}
      else:
        classLoader = ComponentAccessor.getClassLoader()
        request = classLoader.loadClass('com.atlassian.jira.web.ExecutingHttpRequest').get()
        loggedInKey = classLoader.loadClass('com.atlassian.seraph.auth.DefaultAuthenticator').LOGGED_IN_KEY
        request.getSession().setAttribute(loggedInKey, avatar)
        return {'result': True}
    except Exception, e:
      return {'result': False, 'error': str(e)}
  else:
    return {'result': False, 'error': 'User name required'}
    


 