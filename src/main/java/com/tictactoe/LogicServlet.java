package com.tictactoe;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;


@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //Get current session
        HttpSession currentSession = req.getSession();

        //Get game field object
        Field field = extractField(currentSession);

        //Get index on interacted cell
        int index = getSelectedIndex(req);
        Sign curreniSign = field.getField().get(index);

        //Empty cell check
        if (Sign.EMPTY != curreniSign) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);
            return;
        }

        //Placing cross
        field.getField().put(index, Sign.CROSS);
        //Checking if crosses won
        if (checkWin(resp, currentSession, field)) {
            return;
        }

        //Get empty cell
        int emptyFieldIndex = field.getEmptyFieldIndex();
        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            //Checking if noughts won
            if(checkWin(resp , currentSession , field)){
                return;
            }
        }else {
            //Draw check
            currentSession.setAttribute("draw" , true);

            //get symbols
            List<Sign> data = field.getFieldData();

            //Update session state
            currentSession.setAttribute("data" , data);

            resp.sendRedirect("/index.jsp");
            return;
        }


        //Get symbols
        List<Sign> data = field.getFieldData();

        //Update session field state
        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);


        resp.sendRedirect("/index.jsp");
    }

    private boolean checkWin(HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (Sign.CROSS == winner || Sign.NOUGHT == winner) {
            //Creating winner attribute
            currentSession.setAttribute("winner", winner);

            //Get symbols
            List<Sign> data = field.getFieldData();

            //Updating state in session
            currentSession.setAttribute("data", data);

            //Redirecting
            response.sendRedirect("/index.jsp");
            return true;
        }
        return false;
    }


    private int getSelectedIndex(HttpServletRequest request) {
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session terminated");
        }
        return (Field) fieldAttribute;
    }

}