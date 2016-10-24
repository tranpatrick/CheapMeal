package services.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import services.RecetteService;

public class SupprimerRecette {
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		/* Lecture des parametres */
		String id_recette = request.getParameter("id_r");
		String key = request.getParameter("cle");

		try {
			/* Traitement des services */
			JSONObject res = RecetteService.supprimerRecette(id_recette,key);
			
			/* Ecriture de la reponse */
			PrintWriter writer = response.getWriter();
			response.setContentType("application/json");
			writer.println(res.toString());
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
