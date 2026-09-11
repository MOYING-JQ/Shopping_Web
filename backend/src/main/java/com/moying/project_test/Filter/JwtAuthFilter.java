package com.moying.project_test.Filter;

import com.moying.project_test.entity.User;
import com.moying.project_test.exception.BusinessException;
import com.moying.project_test.mapper.UserMapper;
import com.moying.project_test.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;

/**
 * @author 墨莹
 * @date 2026/8/26 17:15
 */

@Component
public class JwtAuthFilter extends OncePerRequestFilter {


    private final JwtUtil jwtUtil;

    //构造器注入，不要@Autowired，只有一个构造函数Spring自动注入
    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = jwtUtil.getAccessTokenFromHeader(request);

        //token不为空才做校验；token=null直接跳过，留给Security处理401
        if (token != null) {
            try {
                Claims claims = jwtUtil.parseToken(token);
                String username = claims.getSubject();
                String tokenType = claims.get("token_type").toString();

                //类型不是access_token
                if (!"access_token".equals(tokenType)) {
                    writeJson(response,401,"token不能使用其他类型");
                    return;
                }
                if (username == null) {
                    writeJson(response,401,"token校验错误");
                    return;
                }



                //全部校验通过，设置Security身份
                UsernamePasswordAuthenticationToken authToken
                        = new UsernamePasswordAuthenticationToken(username,null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authToken);

            } catch (Exception e) {
                //捕获：token过期、篡改、格式错误、解析失败
                writeJson(response,401,"token非法或者已过期");
                return;
            }
        }
        //放行，继续执行后续Security过滤器链
        filterChain.doFilter(request,response);
    }

    /**
     * 过滤器内部输出json工具方法
     */
    private void writeJson(HttpServletResponse response, int code, String msg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        String json = "{\"code\":" + code + ",\"msg\":\"" + msg + "\"}";
        response.getWriter().write(json);
    }
}