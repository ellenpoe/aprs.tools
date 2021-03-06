/*
 * Copyright (c) 2019 Ellen Poe
 *
 * This file is part of APRSTools.
 *
 * APRSTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * APRSTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with APRSTools.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.ellenhp.aprstools.modules.testing

import dagger.Module
import dagger.Provides
import org.threeten.bp.Instant

@Module
class FakeTimeModule {
    val fakeTimeController = FakeTimeController()

    @Provides
    fun provideInstant(): Instant {
        return fakeTimeController.nextTime
    }

    @Provides
    fun provideFakeTimeController(): FakeTimeController {
        return fakeTimeController
    }
}

class FakeTimeController {
    var nextTime = Instant.now()
}