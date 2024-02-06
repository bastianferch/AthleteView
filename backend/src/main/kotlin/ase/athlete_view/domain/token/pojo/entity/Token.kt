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
package ase.athlete_view.domain.token.pojo.entity

import ase.athlete_view.domain.user.pojo.entity.User
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "token_type")
abstract class Token(
    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(generator = "uuid4")
    @GenericGenerator(name = "uuid", strategy = "uuid4")
    open var uuid: UUID? = null,

    @Column(nullable = false, updatable = false)
    open var expiryDate: Date,

    @ManyToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    open var user: User
) {

    val isExpired: Boolean
        get() = expiryDate.before(Date())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        other as Token

        return uuid != null && uuid == other.uuid
    }

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(uuid = $uuid , expiryDate = $expiryDate )"
    }

    override fun hashCode(): Int {
        var result = uuid?.hashCode() ?: 0
        result = 31 * result + expiryDate.hashCode()
        return result
    }
}

