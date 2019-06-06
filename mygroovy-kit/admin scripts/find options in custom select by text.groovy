/**
 * run if install Custom Select List plugin - https://marketplace.atlassian.com/apps/1218105
 * */
import ru.mail.jira.plugins.groovy.api.script.*
import ru.mail.jira.plugins.utils.customselect.customoption.CustomOptionManager
import ru.mail.jira.plugins.utils.customselect.settingscheme.SettingSchemeService
import com.atlassian.jira.config.properties.APKeys


@WithParam(displayName = 'find by text', type = ParamType.STRING, optional = false)
String findText

@WithPlugin('ru.mail.jira.plugins.utils')
@PluginModule
CustomOptionManager customOptionManager

@PluginModule
SettingSchemeService settingSchemeService

def cfnameLinkMap = ['Custom Multi Select List'  : "${APKeys.JIRA_BASEURL}/secure/UtilsCustomMultiSelectOptions.jspa?fieldConfigId=",
                     'Custom Select List'        : "${APKeys.JIRA_BASEURL}/secure/UtilsCustomOptions.jspa?fieldConfigId=",
                     'Cascade Custom Select List': "${APKeys.JIRA_BASEURL}/secure/UtilsCascadeCustomOptions.jspa?fieldConfigId="
]

def rows = []
def heads = ['field name', 'projects', 'option.parent', 'option.value', 'option.data']
customOptionManager.getCustomOptions().findAll {
    def isCondition = false
    if (it.value) {
        isCondition = it.value.toLowerCase().contains(findText.toLowerCase())
    }
    if (it.data) {
        isCondition = it.data.toLowerCase().contains(findText.toLowerCase())
    }
    return isCondition
}.unique { a, b -> a.ID <=> b.ID }.each {
    def id = it.getFieldConfigId()
    def url = cfnameLinkMap.get(_.getCustomFieldObject(settingSchemeService.getSettingScheme(id).getCustomFieldId()).getCustomFieldType().name)
    def field = _.getCustomFieldObject(settingSchemeService.getSettingScheme(id).getCustomFieldId() as Long)
    def fieldName = field.name
    //"<a href=${url}${id}>${fieldName}</a>:{value: <b>${it.value}</b>}"
    def parent = (it.getParentId()) ? customOptionManager.getCustomOption(it.getParentId()).value : '-'
    rows << ["<a href=${url}${id}>${fieldName}</a>", "<a href=${APKeys.JIRA_BASEURL}/secure/admin/ConfigureCustomField!default.jspa?customFieldId=${field.getIdAsLong()}>${field.getAssociatedProjectObjects()*.key}</a>", parent, it.value, it.data]
}

return _.createHTMLTable(heads, rows)