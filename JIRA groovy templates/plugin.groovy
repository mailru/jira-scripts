import com.atlassian.jira.component.ComponentAccessor;

def pluginKey = ""
def moduleKey = ""
ComponentAccessor.getPluginAccessor().getEnabledPlugin(pluginKey).getModuleDescriptor(moduleKey).getModule()
