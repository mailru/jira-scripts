from com.atlassian.jira.component import ComponentAccessor

issueManager = ComponentAccessor.getIssueManager()
customFieldManager = ComponentAccessor.getCustomFieldManager()

cfAllMoney = customFieldManager.getCustomFieldObject(11832)
cfGift = customFieldManager.getCustomFieldObject(11854)
cfSum = customFieldManager.getCustomFieldObject(11833)

allMoney = issue.getCustomFieldValue(cfAllMoney) or 0
gift = issue.getCustomFieldValue(cfGift) or 0

issue.setCustomFieldValue(cfSum, float(allMoney) + float(gift) / 0.18)
