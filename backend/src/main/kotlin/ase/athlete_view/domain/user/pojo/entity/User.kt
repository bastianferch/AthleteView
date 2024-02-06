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

import ase.athlete_view.domain.notification.pojo.entity.Notification
import ase.athlete_view.domain.activity.pojo.entity.PlannedActivity
import ase.athlete_view.domain.user.pojo.dto.UserDTO
import jakarta.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
open class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long?,

    @Column(unique = true)
    open var email: String,

    @OneToMany(mappedBy = "recipient", fetch = FetchType.LAZY, targetEntity = Notification::class)
    open var notifications: List<Notification> = mutableListOf(),

    open var name: String,
    open var password: String,
    open var country: String?,
    open var zip: String?,
    var isConfirmed: Boolean = false,

    @OneToMany(mappedBy = "createdBy", cascade = [CascadeType.MERGE, CascadeType.PERSIST], fetch = FetchType.LAZY)
    open var activities: MutableSet<PlannedActivity> = mutableSetOf(),

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "preferences_id", referencedColumnName = "id")
    open var preferences: Preferences? = null
) {
    open fun toUserDTO(): UserDTO {
        return UserDTO(
            id,
            name,
            email,
            null,
            null,
            this.getUserType())
    }

    open fun getUserType(): String {
        return "undefined"
    }

    override fun toString(): String {
        return "User(id=$id, email='$email', name='$name', country=$country, zip=$zip, isConfirmed=$isConfirmed, activities=$activities, notifications=$notifications)"
    }
}
