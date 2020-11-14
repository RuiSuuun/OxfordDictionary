package ruisun;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Updates.inc;

import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import java.io.*;
import java.util.ArrayList;

@WebServlet(name = "Dictionary", urlPatterns = {"/dictionary/*", "/admin"})
public class DictionaryServlet extends javax.servlet.http.HttpServlet {
    DictionaryModel model = null;
    MongoDatabase mgdatabase = null;
    MongoCollection<Document> logs = null, apiLogs = null, commons = null;

    // Initiate this servlet by instantiating the model that it will use.
    public void init() {
        model = new DictionaryModel();
        MongoClient mongoClient = MongoClients.create("mongodb+srv://ruisun:mongodb123@cluster0.ruvfo.mongodb.net/<dbname>?retryWrites=true&w=majority");
        mgdatabase = mongoClient.getDatabase("DistributedSystems");
        logs = mgdatabase.getCollection("mobile-logs");
        apiLogs = mgdatabase.getCollection("api-logs");
        commons = mgdatabase.getCollection("commons");
    }

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {

    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {

        String path = request.getRequestURI();

        long startTime, endTime;

        // The name is on the path /name so skip over the '/'
        String[] func = path.split("/");

        if (func.length > 2 && func[2].equals("dictionary")) {
            commons.updateOne(gte("count", 0), inc("count", 1));

            if (func.length <= 3) {
                startTime = recordRequest(request, "");
                PrintWriter out = response.getWriter();
                out.println("");
                endTime = recordResponse(response, "","", "Error");
                commons.updateOne(gte("total-response-time", 0), inc("total-response-time", endTime - startTime));
                return;
            }

            String searchTerm = func[3];
            startTime = recordRequest(request, searchTerm);

            // return 401 if word is not provided
            if(searchTerm.equals("")) {
                PrintWriter out = response.getWriter();
                out.println("");
                endTime = recordResponse(response, searchTerm, "", "Error");
                commons.updateOne(gte("total-response-time", 0), inc("total-response-time", endTime - startTime));
                return;
            }

            // Look up the explanation from Oxford Dictionary
            String[] explanation = model.search(searchTerm);

            if (explanation == null || explanation.length == 0) {
                PrintWriter out = response.getWriter();
                out.println("");
                endTime = recordResponse(response, searchTerm,"", "Error");
                commons.updateOne(gte("total-response-time", 0), inc("total-response-time", endTime - startTime));
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
            endTime = recordResponse(response, searchTerm, display, "Success");
            commons.updateOne(gte("success-count", 0), inc("success-count", 1));
            commons.updateOne(gte("total-response-time", 0), inc("total-response-time", endTime - startTime));
        } else {
            ArrayList<String> dbLogs = new ArrayList<>();
            for (Document cur : logs.find()) {
                dbLogs.add(cur.toJson());
            }
            for (Document cur : apiLogs.find()) {
                dbLogs.add(cur.toJson());
            }
            // Transfer control over the the correct "view"
            request.setAttribute("count", analyze()[0]);
            request.setAttribute("rate", analyze()[1]);
            request.setAttribute("time", analyze()[2]);
            request.setAttribute("logs", dbLogs);
            RequestDispatcher view = request.getRequestDispatcher("index.jsp");
            view.forward(request, response);
        }
    }

    private long recordRequest(javax.servlet.http.HttpServletRequest request, String word) {
        // make a document and insert it
        long res = System.currentTimeMillis();
        Document log = new Document("time", res).append("word", word);
        log.append("log-type", "request");
        log.append("user-agent", request.getHeader("user-agent"));
        log.append("accept-content", request.getHeader("accept"));
        log.append("cookie", request.getHeader("cookie"));
        log.append("language", request.getHeader("accept-language"));
        log.append("encoding", request.getHeader("accept-encoding"));
        log.append("connection", request.getHeader("connection"));
        logs.insertOne(log);
        return res;
    }

    private long recordResponse(javax.servlet.http.HttpServletResponse response, String word, String content, String mark) {
        // make a document and insert it
        long res = System.currentTimeMillis();
        Document log = new Document("time", res).append("word", word).append("status", mark);
        log.append("log-type", "response");
        log.append("status-code", response.getStatus());
        log.append("content", content);
        for (String header: response.getHeaderNames()) {
            log.append(header, response.getHeader(header));
        }
        logs.insertOne(log);
        return res;
    }

    private String[] analyze() {
        Document data = commons.find().first();
        String totalResponseTime = String.valueOf(data.get("total-response-time"));
        String count = String.valueOf(data.get("count"));
        String success = String.valueOf(data.get("success-count"));
        String[] res = new String[3];
        res[0] = count;
        res[1] = String.valueOf(Integer.parseInt(success) * 1.0 / Integer.parseInt(count));
        res[2] = String.valueOf(Long.parseLong(totalResponseTime) * 1.0 / Integer.parseInt(count));
        return res;
    }
}
