package it.polimi.tiw.filters;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.utils.PathUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.io.IOException;

@WebFilter(filterName = "CheckLoggedUser")
public class CheckLoggedUser implements Filter {

    public void init(FilterConfig config) throws ServletException {

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
            if (user != null){
                chain.doFilter(request, response);
                return;
            }
        }

        WebContext webContext = new WebContext(req, res, request.getServletContext(), request.getLocale());
        webContext.setVariable("error", "You are not authorized to access this page.");
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(request.getServletContext());
        templateResolver.setTemplateMode(TemplateMode.HTML);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
        templateEngine.process(PathUtils.pathToErrorPage, webContext, response.getWriter());

    }
}
