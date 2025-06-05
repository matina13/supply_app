package org.example.demo.structs;

import org.example.demo.structs.LocationService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/countries")
public class CountryServlet extends HttpServlet {

    private final LocationService locationService = LocationService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        // Java 11 compatible: Traditional switch statement
        try {
            if (action == null) action = "all";

            switch (action) {
                case "popular":
                    sendPopularCountries(response);
                    break;
                case "continent":
                    String continent = request.getParameter("continent");
                    sendCountriesByContinent(response, continent);
                    break;
                case "mapping":
                    sendJavaScriptMapping(response);
                    break;
                default:
                    sendAllCountries(response);
                    break;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            response.getWriter().write(gson.toJson(error));
        }
    }

    private void sendPopularCountries(HttpServletResponse response) throws IOException {
        List<LocationService.Country> popular = locationService.getPopularCountries();
        JsonArray jsonArray = new JsonArray();

        for (LocationService.Country country : popular) {
            JsonObject countryObj = new JsonObject();
            countryObj.addProperty("code", country.getCode());
            countryObj.addProperty("name", country.getName());
            countryObj.addProperty("flag", country.getFlag());
            countryObj.addProperty("continent", locationService.getContinentDisplayName(country.getContinentName()));
            jsonArray.add(countryObj);
        }

        response.getWriter().write(gson.toJson(jsonArray));
    }

    private void sendCountriesByContinent(HttpServletResponse response, String continentName) throws IOException {
        Map<String, List<LocationService.Country>> countriesByContinent = locationService.getCountriesByContinent();

        if (continentName != null) {
            List<LocationService.Country> countries = countriesByContinent.get(continentName.toUpperCase());
            if (countries != null) {
                JsonArray jsonArray = new JsonArray();
                for (LocationService.Country country : countries) {
                    JsonObject countryObj = new JsonObject();
                    countryObj.addProperty("code", country.getCode());
                    countryObj.addProperty("name", country.getName());
                    countryObj.addProperty("flag", country.getFlag());
                    countryObj.addProperty("continent", locationService.getContinentDisplayName(country.getContinentName()));
                    jsonArray.add(countryObj);
                }
                response.getWriter().write(gson.toJson(jsonArray));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonObject error = new JsonObject();
                error.addProperty("error", "Continent not found");
                response.getWriter().write(gson.toJson(error));
            }
        } else {
            sendAllCountries(response);
        }
    }

    private void sendAllCountries(HttpServletResponse response) throws IOException {
        Map<String, List<LocationService.Country>> countriesByContinent = locationService.getCountriesByContinent();
        JsonObject result = new JsonObject();

        // Add popular countries
        JsonArray popularArray = new JsonArray();
        for (LocationService.Country country : locationService.getPopularCountries()) {
            JsonObject countryObj = new JsonObject();
            countryObj.addProperty("code", country.getCode());
            countryObj.addProperty("name", country.getName());
            countryObj.addProperty("flag", country.getFlag());
            countryObj.addProperty("continent", locationService.getContinentDisplayName(country.getContinentName()));
            popularArray.add(countryObj);
        }
        result.add("popular", popularArray);

        // Add countries by continent
        JsonObject continentsObj = new JsonObject();
        for (Map.Entry<String, List<LocationService.Country>> entry : countriesByContinent.entrySet()) {
            JsonArray continentArray = new JsonArray();
            for (LocationService.Country country : entry.getValue()) {
                JsonObject countryObj = new JsonObject();
                countryObj.addProperty("code", country.getCode());
                countryObj.addProperty("name", country.getName());
                countryObj.addProperty("flag", country.getFlag());
                continentArray.add(countryObj);
            }
            continentsObj.add(locationService.getContinentDisplayName(entry.getKey()), continentArray);
        }
        result.add("continents", continentsObj);

        response.getWriter().write(gson.toJson(result));
    }

    private void sendJavaScriptMapping(HttpServletResponse response) throws IOException {
        response.setContentType("application/javascript");
        String jsMapping = locationService.generateJavaScriptMapping();
        response.getWriter().write(jsMapping);
    }
}