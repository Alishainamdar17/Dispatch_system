package onedeoleela.onedeoleela.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationCoordinateService {

    private static final Map<String, double[]> LOCATION_MAP = new HashMap<>();

    static {

        LOCATION_MAP.put("Bopgaon", new double[]{18.3740, 73.8650});
        LOCATION_MAP.put("Pune Office Bopgaon", new double[]{18.3740, 73.8650});

        LOCATION_MAP.put("Yewalewadi", new double[]{18.4358, 73.8963});

        LOCATION_MAP.put("kondhwa nibm road", new double[]{18.4720, 73.9060});
        LOCATION_MAP.put("NIBM", new double[]{18.4720, 73.9060});

        LOCATION_MAP.put("Baner", new double[]{18.5590, 73.7868});
        LOCATION_MAP.put("FC Road", new double[]{18.5204, 73.8410});

        LOCATION_MAP.put("Hinjawdi", new double[]{18.5910, 73.7389});
        LOCATION_MAP.put("Tathwade", new double[]{18.6125, 73.7530});

        LOCATION_MAP.put("Manjari", new double[]{18.4920, 73.9780});
        LOCATION_MAP.put("Wagoli", new double[]{18.5793, 73.9580});

        LOCATION_MAP.put("Undri", new double[]{18.4543, 73.9260});
        LOCATION_MAP.put("Khed Shivapur", new double[]{18.3430, 73.8460});

        LOCATION_MAP.put("Mumbai", new double[]{19.0760, 72.8777});
        LOCATION_MAP.put("Hydrabad", new double[]{17.3850, 78.4867});
    }

    public double[] getCoordinates(String location) {

        if(location == null) return null;

        location = location.trim();

        // 1️⃣ Check local map first
        double[] coords = LOCATION_MAP.get(location);

        if(coords != null){
            return coords;
        }

        // 2️⃣ If not found → fetch automatically
        return fetchFromOpenStreetMap(location);
    }

    private double[] fetchFromOpenStreetMap(String location){

        try{

            String url =
                    "https://nominatim.openstreetmap.org/search?q="
                            + location + " Pune India&format=json&limit=1";

            RestTemplate restTemplate = new RestTemplate();

            List<Map<String,Object>> response =
                    restTemplate.getForObject(url, List.class);

            if(response != null && !response.isEmpty()){

                double lat = Double.parseDouble((String)response.get(0).get("lat"));
                double lon = Double.parseDouble((String)response.get(0).get("lon"));

                return new double[]{lat,lon};
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}