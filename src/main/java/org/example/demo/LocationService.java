package org.example.demo;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

public class LocationService {
    private static LocationService instance;
    private final Map<String, Country> countries;
    private final Map<String, Continent> continents;
    private final List<Country> popularCountries;

    // Data classes
    public static class Continent {
        private final String name;
        private final int x, y;

        public Continent(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }

        public String getName() { return name; }
        public int getX() { return x; }
        public int getY() { return y; }
    }

    public static class Country {
        private final String code;
        private final String name;
        private final String continentName;
        private final String flag;
        private final boolean popular;

        public Country(String code, String name, String continentName, String flag, boolean popular) {
            this.code = code;
            this.name = name;
            this.continentName = continentName;
            this.flag = flag;
            this.popular = popular;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getContinentName() { return continentName; }
        public String getFlag() { return flag; }
        public boolean isPopular() { return popular; }
    }

    private LocationService() {
        countries = new HashMap<>();
        continents = new HashMap<>();
        popularCountries = new ArrayList<>();
        loadFromJson();
    }

    public static synchronized LocationService getInstance() {
        if (instance == null) {
            instance = new LocationService();
        }
        return instance;
    }

    private void loadFromJson() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("countries.json");
             InputStreamReader reader = new InputStreamReader(is)) {

            Gson gson = new Gson();
            JsonObject root = gson.fromJson(reader, JsonObject.class);

            // Load continents
            JsonObject continentsObj = root.getAsJsonObject("continents");
            for (Map.Entry<String, JsonElement> entry : continentsObj.entrySet()) {
                String name = entry.getKey();
                JsonObject coords = entry.getValue().getAsJsonObject();
                int x = coords.get("x").getAsInt();
                int y = coords.get("y").getAsInt();
                continents.put(name, new Continent(name, x, y));
            }

            // Load countries
            JsonArray countriesArray = root.getAsJsonArray("countries");
            for (JsonElement element : countriesArray) {
                JsonObject countryObj = element.getAsJsonObject();

                String code = countryObj.get("code").getAsString();
                String name = countryObj.get("name").getAsString();
                String continent = countryObj.get("continent").getAsString();
                String flag = countryObj.has("flag") ? countryObj.get("flag").getAsString() : "";
                boolean popular = countryObj.has("popular") && countryObj.get("popular").getAsBoolean();

                Country country = new Country(code, name, continent, flag, popular);
                countries.put(code, country);

                if (popular) {
                    popularCountries.add(country);
                }
            }

            System.out.println("Loaded " + countries.size() + " countries and " + continents.size() + " continents");

        } catch (IOException e) {
            throw new RuntimeException("Failed to load countries configuration", e);
        }
    }

    public Country getCountry(String code) {
        return countries.get(code);
    }

    public Continent getContinent(String name) {
        return continents.get(name);
    }

    public List<Country> getPopularCountries() {
        return Collections.unmodifiableList(popularCountries);
    }

    public Map<String, List<Country>> getCountriesByContinent() {
        Map<String, List<Country>> result = new LinkedHashMap<>();

        // Initialize continent groups in order
        result.put("NORTH_AMERICA", new ArrayList<>());
        result.put("SOUTH_AMERICA", new ArrayList<>());
        result.put("EUROPE", new ArrayList<>());
        result.put("AFRICA", new ArrayList<>());
        result.put("ASIA", new ArrayList<>());
        result.put("OCEANIA", new ArrayList<>());

        // Group countries by continent
        for (Country country : countries.values()) {
            if (!country.isPopular()) { // Exclude popular countries from continent groups
                result.computeIfAbsent(country.getContinentName(), k -> new ArrayList<>()).add(country);
            }
        }

        // Sort countries within each continent
        result.values().forEach(list -> list.sort(Comparator.comparing(Country::getName)));

        return result;
    }


    public String getContinentDisplayName(String continentName) {
        // Java 11 compatible: Traditional if-else
        if ("NORTH_AMERICA".equals(continentName)) {
            return "North America";
        } else if ("SOUTH_AMERICA".equals(continentName)) {
            return "South America";
        } else if ("EUROPE".equals(continentName)) {
            return "Europe";
        } else if ("AFRICA".equals(continentName)) {
            return "Africa";
        } else if ("ASIA".equals(continentName)) {
            return "Asia";
        } else if ("OCEANIA".equals(continentName)) {
            return "Oceania";
        } else {
            return continentName;
        }
    }

    // Utility method to generate JavaScript mapping for frontend
    public String generateJavaScriptMapping() {
        StringBuilder js = new StringBuilder();
        js.append("const countryToContinentMap = {\n");

        for (Country country : countries.values()) {
            String continentDisplay = getContinentDisplayName(country.getContinentName());
            js.append(String.format("  \"%s\": \"%s\",\n", country.getCode(), continentDisplay));
        }

        js.append("};\n");
        return js.toString();
    }
}