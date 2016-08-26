package com.composum.sling.nodes;

import com.composum.sling.core.filter.ResourceFilter;
import com.composum.sling.core.mapping.jcr.ResourceFilterMapping;
import com.composum.sling.core.servlet.AbstractServiceServlet;
import com.composum.sling.nodes.servlet.NodeServlet;
import com.composum.sling.nodes.servlet.PropertyServlet;
import com.composum.sling.nodes.servlet.SecurityServlet;
import com.composum.sling.nodes.servlet.VersionServlet;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * The configuration service for all servlets in the core bundle.
 */
@Component(
        label = "Composum Nodes Configuration",
        description = "the configuration service for all servlets in the nodes bundles",
        immediate = true,
        metatype = true
)
@Service
public class NodesConfigImpl implements NodesConfiguration {

    @Property(
            name = CONSOLE_CATEGORIES_KEY,
            label = "Console Categories",
            description = "the list of categories to determine the views in the core console",
            value = {
                    "core",
                    "nodes"
            }
    )
    private String[] consoleCategories;

    @Property(
            name = QUERY_RESULT_LIMIT_KEY,
            label = "Query Result Limit",
            description = "the maximum node count for query results (default: 500)",
            longValue = QUERY_RESULT_LIMIT_DEFAULT
    )
    private long queryResultLimit;

    @Property(
            name = QUERY_TEMPLATES_KEY,
            label = "Query Templates",
            description = "a list of templates for the initial query history list",
            value = {
                    "/content/test ${text}",
                    "${path}//${name}[jcr:contains(.,'${word}')]"
            }
    )
    private String[] queryTemplates;

    @Property(
            name = ERRORPAGES_PATH,
            label = "Errorpages",
            description = "the path to the errorpages; e.g. 'meta/errorpages' for searching errorpages along the requested path",
            value = "meta/errorpages"
    )
    private String errorpagesPath;

    @Property(
            name = PAGE_NODE_FILTER_KEY,
            label = "Content Page Filter",
            description = "the filter configuration to set the scope to the content pages",
            value = "or{ResourceType(+'^[a-z]+:.*([Ss]ite|[Pp]age)$'),and{PrimaryType(+'^nt:file$'),MimeType(+'^text/html$')}}"
    )
    private ResourceFilter pageNodeFilter;

    @Property(
            name = DEFAULT_NODE_FILTER_KEY,
            label = "The default Node Filter",
            description = "the filter configuration to filter out system nodes",
            value = "and{Name(-'^rep:(repo)?[Pp]olicy$'),Path(-'^/bin(/.*)?$,^/services(/.*)?$,^/servlet(/.*)?$,^/(jcr:)?system(/.*)?$')}"
    )
    private ResourceFilter defaultNodeFilter;

    @Property(
            name = TREE_INTERMEDIATE_FILTER_KEY,
            label = "Tree Intermediate (Folder) Filter",
            description = "the filter configuration to determine all intermediate nodes in the tree view",
            value = "or{Folder(),PrimaryType(+'^dam:Asset(Content)?$')}"
    )
    private ResourceFilter treeIntermediateFilter;

    @Property(
            name = REFERENCEABLE_NODES_FILTER_KEY,
            label = "Referenceable Nodes Filter",
            description = "the filter configuration to select reference target nodes",
            value = "Type(mix:referenceable)"
    )
    private ResourceFilter referenceableNodesFilter;

    @Property(
            name = ORDERABLE_NODES_FILTER_KEY,
            label = "Orderable Nodes Filter",
            description = "the filter configuration to detect ordered nodes (prevent from sorting in the tree)",
            value = "or{Type(node:orderable),PrimaryType(+'^.*([Oo]rdered|[Pp]age).*$,^sling:(Mapping)$,^nt:(unstructured|frozenNode)$,^rep:(ACL|Members|system)$')}"
    )
    private ResourceFilter orderableNodesFilter;

    @Property(
            name = PACKAGE_SERVLET_ENABLED,
            label = "Package Servlet",
            description = "the general on/off switch for the services of the Package Servlet",
            boolValue = true
    )
    private boolean packageServletEnabled;

    @Property(
            name = SECURITY_SERVLET_ENABLED,
            label = "Security Servlet",
            description = "the general on/off switch for the services of the Security Servlet",
            boolValue = true
    )
    private boolean securityServletEnabled;

    @Property(
            name = "node.servlet.enabled",
            label = "Node Servlet",
            description = "the general on/off switch for the services of the Node Servlet",
            boolValue = true
    )
    private boolean nodeServletEnabled;

