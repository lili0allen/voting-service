package voting.infrastructure;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Repository;
import voting.domain.Survey;
import voting.domain.SurveyRepository;
import voting.domain.Vote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

@Repository
public class JsonSurveyRepository implements SurveyRepository {
    private static final String FILE_DIR = "data/data.json";
    private ClassLoader classLoader;
    private File file;
    private JSONParser parser = new JSONParser();

    public JsonSurveyRepository() {
        this.classLoader = getClass().getClassLoader();
        this.file = new File(classLoader.getResource(FILE_DIR).getFile());
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            if(br.readLine() == null){
                JSONObject surveys = new JSONObject();
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(surveys.toJSONString());
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveSurvey(Survey survey) {
        try {
            FileReader fileReader = new FileReader(file);

            JSONObject surveyJson = toSurveyJsonObject(survey);

            JSONObject allSurveys = (JSONObject) parser.parse(fileReader);
            allSurveys.put(survey.id(), surveyJson);

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(allSurveys.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Survey getSurvey(String surveyId) {
        try {
            FileReader fileReader = new FileReader(file);
            JSONObject surveys = (JSONObject) parser.parse(fileReader);
            JSONObject survey = (JSONObject) surveys.get(surveyId);

            if(survey == null) {
                return null;
            }

            return toSurveyDomain(survey);
        } catch (Exception e) {
            throw new RuntimeException("Fail to find survey, surveyID:" + surveyId, e);
        }
    }

    private Survey toSurveyDomain(JSONObject jsonObject) {
        JSONObject votesJson = (JSONObject)jsonObject.get("votes");

        Map<Vote, Integer> votesMap = new HashMap<>();
        for(Vote vote: Vote.values()) {
            votesMap.put(vote, ((Number) votesJson.get(vote.toString())).intValue());
        }

        return new Survey(
                (String) jsonObject.get("id"),
                (String) jsonObject.get("title"),
                (String) jsonObject.get("description"),
                (Long) jsonObject.get("createdTime"),
                votesMap);
    }

    private JSONObject toSurveyJsonObject(Survey surveyDomain) {
        JSONObject survey = new JSONObject();
        JSONObject votes = new JSONObject();

        for (Vote vote : Vote.values()) {
            votes.put(vote, surveyDomain.getVote(vote));
        }

        survey.put("id", surveyDomain.id());
        survey.put("title", surveyDomain.title());
        survey.put("description", surveyDomain.description());
        survey.put("createdTime", surveyDomain.createdTime());
        survey.put("votes", votes);
        return survey;
    }
}