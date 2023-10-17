package it.polimi.tiw_ria.filters;

import it.polimi.tiw_ria.utils.PathUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "CheckNotLoggedUser")
public class CheckNotLoggedUser implements Filter {

    public void init(FilterConfig config) {
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
                 res.sendRedirect(request.getServletContext().getContextPath() + PathUtils.pathToHomePage);
                 return;
             }
        }
        chain.doFilter(request, response);
    }
}
