package it.polimi.tiw.filters;

import it.polimi.tiw.utils.PathUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "CheckNotLoggedUser")
public class CheckNotLoggedUser implements Filter {

    public void init(FilterConfig config) {
        ServletContext servletContext = config.getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession();

        if (session != null) {
             Object user = session.getAttribute("currentUser");
             if (user != null) {
                 res.sendRedirect(request.getServletContext().getContextPath() + PathUtils.goToHomeServletPath);
                 return;
             }
        }
        chain.doFilter(request, response);
    }
}
