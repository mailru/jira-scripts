from com.atlassian.jira.component import ComponentAccessor
from java.sql import Timestamp
import time

customFieldManager = ComponentAccessor.getCustomFieldManager()
userManager = ComponentAccessor.getUserManager()

cfLastCaller = customFieldManager.getCustomFieldObject(11850)
cfLastTime = customFieldManager.getCustomFieldObject(10135)

caller = transientVars.get('context').getCaller()
callTime = Timestamp(int(round(time.time() * 1000)))

issue.setCustomFieldValue(cfLastCaller, userManager.getUserByKey(caller))
issue.setCustomFieldValue(cfLastTime, callTime)