    @Property(
            name = "property.servlet.enabled",
            label = "Property Servlet",
            description = "the general on/off switch for the services of the Property Servlet",
            boolValue = true
    )
    private boolean propertyServletEnabled;

    @Property(
            name = "version.servlet.enabled",
            label = "Version Servlet",
            description = "the general on/off switch for the services of the Version Servlet",
            boolValue = true
    )
    private boolean versionServletEnabled;

    public static final String USER_MANAGEMENT_SERVLET_ENABLED = "usermanagement.servlet.enabled";
    @Property(
            name = "usermanagement.servlet.enabled",
            label = "User Management Servlet",
            description = "the general on/off switch for the services of the User Management Servlet",
            boolValue = true
    )
    private boolean userManagementServletEnabled;

    private Map<String, Boolean> enabledServlets;

    @Override
    public boolean isEnabled(AbstractServiceServlet servlet) {
        Boolean result = enabledServlets.get(servlet.getClass().getSimpleName());
        return result != null ? result : false;
    }

    @Override
    public String[] getConsoleCategories() {
        return consoleCategories;
    }

    @Override
    public long getQueryResultLimit() {
        return queryResultLimit;
    }

    @Override
    public String[] getQueryTemplates() {
        return queryTemplates;
    }

    @Override
    public ResourceFilter getPageNodeFilter() {
        return pageNodeFilter;
    }

    @Override
    public ResourceFilter getDefaultNodeFilter() {
        return defaultNodeFilter;
    }

    @Override
    public ResourceFilter getTreeIntermediateFilter() {
        return treeIntermediateFilter;
    }

    @Override
    public ResourceFilter getReferenceableNodesFilter() {
        return referenceableNodesFilter;
    }

    @Override
    public ResourceFilter getOrderableNodesFilter() {
        return orderableNodesFilter;
    }

    public Dictionary getProperties() {
        return properties;
    }

    protected Dictionary properties;

    @Activate
    @Modified
    protected void activate(ComponentContext context) {
        this.properties = context.getProperties();
        consoleCategories = PropertiesUtil.toStringArray(properties.get(CONSOLE_CATEGORIES_KEY));
        queryResultLimit = PropertiesUtil.toLong(properties.get(QUERY_RESULT_LIMIT_KEY), QUERY_RESULT_LIMIT_DEFAULT);
        queryTemplates = PropertiesUtil.toStringArray(properties.get(QUERY_TEMPLATES_KEY));
        errorpagesPath = (String) properties.get(ERRORPAGES_PATH);
        if (errorpagesPath.endsWith("/") && errorpagesPath.length() > 1) {
            errorpagesPath = errorpagesPath.substring(errorpagesPath.length() - 1);
        }
        pageNodeFilter = ResourceFilterMapping.fromString(
                (String) properties.get(PAGE_NODE_FILTER_KEY));
        defaultNodeFilter = ResourceFilterMapping.fromString(
                (String) properties.get(DEFAULT_NODE_FILTER_KEY));
        treeIntermediateFilter = ResourceFilterMapping.fromString(
                (String) properties.get(TREE_INTERMEDIATE_FILTER_KEY));
        referenceableNodesFilter = ResourceFilterMapping.fromString(
                (String) properties.get(REFERENCEABLE_NODES_FILTER_KEY));
        orderableNodesFilter = ResourceFilterMapping.fromString(
                (String) properties.get(ORDERABLE_NODES_FILTER_KEY));
        enabledServlets = new HashMap<>();
        enabledServlets.put("PackageServlet", packageServletEnabled =
                (Boolean) properties.get(PACKAGE_SERVLET_ENABLED));
        enabledServlets.put(SecurityServlet.class.getSimpleName(), securityServletEnabled =
                (Boolean) properties.get(SECURITY_SERVLET_ENABLED));
        enabledServlets.put(NodeServlet.class.getSimpleName(), nodeServletEnabled =
                (Boolean) properties.get(NODE_SERVLET_ENABLED));
        enabledServlets.put(PropertyServlet.class.getSimpleName(), propertyServletEnabled =
                (Boolean) properties.get(PROPERTY_SERVLET_ENABLED));
        enabledServlets.put(VersionServlet.class.getSimpleName(), versionServletEnabled =
                (Boolean) properties.get(VERSION_SERVLET_ENABLED));
        enabledServlets.put("UserManagementServlet", userManagementServletEnabled =
                (Boolean) properties.get(USER_MANAGEMENT_SERVLET_ENABLED));
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        this.properties = null;
    }
}
