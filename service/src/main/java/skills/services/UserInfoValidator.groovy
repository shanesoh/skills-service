/**
 * Copyright 2020 SkillTree
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package skills.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import skills.auth.UserInfo
import skills.controller.exceptions.ErrorCode
import skills.controller.exceptions.SkillException

import static skills.controller.exceptions.SkillException.NA

@Component
class UserInfoValidator {

    static final List<String> landingPageOptions = ['progress', 'admin']

    @Value('#{"${skills.config.ui.maxFirstNameLength}"}')
    int maxFirstNameLength

    @Value('#{"${skills.config.ui.maxLastNameLength}"}')
    int maxLastNameLength

    @Value('#{"${skills.config.ui.maxNicknameLength}"}')
    int maxNicknameLength

    void validate(UserInfo userInfo) {
        if (!userInfo.firstName || userInfo.firstName.length() > maxFirstNameLength) {
            throw new SkillException("First Name is required and can be no longer than ${maxFirstNameLength} characters", NA, NA, ErrorCode.BadParam)
        }
        if (!userInfo.lastName || userInfo.lastName.length() > maxLastNameLength) {
            throw new SkillException("Last Name is required and can be no longer than ${maxLastNameLength} characters", NA, NA, ErrorCode.BadParam)
        }
        // nickname by default is "firstName lastName"
        if (userInfo.nickname && userInfo.nickname.length() > maxNicknameLength) {
            throw new SkillException("Nickname cannot be over ${maxNicknameLength} characters", NA, NA, ErrorCode.BadParam)
        }
        if (userInfo.landingPage && !landingPageOptions.contains(userInfo.landingPage)) {
            throw new SkillException("Invalid Home page preference [${userInfo.landingPage}].  Valid options: ${landingPageOptions}", NA, NA, ErrorCode.BadParam)
        }
    }

}
