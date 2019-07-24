package skills.services.settings.listeners

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import skills.services.LevelUtils
import skills.services.settings.SettingChangedListener
import skills.services.settings.Settings
import skills.storage.model.LevelDef
import skills.storage.model.ProjDef
import skills.storage.model.Setting
import skills.storage.repos.LevelDefRepo
import skills.storage.repos.ProjDefRepo

import javax.transaction.Transactional

@Slf4j
@Component
class LevelPointsSettingListener implements SettingChangedListener{

    static final int MIN_TOTAL_POINTS_REQUIRED_TO_SWITCH = 100

    @Autowired
    ProjDefRepo projDefRepo

    @Autowired
    LevelDefRepo levelDefRepo

    @Override
    boolean supports(skills.controller.request.model.SettingsRequest setting) {
        return setting.setting == Settings.LEVEL_AS_POINTS.settingName
    }

    @Override
    ValidationRes isValid(skills.controller.request.model.SettingsRequest setting){
        ProjDef project = projDefRepo.findByProjectId(setting.projectId)
        boolean isValid = project?.totalPoints > MIN_TOTAL_POINTS_REQUIRED_TO_SWITCH
        return new ValidationRes(isValid: isValid, explanation: !isValid ? getErrExplanation(project) : null)
    }

    @Transactional
    @Override
    void execute(Setting previousValue, skills.controller.request.model.SettingsRequest setting) {
        ProjDef project = projDefRepo.findByProjectId(setting.projectId)

        LevelUtils levelUtils = new LevelUtils()

        if(setting.isEnabled() && (!previousValue?.isEnabled())){
            log.info("converting all levels for project [${setting.projectId}] (including skill levels) to points")
            if(project?.totalPoints < MIN_TOTAL_POINTS_REQUIRED_TO_SWITCH){
                throw new skills.controller.exceptions.SkillException(getErrExplanation(project),
                        project.projectId,
                        "N/A",
                        skills.controller.exceptions.ErrorCode.InsufficientPointsToConvertLevels)
            }
            levelUtils.convertToPoints(project.levelDefinitions, project.totalPoints)
            project.levelDefinitions.each{
                levelDefRepo.save(it)
            }
            project.subjects?.each{
                levelUtils.convertToPoints(it.levelDefinitions, it.totalPoints == 0 ? LevelUtils.defaultTotalPointsGuess : it.totalPoints)
                it.levelDefinitions.each { LevelDef level ->
                    levelDefRepo.save(level)
                }
            }
        }else if(!setting.isEnabled()){
            log.info("converting all levels for project [${setting.projectId}] (including skill levels) to percentages")
            levelUtils.convertToPercentage(project.levelDefinitions, project.totalPoints)
            project.levelDefinitions.each{
                levelDefRepo.save(it)
            }
            project.subjects?.each{
                log.info("converting level definitions ${it.levelDefinitions} for subject ${it}")
                //conditions we need to handle:
                levelUtils.convertToPercentage(it.levelDefinitions, it.totalPoints)
                it.levelDefinitions.each{ LevelDef level ->
                    levelDefRepo.save(level)
                }

            }
        }
    }

    private String getErrExplanation(ProjDef project) {
        "Use Points For Levels: Project has [${project.totalPoints}] total points. " +
                "[$MIN_TOTAL_POINTS_REQUIRED_TO_SWITCH] total points required to switch to points based levels"
    }

}