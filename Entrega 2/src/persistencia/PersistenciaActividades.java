package persistencia;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import java.nio.file.Files;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import modelo.actividad.Actividad;
import modelo.actividad.RecursoEducativo;
import modelo.actividad.Tarea;
import modelo.actividad.examen.Encuesta;
import modelo.actividad.examen.Parcial;
import modelo.actividad.examen.Quiz;

public class PersistenciaActividades {
	private String[] titulos = {"ID", "descripcion", "objetivo", "tipo", "nivelDificultad", "duracionEsperada", "preRequisitos", "contenido"};
	
	public void cargarArchivo(String ruta, List<Actividad> actividades) throws JSONException, IOException {
		JSONArray jActividades = new JSONArray(new String(Files.readAllBytes(new File(ruta).toPath())));
		
		for (int i = 0; i < jActividades.length(); i++) {
			JSONObject jActividad = jActividades.getJSONObject(i);
			List<Actividad> preRequisitos = obtenerActividades(jActividades, actividades);
			Actividad actividad = null;
			
			if (jActividad.getString(titulos[3]).equals("RE")) {
				actividad = new RecursoEducativo(jActividad.getString(titulos[1]), jActividad.getString(titulos[2]), jActividad.getString(titulos[3]), jActividad.getInt(titulos[4]), jActividad.getDouble(titulos[5]), preRequisitos, jActividad.getJSONObject(titulos[7]).getString("recurso"), jActividad.getJSONObject(titulos[7]).getString("tipo"));
			}else if (jActividad.getString(titulos[3]).equals("T")) {
				actividad = new Tarea(jActividad.getString(titulos[1]), jActividad.getString(titulos[2]), jActividad.getString(titulos[3]), jActividad.getInt(titulos[4]), jActividad.getDouble(titulos[5]), preRequisitos, obtenerListaStrings(jActividad.getJSONObject(titulos[7]).getJSONArray("ejercicios")));
			}else if (jActividad.getString(titulos[3]).equals("Q")) {
				actividad = new Quiz(jActividad.getString(titulos[1]), jActividad.getString(titulos[2]), jActividad.getString(titulos[3]), jActividad.getInt(titulos[4]), jActividad.getDouble(titulos[5]), preRequisitos, obtenerListaStrings(jActividad.getJSONObject(titulos[7]).getJSONArray("preguntas")), obtenerListaStrings(jActividad.getJSONObject(titulos[7]).getJSONArray("respuestas")), jActividad.getJSONObject(titulos[7]).getDouble("calificacionMin"));
			}else if (jActividad.getString(titulos[3]).equals("P")) {
				actividad = new Parcial(jActividad.getString(titulos[1]), jActividad.getString(titulos[2]), jActividad.getString(titulos[3]), jActividad.getInt(titulos[4]), jActividad.getDouble(titulos[5]), preRequisitos, obtenerListaStrings(jActividad.getJSONObject(titulos[7]).getJSONArray("preguntas")));
			}else if (jActividad.getString(titulos[3]).equals("E")) {
				actividad = new Encuesta(jActividad.getString(titulos[1]), jActividad.getString(titulos[2]), jActividad.getString(titulos[3]), jActividad.getInt(titulos[4]), jActividad.getDouble(titulos[5]), preRequisitos, obtenerListaStrings(jActividad.getJSONObject(titulos[7]).getJSONArray("preguntas")));
			}
			
			actividades.add(actividad);
		}
	}
	
	private List<String> obtenerListaStrings(JSONArray jStrings) {
		List<String> strings = new ArrayList<String>();
		for (int i = 0; i < jStrings.length(); i++) {
			strings.add(jStrings.getString(i));
		}
		return strings;
	}
	
	private List<Actividad> obtenerActividades(JSONArray jActividades, List<Actividad> actividadesCompleta) {
		List<Actividad> actividades = new ArrayList<Actividad>();
		for (int i = 0; i < jActividades.length(); i++) {
			for (int j = 0; j < actividadesCompleta.size(); j++) {
				if (String.valueOf(actividadesCompleta.get(j).getID()).equals(jActividades.getString(i))) {
					actividades.add(actividadesCompleta.get(j));
					break;
				}
			}
		}
		return actividades;
	}
	
	public void guardarArchivo(String ruta, List<Actividad> actividades) {
		JSONArray jActividades = new JSONArray();

		for (Actividad actividad: actividades) {
			JSONObject jActividad = new JSONObject();
			String[] actividadArray = actividad.toString().split("%");
			
			for (int i = 0; i < titulos.length; i++) {
				if (i < 6) {
					jActividad.put(titulos[i], actividadArray[i]);
				}else if (i == 7) {
					jActividad.put(titulos[i], guardarContenido(actividadArray[3], actividadArray[i]));
				}
			}
			
			JSONArray jActividadesPrev = new JSONArray();
			
			if (!actividadArray[6].equals("NA")) {
				String[] actividadesPrev = actividadArray[6].split("//");
				
				for (String actividadPrev: actividadesPrev) {
					jActividadesPrev.put(actividadPrev);
				}
			}
			
			jActividad.put(titulos[6], jActividadesPrev);
			jActividades.put(jActividad);
		}
		try {
			PrintWriter pw = new PrintWriter(ruta);
			jActividades.write(pw, 2, 0);
	        pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private JSONObject guardarContenido(String tipo, String contenido) {
		JSONObject jContenido = new JSONObject();

		if (tipo.equals("RE")) {
			String[] arrayContenido = contenido.split(":/:");
			jContenido.put("recurso", arrayContenido[0]);
			jContenido.put("tipo", arrayContenido[1]);
		}else if (tipo.equals("T")) {
			String[] arrayEjercicios = contenido.split("//");
			JSONArray ejericios = new JSONArray();
			for (int i = 0; i < arrayEjercicios.length; i++) {
				ejericios.put(arrayEjercicios[i]);
			}
			jContenido.put("preguntas", ejericios);
		}else if (tipo.equals("Q")) {
			String[] titulosQuiz = {"preguntas", "respuestas", "calificacionMin"};
			String[] arrayContenido = contenido.split(":/:");
			for (int i = 0; i < titulosQuiz.length-1; i++) {
				String[] arreglo = arrayContenido[i].split("//");
				JSONArray jArreglo = new JSONArray();
				for (int j = 0; j < arreglo.length; j++) {
					jArreglo.put(arreglo[j]);
				}
				jContenido.put(titulosQuiz[i], jArreglo);
			}
			jContenido.put(titulosQuiz[3], arrayContenido[3]);
		}else if (tipo.equals("P")) {
			String[] arrayPreguntas = contenido.split("//");
			JSONArray preguntas = new JSONArray();
			for (int i = 0; i < arrayPreguntas.length; i++) {
				preguntas.put(arrayPreguntas[i]);
			}
			jContenido.put("preguntas", preguntas);
		}else if (tipo.equals("E")) {
			String[] arrayPreguntas = contenido.split("//");
			JSONArray preguntas = new JSONArray();
			for (int i = 0; i < arrayPreguntas.length; i++) {
				preguntas.put(arrayPreguntas[i]);
			}
			jContenido.put("preguntas", preguntas);
		}
		return jContenido;
	}
}