package test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Test
 */
@MultipartConfig
public class TestMongoDB extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestMongoDB() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		String login = request.getParameter("login");
//		String name = request.getParameter("name");
//		String surname = request.getParameter("surname");
		try {
//			MongoDatabase database = DBStatic.getMongoConnection();
//			MongoCollection<BasicDBObject> col = database.getCollection("users", BasicDBObject.class);
//			BasicDBObject document = new BasicDBObject("login", "pet");
//			document.append("name", "pat").append("surname", "patate");
//			col.insertOne(document);
//			
//			DBStatic.closeMongoDBConnection();
//			ArrayList<String> ingredients = new ArrayList<String>();
//			ingredients.add("pommes");
//			ingredients.add("pate feuilletee");
//			ArrayList<Double> quantites = new ArrayList<>();
//			quantites.add(5.0);
//			quantites.add(250.0);
//			ArrayList<String> mesures = new ArrayList<String>();
//			mesures.add("unite(s)");
//			mesures.add("g");
//			ArrayList<String> preparation = new ArrayList<String>();
//			preparation.add("cuir les patates");
//			Part photo =null;
//			
//			
//			BasicDBObject doc = MongoFactory.creerDocumentRecette("tarte aux pomme", 5, "victor", ingredients, quantites, mesures, preparation,photo);
//			MongoDatabase database = DBStatic.getMongoConnection();
//			MongoCollection<BasicDBObject> col = database.getCollection("Recettes", BasicDBObject.class);
//			col.insertOne(doc);
			
		}
		catch (Exception e) {
			response.setContentType("text/html");
			PrintWriter pw = response.getWriter();
			pw.println(e.getMessage());
		}
		// TODO Auto-generated method stub
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		pw.println("OK");
		pw.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
