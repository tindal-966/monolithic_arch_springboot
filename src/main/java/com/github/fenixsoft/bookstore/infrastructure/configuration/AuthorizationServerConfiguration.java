/*
 * Copyright 2012-2020. the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. More information from:
 *
 *        https://github.com/fenixsoft
 */

package com.github.fenixsoft.bookstore.infrastructure.configuration;

import com.github.fenixsoft.bookstore.domain.auth.service.AuthenticAccountDetailsService;
import com.github.fenixsoft.bookstore.domain.auth.service.JWTAccessTokenService;
import com.github.fenixsoft.bookstore.domain.auth.service.OAuthClientDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

/**
 * Spring Security OAuth2 授权服务器配置
 * <p>
 * 在该配置中，设置了授权服务 Endpoint 的 相关信息（端点的位置、请求方法、使用怎样的令牌、支持怎样的客户端）
 * 以及针对 OAuth2 的密码模式所需要的用户身份认证服务和用户详情查询服务
 *
 * @author icyfenix@gmail.com
 * @date 2020/3/7 17:38
 **/
@Configuration
@EnableAuthorizationServer // 启用授权服务器
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    /**
     * 令牌服务
     */
    @Autowired
    private JWTAccessTokenService tokenService;

    /**
     * OAuth2客户端信息服务
     */
    @Autowired
    private OAuthClientDetailsService clientService;

    /**
     * 认证服务管理器
     * <p>
     * 一个认证服务管理器里面包含着多个可以从事不同认证类型的认证提供者（Provider）
     * 认证服务由认证服务器 {@link AuthenticationServerConfiguration} 定义并提供注入源
     */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 用户信息服务
     */
    @Autowired
    private AuthenticAccountDetailsService accountService;


    /**
     * 配置客户端详情服务
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientService);
    }

    /**
     * 配置授权的服务 Endpoint
     * <p>
     * Spring Security OAuth2 会根据配置的认证服务、用户详情服务、令牌服务自动生成以下端点：
     * /oauth/authorize：授权端点
     * /oauth/token：令牌端点
     * /oauth/confirm_access：用户确认授权提交端点
     * /oauth/error：授权服务错误信息端点
     * /oauth/check_token：用于资源服务访问的令牌解析端点
     * /oauth/token_key：提供公有密匙的端点，如果 JWT 采用的是非对称加密加密算法，则资源服务其在鉴权时就需要这个公钥来解码
     * 如有必要，这些端点可以使用pathMapping()方法来修改它们的位置，使用prefix()方法来设置路径前缀
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoint) {
        endpoint.authenticationManager(authenticationManager)
                .userDetailsService(accountService)
                .tokenServices(tokenService)
                //控制TokenEndpoint端点请求访问的类型，默认HttpMethod.POST
                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
    }

    /**
     * 配置 OAuth2 发布出来的 Endpoint 本身的安全约束
     * <p>
     * 这些端点的默认访问规则原本是：
     * 1. 端点开启了 HTTP Basic Authentication，通过 allowFormAuthenticationForClients() 关闭，即允许通过表单来验证
     * 2. 端点的访问均为 denyAll()，可以在这里通过 SpringEL 表达式来改变为 permitAll()
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.allowFormAuthenticationForClients()
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()");
    }

}
