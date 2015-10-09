package com.composum.sling.core;

import com.composum.sling.core.filter.ResourceFilter;
import com.composum.sling.core.mapping.jcr.ResourceFilterMapping;
import com.composum.sling.core.servlet.*;
import com.composum.sling.core.util.ResourceUtil;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * The configuration service for all servlets in the core bundle.
 */
@Component(
        label = "Composum Core Configuration",
        description = "the configuration service for all servlets in the core bundle",
        immediate = true,
        metatype = true
)
@Service
public class CoreConfigImpl implements CoreConfiguration {

    public static final String ERRORPAGE_STATUS = "errorpage.status";

    public static final long QUERY_RESULT_LIMIT_DEFAULT = 500L;
    public static final String QUERY_RESULT_LIMIT_KEY = "query.result.limit";
    @Property(
            name = QUERY_RESULT_LIMIT_KEY,
            label = "Query Result Limit",
            description = "the maximum node count for query results (default: 500)",
            longValue = QUERY_RESULT_LIMIT_DEFAULT
    )
    private long queryResultLimit;

    public static final String ERRORPAGES_PATH = "errorpages.path";
    @Property(
            name = ERRORPAGES_PATH,
            label = "Errorpages",
            description = "the path to the errorpages; e.g. 'meta/errorpages' for searching errorpages along the requested path",
            value = "meta/errorpages"
    )
    private String errorpagesPath;

    public static final String PAGE_NODE_FILTER_KEY = "node.page.filter";
    @Property(
            name = PAGE_NODE_FILTER_KEY,
            label = "Content Page Filter",
            description = "the filter configuration to set the scope to the content pages",
            value = "or{ResourceType(+'^[a-z]+:.*[Pp]age$'),and{PrimaryType(+'^nt:file$'),MimeType(+'^text/html$')}}"
    )
    private ResourceFilter pageNodeFilter;

    public static final String DEFAULT_NODE_FILTER_KEY = "node.default.filter";
    @Property(
            name = DEFAULT_NODE_FILTER_KEY,
            label = "The default Node Filter",
            description = "the filter configuration to filter out system nodes",
            value = "and{Name(-'^rep:(repo)?[Pp]olicy$'),Path(-'^/bin(/.*)?$,^/services(/.*)?$,^/servlet(/.*)?$,^/(jcr:)?system(/.*)?$')}"
    )
    private ResourceFilter defaultNodeFilter;

    public static final String TREE_INTERMEDIATE_FILTER_KEY = "tree.intermediate.filter";
    @Property(
            name = TREE_INTERMEDIATE_FILTER_KEY,
            label = "Tree Intermediate (Folder) Filter",
            description = "the filter configuration to determine all intermediate nodes in the tree view",
            value = "or{Folder(),PrimaryType(+'^dam:Asset(Content)?$')}"
    )
    private ResourceFilter treeIntermediateFilter;

    public static final String REFERENCEABLE_NODES_FILTER_KEY = "node.referenceable.filter";
    @Property(
            name = REFERENCEABLE_NODES_FILTER_KEY,
            label = "Referenceable Nodes Filter",
            description = "the filter configuration to select reference target nodes",
            value = "MixinType(+'^mix:referenceable$')"
    )
    private ResourceFilter referenceableNodesFilter;

    public static final String ORDERABLE_NODES_FILTER_KEY = "node.orderable.filter";
    @Property(
            name = ORDERABLE_NODES_FILTER_KEY,
            label = "Orderable Nodes Filter",
            description = "the filter configuration to detect ordered nodes (prevent from sorting in the tree)",
            value = "or{MixinType(+'^mix:orderable$'),PrimaryType(+'^.*([Oo]rdered|[Pp]age).*$,^sling:(Mapping)$,^nt:(unstructured|frozenNode)$,^rep:(ACL|Members|system)$')}"
    )
    private ResourceFilter orderableNodesFilter;

    public static final String SYSTEM_SERVLET_ENABLED = "system.servlet.enabled";
    @Property(
            name = SYSTEM_SERVLET_ENABLED,
            label = "System Servlet",
            description = "the general on/off switch for the services of the System Servlet",
            boolValue = true
    )
    private boolean systemServletEnabled;

    public static final String PACKAGE_SERVLET_ENABLED = "package.servlet.enabled";
    @Property(
            name = PACKAGE_SERVLET_ENABLED,
            label = "Package Servlet",
            description = "the general on/off switch for the services of the Package Servlet",
            boolValue = true
    )
    private boolean packageServletEnabled;

