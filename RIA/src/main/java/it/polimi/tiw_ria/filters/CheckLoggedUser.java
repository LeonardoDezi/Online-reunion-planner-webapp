package it.polimi.tiw_ria.filters;

import it.polimi.tiw_ria.utils.PathUtils;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
        HttpSession s = req.getSession(false);

        if(s != null) {
            Object user = s.getAttribute("currentUser");
            if(user != null) {
                chain.doFilter(request, response);
                return;
            }
        }

        res.sendRedirect(req.getServletContext().getContextPath() + PathUtils.pathToLoginPage);

    }
}
