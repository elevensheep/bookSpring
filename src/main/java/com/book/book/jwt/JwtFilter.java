package com.book.book.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;  // JWT 토큰을 처리하는 유틸리티 클래스

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println("필터 실행됨");

        // 🔥 CORS 관련 응답 헤더 추가
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");

        // ✅ Preflight 요청(OPTIONS)은 여기서 바로 응답 후 종료
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Authorization 헤더에서 JWT 추출
        String jwt = getJwtFromRequest(request);

        if (jwt != null && jwtUtil.validateToken(jwt)) {
            // 토큰이 유효하면 인증 정보 설정
            String userUuid = jwtUtil.getUserUuidFromToken(jwt);

            var authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));  // 필요한 권한을 추가

            var authToken = new UsernamePasswordAuthenticationToken(userUuid, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer "를 제외한 토큰만 반환
        }

        return null;  // JWT가 없으면 null 반환
    }
}
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain
//    ) throws ServletException, IOException {
//        System.out.println("필터 실행됨");
//
//        // 🔥 CORS 관련 응답 헤더 추가
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
//        response.setHeader("Access-Control-Allow-Credentials", "true");
//        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
//
//        // ✅ Preflight 요청(OPTIONS)은 여기서 바로 응답 후 종료
//        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
//            response.setStatus(HttpServletResponse.SC_OK);
//            return;
//        }
//
//        // 세션에서 사용자 정보 확인
//        Object user = request.getSession().getAttribute("user");
//
//        if (user == null) {
//            filterChain.doFilter(request, response);  // 인증되지 않은 사용자라면 필터 체인 진행
//            return;
//        }
//
//        // 세션에 사용자 정보가 있을 경우 인증 정보 설정
//        CustomUser customUser = (CustomUser) user;
//
//        // 사용자 권한 처리
//        String[] authoritiesArray = customUser.getAuthorities().stream()
//                .map(a -> a.getAuthority())
//                .toArray(String[]::new);
//
//        // 인증 객체 생성
//        var authToken = new UsernamePasswordAuthenticationToken(
//                customUser.getUserUuid(),
//                null,
//                Arrays.stream(authoritiesArray)
//                        .map(SimpleGrantedAuthority::new)
//                        .toList()
//        );
//
//        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//        // 필터 체인 계속 진행
//        filterChain.doFilter(request, response);
//    }
//}