    public static final String SECURITY_SERVLET_ENABLED = "security.servlet.enabled";
    @Property(
            name = SECURITY_SERVLET_ENABLED,
            label = "Security Servlet",
            description = "the general on/off switch for the services of the Security Servlet",
            boolValue = true
    )
    private boolean securityServletEnabled;

    public static final String NODE_SERVLET_ENABLED = "node.servlet.enabled";
    @Property(
            name = "node.servlet.enabled",
            label = "Node Servlet",
            description = "the general on/off switch for the services of the Node Servlet",
            boolValue = true
    )
    private boolean nodeServletEnabled;

    public static final String PROPERTY_SERVLET_ENABLED = "property.servlet.enabled";
    @Property(
            name = "property.servlet.enabled",
            label = "Property Servlet",
            description = "the general on/off switch for the services of the Property Servlet",
            boolValue = true
    )
    private boolean propertyServletEnabled;

    public static final String VERSION_SERVLET_ENABLED = "version.servlet.enabled";
    @Property(
            name = "version.servlet.enabled",
            label = "Version Servlet",
            description = "the general on/off switch for the services of the Version Servlet",
            boolValue = true
    )
    private boolean versionServletEnabled;

    private Map<Class<? extends AbstractServiceServlet>, Boolean> enabledServlets;

    @Override
    public boolean isEnabled(AbstractServiceServlet servlet) {
        Boolean result = enabledServlets.get(servlet.getClass());
        return result != null ? result : false;
    }

    @Override
    public long getQueryResultLimit() {
        return queryResultLimit;
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

    @Override
    public Resource getErrorpage(SlingHttpServletRequest request, int status) {

        Resource errorpage = null;

        RequestPathInfo pathInfo = request.getRequestPathInfo();
        if ("html".equalsIgnoreCase(pathInfo.getExtension())) { // handle page requests only

            ResourceResolver resolver = request.getResourceResolver();
            if (errorpagesPath.startsWith("/")) {
                errorpage = resolver.getResource(errorpagesPath + "/" + status);
            } else {
                String path = request.getRequestPathInfo().getResourcePath();
                Resource resource = resolver.resolve(request, path);
                while (resource == null || ResourceUtil.isNonExistingResource(resource)) {
                    int lastSlash = path.lastIndexOf('/');
                    if (lastSlash > 0) {
                        path = path.substring(0, lastSlash);
                    } else {
                        path = "/";
                    }
                    resource = resolver.resolve(request, path);
                }
                if (resource != null) {
                    while (errorpage == null && resource != null) {
                        path = resource.getPath();
                        if ("/".equals(path)) {
                            path = "";
                        }
                        errorpage = resolver.getResource(path + "/" + errorpagesPath + "/" + status);
                        if (errorpage == null) {
                            resource = resource.getParent();
                        }
                    }
                }
            }
        }
        return errorpage;
    }

    @Override
    public boolean forwardToErrorpage(SlingHttpServletRequest request,
                                      SlingHttpServletResponse response, int status)
            throws ServletException, IOException {
        Resource errorpage = getErrorpage(request, status);
        if (errorpage != null) {
            request.setAttribute(ERRORPAGE_STATUS, status); // hint for the custom page
            RequestDispatcher dispatcher = request.getRequestDispatcher(errorpage);
            dispatcher.forward(request, response);
            return true;
        }
        return false;
    }

    protected Dictionary properties;

    protected void activate(ComponentContext context) {
        this.properties = context.getProperties();
        queryResultLimit = PropertiesUtil.toLong(QUERY_RESULT_LIMIT_KEY, QUERY_RESULT_LIMIT_DEFAULT);
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
        enabledServlets.put(SystemServlet.class, systemServletEnabled =
                (Boolean) properties.get(SYSTEM_SERVLET_ENABLED));
        enabledServlets.put(PackageServlet.class, packageServletEnabled =
                (Boolean) properties.get(PACKAGE_SERVLET_ENABLED));
        enabledServlets.put(SecurityServlet.class, securityServletEnabled =
                (Boolean) properties.get(SECURITY_SERVLET_ENABLED));
        enabledServlets.put(NodeServlet.class, nodeServletEnabled =
                (Boolean) properties.get(NODE_SERVLET_ENABLED));
        enabledServlets.put(PropertyServlet.class, propertyServletEnabled =
                (Boolean) properties.get(PROPERTY_SERVLET_ENABLED));
        enabledServlets.put(VersionServlet.class, versionServletEnabled =
                (Boolean) properties.get(VERSION_SERVLET_ENABLED));
    }

    protected void deactivate(ComponentContext context) {
        this.properties = null;
    }
}
