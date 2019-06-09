package com.ecommerce.filters;

import com.ecommerce.common.utils.CookieUtils;
import com.ecommerce.common.utils.JwtUtils;
import com.ecommerce.config.FilterProperties;
import com.ecommerce.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {
    @Autowired
    private JwtProperties props;
    @Autowired
    private FilterProperties filterProps;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        // Get context
        RequestContext ctx = RequestContext.getCurrentContext();
        // Get request
        HttpServletRequest request = ctx.getRequest();
        // Get request url
        String requestURI = request.getRequestURI();
        return !isAllowPath(requestURI);
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();

        HttpServletRequest request = context.getRequest();
        // get token from cookie
        String token = CookieUtils.getCookieValue(request, props.getCookieName());
        try {
            JwtUtils.getInfoFromToken(token, props.getPublicKey());
        } catch (Exception e) {
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(403);
            log.error("Illegal access，url：{}", request.getRequestURI(), e);
        }
        return null;
    }

    /*
     * Determine if the request URI is a URI in the allowed list
     * @param: requestURI
     * @return java.lang.Boolean
     * @author dunklee
     */
    private Boolean isAllowPath(String requestURI) {
        for (String allowPath : filterProps.getAllowPaths()) {
            if (requestURI.startsWith(allowPath)) {
                return true;
            }
        }
        return false;
    }
}
