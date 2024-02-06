/*-
 * #%L
 * athlete_view
 * %%
 * Copyright (C) 2023 - 2024 TU Wien INSO ASE GROUP 5 WS2023
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package ase.athlete_view.domain.user.pojo.entity

import ase.athlete_view.domain.user.pojo.dto.PreferencesDTO
import jakarta.persistence.*

@Entity
@Table(name = "preferences")
open class Preferences(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long?,
    // controls if users who are online receive email notifications even if they already received a push notification
    open var emailNotifications: Boolean = false,
    open var commentNotifications: NotificationPreferenceType = NotificationPreferenceType.PUSH,
    open var ratingNotifications: NotificationPreferenceType = NotificationPreferenceType.PUSH,
    open var otherNotifications: NotificationPreferenceType = NotificationPreferenceType.PUSH,

    open var shareHealthWithTrainer: Boolean = false
) {
    open fun toDTO(): PreferencesDTO {
        return PreferencesDTO(emailNotifications, commentNotifications, ratingNotifications, otherNotifications, shareHealthWithTrainer)
    }

    override fun toString(): String {
        return "Preferences(id=$id, emailNotifications=$emailNotifications, commentNotifications=$commentNotifications, ratingNotifications=$ratingNotifications, otherNotifications=$otherNotifications, shareHealthWithTrainer=$shareHealthWithTrainer)"
    }


}

enum class NotificationPreferenceType {
    EMAIL,
    PUSH,
    NONE,
    BOTH
}
