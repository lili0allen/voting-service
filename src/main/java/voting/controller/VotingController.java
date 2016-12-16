package voting.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.context.request.WebRequest;
import voting.controller.assembler.SurveyTemplateUiModelAssembler;
import voting.controller.model.SurveyTemplateUiModel;
import voting.domain.Survey;
import voting.domain.SurveyService;
import voting.domain.Vote;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
public class VotingController {

    @Value("${spring.expireTime}")
    private int expireTime;

    @GetMapping("/survey/{id}")
    public String getSurveyEntryForm(@PathVariable String id, Model model){
        Survey survey = surveyService.getSurveyTemplate(id);
        List<String> voteOptions = voteOptionsCapitalised();

        SurveyTemplateUiModel surveyTemplateUiModel = new SurveyTemplateUiModelAssembler().toSurveyTemplateUiModel(survey);
        int TEN_MINUTES_IN_MILLISECONDS = expireTime * 60 * 1000;
        model.addAttribute("survey", surveyTemplateUiModel);
        model.addAttribute("options", voteOptions);
        model.addAttribute("expire", TEN_MINUTES_IN_MILLISECONDS);

        boolean expired = survey.isExpired(TEN_MINUTES_IN_MILLISECONDS);
        if (expired) {
            return "survey-close";
        } else {
            return "survey";
        }
    }

    @GetMapping("/survey/result/{id}")
    public String surveyResult(@PathVariable String id, Model model){
        Survey survey = surveyService.getSurveyTemplate(id);

        int totalVoteCount = survey.totalVoteCount();
        double poor = survey.getVote(Vote.POOR);
        double fair = survey.getVote(Vote.FAIR);
        double good = survey.getVote(Vote.GOOD);
        double poorPercentage = 0;
        double fairPercentage = 0;
        double goodPercentage = 0;

        if(survey.totalVoteCount() != 0){
            poorPercentage = (poor / totalVoteCount);
            fairPercentage = (fair / totalVoteCount);
            goodPercentage = (good / totalVoteCount);
        }

        NumberFormat defaultFormat = NumberFormat.getPercentInstance();
        defaultFormat.setMinimumFractionDigits(2);

        SurveyTemplateUiModel surveyTemplateUiModel = new SurveyTemplateUiModelAssembler().toSurveyTemplateUiModel(survey);
        int TEN_MINUTES_IN_MILLISECONDS = expireTime * 60 * 1000;
        model.addAttribute("survey", surveyTemplateUiModel);
        model.addAttribute("total", totalVoteCount);
        model.addAttribute("expire", TEN_MINUTES_IN_MILLISECONDS);

        model.addAttribute("poorResult", defaultFormat.format(poorPercentage));
        model.addAttribute("fairResult", defaultFormat.format(fairPercentage));
        model.addAttribute("goodResult", defaultFormat.format(goodPercentage));

        return "survey-result";
    }

    @PostMapping("/survey/voteSubmit")
    public String voteSubmit(WebRequest request, Model model){
        String surveyID = request.getParameter("surveyID");
        Vote vote = Vote.valueOf(request.getParameter("vote"));
        model.addAttribute("surveyUiModelID", surveyID);
        surveyService.addVote(surveyID, vote);
        return "vote-finish";
    }

    private List<String> voteOptionsCapitalised() {
        List<String> voteOptions = new ArrayList<>();
        for (Vote voteEnum : Vote.values()) {
            voteOptions.add(voteEnum.toString().toUpperCase());
        }
        return voteOptions;
    }

    @Autowired
    private SurveyService surveyService;
}
