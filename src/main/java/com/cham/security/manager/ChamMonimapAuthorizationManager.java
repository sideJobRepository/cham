package com.cham.security.manager;


import com.cham.security.service.impl.ChamMonimapDynamicAuthorizationServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcherEntry;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service(value = "chamMonimapAuthorizationManager")
public class ChamMonimapAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private static final AuthorizationDecision ACCESS = new AuthorizationDecision(true);
    private  List<RequestMatcherEntry<AuthorizationManager<RequestAuthorizationContext>>> mappings;
    private final HandlerMappingIntrospector introspector;
    private final RoleHierarchyImpl roleHierarchy;
    private final ChamMonimapDynamicAuthorizationServiceImpl chamMonimapDynamicAuthorizationService;


    @PostConstruct
    public void mapping() {
        setMappings();
    }
    private void setMappings() {
        Map<String, String> urlRoleMappings = chamMonimapDynamicAuthorizationService.getUrlRoleMappings();
        mappings = urlRoleMappings.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split(" ", 2);
                    String method = parts[0];
                    String path = parts[1];

                    MvcRequestMatcher matcher = new MvcRequestMatcher(introspector, path);
                    matcher.setMethod(HttpMethod.valueOf(method));

                    return new RequestMatcherEntry<>(matcher, bgmAgitAuthorizationManager(e.getValue()));
                })
                .collect(Collectors.toList());
    }

    private AuthorizationManager<RequestAuthorizationContext> bgmAgitAuthorizationManager(String role) {
        if (role != null) {
            if (role.startsWith("ROLE")) {
                AuthorityAuthorizationManager<RequestAuthorizationContext> authorizationManager =
                        AuthorityAuthorizationManager.hasAnyAuthority(role);
                authorizationManager.setRoleHierarchy(roleHierarchy);
                return authorizationManager;
            } else {
                DefaultHttpSecurityExpressionHandler handler = new DefaultHttpSecurityExpressionHandler();
                handler.setRoleHierarchy(roleHierarchy);
                WebExpressionAuthorizationManager authorizationManager = new WebExpressionAuthorizationManager(role);
                authorizationManager.setExpressionHandler(handler);
                return authorizationManager;
            }
        }
        return null;
    }



    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext request) {
        for (RequestMatcherEntry<AuthorizationManager<RequestAuthorizationContext>> mapping : this.mappings) {
            RequestMatcher matcher = mapping.getRequestMatcher();
            RequestMatcher.MatchResult matchResult = matcher.matcher(request.getRequest());
            if (matchResult.isMatch()) {
                AuthorizationManager<RequestAuthorizationContext> manager = mapping.getEntry();
                return manager.check(authentication,
                        new RequestAuthorizationContext(request.getRequest(), matchResult.getVariables()));
            }
        }
        return ACCESS;
    }

    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        AuthorizationManager.super.verify(authentication, object);
    }

    public synchronized void reload() {
        this.mappings.clear();
        setMappings();
    }
}
