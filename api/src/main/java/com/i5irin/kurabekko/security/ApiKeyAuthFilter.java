package com.i5irin.kurabekko.security;

import com.i5irin.kurabekko.app.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class ApiKeyAuthFilter extends OncePerRequestFilter {

  private final AppProperties props;

  public ApiKeyAuthFilter(AppProperties props) {
    this.props = props;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/app/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String apiKey = extractApiKey(request);
    if (!StringUtils.hasText(apiKey) || !apiKey.equals(props.apiKey())) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"message\":\"unauthorized\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String extractApiKey(HttpServletRequest request) {
    String x = request.getHeader("X-API-Key");
    if (StringUtils.hasText(x)) return x;

    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) return null;
    return auth.substring("Bearer ".length()).trim();
  }
}
