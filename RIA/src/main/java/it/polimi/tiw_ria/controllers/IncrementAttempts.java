package it.polimi.tiw_ria.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "IncrementAttempts", value = "/IncrementAttempts")
@MultipartConfig
public class IncrementAttempts extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        int attempts = (int) session.getAttribute("attempts");
        if (attempts == 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("attempts");   // quando il client riceve "attempts", capisce che sono stati fatti troppi tentativi
            request.getSession().removeAttribute("attempts");
            return;
        }
        attempts++;
        session.setAttribute("attempts", attempts);

        Gson gson = new Gson();
        String json_attempts = gson.toJson(attempts);
        response.getWriter().write(json_attempts);
    }
}
