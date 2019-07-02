package skills.service.controller

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import skills.service.controller.exceptions.SkillsValidator
import skills.service.controller.result.model.SettingsResult
import skills.service.datastore.services.settings.SettingsService

@RestController
@RequestMapping("/app/public")
@Slf4j
class PublicSettingsController {

    @Autowired
    SettingsService settingsService

    @RequestMapping(value = "/settings/group/{settingGroup}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    List<SettingsResult> getPublicSettings(@PathVariable("settingGroup") String settingGroup) {
        SkillsValidator.isNotBlank(settingGroup, "Setting Group")
        SkillsValidator.isTrue(settingGroup.startsWith("public_"), "Setting Group [$settingGroup] must be prefixed with 'public_'")
        return settingsService.loadSettingsByType(null, settingGroup)
    }

    @RequestMapping(value = "/settings/{setting}/group/{settingGroup}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    SettingsResult getPublicSetting(@PathVariable("setting") String setting, @PathVariable("settingGroup") String settingGroup) {
        SkillsValidator.isNotBlank(setting, "Setting Id")
        SkillsValidator.isNotBlank(settingGroup, "Setting Group")
        SkillsValidator.isTrue(settingGroup.startsWith("public_"), "Setting Group [$settingGroup] must be prefixed with 'public_'")
        return settingsService.getSetting(null, setting, settingGroup)
    }
}