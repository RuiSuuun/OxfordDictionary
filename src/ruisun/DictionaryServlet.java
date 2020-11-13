package ruisun;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "Dictionary", urlPatterns = {"/dictionary/*", "/admin"})
public class DictionaryServlet extends javax.servlet.http.HttpServlet {
    DictionaryModel model = null;

    // Initiate this servlet by instantiating the model that it will use.
    public void init() {
        model = new DictionaryModel();
    }

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {

    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        String path = request.getRequestURI();

        // The name is on the path /name so skip over the '/'
        String[] func = path.split("/");

        if (func[2].equals("admin")) {
            ;
        } else {
            if (func.length <=3) {
                PrintWriter out = response.getWriter();
                out.println("");
                return;
            }
            String searchTerm = func[3];

            // return 401 if word is not provided
            if(searchTerm.equals("")) {
                response.setStatus(200);
                response.setContentType("text/plain;charset=UTF-8");

                PrintWriter out = response.getWriter();
                out.println("");
                return;
            }

            // Look up the explanation from Oxford Dictionary
            String[] explanation = model.search(searchTerm);

            if (explanation == null || explanation.length == 0) {
                PrintWriter out = response.getWriter();
                out.println("");
                return;
            }

            // Things went well so set the HTTP response code to 200 OK
            response.setStatus(200);
            // tell the client the type of the response
            response.setContentType("text/plain;charset=UTF-8");
            // return the explanation from a GET request
            PrintWriter out = response.getWriter();
            String display = String.join("\n", explanation);
            out.println(display);
        }
    }
}
